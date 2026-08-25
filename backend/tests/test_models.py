import uuid
from datetime import datetime, timezone
import pytest
from sqlalchemy import select
from app.core.database import SessionLocal
from app.models.domain import (
    User,
    Project,
    Building,
    Floor,
    SurveyArea,
    FloorPlan,
    Survey,
    SpatialPosition,
    LocationFix,
    ScanCycle,
    WifiObservation,
)


@pytest.fixture
def db_session():
    session = SessionLocal()
    try:
        yield session
    finally:
        session.rollback()
        session.close()


def test_user_creation_and_role_constraint(db_session):
    user = User(
        email=f"test_{uuid.uuid4().hex[:6]}@netraze.app",
        password_hash="argon2_hashed_secret",
        role="administrator"
    )
    db_session.add(user)
    db_session.commit()

    saved_user = db_session.execute(select(User).where(User.id == user.id)).scalar_one()
    assert saved_user.email == user.email
    assert saved_user.role == "administrator"


def test_invalid_role_constraint_raises_error(db_session):
    invalid_user = User(
        email=f"test_{uuid.uuid4().hex[:6]}@netraze.app",
        password_hash="secret",
        role="invalid_role"
    )
    db_session.add(invalid_user)
    with pytest.raises(Exception):
        db_session.commit()
    db_session.rollback()


def test_survey_hierarchy_and_mode_constraint(db_session):
    admin = User(
        email=f"admin_{uuid.uuid4().hex[:6]}@netraze.app",
        password_hash="secret",
        role="administrator"
    )
    tech = User(
        email=f"tech_{uuid.uuid4().hex[:6]}@netraze.app",
        password_hash="secret",
        role="user"
    )
    db_session.add_all([admin, tech])
    db_session.commit()

    project = Project(owner_id=admin.id, name="Test HQ Survey")
    db_session.add(project)
    db_session.commit()

    building = Building(project_id=project.id, name="Main Building")
    db_session.add(building)
    db_session.commit()

    floor = Floor(building_id=building.id, name="Floor 1")
    db_session.add(floor)
    db_session.commit()

    area = SurveyArea(floor_id=floor.id, name="North Wing")
    db_session.add(area)
    db_session.commit()

    floor_plan = FloorPlan(
        survey_area_id=area.id,
        storage_path="/uploads/plans/fp1.png",
        original_filename="fp1.png",
        width_px=1920,
        height_px=1080,
        uploaded_by=admin.id
    )
    db_session.add(floor_plan)
    db_session.commit()

    # Valid Floor Plan Mode Survey
    survey_fp = Survey(
        id=uuid.uuid4(),
        survey_area_id=area.id,
        title="Floor Plan Survey Session 1",
        mode="floor_plan",
        status="in_progress",
        floor_plan_id=floor_plan.id,
        simple_map_id=None,
        created_by=tech.id,
        started_at=datetime.now(timezone.utc)
    )
    db_session.add(survey_fp)
    db_session.commit()

    assert survey_fp.mode == "floor_plan"
    assert survey_fp.floor_plan_id == floor_plan.id


def test_location_fix_one_to_one_constraint(db_session):
    admin = User(email=f"admin_{uuid.uuid4().hex[:6]}@netraze.app", password_hash="secret", role="administrator")
    tech = User(email=f"tech_{uuid.uuid4().hex[:6]}@netraze.app", password_hash="secret", role="user")
    db_session.add_all([admin, tech])
    db_session.commit()

    project = Project(owner_id=admin.id, name="Outdoor Project")
    db_session.add(project)
    db_session.commit()

    building = Building(project_id=project.id, name="B1")
    db_session.add(building)
    db_session.commit()

    floor = Floor(building_id=building.id, name="F1")
    db_session.add(floor)
    db_session.commit()

    area = SurveyArea(floor_id=floor.id, name="Courtyard")
    db_session.add(area)
    db_session.commit()

    survey_loc = Survey(
        id=uuid.uuid4(),
        survey_area_id=area.id,
        title="Location Survey Session",
        mode="location_survey",
        status="in_progress",
        floor_plan_id=None,
        simple_map_id=None,
        created_by=tech.id,
        started_at=datetime.now(timezone.utc)
    )
    db_session.add(survey_loc)
    db_session.commit()

    pos = SpatialPosition(id=uuid.uuid4(), survey_id=survey_loc.id, label="P1")
    db_session.add(pos)
    db_session.commit()

    loc_fix = LocationFix(
        spatial_position_id=pos.id,
        latitude=12.9716,
        longitude=77.5946,
        accuracy_meters=4.5,
        captured_at=datetime.now(timezone.utc)
    )
    db_session.add(loc_fix)
    db_session.commit()

    saved_fix = db_session.execute(select(LocationFix).where(LocationFix.spatial_position_id == pos.id)).scalar_one()
    assert saved_fix.latitude == 12.9716
    assert saved_fix.accuracy_meters == 4.5


def test_unbound_scan_cycle_and_multiple_same_bssid_observations(db_session):
    admin = User(email=f"admin_{uuid.uuid4().hex[:6]}@netraze.app", password_hash="secret", role="administrator")
    tech = User(email=f"tech_{uuid.uuid4().hex[:6]}@netraze.app", password_hash="secret", role="user")
    db_session.add_all([admin, tech])
    db_session.commit()

    project = Project(owner_id=admin.id, name="Scan Test Project")
    db_session.add(project)
    db_session.commit()

    building = Building(project_id=project.id, name="B1")
    db_session.add(building)
    db_session.commit()

    floor = Floor(building_id=building.id, name="F1")
    db_session.add(floor)
    db_session.commit()

    area = SurveyArea(floor_id=floor.id, name="A1")
    db_session.add(area)
    db_session.commit()

    survey = Survey(
        id=uuid.uuid4(),
        survey_area_id=area.id,
        title="Unbound Scan Test",
        mode="location_survey",
        status="in_progress",
        created_by=tech.id,
        started_at=datetime.now(timezone.utc)
    )
    db_session.add(survey)
    db_session.commit()

    # Unbound ScanCycle (spatial_position_id IS NULL)
    cycle = ScanCycle(
        id=uuid.uuid4(),
        survey_id=survey.id,
        spatial_position_id=None,
        captured_at_wallclock=datetime.now(timezone.utc),
        fresh_results=True
    )
    db_session.add(cycle)
    db_session.commit()

    # Multiple observations with same BSSID in same cycle
    bssid_target = "AA:BB:CC:DD:EE:FF"
    obs1 = WifiObservation(
        id=uuid.uuid4(),
        scan_cycle_id=cycle.id,
        ssid="Office_Guest",
        bssid=bssid_target,
        rssi_dbm=-65,
        frequency_mhz=2412,
        channel=1,
        channel_source="derived"
    )
    obs2 = WifiObservation(
        id=uuid.uuid4(),
        scan_cycle_id=cycle.id,
        ssid="Office_Guest",
        bssid=bssid_target,
        rssi_dbm=-63,
        frequency_mhz=2412,
        channel=1,
        channel_source="derived"
    )
    db_session.add_all([obs1, obs2])
    db_session.commit()

    saved_obs = db_session.execute(select(WifiObservation).where(WifiObservation.scan_cycle_id == cycle.id)).scalars().all()
    assert len(saved_obs) == 2
    assert saved_obs[0].bssid == bssid_target
    assert saved_obs[1].bssid == bssid_target
