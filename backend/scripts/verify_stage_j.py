import uuid
import sys
import os
import requests

BASE_URL = "http://127.0.0.1:8000"

def run_verification():
    print("=== NETRAZE STAGE J (RAW EVIDENCE QUERY ROUTES) VERIFICATION ===")

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

    # 3. Create survey and sync observations
    survey_id = str(uuid.uuid4())
    pos_id = str(uuid.uuid4())
    cycle_id = str(uuid.uuid4())
    obs_id = str(uuid.uuid4())

    sync_payload = {
        "survey": {
            "id": survey_id,
            "survey_area_id": area_id,
            "title": "Stage J Verification Survey",
            "mode": "location_survey"
        },
        "spatial_positions": [
            {
                "id": pos_id,
                "label": "Test Point 1",
                "latitude": 12.9716,
                "longitude": 77.5946,
                "accuracy_meters": 3.0,
                "captured_at": "2026-08-19T00:00:00Z"
            }
        ],
        "scan_cycles": [
            {
                "id": cycle_id,
                "spatial_position_id": pos_id,
                "captured_at_wallclock": "2026-08-19T00:00:05Z",
                "observations": [
                    {
                        "id": obs_id,
                        "scan_cycle_id": cycle_id,
                        "ssid": "StageJ_WiFi",
                        "bssid": "33:44:55:66:77:88",
                        "rssi_dbm": -48,
                        "frequency_mhz": 5200,
                        "channel": 40,
                        "channel_source": "frequency_conversion"
                    }
                ]
            }
        ]
    }

    sync_resp = requests.post(f"{BASE_URL}/api/v1/surveys/{survey_id}/sync", json=sync_payload, headers=headers)
    assert sync_resp.status_code == 200, f"Sync failed: {sync_resp.text}"
    print("3. Central Evidence Ingestion: 200 OK")

    # 4. Verify GET /surveys/{id}/access-points
    ap_resp = requests.get(f"{BASE_URL}/api/v1/surveys/{survey_id}/access-points", headers=headers)
    assert ap_resp.status_code == 200, f"Failed APs: {ap_resp.text}"
    aps = ap_resp.json()
    assert len(aps) >= 1
    assert aps[0]["bssid"] == "33:44:55:66:77:88"
    print(f"4. GET /surveys/{survey_id}/access-points: 200 OK (BSSID 33:44:55:66:77:88 match OK)")

    # 5. Verify GET /surveys/{id}/channels
    ch_resp = requests.get(f"{BASE_URL}/api/v1/surveys/{survey_id}/channels", headers=headers)
    assert ch_resp.status_code == 200, f"Failed Channels: {ch_resp.text}"
    channels = ch_resp.json()
    assert len(channels) >= 1
    assert channels[0]["channel"] == 40
    print(f"5. GET /surveys/{survey_id}/channels: 200 OK (Channel 40 match OK)")

    # 6. Verify GET /surveys/{id}/observations
    obs_resp = requests.get(f"{BASE_URL}/api/v1/surveys/{survey_id}/observations", headers=headers)
    assert obs_resp.status_code == 200, f"Failed Observations: {obs_resp.text}"
    obs = obs_resp.json()
    assert len(obs) >= 1
    assert obs[0]["ssid"] == "StageJ_WiFi"
    print(f"6. GET /surveys/{survey_id}/observations: 200 OK (SSID StageJ_WiFi match OK)")

    print("=== ALL STAGE J MATRIX CHECKS PASSED 100% ===")

if __name__ == "__main__":
    run_verification()
