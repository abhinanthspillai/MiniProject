import getpass
import os
import re
import sys
import uuid
from sqlalchemy import select
from app.core.database import SessionLocal
from app.core.security import get_password_hash
from app.models.domain import User

EMAIL_REGEX = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


def validate_email(email: str) -> bool:
    return bool(email and EMAIL_REGEX.match(email.strip()))


def bootstrap_admin():
    # Check if env vars are provided for automated/non-interactive execution
    env_email = os.getenv("ADMIN_EMAIL")
    env_password = os.getenv("ADMIN_PASSWORD")

    if env_email and env_password:
        email = env_email.strip().lower()
        password = env_password
    else:
        print("=== Netraze Initial Administrator Setup ===")
        email_input = input("Email: ").strip()
        if not validate_email(email_input):
            print("Error: Invalid email format.", file=sys.stderr)
            sys.exit(1)

        password = getpass.getpass("Password: ")
        if not password:
            print("Error: Password cannot be blank.", file=sys.stderr)
            sys.exit(1)

        confirm_password = getpass.getpass("Confirm Password: ")
        if password != confirm_password:
            print("Error: Password and Confirm Password do not match.", file=sys.stderr)
            sys.exit(1)

        email = email_input.lower()

    with SessionLocal() as db:
        existing_user = db.execute(select(User).where(User.email == email)).scalar_one_or_none()
        if existing_user:
            print(f"Account already exists: {email}")
            return existing_user.id

        admin_user = User(
            id=uuid.uuid4(),
            email=email,
            password_hash=get_password_hash(password),
            role="administrator"
        )
        db.add(admin_user)
        db.commit()
        db.refresh(admin_user)

        print("Administrator account created successfully.")
        print(f"Email: {admin_user.email}")
        return admin_user.id


if __name__ == "__main__":
    bootstrap_admin()
