import uuid
from datetime import datetime, timedelta, timezone
from typing import Optional
import jwt
from pwdlib import PasswordHash
from app.core.config import settings

# Argon2id password hashing via pwdlib
password_hash_context = PasswordHash.recommended()


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verifies a plaintext password against an Argon2 hash."""
    return password_hash_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """Hashes a password using Argon2id."""
    return password_hash_context.hash(password)


def create_access_token(
    subject: str | uuid.UUID,
    expires_delta: Optional[timedelta] = None
) -> str:
    """
    Creates an expiring signed JWT bearer access token with minimal claims (sub, exp, iat).
    """
    now = datetime.now(timezone.utc)
    if expires_delta:
        expire = now + expires_delta
    else:
        expire = now + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)

    payload = {
        "sub": str(subject),
        "exp": expire,
        "iat": now
    }
    return jwt.encode(payload, settings.SECRET_KEY, algorithm=settings.ALGORITHM)


def decode_access_token(token: str) -> Optional[str]:
    """
    Decodes and validates a signed JWT bearer token.
    Enforces algorithm restriction and expiration.
    Returns sub (user_id) if valid, or None if invalid/expired.
    """
    try:
        payload = jwt.decode(
            token,
            settings.SECRET_KEY,
            algorithms=[settings.ALGORITHM]
        )
        return payload.get("sub")
    except (jwt.PyJWTError, ValueError):
        return None
