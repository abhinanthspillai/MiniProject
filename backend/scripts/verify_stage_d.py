import uuid
import requests

BASE_URL = "http://127.0.0.1:8000/api/v1"

def login(email, password):
    res = requests.post(f"{BASE_URL}/auth/login", json={"email": email, "password": password})
    assert res.status_code == 200, f"Login failed for {email}: {res.text}"
    return res.json()["access_token"]

def main():
    print("--- STAGE D BACKEND AUTHORIZATION MATRIX VERIFICATION ---")

    # 1. Login as Owner Admin
    token_owner = login("admin@netraze.app", "AdminPassword123!")
    headers_owner = {"Authorization": f"Bearer {token_owner}"}

    # 2. Create Project
    res_p = requests.post(f"{BASE_URL}/projects", json={"name": "HQ Innovation Center"}, headers=headers_owner)
    assert res_p.status_code == 201, f"Create project failed: {res_p.text}"
    project = res_p.json()
    project_id = project["id"]
    print(f"[PASS] Owner Admin created Project: {project['name']} ({project_id}) -> 201 Created")

    # 3. Create Building
    res_b = requests.post(f"{BASE_URL}/projects/{project_id}/buildings", json={"name": "Tower A"}, headers=headers_owner)
    assert res_b.status_code == 201, f"Create building failed: {res_b.text}"
    building = res_b.json()
    building_id = building["id"]
    print(f"[PASS] Owner Admin created Building: {building['name']} ({building_id}) -> 201 Created")

    # 4. Create Floor
    res_f = requests.post(f"{BASE_URL}/buildings/{building_id}/floors", json={"name": "Ground Floor"}, headers=headers_owner)
    assert res_f.status_code == 201, f"Create floor failed: {res_f.text}"
    floor = res_f.json()
    floor_id = floor["id"]
    print(f"[PASS] Owner Admin created Floor: {floor['name']} ({floor_id}) -> 201 Created")

    # 5. Create Survey Area
    res_sa = requests.post(f"{BASE_URL}/floors/{floor_id}/survey-areas", json={"name": "Research Lab"}, headers=headers_owner)
    assert res_sa.status_code == 201, f"Create survey area failed: {res_sa.text}"
    area = res_sa.json()
    area_id = area["id"]
    print(f"[PASS] Owner Admin created Survey Area: {area['name']} ({area_id}) -> 201 Created")

    # 6. Add tech@netraze.app and a Member Admin to project_members via DB or bootstrap
    token_tech = login("tech@netraze.app", "Password123!")
    headers_tech = {"Authorization": f"Bearer {token_tech}"}

    # Technician creation attempt -> 403 Forbidden
    res_tech_create = requests.post(f"{BASE_URL}/projects", json={"name": "Tech Rogue Project"}, headers=headers_tech)
    assert res_tech_create.status_code == 403
    print(f"[PASS] Technician POST /projects -> 403 Forbidden")

    # Non-member GET /projects/{project_id} -> 404 Not Found
    res_non_get = requests.get(f"{BASE_URL}/projects/{project_id}", headers=headers_tech)
    assert res_non_get.status_code == 404
    print(f"[PASS] Non-member GET /projects/{project_id} -> 404 Not Found")

    # Add tech@netraze.app to project_members using DB directly
    from app.core.database import SessionLocal
    from app.models.domain import User, ProjectMember
    with SessionLocal() as db:
        tech_user = db.query(User).filter(User.email == "tech@netraze.app").first()
        if tech_user:
            existing_pm = db.query(ProjectMember).filter(ProjectMember.project_id == project_id, ProjectMember.user_id == tech_user.id).first()
            if not existing_pm:
                db.add(ProjectMember(project_id=uuid.UUID(project_id), user_id=tech_user.id))
                db.commit()
                print(f"[INFO] Added tech@netraze.app to project_members for {project_id}")

        # Create member_admin@netraze.app
        from app.core.security import get_password_hash
        madmin = db.query(User).filter(User.email == "member_admin@netraze.app").first()
        if not madmin:
            madmin = User(id=uuid.uuid4(), email="member_admin@netraze.app", password_hash=get_password_hash("Password123!"), role="administrator")
            db.add(madmin)
            db.commit()
        db.add(ProjectMember(project_id=uuid.UUID(project_id), user_id=madmin.id))
        db.commit()
        print(f"[INFO] Added member_admin@netraze.app to project_members for {project_id}")

    # Tech Member GET project & survey area -> 200 OK
    res_tech_get_p = requests.get(f"{BASE_URL}/projects/{project_id}", headers=headers_tech)
    assert res_tech_get_p.status_code == 200
    print(f"[PASS] Member Technician GET /projects/{project_id} -> 200 OK")

    res_tech_get_sa = requests.get(f"{BASE_URL}/survey-areas/{area_id}", headers=headers_tech)
    assert res_tech_get_sa.status_code == 200
    print(f"[PASS] Member Technician GET /survey-areas/{area_id} -> 200 OK")

    # Tech Member POST building -> 403 Forbidden
    res_tech_post_b = requests.post(f"{BASE_URL}/projects/{project_id}/buildings", json={"name": "Tech Tower"}, headers=headers_tech)
    assert res_tech_post_b.status_code == 403
    print(f"[PASS] Member Technician POST /buildings -> 403 Forbidden")

    # Member Admin login
    token_madmin = login("member_admin@netraze.app", "Password123!")
    headers_madmin = {"Authorization": f"Bearer {token_madmin}"}

    # Member Admin GET -> 200 OK
    res_madmin_get = requests.get(f"{BASE_URL}/projects/{project_id}", headers=headers_madmin)
    assert res_madmin_get.status_code == 200
    print(f"[PASS] Member Admin GET /projects/{project_id} -> 200 OK")

    # Member Admin POST building (not owner) -> 403 Forbidden
    res_madmin_post = requests.post(f"{BASE_URL}/projects/{project_id}/buildings", json={"name": "MAdmin Tower"}, headers=headers_madmin)
    assert res_madmin_post.status_code == 403
    print(f"[PASS] Member Admin (non-owner) POST /buildings -> 403 Forbidden")

    print("\n>>> ALL STAGE D BACKEND AUTHORIZATION MATRIX TESTS PASSED PERFECTLY! <<<")

if __name__ == "__main__":
    main()
