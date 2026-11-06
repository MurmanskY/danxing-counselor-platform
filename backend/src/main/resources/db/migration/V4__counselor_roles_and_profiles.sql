CREATE TABLE app_roles (
  code varchar(40) PRIMARY KEY,
  name varchar(80) NOT NULL,
  assignable boolean NOT NULL DEFAULT true,
  dashboard_scope varchar(20) NOT NULL CHECK (dashboard_scope IN ('SELF', 'MANAGER'))
);

CREATE TABLE app_role_permissions (
  role_code varchar(40) NOT NULL REFERENCES app_roles(code) ON DELETE CASCADE,
  permission_code varchar(60) NOT NULL,
  PRIMARY KEY (role_code, permission_code)
);

INSERT INTO app_roles (code, name, assignable, dashboard_scope) VALUES
  ('COUNSELOR', '普通辅导员', true, 'SELF'),
  ('COUNSELOR_ADMIN', '管理员辅导员', true, 'MANAGER');

INSERT INTO app_role_permissions (role_code, permission_code) VALUES
  ('COUNSELOR_ADMIN', 'VIEW_TEAM_DASHBOARD'),
  ('COUNSELOR_ADMIN', 'MANAGE_USERS'),
  ('COUNSELOR_ADMIN', 'REVIEW_RECORDS'),
  ('COUNSELOR_ADMIN', 'VIEW_AUDIT');

ALTER TABLE app_users ADD COLUMN profile_id varchar(20);
ALTER TABLE app_users DROP CONSTRAINT app_users_role_check;
UPDATE app_users SET role = 'COUNSELOR_ADMIN' WHERE role = 'ADMIN';
ALTER TABLE app_users ALTER COLUMN role TYPE varchar(40);
ALTER TABLE app_users
  ADD CONSTRAINT app_users_role_fk FOREIGN KEY (role) REFERENCES app_roles(code);

CREATE TABLE counselor_profiles (
  profile_id varchar(20) PRIMARY KEY REFERENCES source_records(id) ON DELETE CASCADE,
  role_code varchar(40) NOT NULL REFERENCES app_roles(code),
  manager_profile_id varchar(20) REFERENCES counselor_profiles(profile_id),
  CHECK (manager_profile_id IS NULL OR manager_profile_id <> profile_id)
);
CREATE INDEX idx_counselor_profiles_manager ON counselor_profiles(manager_profile_id);
ALTER TABLE app_users
  ADD CONSTRAINT app_users_profile_fk FOREIGN KEY (profile_id) REFERENCES counselor_profiles(profile_id);
CREATE INDEX idx_app_users_profile ON app_users(profile_id);
CREATE UNIQUE INDEX uq_app_users_profile ON app_users(profile_id) WHERE profile_id IS NOT NULL;
