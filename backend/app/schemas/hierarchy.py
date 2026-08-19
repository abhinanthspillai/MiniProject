import uuid
from datetime import datetime
from pydantic import BaseModel, ConfigDict, Field


# --- PROJECT ---
class ProjectCreate(BaseModel):
    name: str = Field(..., min_length=1)

class ProjectUpdate(BaseModel):
    name: str = Field(..., min_length=1)

class ProjectOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    owner_id: uuid.UUID
    name: str
    is_active: bool
    created_at: datetime
    updated_at: datetime


# --- BUILDING ---
class BuildingCreate(BaseModel):
    name: str = Field(..., min_length=1)

class BuildingUpdate(BaseModel):
    name: str = Field(..., min_length=1)

class BuildingOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    project_id: uuid.UUID
    name: str
    created_at: datetime
    updated_at: datetime


# --- FLOOR ---
class FloorCreate(BaseModel):
    name: str = Field(..., min_length=1)

class FloorUpdate(BaseModel):
    name: str = Field(..., min_length=1)

class FloorOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    building_id: uuid.UUID
    name: str
    created_at: datetime
    updated_at: datetime


# --- SURVEY AREA ---
class SurveyAreaCreate(BaseModel):
    name: str = Field(..., min_length=1)

class SurveyAreaUpdate(BaseModel):
    name: str = Field(..., min_length=1)

class SurveyAreaOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    floor_id: uuid.UUID
    name: str
    created_at: datetime
    updated_at: datetime
