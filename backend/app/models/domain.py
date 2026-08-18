import uuid
from datetime import datetime, timezone
from typing import Optional, List
from sqlalchemy import (
    String,
    Text,
    Boolean,
    Integer,
    Double,
    Numeric,
    DateTime,
    ForeignKey,
    CheckConstraint,
    Index,
    PrimaryKeyConstraint,
    func
)
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy.dialects.postgresql import UUID
from app.core.database import Base


def utc_now():
    return datetime.now(timezone.utc)


# 1. users
class User(Base):
    __tablename__ = "users"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    email: Mapped[str] = mapped_column(Text, unique=True, nullable=False)
    password_hash: Mapped[str] = mapped_column(Text, nullable=False)
    role: Mapped[str] = mapped_column(Text, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now, onupdate=utc_now)

    __table_args__ = (
        CheckConstraint("role IN ('administrator', 'survey_technician')", name="ck_users_role"),
    )


# 2. project_members
class ProjectMember(Base):
    __tablename__ = "project_members"

    project_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("projects.id", ondelete="CASCADE"), primary_key=True)
    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)

    __table_args__ = (
        Index("idx_project_members_project_id", "project_id"),
        Index("idx_project_members_user_id", "user_id"),
    )


# 3. projects
class Project(Base):
    __tablename__ = "projects"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    owner_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    name: Mapped[str] = mapped_column(Text, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now, onupdate=utc_now)


# 4. buildings
class Building(Base):
    __tablename__ = "buildings"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    project_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("projects.id", ondelete="RESTRICT"), nullable=False)
    name: Mapped[str] = mapped_column(Text, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now, onupdate=utc_now)

    __table_args__ = (
        Index("idx_buildings_project_id", "project_id"),
    )


# 5. floors
class Floor(Base):
    __tablename__ = "floors"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    building_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("buildings.id", ondelete="RESTRICT"), nullable=False)
    name: Mapped[str] = mapped_column(Text, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now, onupdate=utc_now)

    __table_args__ = (
        Index("idx_floors_building_id", "building_id"),
    )


# 6. survey_areas
class SurveyArea(Base):
    __tablename__ = "survey_areas"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    floor_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("floors.id", ondelete="RESTRICT"), nullable=False)
    name: Mapped[str] = mapped_column(Text, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now, onupdate=utc_now)

    __table_args__ = (
        Index("idx_survey_areas_floor_id", "floor_id"),
    )


# 7. floor_plans
class FloorPlan(Base):
    __tablename__ = "floor_plans"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    survey_area_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("survey_areas.id", ondelete="RESTRICT"), nullable=False)
    storage_path: Mapped[str] = mapped_column(Text, nullable=False)
    original_filename: Mapped[str] = mapped_column(Text, nullable=False)
    width_px: Mapped[int] = mapped_column(Integer, nullable=False)
    height_px: Mapped[int] = mapped_column(Integer, nullable=False)
    uploaded_by: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)

    __table_args__ = (
        CheckConstraint("width_px > 0", name="ck_floor_plans_width_px"),
        CheckConstraint("height_px > 0", name="ck_floor_plans_height_px"),
        Index("idx_floor_plans_survey_area_id", "survey_area_id"),
    )


# 8. simple_maps
class SimpleMap(Base):
    __tablename__ = "simple_maps"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    survey_area_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("survey_areas.id", ondelete="RESTRICT"), nullable=False)
    artifact_reference: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    created_by: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)

    __table_args__ = (
        Index("idx_simple_maps_survey_area_id", "survey_area_id"),
    )


# 9. surveys
class Survey(Base):
    __tablename__ = "surveys"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    survey_area_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("survey_areas.id", ondelete="RESTRICT"), nullable=False)
    title: Mapped[str] = mapped_column(Text, nullable=False)
    mode: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[str] = mapped_column(Text, nullable=False, default="in_progress")
    floor_plan_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), ForeignKey("floor_plans.id", ondelete="RESTRICT"), nullable=True)
    simple_map_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), ForeignKey("simple_maps.id", ondelete="RESTRICT"), nullable=True)
    created_by: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    started_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    completed_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now, onupdate=utc_now)

    __table_args__ = (
        CheckConstraint("mode IN ('floor_plan', 'simple_map', 'location_survey')", name="ck_surveys_mode"),
        CheckConstraint("status IN ('in_progress', 'completed')", name="ck_surveys_status"),
        CheckConstraint(
            "(mode = 'floor_plan' AND floor_plan_id IS NOT NULL AND simple_map_id IS NULL) OR "
            "(mode = 'simple_map' AND simple_map_id IS NOT NULL AND floor_plan_id IS NULL) OR "
            "(mode = 'location_survey' AND floor_plan_id IS NULL AND simple_map_id IS NULL)",
            name="ck_surveys_mode_reference_consistency"
        ),
        CheckConstraint(
            "(status = 'completed' AND completed_at IS NOT NULL) OR "
            "(status = 'in_progress' AND completed_at IS NULL)",
            name="ck_surveys_completion_consistency"
        ),
        Index("idx_surveys_survey_area_id", "survey_area_id"),
        Index("idx_surveys_status", "status"),
    )


# 10. spatial_positions
class SpatialPosition(Base):
    __tablename__ = "spatial_positions"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    survey_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("surveys.id", ondelete="RESTRICT"), nullable=False)
    label: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)

    __table_args__ = (
        Index("idx_spatial_positions_survey_id", "survey_id"),
    )


# 11. floor_plan_positions
class FloorPlanPosition(Base):
    __tablename__ = "floor_plan_positions"

    spatial_position_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("spatial_positions.id", ondelete="CASCADE"), primary_key=True)
    x: Mapped[float] = mapped_column(Double, nullable=False)
    y: Mapped[float] = mapped_column(Double, nullable=False)

    __table_args__ = (
        CheckConstraint("x >= 0 AND x <= 1", name="ck_floor_plan_positions_x"),
        CheckConstraint("y >= 0 AND y <= 1", name="ck_floor_plan_positions_y"),
    )


# 12. simple_map_positions
class SimpleMapPosition(Base):
    __tablename__ = "simple_map_positions"

    spatial_position_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("spatial_positions.id", ondelete="CASCADE"), primary_key=True)
    x: Mapped[float] = mapped_column(Double, nullable=False)
    y: Mapped[float] = mapped_column(Double, nullable=False)

    __table_args__ = (
        CheckConstraint("x >= 0 AND x <= 1", name="ck_simple_map_positions_x"),
        CheckConstraint("y >= 0 AND y <= 1", name="ck_simple_map_positions_y"),
    )


# 13. location_fixes
class LocationFix(Base):
    __tablename__ = "location_fixes"

    spatial_position_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("spatial_positions.id", ondelete="CASCADE"), primary_key=True)
    latitude: Mapped[float] = mapped_column(Double, nullable=False)
    longitude: Mapped[float] = mapped_column(Double, nullable=False)
    accuracy_meters: Mapped[float] = mapped_column(Double, nullable=False)
    captured_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)

    __table_args__ = (
        CheckConstraint("latitude >= -90 AND latitude <= 90", name="ck_location_fixes_latitude"),
        CheckConstraint("longitude >= -180 AND longitude <= 180", name="ck_location_fixes_longitude"),
        CheckConstraint("accuracy_meters > 0", name="ck_location_fixes_accuracy_meters"),
    )


# 14. scan_cycles
class ScanCycle(Base):
    __tablename__ = "scan_cycles"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    survey_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("surveys.id", ondelete="RESTRICT"), nullable=False)
    spatial_position_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), ForeignKey("spatial_positions.id", ondelete="RESTRICT"), nullable=True)
    captured_at_wallclock: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    android_scan_timestamp_raw: Mapped[Optional[float]] = mapped_column(Numeric, nullable=True)
    fresh_results: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utc_now)

    __table_args__ = (
        Index("idx_scan_cycles_survey_id", "survey_id"),
        Index("idx_scan_cycles_spatial_position_id", "spatial_position_id"),
        Index("idx_scan_cycles_captured_at_wallclock", "captured_at_wallclock"),
    )


# 15. wifi_observations
class WifiObservation(Base):
    __tablename__ = "wifi_observations"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    scan_cycle_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("scan_cycles.id", ondelete="RESTRICT"), nullable=False)
    ssid: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    bssid: Mapped[str] = mapped_column(Text, nullable=False)
    rssi_dbm: Mapped[int] = mapped_column(Integer, nullable=False)
    frequency_mhz: Mapped[int] = mapped_column(Integer, nullable=False)
    channel: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    channel_source: Mapped[str] = mapped_column(Text, nullable=False)
    capabilities: Mapped[Optional[str]] = mapped_column(Text, nullable=True)

    __table_args__ = (
        CheckConstraint("frequency_mhz > 0", name="ck_wifi_observations_frequency_mhz"),
        Index("idx_wifi_observations_scan_cycle_id", "scan_cycle_id"),
        Index("idx_wifi_observations_bssid", "bssid"),
    )
