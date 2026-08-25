import uuid
from datetime import timedelta
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import select
from app.main import app
from app.core.database import SessionLocal
from app.core.security import get_password_hash, verify_password, create_access_token, decode_access_token
from app.models.domain import User

client = TestClient(app)


@pytest.fixture
def db_session():
    session = SessionLocal()
    try:
        yield session
    finally:
        session.rollback()
        session.close()


def test_argon2_password_hashing():
    plain_password = "SecretPassword123!"
    hashed = get_password_hash(plain_password)

    assert hashed != plain_password
    assert hashed.startswith("$argon2")
    assert verify_password(plain_password, hashed) is True
    assert verify_password("WrongPassword", hashed) is False


def test_jwt_token_issuance_and_decoding():
    user_id = uuid.uuid4()
    token = create_access_token(subject=user_id, expires_delta=timedelta(minutes=5))

    sub = decode_access_token(token)
    assert sub == str(user_id)


def test_expired_jwt_token_rejection():
    user_id = uuid.uuid4()
    expired_token = create_access_token(subject=user_id, expires_delta=timedelta(seconds=-10))

    sub = decode_access_token(expired_token)
    assert sub is None


def test_login_success_and_token_issuance(db_session):
    email = f"tech_{uuid.uuid4().hex[:6]}@netraze.app"
    password = "CorrectPassword123!"
    user = User(
        email=email,
        password_hash=get_password_hash(password),
        role="user"
    )
    db_session.add(user)
    db_session.commit()

    response = client.post("/api/v1/auth/login", json={"email": email, "password": password})
    assert response.status_code == 200

    data = response.json()
    assert "access_token" in data
    assert data["token_type"] == "bearer"
    assert "refresh_token" not in data  # No refresh token per D090
    assert data["user"]["email"] == email
    assert data["user"]["role"] == "user"


def test_login_invalid_password_returns_generic_401(db_session):
    email = f"admin_{uuid.uuid4().hex[:6]}@netraze.app"
    password = "CorrectPassword123!"
    user = User(
        email=email,
        password_hash=get_password_hash(password),
        role="administrator"
    )
    db_session.add(user)
    db_session.commit()

    response = client.post("/api/v1/auth/login", json={"email": email, "password": "WrongPassword!"})
    assert response.status_code == 401
    assert response.json()["detail"] == "Invalid email or password"


def test_login_unknown_email_returns_generic_401():
    response = client.post("/api/v1/auth/login", json={"email": "nonexistent@netraze.app", "password": "AnyPassword!"})
    assert response.status_code == 401
    assert response.json()["detail"] == "Invalid email or password"


def test_get_me_protected_endpoint(db_session):
    email = f"admin_{uuid.uuid4().hex[:6]}@netraze.app"
    password = "SecretAdmin123!"
    user = User(
        email=email,
        password_hash=get_password_hash(password),
        role="administrator"
    )
    db_session.add(user)
    db_session.commit()

    token = create_access_token(subject=user.id)

    # Valid Bearer Token
    response = client.get("/api/v1/auth/me", headers={"Authorization": f"Bearer {token}"})
    assert response.status_code == 200
    assert response.json()["email"] == email
    assert response.json()["role"] == "administrator"

    # Missing Header
    response_no_auth = client.get("/api/v1/auth/me")
    assert response_no_auth.status_code == 401

    # Malformed Token
    response_bad_auth = client.get("/api/v1/auth/me", headers={"Authorization": "Bearer invalid_jwt_token_string"})
    assert response_bad_auth.status_code == 401


def test_public_registration_success():
    email = f"newuser_{uuid.uuid4().hex[:6]}@netraze.app"
    response = client.post("/api/v1/auth/register", json={
        "email": email,
        "password": "SecurePassword123!",
        "confirm_password": "SecurePassword123!"
    })
    
    assert response.status_code == 201
    data = response.json()
    assert data["email"] == email
    assert data["role"] == "user"


def test_public_registration_passwords_mismatch():
    email = f"newuser_{uuid.uuid4().hex[:6]}@netraze.app"
    response = client.post("/api/v1/auth/register", json={
        "email": email,
        "password": "SecurePassword123!",
        "confirm_password": "WrongPassword123!"
    })
    
    assert response.status_code == 422
