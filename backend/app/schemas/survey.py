import uuid
from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field, field_validator


class SurveyCreate(BaseModel):
    id: Optional[uuid.UUID] = Field(None, description="Canonical Android-generated Survey UUID")
    title: Optional[str] = None
    mode: str
    floor_plan_id: Optional[uuid.UUID] = None
    simple_map_id: Optional[uuid.UUID] = None
    started_at: Optional[datetime] = None

    @field_validator("mode")
    @classmethod
    def validate_mode(cls, v: str) -> str:
        valid_modes = {"floor_plan", "simple_map", "location_survey"}
        if v not in valid_modes:
            raise ValueError(f"Invalid survey mode: '{v}'. Must be one of {valid_modes}")
        return v


class SurveyUpdate(BaseModel):
    title: Optional[str] = None

    model_config = ConfigDict(extra="forbid")


class SurveyOut(BaseModel):
    id: uuid.UUID
    survey_area_id: uuid.UUID
    title: Optional[str] = None
    mode: str
    status: str
    floor_plan_id: Optional[uuid.UUID] = None
    simple_map_id: Optional[uuid.UUID] = None
    created_by: uuid.UUID
    started_at: datetime
    completed_at: Optional[datetime] = None
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)


class AccessPointOut(BaseModel):
    bssid: str
    ssid: Optional[str] = None
    observation_count: int
    max_rssi: int
    avg_rssi: float
    latest_frequency: int


class ChannelOut(BaseModel):
    channel: Optional[int] = None
    frequency_mhz: int
    observation_count: int
    ap_count: int


class ObservationOut(BaseModel):
    id: uuid.UUID
    scan_cycle_id: uuid.UUID
    bssid: str
    ssid: Optional[str] = None
    rssi_dbm: int
    frequency_mhz: int
    channel: Optional[int] = None
    channel_source: str
    capabilities: Optional[str] = None
    captured_at_wallclock: datetime
