import uuid
from typing import Annotated
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.dependencies.auth import get_current_user
from app.models.domain import (
    FloorPlanPosition,
    LocationFix,
    ScanCycle,
    SimpleMapPosition,
    SpatialPosition,
    Survey,
    SurveyArea,
    User,
    WifiObservation,
    utc_now,
)
from app.schemas.sync import SurveySyncPayload, SurveySyncResult

router = APIRouter(prefix="/surveys", tags=["Sync Ingestion"])


def _verify_survey_access(survey_id: uuid.UUID, current_user: User, db: Session) -> Survey:
    survey = db.query(Survey).filter(Survey.id == survey_id).first()
    if not survey:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Survey not found")
    return survey


@router.post("/{survey_id}/sync", response_model=SurveySyncResult, status_code=status.HTTP_200_OK)
def sync_survey_root(
    survey_id: uuid.UUID,
    payload: SurveySyncPayload,
    current_user: Annotated[User, Depends(get_current_user)],
    db: Annotated[Session, Depends(get_db)]
):
    survey = db.query(Survey).filter(Survey.id == survey_id).first()

    # 1. Ingest Survey Root if not centrally present and provided in payload
    if not survey:
        if not payload.survey:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Survey root does not exist centrally and survey creation payload was not supplied"
            )
        # Verify survey_area exists
        area = db.query(SurveyArea).filter(SurveyArea.id == payload.survey.survey_area_id).first()
        if not area:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Survey area not found")

        started_at = payload.survey.started_at or utc_now()
        survey = Survey(
            id=survey_id,
            survey_area_id=payload.survey.survey_area_id,
            title=payload.survey.title,
            mode=payload.survey.mode,
            status="in_progress",
            floor_plan_id=payload.survey.floor_plan_id,
            simple_map_id=payload.survey.simple_map_id,
            created_by=current_user.id,
            started_at=started_at,
            created_at=utc_now(),
            updated_at=utc_now()
        )
        db.add(survey)
        db.flush()

    ingested_positions = 0
    ingested_cycles = 0
    ingested_obs = 0

    # 2. Ingest Spatial Positions (Idempotent + D077 Atomic Child Tables)
    for pos_item in payload.spatial_positions:
        existing_pos = db.query(SpatialPosition).filter(SpatialPosition.id == pos_item.id).first()
        if not existing_pos:
            created_at = pos_item.created_at or utc_now()
            new_pos = SpatialPosition(
                id=pos_item.id,
                survey_id=survey_id,
                label=pos_item.label,
                created_at=created_at
            )
            db.add(new_pos)
            db.flush()

            # D077 Group Atomicity: Floor Plan mode child
            if pos_item.floor_plan_x is not None and pos_item.floor_plan_y is not None:
                fp_pos = FloorPlanPosition(
                    spatial_position_id=pos_item.id,
                    x=pos_item.floor_plan_x,
                    y=pos_item.floor_plan_y
                )
                db.add(fp_pos)

            # D077 Group Atomicity: Simple Map mode child
            if pos_item.simple_map_x is not None and pos_item.simple_map_y is not None:
                sm_pos = SimpleMapPosition(
                    spatial_position_id=pos_item.id,
                    x=pos_item.simple_map_x,
                    y=pos_item.simple_map_y
                )
                db.add(sm_pos)

            # D077 Group Atomicity: Location Survey mode child (LocationFix if present)
            if (
                pos_item.latitude is not None
                and pos_item.longitude is not None
                and pos_item.accuracy_meters is not None
                and pos_item.captured_at is not None
            ):
                loc_fix = LocationFix(
                    spatial_position_id=pos_item.id,
                    latitude=pos_item.latitude,
                    longitude=pos_item.longitude,
                    accuracy_meters=pos_item.accuracy_meters,
                    captured_at=pos_item.captured_at
                )
                db.add(loc_fix)

            ingested_positions += 1

    db.flush()

    # 3. Ingest Scan Cycles & Wifi Observations (Idempotent)
    for cycle_item in payload.scan_cycles:
        existing_cycle = db.query(ScanCycle).filter(ScanCycle.id == cycle_item.id).first()
        if not existing_cycle:
            created_at = cycle_item.created_at or utc_now()
            new_cycle = ScanCycle(
                id=cycle_item.id,
                survey_id=survey_id,
                spatial_position_id=cycle_item.spatial_position_id,
                captured_at_wallclock=cycle_item.captured_at_wallclock,
                android_scan_timestamp_raw=cycle_item.android_scan_timestamp_raw,
                fresh_results=cycle_item.fresh_results,
                created_at=created_at
            )
            db.add(new_cycle)
            ingested_cycles += 1
            db.flush()

        for obs_item in cycle_item.observations:
            existing_obs = db.query(WifiObservation).filter(WifiObservation.id == obs_item.id).first()
            if not existing_obs:
                bssid_clean = obs_item.bssid.strip().upper()
                new_obs = WifiObservation(
                    id=obs_item.id,
                    scan_cycle_id=cycle_item.id,
                    ssid=obs_item.ssid,
                    bssid=bssid_clean,
                    rssi_dbm=obs_item.rssi_dbm,
                    frequency_mhz=obs_item.frequency_mhz,
                    channel=obs_item.channel,
                    channel_source=obs_item.channel_source,
                    capabilities=obs_item.capabilities
                )
                db.add(new_obs)
                ingested_obs += 1

    db.commit()

    return SurveySyncResult(
        survey_id=survey_id,
        ingested_spatial_positions=ingested_positions,
        ingested_scan_cycles=ingested_cycles,
        ingested_wifi_observations=ingested_obs
    )
