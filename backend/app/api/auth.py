import uuid
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import verify_password, create_access_token, get_password_hash
from app.models.domain import User
from app.schemas.auth import LoginRequest, TokenResponse, UserAuthProfile, PublicRegistrationRequest
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/auth", tags=["Authentication"])


@router.post("/register", response_model=UserAuthProfile, status_code=status.HTTP_201_CREATED)
def register_user(request: PublicRegistrationRequest, db: Session = Depends(get_db)):
    clean_email = request.email.lower().strip()

    # 1. Check for duplicate email
    existing_user = db.execute(select(User).where(User.email == clean_email)).scalar_one_or_none()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="An account with this email address already exists"
        )

    # 2. Create user with 'user' role
    new_user = User(
        id=uuid.uuid4(),
        email=clean_email,
        password_hash=get_password_hash(request.password),
        role="user"
    )

    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return UserAuthProfile.model_validate(new_user)


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
