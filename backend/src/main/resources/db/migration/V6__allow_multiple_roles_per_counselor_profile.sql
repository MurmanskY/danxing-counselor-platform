DROP INDEX uq_app_users_profile;
ALTER TABLE app_users DROP CONSTRAINT app_users_profile_fk;

ALTER TABLE counselor_profiles
  DROP CONSTRAINT counselor_profiles_manager_profile_id_fkey,
  DROP CONSTRAINT counselor_profiles_pkey,
  ADD CONSTRAINT counselor_profiles_pkey PRIMARY KEY (profile_id, role_code),
  ADD CONSTRAINT counselor_profiles_manager_profile_id_fkey
    FOREIGN KEY (manager_profile_id) REFERENCES source_records(id);

ALTER TABLE app_users
  ADD CONSTRAINT app_users_profile_role_fk
    FOREIGN KEY (profile_id, role) REFERENCES counselor_profiles(profile_id, role_code);

CREATE UNIQUE INDEX uq_app_users_profile_role
  ON app_users(profile_id, role) WHERE profile_id IS NOT NULL;
