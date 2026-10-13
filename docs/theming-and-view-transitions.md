# Theming & View Transitions

## Theme Architecture

Themes are implemented entirely in CSS via `data-theme` attributes on `<html>`. The default (Blueprint) theme lives in `:root`; its component rules are scoped with `html:not([data-theme])` so the other themes are not affected by them. Each additional theme is a `[data-theme="<name>"]` block that overrides design tokens and adds component-specific rules.

**Current themes:**

| Value | Name | Character |
|-------|------|-----------|
| _(none)_ | **Blueprint** — default | domaincentric.dev identity: paper, ink and one teal; Space Grotesk for the interface, Newsreader for running prose, JetBrains Mono for prices; dark header with the Hex Graph mark, honeycomb hero, hairlines instead of shadows |
| `organic` | Organic | Earthy luxury — warm tones, Cormorant Garamond + Nunito Sans |
| `sharp` | Sharp & Minimal | Monochrome + blue — Inter + Instrument Sans, sharp corners |
| `classic` | Classic | Blue/purple gradient hero — Inter only, original design |
| `vibrant` | Neon + Dark | Dark bg, cyan/magenta neon — Space Grotesk, glow shadows |

### How It Works

```
layout.pug
├── <head> inline script    → reads localStorage, sets data-theme before paint (no flash)
├── <footer> <select>       → theme picker with all options
└── <body> bottom script    → syncs <select> value, listens for changes, writes localStorage
```

The Blueprint theme is the default — it uses no `data-theme` attribute (`:root` values apply, and its component rules are written as `html:not([data-theme]) …`). All other themes, Organic included, set `data-theme` on `<html>`, which activates their CSS overrides and switches the Blueprint rules off.

### File Locations

| File | Purpose |
|------|---------|
| `src/main/resources/static/css/main.css` | All theme definitions (tokens + overrides) |
| `src/main/resources/templates/layout.pug` | Theme picker UI + persistence scripts |
| Google Fonts URL (in `layout.pug`) | Font loading for all themes |

## Adding a New Theme

### Step 1: Define Design Tokens

Add a `[data-theme="<name>"]` block in `main.css` after the existing themes (before section 2 — Animations). Override every token category:

```css
[data-theme="mytheme"] {
  /* Colors — all required */
  --color-bg: ...;
  --color-surface: ...;
  --color-primary: ...;
  --color-primary-dark: ...;
  --color-primary-light: ...;
  --color-accent: ...;
  --color-accent-dark: ...;
  --color-accent-light: ...;
  --color-text: ...;
  --color-text-light: ...;
  --color-text-muted: ...;
  --color-border: ...;
  --color-border-light: ...;
  --color-success: ...;
  --color-success-light: ...;
  --color-danger: ...;
  --color-danger-light: ...;
  --color-warning: ...;
  --color-warning-light: ...;
  --color-info: ...;
  --color-info-light: ...;

  /* Semantic contextual colors */
  --color-success-text: ...;
  --color-success-border: ...;
  --color-danger-text: ...;
  --color-danger-hover: ...;
  --color-danger-border: ...;
  --color-warning-text: ...;
  --color-warning-border: ...;
  --color-info-text: ...;
  --color-info-border: ...;

  /* Primary/accent alpha variants */
  --color-primary-border: ...;
  --color-primary-focus: ...;
  --color-accent-border: ...;

  /* Typography */
  --font-display: ...;        /* headings, hero titles */
  --font-family: ...;         /* body text, UI elements */

  /* Borders */
  --radius-sm: ...;
  --radius-md: ...;
  --radius-lg: ...;
  --radius-xl: ...;

  /* Shadows */
  --shadow-sm: ...;
  --shadow-md: ...;
  --shadow-lg: ...;
  --shadow-xl: ...;

  /* Transitions */
  --transition-fast: ...;
  --transition-normal: ...;
  --transition-slow: ...;

  /* Hero & section backgrounds */
  --hero-bg-image: none;       /* or a gradient for the hero background-image */
  --color-muted-bg: ...;       /* .section--muted background */
}
```

**Token guidance:**
- For light themes: `--color-bg` is the page background, `--color-surface` is card/panel backgrounds (usually white)
- For dark themes: both need to be dark values; ensure sufficient contrast for `--color-text` on `--color-bg`
- `--color-primary-light` should work as a hover/focus background — for dark themes this is a darkened tint, not a light pastel
- `--color-*-text` and `--color-*-border` are used in alerts/badges — they must be readable on the corresponding `--color-*-light` background

### Step 2: Add Component Overrides

Tokens alone cover ~80% of the theme. For the remaining visual identity, add component overrides below the token block:

```css
/* Hero — every theme typically needs custom hero styling */
[data-theme="mytheme"] .hero { ... }
[data-theme="mytheme"] .hero__title { ... }
[data-theme="mytheme"] .hero__subtitle { ... }
[data-theme="mytheme"] .hero__description { ... }
[data-theme="mytheme"] .hero__cta--primary { ... }
[data-theme="mytheme"] .hero__cta--primary:hover { ... }
[data-theme="mytheme"] .hero__cta--secondary { ... }
[data-theme="mytheme"] .hero__cta--secondary:hover { ... }
```

**Common overrides to consider:**
- `.hero` — background gradient/color, text color
- `.nav__link` — hover style (underline vs background fill)
- `.product-card:hover` — shadow style (glow, lift, or none)
- `.feature-card` — border accent style
- `.highlight-card` — border accent position (top, left, or full)
- `.section--muted` — background
- `.checkout__step` — progress indicator style (pills vs circles)
- `.section__title`, `h1` — font-family override if display font differs

### Step 3: Add Font (if needed)

If your theme uses a font not already loaded, add it to the Google Fonts URL in `layout.pug`. The URL appears twice (line 10 for normal load, line 15 inside `<noscript>`):

```
&family=Your+Font:wght@400..700
```

Append before `&display=swap`.

### Step 4: Register in the Picker

Add an `<option>` to the theme `<select>` in `layout.pug`:

```pug
option(value="mytheme") My Theme Name
```

The `value` must match the `data-theme` attribute name exactly. No JS changes needed — the existing script handles any value generically.

### Checklist

- [ ] Token block with all required variables
- [ ] Component overrides (at minimum: hero, section titles)
- [ ] Font added to Google Fonts URL (both occurrences) if needed
- [ ] Option added to theme picker in `layout.pug`
- [ ] `./gradlew build` passes
- [ ] Visual check: switch to new theme, verify all pages (home, catalog, detail, cart, checkout, auth, error)

---

## View Transitions

The app uses the [CSS View Transitions API (Level 2)](https://developer.mozilla.org/en-US/docs/Web/API/View_Transition_API) for cross-document (MPA) page transitions. This is a CSS-only approach — no JavaScript is involved.

### Opt-in

```css
@view-transition {
  navigation: auto;
}
```

This tells the browser to automatically create view transitions on same-origin navigations. Works in Chromium browsers; other browsers fall back to normal page loads.

### Page-Level Transition

The default page transition is a fade-out + slide-up:

```css
::view-transition-old(root) {
  animation: vt-fade-out 0.15s ease forwards;
}

::view-transition-new(root) {
  animation: vt-slide-up 0.25s ease forwards;
}
```

The old page fades out quickly (150ms), the new page slides up (250ms). This applies to all content not assigned a specific `view-transition-name`.

### Named Transitions

Elements with `view-transition-name` are excluded from the default page transition and get their own animation. The browser matches elements with the same name across the old and new page and morphs between them.

| Element | `view-transition-name` | Behavior |
|---------|----------------------|----------|
| `.site-header` | `site-header` | Stays fixed — `animation: none` |
| `.site-header__logo` | `site-logo` | Smooth morph (0.25s ease) |
| `.site-footer` | `site-footer` | Stays fixed — `animation: none` |
| `.breadcrumb` | `breadcrumb` | Cross-fade (fade-out 0.12s, fade-in 0.2s with 0.08s delay) |
| `.checkout__progress` | `checkout-progress` | Smooth morph (0.3s ease) |
| Product images | `product-img-<id>` | Morph between card thumbnail and detail view (0.35s) |

### Product Image Morph

Product images have a dynamic `view-transition-name` set via inline styles in the Pug templates:

```pug
//- catalog.pug — card thumbnail
.product-card__image(style="view-transition-name: product-img-" + product.productId())

//- detail.pug — full image
.product-detail__image(style="view-transition-name: product-img-" + productDetail.productId())
```

Because the `view-transition-name` includes the product ID, the browser matches the specific product image across the catalog and detail pages. This creates a smooth "expand" effect when clicking into a product and a "shrink" effect when navigating back.

**Constraint:** Each `view-transition-name` must be unique on a page. Since product IDs are unique, this is naturally satisfied.

### Accessibility

All view transitions respect `prefers-reduced-motion`: the groups (the morph of a named element such as the
product image) are stilled as well as the old and new snapshots, because the universal selector `*` matches no
view-transition pseudo-element:

```css
@media (prefers-reduced-motion: reduce) {
  ::view-transition-group(*),
  ::view-transition-old(*),
  ::view-transition-new(*) {
    animation: none !important;
  }
}
```

### Adding a New Named Transition

1. Assign a `view-transition-name` in CSS (static) or via inline style (dynamic):
   ```css
   .my-element { view-transition-name: my-element; }
   ```
2. Customize the transition animation:
   ```css
   ::view-transition-group(my-element) {
     animation-duration: 0.3s;
   }
   ```
   Or suppress it entirely:
   ```css
   ::view-transition-old(my-element),
   ::view-transition-new(my-element) {
     animation: none;
   }
   ```

### Browser Support

View Transitions Level 2 (cross-document) is supported in Chrome 126+ and Edge 126+. Other browsers ignore the `@view-transition` rule and render normal page loads — the app works identically, just without the animated transitions.
