package dev.domaincentric.sample.ecommerce.infrastructure.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.AbstractFlashMapManager;

/**
 * Keeps Spring MVC's flash maps in one sealed cookie instead of the server session, so the message
 * a redirect carries reaches the next page whichever instance of the shop answers it.
 *
 * <p>{@link AbstractFlashMapManager} does the bookkeeping: a flash map is bound to the path (and
 * query) the redirect leads to, read only by a request for that page, removed once read, and
 * dropped when it expires. This class only stores the list: AES-GCM encrypts and authenticates it
 * with a key every instance shares, so a visitor can neither read nor forge what the cookie says. A
 * cookie that does not open — tampered, sealed with another key, from an older format — counts as
 * no flash maps at all.
 *
 * <p>A flash attribute holds a short code and a number, never a sentence: the cookie keeps only
 * {@link String} and {@link Integer} values and refuses anything else, so it stays small and its
 * content stays data.
 */
public class CookieFlashMapManager extends AbstractFlashMapManager {

  /** The cookie that carries the sealed flash maps. */
  public static final String COOKIE_NAME = "shop-notice";

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int NONCE_BYTES = 12;
  private static final int TAG_BITS = 128;
  private static final byte FORMAT = 1;
  private static final byte STRING = 'S';
  private static final byte INTEGER = 'I';
  private static final String CURRENT = CookieFlashMapManager.class.getName() + ".CURRENT";

  private final SecretKey key;
  private final boolean secure;
  private final String sameSite;
  private final SecureRandom random = new SecureRandom();

  /**
   * A manager sealing with that key.
   *
   * @param key the AES key every instance shares, 16, 24 or 32 bytes
   * @param secure whether the cookie is flagged {@code Secure}
   * @param sameSite the cookie's {@code SameSite} attribute
   */
  public CookieFlashMapManager(final byte[] key, final boolean secure, final String sameSite) {
    if (key.length != 16 && key.length != 24 && key.length != 32) {
      throw new IllegalArgumentException(
          "An AES key has 16, 24 or 32 bytes, this one has " + key.length);
    }
    this.key = new SecretKeySpec(key.clone(), "AES");
    this.secure = secure;
    this.sameSite = sameSite;
  }

  @Override
  protected @Nullable List<FlashMap> retrieveFlashMaps(final HttpServletRequest request) {
    // Within one request the list the manager last wrote is the current one: the cookie the
    // request brought still holds a flash map this request has already read.
    @SuppressWarnings("unchecked")
    final List<FlashMap> current = (List<FlashMap>) request.getAttribute(CURRENT);
    if (current != null) {
      return new CopyOnWriteArrayList<>(current);
    }
    final Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (final Cookie cookie : cookies) {
      if (COOKIE_NAME.equals(cookie.getName())) {
        return open(cookie.getValue());
      }
    }
    return null;
  }

  @Override
  protected void updateFlashMaps(
      final List<FlashMap> flashMaps,
      final HttpServletRequest request,
      final HttpServletResponse response) {
    final List<FlashMap> kept = latestPerPage(flashMaps);
    request.setAttribute(CURRENT, kept);
    final ResponseCookie.ResponseCookieBuilder cookie =
        ResponseCookie.from(COOKIE_NAME).path("/").httpOnly(true).secure(secure).sameSite(sameSite);
    if (kept.isEmpty()) {
      cookie.value("").maxAge(0);
    } else {
      cookie.value(seal(kept)).maxAge(getFlashMapTimeout());
    }
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.build().toString());
  }

  /**
   * One flash map per page: a later redirect to the same page replaces what an earlier one left
   * there unread, so a page never shows a notice older than the last one sent to it.
   */
  private static List<FlashMap> latestPerPage(final List<FlashMap> flashMaps) {
    final Map<List<Object>, FlashMap> latest = new LinkedHashMap<>();
    for (final FlashMap flashMap : flashMaps) {
      final List<Object> page =
          Arrays.asList(flashMap.getTargetRequestPath(), flashMap.getTargetRequestParams());
      latest.remove(page);
      latest.put(page, flashMap);
    }
    return List.copyOf(latest.values());
  }

  /** No lock: the flash maps live in the request and the response, not in shared state. */
  @Override
  protected @Nullable Object getFlashMapsMutex(final HttpServletRequest request) {
    return null;
  }

  private String seal(final List<FlashMap> flashMaps) {
    try {
      final byte[] nonce = new byte[NONCE_BYTES];
      random.nextBytes(nonce);
      final Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
      cipher.updateAAD(COOKIE_NAME.getBytes(StandardCharsets.US_ASCII));
      final byte[] sealed = cipher.doFinal(write(flashMaps));
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(
              ByteBuffer.allocate(nonce.length + sealed.length).put(nonce).put(sealed).array());
    } catch (final GeneralSecurityException e) {
      throw new IllegalStateException("The flash maps could not be sealed", e);
    }
  }

  private @Nullable List<FlashMap> open(final String value) {
    try {
      final byte[] bytes = Base64.getUrlDecoder().decode(value);
      if (bytes.length <= NONCE_BYTES) {
        return null;
      }
      final Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, bytes, 0, NONCE_BYTES));
      cipher.updateAAD(COOKIE_NAME.getBytes(StandardCharsets.US_ASCII));
      return read(cipher.doFinal(bytes, NONCE_BYTES, bytes.length - NONCE_BYTES));
    } catch (final GeneralSecurityException | IllegalArgumentException | IOException e) {
      logger.debug("Ignoring a flash map cookie that does not open", e);
      return null;
    }
  }

  private static byte[] write(final List<FlashMap> flashMaps) {
    try {
      final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      final DataOutputStream out = new DataOutputStream(bytes);
      out.writeByte(FORMAT);
      out.writeShort(flashMaps.size());
      for (final FlashMap flashMap : flashMaps) {
        final String path = flashMap.getTargetRequestPath();
        out.writeBoolean(path != null);
        if (path != null) {
          out.writeUTF(path);
        }
        out.writeShort(flashMap.getTargetRequestParams().size());
        for (final Map.Entry<String, List<String>> param :
            flashMap.getTargetRequestParams().entrySet()) {
          out.writeUTF(param.getKey());
          out.writeShort(param.getValue().size());
          for (final String paramValue : param.getValue()) {
            out.writeUTF(paramValue);
          }
        }
        out.writeLong(flashMap.getExpirationTime());
        out.writeShort(flashMap.size());
        for (final Map.Entry<String, Object> attribute : flashMap.entrySet()) {
          out.writeUTF(attribute.getKey());
          switch (attribute.getValue()) {
            case String text -> {
              out.writeByte(STRING);
              out.writeUTF(text);
            }
            case Integer number -> {
              out.writeByte(INTEGER);
              out.writeInt(number);
            }
            default ->
                throw new IllegalArgumentException(
                    "A flash attribute in the cookie is a String or an Integer, not "
                        + attribute.getValue().getClass().getName()
                        + " ("
                        + attribute.getKey()
                        + ")");
          }
        }
      }
      out.flush();
      return bytes.toByteArray();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static @Nullable List<FlashMap> read(final byte[] plain) throws IOException {
    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(plain));
    if (in.readByte() != FORMAT) {
      return null;
    }
    final int count = in.readUnsignedShort();
    final List<FlashMap> flashMaps = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      final FlashMap flashMap = new FlashMap();
      if (in.readBoolean()) {
        flashMap.setTargetRequestPath(in.readUTF());
      }
      final int params = in.readUnsignedShort();
      for (int p = 0; p < params; p++) {
        final String name = in.readUTF();
        final int values = in.readUnsignedShort();
        for (int v = 0; v < values; v++) {
          flashMap.addTargetRequestParam(name, in.readUTF());
        }
      }
      flashMap.setExpirationTime(in.readLong());
      final int attributes = in.readUnsignedShort();
      for (int a = 0; a < attributes; a++) {
        final String name = in.readUTF();
        final byte type = in.readByte();
        if (type == STRING) {
          flashMap.put(name, in.readUTF());
        } else if (type == INTEGER) {
          flashMap.put(name, in.readInt());
        } else {
          return null;
        }
      }
      flashMaps.add(flashMap);
    }
    return new CopyOnWriteArrayList<>(flashMaps);
  }
}
