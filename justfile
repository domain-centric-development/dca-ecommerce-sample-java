# The shop on this machine, with or without the payment provider's stub. `just` lists the recipes.
# Everything else — build, tests, the browser suite — is Gradle's (`./gradlew build test-e2e`).

payment_stub := env("PAYMENT_STUB", "authorize")
port := env("PORT", "8080")
stub_url := "http://localhost:8089"
# Docker where there is one, else Podman — a shell alias `docker=podman` does not reach a recipe
compose := `command -v docker >/dev/null 2>&1 && echo "docker compose" || echo "podman compose"`

# the recipes
default:
    @just --list

# the shop on the port (default 8080), paying through the stand-in inside the shop
run port=port:
    SERVER_PORT={{port}} ./gradlew bootRun

# the shop paying through the stub — authorize (default), refuse or slow; the stub stops with the shop
run-with-provider mode=payment_stub port=port: (stub mode)
    trap 'just stub-stop' EXIT; CHECKOUT_PAYMENTPROVIDER_BASEURL={{stub_url}} SERVER_PORT={{port}} ./gradlew bootRun

# start the payment provider's stub on localhost:8089 — authorize (default), refuse or slow
stub mode=payment_stub:
    PAYMENT_STUB={{mode}} {{compose}} --profile provider-stub up -d --force-recreate payment-provider
    for i in $(seq 1 30); do curl -sf -o /dev/null {{stub_url}}/__admin/health && exit 0; sleep 1; done; echo "the stub did not answer on {{stub_url}}" >&2; exit 1

# stop the payment provider's stub
stub-stop:
    {{compose}} --profile provider-stub down payment-provider
