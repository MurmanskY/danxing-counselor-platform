#!/usr/bin/env python3
"""Generate local credentials without overwriting any existing configuration."""
from pathlib import Path
import os
import secrets

root = Path(__file__).resolve().parent.parent
target = root / '.env'
if target.exists():
    raise SystemExit('.env already exists; kept unchanged.')
values = {
    'ADMIN_USERNAME': 'admin', 'ADMIN_PASSWORD': secrets.token_hex(16),
    'COUNSELOR_P001_USERNAME': 'counselor-p001',
    'COUNSELOR_P001_PASSWORD': secrets.token_hex(16),
    'COUNSELOR_P002_USERNAME': 'counselor-p002',
    'COUNSELOR_P002_PASSWORD': secrets.token_hex(16),
    'COUNSELOR_P003_USERNAME': 'counselor-p003',
    'COUNSELOR_P003_PASSWORD': secrets.token_hex(16),
    'COUNSELOR_P004_USERNAME': 'counselor-p004',
    'COUNSELOR_P004_PASSWORD': secrets.token_hex(16),
    'COUNSELOR_P005_USERNAME': 'counselor-p005',
    'COUNSELOR_P005_PASSWORD': secrets.token_hex(16),
    'COUNSELOR_P006_USERNAME': 'counselor-p006',
    'COUNSELOR_P006_PASSWORD': secrets.token_hex(16),
    'POSTGRES_PASSWORD': secrets.token_hex(24), 'REDIS_PASSWORD': secrets.token_hex(24),
    'MINIO_ACCESS_KEY': 'danxing-storage', 'MINIO_SECRET_KEY': secrets.token_hex(24),
    'WEB_PORT': '8080', 'COOKIE_SECURE': 'false'
}
fd = os.open(target, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
with os.fdopen(fd, 'w', encoding='utf-8') as f:
    f.write('# Local credentials. Do not commit or share this file.\n')
    f.writelines(f'{key}={value}\n' for key, value in values.items())
print('Created .env. Initial logins:')
print('  Admin counselor: admin; password: ADMIN_PASSWORD in .env.')
print('  Ordinary counselor Tang Chao (P001): counselor-p001; password: COUNSELOR_P001_PASSWORD in .env.')
print('  Counselor (P002): counselor-p002; password: COUNSELOR_P002_PASSWORD in .env.')
print('  Counselor Wang Yu (P003): counselor-p003; password: COUNSELOR_P003_PASSWORD in .env.')
print('  Counselor Chen Jiaxu (P004): counselor-p004; password: COUNSELOR_P004_PASSWORD in .env.')
print('Next: docker compose up -d --build')
