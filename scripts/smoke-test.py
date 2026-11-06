#!/usr/bin/env python3
"""Exercise a running deployment. Creates two isolated test users and audit records.
Usage: SMOKE_ADMIN_PASSWORD=... python3 scripts/smoke-test.py --base-url http://localhost:8080
Only use a demo/test database. No third-party Python packages required.
"""
import argparse
import http.cookiejar
import json
import os
import secrets
import urllib.error
import urllib.parse
import urllib.request

parser = argparse.ArgumentParser()
parser.add_argument('--base-url', default='http://localhost:8080')
parser.add_argument('--skip-files', action='store_true')
args = parser.parse_args()
passed = []

class Client:
    def __init__(self):
        self.opener = urllib.request.build_opener(urllib.request.ProxyHandler({}), urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
        self.csrf = None

    def request(self, method, path, data=None, expected=200, content_type='application/json', csrf=True):
        headers = {}
        if self.csrf and method != 'GET' and csrf:
            headers[self.csrf['headerName']] = self.csrf['token']
        if data is not None:
            headers['Content-Type'] = content_type
            if not isinstance(data, bytes):
                data = json.dumps(data, ensure_ascii=False).encode()
        req = urllib.request.Request(args.base_url.rstrip('/') + '/api' + path, data=data, headers=headers, method=method)
        try:
            with self.opener.open(req, timeout=65) as r:
                code, raw, ctype = r.status, r.read(), r.headers.get('Content-Type', '')
        except urllib.error.HTTPError as e:
            code, raw, ctype = e.code, e.read(), e.headers.get('Content-Type', '')
        assert code == expected, f'{method} {path}: expected {expected}, got {code}: {raw[:200]!r}'
        return json.loads(raw) if raw and 'application/json' in ctype else raw

    def login(self, username, password):
        self.csrf = self.request('GET', '/auth/csrf')
        self.request('POST', '/auth/login', urllib.parse.urlencode({'username':username,'password':password}).encode(), content_type='application/x-www-form-urlencoded')
        self.csrf = self.request('GET', '/auth/csrf')

def check(name, condition=True):
    assert condition, name
    passed.append(name)
    print('PASS', name, flush=True)

admin_password = os.environ.get('SMOKE_ADMIN_PASSWORD')
if not admin_password:
    raise SystemExit('Set SMOKE_ADMIN_PASSWORD. Test uses existing admin and creates two test accounts.')
admin = Client()
admin.request('GET', '/catalog', expected=401)
check('Unauthenticated requests rejected')
admin.request('POST', '/auth/login', {}, expected=403, csrf=False)
check('Login CSRF enforced')
admin.login(os.environ.get('SMOKE_ADMIN_USERNAME','admin'), admin_password)
admin_user=admin.request('GET','/auth/me')
check('Admin counselor session login', admin_user['role']=='COUNSELOR_ADMIN')
dashboard=admin.request('GET','/home/dashboard')
check('Manager dashboard scope loaded',set(dashboard['managedProfileIds'])=={'P002','P003','P004'})
catalog = admin.request('GET','/catalog')
check('567 source records loaded',len(catalog['records'])==567)
check('4 detail and 148 legacy people',sum(r.get('recordType')=='detail' for r in catalog['records'])==4 and sum(r.get('recordType')=='legacy' for r in catalog['records'])==148)
check('Cached catalog response',len(admin.request('GET','/catalog')['records'])==567)
suffix=secrets.token_hex(4)
profile_options=admin.request('GET','/admin/counselor-profiles')
if len(profile_options)<2:
    raise SystemExit('The test database needs at least two unassigned counselor profiles.')
password=secrets.token_hex(16)
for username,profile in zip(['smoke_a_'+suffix,'smoke_b_'+suffix],profile_options[:2]):
    admin.request('POST','/admin/users',{'username':username,'password':password,'displayName':'自动验收账号','role':'COUNSELOR','profileId':profile['profileId']})
a,b=Client(),Client()
a.login('smoke_a_'+suffix,password)
b.login('smoke_b_'+suffix,password)
a.request('GET','/admin/users',expected=403)
a.request('GET','/admin/audit',expected=403)
check('Counselor cannot access admin accounts or audit')
a.request('POST','/plans',{},expected=403,csrf=False)
a.request('POST','/plans',{'title':'','domain':'test','tasks':[]},expected=400)
check('CSRF and validation enforced on writes')
source=next(r for r in catalog['records'] if r['kind']=='opportunity')
plan=a.request('POST','/plans',{'title':'=验收计划','domain':'专业能力提升','deadline':'2027-03-10','description':'接口联调用','opportunityId':source['id'],'personId':'P001','tasks':['整理资料','完成复盘']})
pid=plan['id']
view=next(v for v in a.request('GET','/plans') if v['plan']['id']==pid)
check('Plan and tasks persisted',len(view['tasks'])==2)
check('Other account cannot list plan',not b.request('GET','/plans'))
b.request('PATCH','/plans/'+pid,{'title':'越权','description':''},expected=404)
b.request('DELETE','/plans/'+pid,expected=404)
b.request('PATCH','/tasks/'+view['tasks'][0]['id'],{'done':True},expected=404)
check('Cross-account edits, deletes and tasks rejected')
for task in view['tasks']:
    a.request('PATCH','/tasks/'+task['id'],{'done':True})
check('All tasks complete plan',a.request('GET','/plans')[0]['plan']['status']=='已完成')
a.request('PATCH','/tasks/'+view['tasks'][0]['id'],{'done':False})
check('Reopened task reopens plan',a.request('GET','/plans')[0]['plan']['status']=='进行中')
a.request('PATCH','/plans/'+pid,{'title':'=验收计划','description':'已编辑','deadline':None})
check('Deadline can be cleared',a.request('GET','/plans')[0]['plan']['deadline'] is None)
check('CSV formula escaped',"'=验收计划" in a.request('GET','/plans/export').decode('utf-8-sig'))
for active in [True,True]:a.request('PUT','/favorites/'+source['id'],{'active':active})
check('Favorite is idempotent',a.request('GET','/favorites').count(source['id'])==1)
check('Favorites isolated',not b.request('GET','/favorites'))
cal={'title':'验收日程','startDate':'2027-02-10','endDate':'2027-02-11','category':'个人准备','note':'','recordId':source['id']}
entry=a.request('POST','/calendar',cal)
b.request('DELETE','/calendar/'+entry['id'],expected=404)
a.request('POST','/calendar',{**cal,'endDate':'2027-02-01'},expected=400)
a.request('PUT','/calendar/'+entry['id'],{**cal,'title':'更新后的日程','recordId':''})
check('Calendar validation, ownership and editing',a.request('GET','/calendar')[0]['title']=='更新后的日程')
review={'keywords':'验收关键词','philosophy':'','reviewStatus':'已核验','note':''}
a.request('PUT','/admin/records/'+source['id']+'/review',review,expected=403)
admin.request('PUT','/admin/records/'+source['id']+'/review',review,expected=400)
check('Review requires admin and evidence')
previous=next((e for e in catalog['extensions'] if e['recordId']==source['id']),{'keywords':'','philosophy':'','reviewStatus':'待核验','note':''})
admin.request('PUT','/admin/records/'+source['id']+'/review',{**review,'note':'接口验收临时记录'})
updated=admin.request('GET','/catalog')
check('Review preserves source facts',next(r for r in updated['records'] if r['id']==source['id'])==source)
admin.request('PUT','/admin/records/'+source['id']+'/review',previous)
if not args.skip_files:
    boundary='Danxing'+secrets.token_hex(12)
    payload='附件上传下载验收\n'.encode()
    body=(f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="smoke.txt"\r\nContent-Type: text/plain\r\n\r\n'.encode()+payload+f'\r\n--{boundary}--\r\n'.encode())
    asset=a.request('POST','/files',body,content_type='multipart/form-data; boundary='+boundary)
    check('MinIO upload and byte-exact download',a.request('GET','/files/'+asset['id']+'/download')==payload)
    b.request('GET','/files/'+asset['id']+'/download',expected=404)
    b.request('DELETE','/files/'+asset['id'],expected=404)
    check('Private attachment ownership enforced')
    a.request('DELETE','/files/'+asset['id'])
    a.request('GET','/files/'+asset['id']+'/download',expected=404)
    check('Attachment deletion persisted')
else:
    print('SKIP MinIO upload/download (explicit --skip-files)',flush=True)
a.request('DELETE','/plans/'+pid)
a.request('DELETE','/calendar/'+entry['id'])
a.request('PUT','/favorites/'+source['id'],{'active':False})
check('Test business records cleaned',not a.request('GET','/plans') and not a.request('GET','/calendar'))
check('Audit records written',bool(admin.request('GET','/admin/audit')))
a.request('POST','/auth/logout',expected=204)
a.request('GET','/auth/me',expected=401)
check('Logout invalidates session')
print(f'\n{len(passed)} checks passed. Test accounts retained: smoke_a_{suffix}, smoke_b_{suffix}.')
