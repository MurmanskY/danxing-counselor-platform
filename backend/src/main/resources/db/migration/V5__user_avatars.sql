ALTER TABLE app_users ADD COLUMN avatar_file_id varchar(36) REFERENCES file_assets(id) ON DELETE SET NULL;
