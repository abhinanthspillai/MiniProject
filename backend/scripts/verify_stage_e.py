import uuid
import sys
import os
import requests

BASE_URL = "http://127.0.0.1:8000"

def run_verification():
    print("=== NETRAZE STAGE E (SURVEY LIFECYCLE) VERIFICATION ===")

    # 1. Login as Administrator admin@gamail.com / Admin@123
    resp = requests.post(f"{BASE_URL}/api/v1/auth/login", json={"email": "admin@gamail.com", "password": "Admin@123"})
    if resp.status_code != 200:
        print(f"FAILED to login as admin: {resp.status_code} {resp.text}")
        sys.exit(1)
    admin_token = resp.json()["access_token"]
    headers = {"Authorization": f"Bearer {admin_token}"}
    print("1. Admin Login: 200 OK")

    # 2. Get projects
    resp = requests.get(f"{BASE_URL}/api/v1/projects", headers=headers)
    projects = resp.json()
    if not projects:
        print("Creating project...")
        resp = requests.post(f"{BASE_URL}/api/v1/projects", json={"name": "Stage E Project"}, headers=headers)
        project_id = resp.json()["id"]
    else:
        project_id = projects[0]["id"]
    print(f"2. Project ID: {project_id}")

    # 3. Get buildings
    resp = requests.get(f"{BASE_URL}/api/v1/projects/{project_id}/buildings", headers=headers)
    buildings = resp.json()
    if not buildings:
        resp = requests.post(f"{BASE_URL}/api/v1/projects/{project_id}/buildings", json={"name": "Building Alpha"}, headers=headers)
        building_id = resp.json()["id"]
    else:
        building_id = buildings[0]["id"]

    # 4. Get floors
    resp = requests.get(f"{BASE_URL}/api/v1/buildings/{building_id}/floors", headers=headers)
    floors = resp.json()
    if not floors:
        resp = requests.post(f"{BASE_URL}/api/v1/buildings/{building_id}/floors", json={"name": "Floor 1"}, headers=headers)
        floor_id = resp.json()["id"]
    else:
        floor_id = floors[0]["id"]

    # 5. Get survey areas
    resp = requests.get(f"{BASE_URL}/api/v1/floors/{floor_id}/survey-areas", headers=headers)
    areas = resp.json()
    if not areas:
        resp = requests.post(f"{BASE_URL}/api/v1/floors/{floor_id}/survey-areas", json={"name": "Server Room Area"}, headers=headers)
        area_id = resp.json()["id"]
    else:
        area_id = areas[0]["id"]
    print(f"5. Survey Area ID: {area_id}")

    # 6. Test Android Canonical UUID Preservation
    android_generated_uuid = str(uuid.uuid4())
    payload = {
        "id": android_generated_uuid,
        "title": "Stage E Physical Device Survey",
        "mode": "location_survey"
    }
    resp = requests.post(f"{BASE_URL}/api/v1/survey-areas/{area_id}/surveys", json=payload, headers=headers)
    assert resp.status_code == 201, f"Expected 201, got {resp.status_code}: {resp.text}"
    created = resp.json()
    assert created["id"] == android_generated_uuid, f"UUID mismatch! Sent {android_generated_uuid}, got {created['id']}"
    assert created["title"] == "Stage E Physical Device Survey"
    assert created["mode"] == "location_survey"
    assert created["status"] == "in_progress"
    print("6. Android Canonical UUID Preservation: 201 CREATED (Match OK)")

    # 7. Test REST PATCH restricted to title ONLY
    resp = requests.patch(f"{BASE_URL}/api/v1/surveys/{android_generated_uuid}", json={"title": "Updated Survey Title"}, headers=headers)
    assert resp.status_code == 200
    assert resp.json()["title"] == "Updated Survey Title"
    print("7. PATCH title only: 200 OK")

    # 8. Test rejection of prohibited PATCH fields (notes, status)
    resp_prohibited_notes = requests.patch(f"{BASE_URL}/api/v1/surveys/{android_generated_uuid}", json={"notes": "Illegal note"}, headers=headers)
    assert resp_prohibited_notes.status_code == 422, f"Expected 422 for notes, got {resp_prohibited_notes.status_code}"

    resp_prohibited_status = requests.patch(f"{BASE_URL}/api/v1/surveys/{android_generated_uuid}", json={"status": "completed"}, headers=headers)
    assert resp_prohibited_status.status_code == 422, f"Expected 422 for status, got {resp_prohibited_status.status_code}"
    print("8. Prohibited PATCH Field Rejection (notes, status -> 422 Unprocessable Entity): REJECTED OK")

    print("=== ALL STAGE E MATRIX CHECKS PASSED 100% ===")

if __name__ == "__main__":
    run_verification()
