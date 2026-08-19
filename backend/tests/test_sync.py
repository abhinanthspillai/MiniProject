import uuid
import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.core.database import SessionLocal
from app.core.security import create_access_token, get_password_hash
from app.models.domain import Building, Floor, Project, ProjectMember, SurveyArea, User

client = TestClient(app)


def setup_sync_test_data():
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
        project = Project(id=project_id, owner_id=admin_id, name="Sync Test Project", is_active=True)
        session.add(project)
        session.commit()

        building_id = uuid.uuid4()
        building = Building(id=building_id, project_id=project_id, name="Building B")
        session.add(building)
        session.commit()

        floor_id = uuid.uuid4()
        floor = Floor(id=floor_id, building_id=building_id, name="Floor 2")
        session.add(floor)
        session.commit()

        area_id = uuid.uuid4()
        area = SurveyArea(id=area_id, floor_id=floor_id, name="Meeting Room 2A")
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


def test_sync_survey_root_spatial_positions_and_scan_cycles():
    data = setup_sync_test_data()
    headers = {"Authorization": f"Bearer {data['token_admin']}"}

    android_survey_id = str(uuid.uuid4())
    android_pos_id = str(uuid.uuid4())
    android_cycle_id = str(uuid.uuid4())
    android_obs_id = str(uuid.uuid4())

    payload = {
        "survey": {
            "id": android_survey_id,
            "survey_area_id": data["area_id"],
            "title": "Offline Created Survey",
            "mode": "location_survey"
        },
        "spatial_positions": [
            {
                "id": android_pos_id,
                "label": "Point 1",
                "latitude": 12.9716,
                "longitude": 77.5946,
                "accuracy_meters": 4.0,
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
                        "id": android_obs_id,
                        "scan_cycle_id": android_cycle_id,
                        "ssid": "Netraze_AP1",
                        "bssid": "AA:BB:CC:DD:EE:FF",
                        "rssi_dbm": -55,
                        "frequency_mhz": 2412,
                        "channel": 1,
                        "channel_source": "frequency_conversion"
                    }
                ]
            }
        ]
    }

    # First Sync -> Ingests root + 1 pos + 1 cycle + 1 obs
    res = client.post(f"/api/v1/surveys/{android_survey_id}/sync", json=payload, headers=headers)
    assert res.status_code == 200
    res_data = res.json()
    assert res_data["survey_id"] == android_survey_id
    assert res_data["ingested_spatial_positions"] == 1
    assert res_data["ingested_scan_cycles"] == 1
    assert res_data["ingested_wifi_observations"] == 1

    # Idempotent Second Sync -> Returns 0 new ingestions without error
    res_idempotent = client.post(f"/api/v1/surveys/{android_survey_id}/sync", json=payload, headers=headers)
    assert res_idempotent.status_code == 200
    idempotent_data = res_idempotent.json()
    assert idempotent_data["ingested_spatial_positions"] == 0
    assert idempotent_data["ingested_scan_cycles"] == 0
    assert idempotent_data["ingested_wifi_observations"] == 0
