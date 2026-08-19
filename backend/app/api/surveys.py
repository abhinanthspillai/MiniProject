import uuid
from datetime import datetime, timezone
from typing import List, Tuple
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.api.auth import get_current_user
from app.core.database import get_db
from app.models.domain import Building, Floor, Project, ProjectMember, Survey, SurveyArea, User
from app.schemas.survey import SurveyCreate, SurveyOut, SurveyUpdate

router = APIRouter(tags=["surveys"])


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


def _get_survey_area_authorization(
    db: Session, survey_area_id: uuid.UUID, current_user: User
) -> Tuple[SurveyArea, Project, bool, bool]:
    area = db.execute(select(SurveyArea).where(SurveyArea.id == survey_area_id)).scalar_one_or_none()
    if not area:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Survey area not found")

    floor = db.execute(select(Floor).where(Floor.id == area.floor_id)).scalar_one_or_none()
    if not floor:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Floor not found")

    building = db.execute(select(Building).where(Building.id == floor.building_id)).scalar_one_or_none()
    if not building:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Building not found")

    project = db.execute(select(Project).where(Project.id == building.project_id)).scalar_one_or_none()
    if not project:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Project not found")

    is_owner = (project.owner_id == current_user.id) and (current_user.role == "administrator")
    member_record = db.execute(
        select(ProjectMember).where(
            ProjectMember.project_id == project.id, ProjectMember.user_id == current_user.id
        )
    ).scalar_one_or_none()
    is_member = (member_record is not None) or (project.owner_id == current_user.id)

    if not is_member and not is_owner:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Survey area not found")

    return area, project, is_owner, is_member


def _get_survey_authorization(
    db: Session, survey_id: uuid.UUID, current_user: User
) -> Tuple[Survey, Project, bool, bool]:
    survey = db.execute(select(Survey).where(Survey.id == survey_id)).scalar_one_or_none()
    if not survey:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Survey not found")

    _, project, is_owner, is_member = _get_survey_area_authorization(db, survey.survey_area_id, current_user)
    return survey, project, is_owner, is_member


@router.post("/survey-areas/{survey_area_id}/surveys", response_model=SurveyOut, status_code=status.HTTP_201_CREATED)
def create_survey(
    survey_area_id: uuid.UUID,
    payload: SurveyCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    _, project, _, is_member = _get_survey_area_authorization(db, survey_area_id, current_user)

    if not is_member:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not authorized to create survey in this project")

    survey_id = payload.id or uuid.uuid4()

    existing_survey = db.execute(select(Survey).where(Survey.id == survey_id)).scalar_one_or_none()
    if existing_survey:
        return existing_survey

    started_at = payload.started_at or utc_now()

    survey = Survey(
        id=survey_id,
        survey_area_id=survey_area_id,
        title=payload.title,
        mode=payload.mode,
        status="in_progress",
        floor_plan_id=payload.floor_plan_id,
        simple_map_id=payload.simple_map_id,
        created_by=current_user.id,
        started_at=started_at,
        created_at=utc_now(),
        updated_at=utc_now(),
    )
    db.add(survey)
    db.commit()
    db.refresh(survey)
    return survey


@router.get("/survey-areas/{survey_area_id}/surveys", response_model=List[SurveyOut])
def get_surveys_for_area(
    survey_area_id: uuid.UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    _get_survey_area_authorization(db, survey_area_id, current_user)
    surveys = db.execute(
        select(Survey).where(Survey.survey_area_id == survey_area_id).order_by(Survey.created_at.desc())
    ).scalars().all()
    return list(surveys)


@router.get("/surveys/{survey_id}", response_model=SurveyOut)
def get_survey(
    survey_id: uuid.UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    survey, _, _, _ = _get_survey_authorization(db, survey_id, current_user)
    return survey


@router.patch("/surveys/{survey_id}", response_model=SurveyOut)
def update_survey(
    survey_id: uuid.UUID,
    payload: SurveyUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    survey, _, is_owner, is_member = _get_survey_authorization(db, survey_id, current_user)

    if not is_member and not is_owner:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not authorized to modify this survey")

    if payload.title is not None:
        survey.title = payload.title
        survey.updated_at = utc_now()
        db.commit()
        db.refresh(survey)

    return survey
