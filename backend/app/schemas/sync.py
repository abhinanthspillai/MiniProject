import uuid
from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict
from app.schemas.survey import SurveyCreate


class SurveySyncRootItem(SurveyCreate):
    survey_area_id: uuid.UUID


class SpatialPositionSyncItem(BaseModel):
    id: uuid.UUID
    label: Optional[str] = None
    floor_plan_x: Optional[float] = None
    floor_plan_y: Optional[float] = None
    simple_map_x: Optional[float] = None
    simple_map_y: Optional[float] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    accuracy_meters: Optional[float] = None
    captured_at: Optional[datetime] = None
    created_at: Optional[datetime] = None


class WifiObservationSyncItem(BaseModel):
    id: uuid.UUID
    scan_cycle_id: uuid.UUID
    ssid: Optional[str] = None
    bssid: str
    rssi_dbm: int
    frequency_mhz: int
    channel: Optional[int] = None
    channel_source: str = "frequency_conversion"
    capabilities: Optional[str] = None


class ScanCycleSyncItem(BaseModel):
    id: uuid.UUID
    spatial_position_id: Optional[uuid.UUID] = None
    captured_at_wallclock: datetime
    android_scan_timestamp_raw: Optional[int] = None
    fresh_results: bool = True
    created_at: Optional[datetime] = None
    observations: List[WifiObservationSyncItem] = []


class SurveySyncPayload(BaseModel):
    model_config = ConfigDict(extra="forbid")

    survey: Optional[SurveySyncRootItem] = None
    spatial_positions: List[SpatialPositionSyncItem] = []
    scan_cycles: List[ScanCycleSyncItem] = []


class SurveySyncResult(BaseModel):
    survey_id: uuid.UUID
    ingested_spatial_positions: int
    ingested_scan_cycles: int
    ingested_wifi_observations: int
