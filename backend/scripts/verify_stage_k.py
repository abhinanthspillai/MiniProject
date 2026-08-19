import uuid
import sys
import os
import requests

BASE_URL = "http://127.0.0.1:8000"

def run_verification():
    print("=== NETRAZE STAGE K (D080 COMPLETION BARRIER) VERIFICATION ===")

    # 1. Login as Administrator
    resp = requests.post(f"{BASE_URL}/api/v1/auth/login", json={"email": "admin@gamail.com", "password": "Admin@123"})
    assert resp.status_code == 200, f"Failed login: {resp.text}"
    token = resp.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}
    print("1. Admin Login: 200 OK")

    # 2. Get survey area
    projects = requests.get(f"{BASE_URL}/api/v1/projects", headers=headers).json()
    project_id = projects[0]["id"]
    buildings = requests.get(f"{BASE_URL}/api/v1/projects/{project_id}/buildings", headers=headers).json()
    building_id = buildings[0]["id"]
    floors = requests.get(f"{BASE_URL}/api/v1/buildings/{building_id}/floors", headers=headers).json()
    floor_id = floors[0]["id"]
    areas = requests.get(f"{BASE_URL}/api/v1/floors/{floor_id}/survey-areas", headers=headers).json()
    area_id = areas[0]["id"]

    # 3. Create survey and verify initial status = in_progress
    survey_id = str(uuid.uuid4())
    create_resp = requests.post(
        f"{BASE_URL}/api/v1/survey-areas/{area_id}/surveys",
        json={"id": survey_id, "title": "Stage K Completion Test Survey", "mode": "location_survey"},
        headers=headers
    )
    assert create_resp.status_code == 201
    assert create_resp.json()["status"] == "in_progress"
    print("3. Survey Created (status = in_progress): 201 CREATED")

    # 4. Execute POST /surveys/{id}/complete
    comp_resp = requests.post(f"{BASE_URL}/api/v1/surveys/{survey_id}/complete", headers=headers)
    assert comp_resp.status_code == 200, f"Completion failed: {comp_resp.text}"
    comp_data = comp_resp.json()
    assert comp_data["status"] == "completed"
    assert comp_data["completed_at"] is not None
    print("4. POST /surveys/{id}/complete: 200 OK (status = completed, completed_at set)")

    # 5. Verify D080 Frozen Enforcement: PATCH on completed survey fails with 409 Conflict
    patch_resp = requests.patch(f"{BASE_URL}/api/v1/surveys/{survey_id}", json={"title": "Should Fail"}, headers=headers)
    assert patch_resp.status_code == 409, f"Expected 409 Conflict for completed survey, got {patch_resp.status_code}"
    print("5. D080 Frozen State Protection (PATCH on completed survey -> 409 Conflict): REJECTED OK")

    print("=== ALL STAGE K MATRIX CHECKS PASSED 100% ===")

if __name__ == "__main__":
    run_verification()
