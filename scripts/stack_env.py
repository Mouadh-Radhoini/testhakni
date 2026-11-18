"""Generate private Compose settings for a new installation or isolated demo."""
import argparse
import base64
import os
import secrets

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("path", help="New private env file (for example .env)")
parser.add_argument("--demo", action="store_true", help="Seed demo accounts in the dedicated test database")
args = parser.parse_args()

settings = {
    "MONGO_ROOT_PASSWORD": secrets.token_hex(32),
    "MONGO_APP_PASSWORD": secrets.token_hex(32),
    "JWT_SECRET": base64.b64encode(secrets.token_bytes(48)).decode(),
}
if args.demo:
    settings.update(APP_PROFILE="dev", APP_DATABASE="testhakni_test", SEED_ENABLED="true",
                    SEED_PASSWORD=secrets.token_urlsafe(24), WEB_PORT="8088")
with os.fdopen(os.open(args.path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600), "w") as env:
    env.writelines(f"{key}={value}\n" for key, value in settings.items())
