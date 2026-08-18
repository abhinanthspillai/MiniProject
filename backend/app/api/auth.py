from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import verify_password, create_access_token
from app.models.domain import User
from app.schemas.auth import LoginRequest, TokenResponse, UserAuthProfile
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/auth", tags=["Authentication"])


@router.post("/login", response_model=TokenResponse)
def login(request: LoginRequest, db: Session = Depends(get_db)):
    # 1. Search user by email only
    user = db.execute(select(User).where(User.email == request.email.lower().strip())).scalar_one_or_none()

    # 2. Generic authentication failure if missing or password mismatch
    if not user or not verify_password(request.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # 3. Issue expiring signed JWT bearer token with minimal claims (sub, exp, iat)
    access_token = create_access_token(subject=user.id)

    return TokenResponse(
        access_token=access_token,
        token_type="bearer",
        user=UserAuthProfile.model_validate(user)
    )


@router.get("/me", response_model=UserAuthProfile)
def get_authenticated_user_profile(current_user: User = Depends(get_current_user)):
    return UserAuthProfile.model_validate(current_user)
