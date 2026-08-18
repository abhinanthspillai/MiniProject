from fastapi import APIRouter
from app.core.config import settings
from app.core.database import check_database_connection

router = APIRouter()


@router.get("/health", summary="Health Check & Database Reachability")
def health_check():
    db_health = check_database_connection()
    return {
        "project": settings.PROJECT_NAME,
        "status": "ok",
        "database": db_health
    }
