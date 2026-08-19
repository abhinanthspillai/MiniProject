import os
import sys
import uuid
from sqlalchemy import select
from app.core.database import SessionLocal
from app.core.security import get_password_hash
from app.models.domain import User

def bootstrap_admin():
    email = os.getenv("ADMIN_EMAIL", "admin@netraze.app")
    password = os.getenv("ADMIN_PASSWORD", "AdminPassword123!")

    with SessionLocal() as db:
        existing_user = db.execute(select(User).where(User.email == email)).scalar_one_or_none()
        if existing_user:
            print(f"User with email '{email}' already exists.")
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
        print(f"Created Administrator account successfully: {admin_user.email} ({admin_user.id})")
        return admin_user.id

if __name__ == "__main__":
    bootstrap_admin()
