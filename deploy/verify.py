#!/usr/bin/env python3
"""Read-only deployment checks: page, login, catalog and dashboard."""
import argparse, http.cookiejar, json, urllib.parse, urllib.request
from pathlib import Path
p = argparse.ArgumentParser()
p.add_argument('--env-file', required=True)
p.add_argument('--base-url')
a = p.parse_args()
env = dict(line.split('=', 1) for line in Path(a.env_file).read_text().splitlines() if line and not line.startswith('#'))
base = a.base_url or 'http://127.0.0.1:' + env.get('WEB_PORT', '80')
op = urllib.request.build_opener(urllib.request.ProxyHandler({}), urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
def request(path, data=None, headers=None):
    with op.open(urllib.request.Request(base + path, data=data, headers=headers or {}), timeout=30) as r:
        raw = r.read()
        return json.loads(raw) if 'application/json' in r.headers.get('Content-Type', '') else raw
assert b'<html' in request('/').lower(), 'Homepage not served'
csrf = request('/api/auth/csrf')
request('/api/auth/login', urllib.parse.urlencode({'username':env['ADMIN_USERNAME'],'password':env['ADMIN_PASSWORD']}).encode(), {'Content-Type':'application/x-www-form-urlencoded', csrf['headerName']:csrf['token']})
assert request('/api/auth/me')['role'] == 'COUNSELOR_ADMIN'
catalog = request('/api/catalog')
assert len(catalog['records']) > 0, 'Catalog is empty'
request('/api/home/dashboard')
csrf = request('/api/auth/csrf')
request('/api/auth/logout', b'', {csrf['headerName']:csrf['token']})
print('PASS homepage, login, dashboard, catalog (%s records), logout' % len(catalog['records']))
