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
