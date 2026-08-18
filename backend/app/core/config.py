from typing import List, Union
from pydantic import AnyHttpUrl, validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    PROJECT_NAME: str = "Netraze Backend"
    API_V1_STR: str = "/api/v1"
    DEBUG: bool = True

    # Database Connection
    DATABASE_URL: str = "postgresql+psycopg://netraze_app:1234@127.0.0.1:5432/netraze"

    # JWT Authentication Security Settings (D090)
    SECRET_KEY: str = "netraze_development_secret_key_change_in_production_987654321"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 480

    # CORS
    BACKEND_CORS_ORIGINS: List[str] = [
        "http://localhost:8000",
        "http://127.0.0.1:8000"
    ]

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )


settings = Settings()
