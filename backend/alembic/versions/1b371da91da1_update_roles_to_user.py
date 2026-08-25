"""update_roles_to_user

Revision ID: 1b371da91da1
Revises: ea4f99e285b3
Create Date: 2026-08-19 14:47:37.666669

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '1b371da91da1'
down_revision: Union[str, Sequence[str], None] = 'ea4f99e285b3'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    # 1. Drop the old constraint
    op.drop_constraint('ck_users_role', 'users', type_='check')
    
    # 2. Update existing data
    op.execute("UPDATE users SET role = 'user' WHERE role = 'survey_technician'")
    
    # 3. Create the new constraint
    op.create_check_constraint(
        'ck_users_role',
        'users',
        "role IN ('administrator', 'user')"
    )


def downgrade() -> None:
    """Downgrade schema."""
    # 1. Drop the new constraint
    op.drop_constraint('ck_users_role', 'users', type_='check')
    
    # 2. Revert existing data (Note: this makes all 'user' roles 'survey_technician')
    op.execute("UPDATE users SET role = 'survey_technician' WHERE role = 'user'")
    
    # 3. Create the old constraint
    op.create_check_constraint(
        'ck_users_role',
        'users',
        "role IN ('administrator', 'survey_technician')"
    )
