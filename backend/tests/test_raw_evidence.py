import uuid
import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.core.database import SessionLocal
from app.core.security import create_access_token, get_password_hash
from app.models.domain import Building, Floor, Project, ProjectMember, SurveyArea, User

client = TestClient(app)


def setup_raw_evidence_test_data():
    session = SessionLocal()
    try:
        admin_id = uuid.uuid4()
        admin = User(
            id=admin_id,
            email=f"admin_{admin_id.hex[:6]}@netraze.app",
            password_hash=get_password_hash("Password123!"),
            role="administrator"
        )
        session.add(admin)
        session.commit()

        project_id = uuid.uuid4()
        project = Project(id=project_id, owner_id=admin_id, name="Raw Evidence Test Project", is_active=True)
        session.add(project)
        session.commit()

        building_id = uuid.uuid4()
        building = Building(id=building_id, project_id=project_id, name="Building C")
        session.add(building)
        session.commit()

        floor_id = uuid.uuid4()
        floor = Floor(id=floor_id, building_id=building_id, name="Floor 4")
        session.add(floor)
        session.commit()

        area_id = uuid.uuid4()
        area = SurveyArea(id=area_id, floor_id=floor_id, name="Server Room C")
        session.add(area)
        session.commit()

        pm = ProjectMember(project_id=project_id, user_id=admin_id)
        session.add(pm)
        session.commit()

        token_admin = create_access_token(admin_id)

        return {
            "area_id": str(area_id),
            "token_admin": token_admin
        }
    finally:
        session.close()


def test_query_access_points_channels_and_observations():
    data = setup_raw_evidence_test_data()
    headers = {"Authorization": f"Bearer {data['token_admin']}"}

    android_survey_id = str(uuid.uuid4())
    android_pos_id = str(uuid.uuid4())
    android_cycle_id = str(uuid.uuid4())
    android_obs_id_1 = str(uuid.uuid4())
    android_obs_id_2 = str(uuid.uuid4())

    sync_payload = {
        "survey": {
            "id": android_survey_id,
            "survey_area_id": data["area_id"],
            "title": "Evidence Query Test Survey",
            "mode": "location_survey"
        },
        "spatial_positions": [
            {
                "id": android_pos_id,
                "label": "Rack 1",
                "latitude": 12.9716,
                "longitude": 77.5946,
                "accuracy_meters": 2.0,
                "captured_at": "2026-08-19T00:00:00Z"
            }
        ],
        "scan_cycles": [
            {
                "id": android_cycle_id,
                "spatial_position_id": android_pos_id,
                "captured_at_wallclock": "2026-08-19T00:00:05Z",
                "observations": [
                    {
                        "id": android_obs_id_1,
                        "scan_cycle_id": android_cycle_id,
                        "ssid": "Netraze_AP_5G",
                        "bssid": "11:22:33:44:55:66",
                        "rssi_dbm": -45,
                        "frequency_mhz": 5180,
                        "channel": 36,
                        "channel_source": "frequency_conversion"
                    },
                    {
                        "id": android_obs_id_2,
                        "scan_cycle_id": android_cycle_id,
                        "ssid": "Netraze_AP_2G",
                        "bssid": "AA:BB:CC:DD:EE:FF",
                        "rssi_dbm": -60,
                        "frequency_mhz": 2412,
                        "channel": 1,
                        "channel_source": "frequency_conversion"
                    }
                ]
            }
        ]
    }

    # Ingest evidence
    sync_res = client.post(f"/api/v1/surveys/{android_survey_id}/sync", json=sync_payload, headers=headers)
    assert sync_res.status_code == 200

    # 1. GET /surveys/{id}/access-points
    ap_res = client.get(f"/api/v1/surveys/{android_survey_id}/access-points", headers=headers)
    assert ap_res.status_code == 200
    aps = ap_res.json()
    assert len(aps) == 2
    bssids = {ap["bssid"] for ap in aps}
    assert "11:22:33:44:55:66" in bssids
    assert "AA:BB:CC:DD:EE:FF" in bssids

    # 2. GET /surveys/{id}/channels
    ch_res = client.get(f"/api/v1/surveys/{android_survey_id}/channels", headers=headers)
    assert ch_res.status_code == 200
    channels = ch_res.json()
    assert len(channels) == 2

    # 3. GET /surveys/{id}/observations
    obs_res = client.get(f"/api/v1/surveys/{android_survey_id}/observations", headers=headers)
    assert obs_res.status_code == 200
    obs_list = obs_res.json()
    assert len(obs_list) == 2
