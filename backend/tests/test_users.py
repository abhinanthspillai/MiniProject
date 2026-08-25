import uuid
import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.core.database import SessionLocal
from app.core.security import create_access_token, get_password_hash
from app.models.domain import User

client = TestClient(app)


def setup_users_test_data():
    session = SessionLocal()
    try:
        admin_id = uuid.uuid4()
        admin = User(
            id=admin_id,
            email=f"admin_{admin_id.hex[:6]}@netraze.app",
            password_hash=get_password_hash("AdminPass123!"),
            role="administrator"
        )
        tech_id = uuid.uuid4()
        tech = User(
            id=tech_id,
            email=f"tech_{tech_id.hex[:6]}@netraze.app",
            password_hash=get_password_hash("TechPass123!"),
            role="user"
        )
        session.add_all([admin, tech])
        session.commit()

        token_admin = create_access_token(admin_id)
        token_tech = create_access_token(tech_id)

        return {
            "admin_email": admin.email,
            "tech_email": tech.email,
            "token_admin": token_admin,
            "token_tech": token_tech
        }
    finally:
        session.close()


def test_create_user_unauthorized_if_no_token():
    res = client.post("/api/v1/users", json={
        "email": "newuser@netraze.app",
        "password": "Password123!",
        "role": "user"
    })
    assert res.status_code == 401


def test_create_user_forbidden_if_not_administrator():
    data = setup_users_test_data()
    headers = {"Authorization": f"Bearer {data['token_tech']}"}

    res = client.post("/api/v1/users", json={
        "email": "newtech@netraze.app",
        "password": "Password123!",
        "role": "user"
    }, headers=headers)

    assert res.status_code == 403


def test_create_user_succeeds_for_administrator():
    data = setup_users_test_data()
    headers = {"Authorization": f"Bearer {data['token_admin']}"}
    new_email = f"newuser_{uuid.uuid4().hex[:6]}@netraze.app"

    res = client.post("/api/v1/users", json={
        "email": new_email,
        "password": "Password123!",
        "role": "user"
    }, headers=headers)

    assert res.status_code == 201
    res_data = res.json()
    assert res_data["email"] == new_email
    assert res_data["role"] == "user"

    # Verify user can authenticate with new password
    login_res = client.post("/api/v1/auth/login", json={
        "email": new_email,
        "password": "Password123!"
    })
    assert login_res.status_code == 200
    assert "access_token" in login_res.json()


def test_create_user_duplicate_email_rejected():
    data = setup_users_test_data()
    headers = {"Authorization": f"Bearer {data['token_admin']}"}

    res = client.post("/api/v1/users", json={
        "email": data["tech_email"],
        "password": "Password123!",
        "role": "user"
    }, headers=headers)

    assert res.status_code == 409


def test_create_user_invalid_role_rejected():
    data = setup_users_test_data()
    headers = {"Authorization": f"Bearer {data['token_admin']}"}

    res = client.post("/api/v1/users", json={
        "email": "guest@netraze.app",
        "password": "Password123!",
        "role": "guest"
    }, headers=headers)

    assert res.status_code == 422


def test_reset_password_succeeds_for_administrator():
    data = setup_users_test_data()
    headers = {"Authorization": f"Bearer {data['token_admin']}"}

    res = client.post("/api/v1/users/reset-password", json={
        "target_email": data["tech_email"],
        "new_password": "NewTechPass123!"
    }, headers=headers)

    assert res.status_code == 200
    assert res.json()["message"] == "Password reset successfully."

    # Verify old password fails
    old_login = client.post("/api/v1/auth/login", json={
        "email": data["tech_email"],
        "password": "TechPass123!"
    })
    assert old_login.status_code == 401

    # Verify new password succeeds
    new_login = client.post("/api/v1/auth/login", json={
        "email": data["tech_email"],
        "password": "NewTechPass123!"
    })
    assert new_login.status_code == 200


def test_reset_password_fails_if_not_administrator():
    data = setup_users_test_data()
    headers = {"Authorization": f"Bearer {data['token_tech']}"}

    res = client.post("/api/v1/users/reset-password", json={
        "target_email": data["admin_email"],
        "new_password": "HackedPass123!"
    }, headers=headers)

    assert res.status_code == 403


def test_reset_password_fails_for_missing_user():
    data = setup_users_test_data()
    headers = {"Authorization": f"Bearer {data['token_admin']}"}

    res = client.post("/api/v1/users/reset-password", json={
        "target_email": "nonexistent@netraze.app",
        "new_password": "SomePassword123!"
    }, headers=headers)

    assert res.status_code == 404
