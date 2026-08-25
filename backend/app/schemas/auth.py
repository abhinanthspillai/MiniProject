import uuid
from pydantic import BaseModel, EmailStr, ConfigDict, field_validator


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


class UserCreateRequest(BaseModel):
    email: EmailStr
    password: str
    role: str

    @field_validator("role")
    @classmethod
    def validate_role(cls, v: str) -> str:
        valid_roles = {"administrator", "user"}
        role_clean = v.lower().strip()
        if role_clean not in valid_roles:
            raise ValueError(f"Invalid role: '{v}'. Must be one of {valid_roles}")
        return role_clean


class PublicRegistrationRequest(BaseModel):
    email: EmailStr
    password: str
    confirm_password: str

    @field_validator("confirm_password")
    @classmethod
    def passwords_match(cls, v, info):
        if "password" in info.data and v != info.data["password"]:
            raise ValueError("Passwords do not match")
        return v


class ResetPasswordRequest(BaseModel):
    target_email: EmailStr
    new_password: str


class ResetPasswordResponse(BaseModel):
    message: str
    target_email: str
