import uuid
from pydantic import BaseModel, EmailStr, ConfigDict


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class UserAuthProfile(BaseModel):
    id: uuid.UUID
    email: str
    role: str

    model_config = ConfigDict(from_attributes=True)


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserAuthProfile
