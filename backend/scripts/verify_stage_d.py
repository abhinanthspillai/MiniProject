import uuid
import sys
import os
import requests

BASE_URL = "http://127.0.0.1:8000"

def run_stage_d_verification():
    print("=== NETRAZE STAGE D-FINAL VERIFICATION ===")

    # 1. Health check
    health_resp = requests.get(f"{BASE_URL}/api/v1/health")
    assert health_resp.status_code == 200, f"Health check failed: {health_resp.text}"
    health_data = health_resp.json()
    assert health_data["status"] == "ok"
    assert health_data["database"]["status"] == "healthy"
    print("1. FastAPI Health Check: 200 OK")
    print("2. PostgreSQL Database Connection: HEALTHY (connected via psycopg3)")

    # 2. Login as Administrator
    login_resp = requests.post(f"{BASE_URL}/api/v1/auth/login", json={"email": "admin@gamail.com", "password": "Admin@123"})
    assert login_resp.status_code == 200, f"Admin login failed: {login_resp.text}"
    token_data = login_resp.json()
    access_token = token_data["access_token"]
    headers = {"Authorization": f"Bearer {access_token}"}
    print("3. Administrator Login (admin@gamail.com): 200 OK")

    # 3. Hierarchy Traversal (Projects -> Buildings -> Floors -> Survey Areas)
    projects_resp = requests.get(f"{BASE_URL}/api/v1/projects", headers=headers)
    assert projects_resp.status_code == 200, f"Failed projects fetch: {projects_resp.text}"
    projects = projects_resp.json()
    assert len(projects) >= 1
    project_id = projects[0]["id"]
    print(f"4. Projects Query: 200 OK (Found {len(projects)} projects, project_id={project_id})")

    buildings_resp = requests.get(f"{BASE_URL}/api/v1/projects/{project_id}/buildings", headers=headers)
    assert buildings_resp.status_code == 200, f"Failed buildings fetch: {buildings_resp.text}"
    buildings = buildings_resp.json()
    assert len(buildings) >= 1
    building_id = buildings[0]["id"]
    print(f"5. Buildings Query: 200 OK (Found {len(buildings)} buildings, building_id={building_id})")

    floors_resp = requests.get(f"{BASE_URL}/api/v1/buildings/{building_id}/floors", headers=headers)
    assert floors_resp.status_code == 200, f"Failed floors fetch: {floors_resp.text}"
    floors = floors_resp.json()
    assert len(floors) >= 1
    floor_id = floors[0]["id"]
    print(f"6. Floors Query: 200 OK (Found {len(floors)} floors, floor_id={floor_id})")

    areas_resp = requests.get(f"{BASE_URL}/api/v1/floors/{floor_id}/survey-areas", headers=headers)
    assert areas_resp.status_code == 200, f"Failed survey areas fetch: {areas_resp.text}"
    areas = areas_resp.json()
    assert len(areas) >= 1
    area_id = areas[0]["id"]
    print(f"7. Survey Areas Query: 200 OK (Found {len(areas)} areas, area_id={area_id})")

    print("=== ALL STAGE D-FINAL CHECKS PASSED 100% ===")

if __name__ == "__main__":
    run_stage_d_verification()
