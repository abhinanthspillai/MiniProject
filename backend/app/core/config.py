from typing import List
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    PROJECT_NAME: str = "Netraze Backend"
    API_V1_STR: str = "/api/v1"
    DEBUG: bool = True

    # Database URL using modern Psycopg 3 driver
    DATABASE_URL: str = "postgresql+psycopg://netraze_user:netraze_password@localhost:5432/netraze_db"

    # Allowed CORS Origins
    BACKEND_CORS_ORIGINS: List[str] = [
        "http://localhost:8000",
        "http://127.0.0.1:8000",
    ]

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore"
    )


settings = Settings()
