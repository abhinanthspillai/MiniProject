import uuid
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import get_password_hash
from app.dependencies.auth import get_current_user
from app.models.domain import User
from app.schemas.auth import UserCreateRequest, UserAuthProfile, ResetPasswordRequest, ResetPasswordResponse

router = APIRouter(prefix="/users", tags=["Users"])


@router.post("", response_model=UserAuthProfile, status_code=status.HTTP_201_CREATED)
def create_user(
    request: UserCreateRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # 1. Require verified Administrator authorization
    if current_user.role != "administrator":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Administrator authorization required to create new user accounts"
        )

    clean_email = request.email.lower().strip()

    # 2. Check for duplicate email
    existing_user = db.execute(select(User).where(User.email == clean_email)).scalar_one_or_none()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="An account with this email address already exists"
        )

    # 3. Create user with Argon2 password hash
    new_user = User(
        id=uuid.uuid4(),
        email=clean_email,
        password_hash=get_password_hash(request.password),
        role=request.role
    )

    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return UserAuthProfile.model_validate(new_user)


@router.post("/reset-password", response_model=ResetPasswordResponse, status_code=status.HTTP_200_OK)
def reset_user_password(
    request: ResetPasswordRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # 1. Require verified Administrator authorization
    if current_user.role != "administrator":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Administrator authorization required to reset user passwords"
        )

    clean_email = request.target_email.lower().strip()

    # 2. Find target user
    target_user = db.execute(select(User).where(User.email == clean_email)).scalar_one_or_none()
    if not target_user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User with specified email address does not exist"
        )

    # 3. Update password with Argon2 hash
    target_user.password_hash = get_password_hash(request.new_password)
    db.commit()

    return ResetPasswordResponse(
        message="Password reset successfully.",
        target_email=clean_email
    )
