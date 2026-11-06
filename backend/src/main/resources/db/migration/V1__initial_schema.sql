CREATE TABLE app_users (
  id varchar(36) PRIMARY KEY, username varchar(60) NOT NULL UNIQUE,
  password_hash varchar(100) NOT NULL, display_name varchar(80) NOT NULL,
  role varchar(20) NOT NULL CHECK(role IN ('ADMIN','COUNSELOR')),
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE source_records (
  id varchar(20) PRIMARY KEY, kind varchar(30) NOT NULL,
  title text NOT NULL, domain text NOT NULL, source text NOT NULL,
  payload jsonb NOT NULL, department text, staff_id text, record_type varchar(20)
);
CREATE INDEX idx_source_kind ON source_records(kind);
CREATE TABLE profile_extensions (
  record_id varchar(20) PRIMARY KEY REFERENCES source_records(id),
  keywords varchar(200) NOT NULL DEFAULT '', philosophy varchar(1000) NOT NULL DEFAULT '',
  review_status varchar(20) NOT NULL DEFAULT '待核验' CHECK(review_status IN ('待核验','已核验','需修正')),
  note varchar(1000) NOT NULL DEFAULT '', updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE plans (
  id varchar(36) PRIMARY KEY, owner_id varchar(36) NOT NULL REFERENCES app_users(id),
  title varchar(120) NOT NULL, person_id varchar(20) REFERENCES source_records(id),
  opportunity_id varchar(20) REFERENCES source_records(id), domain varchar(60) NOT NULL,
  deadline date, description varchar(2000) NOT NULL DEFAULT '',
  status varchar(20) NOT NULL DEFAULT '进行中', created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_plans_owner ON plans(owner_id);
CREATE TABLE tasks (
  id varchar(36) PRIMARY KEY, plan_id varchar(36) NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
  title varchar(180) NOT NULL, done boolean NOT NULL DEFAULT false, sort_order int NOT NULL
);
CREATE INDEX idx_tasks_plan ON tasks(plan_id);
CREATE TABLE favorites (
  id varchar(60) PRIMARY KEY, owner_id varchar(36) NOT NULL REFERENCES app_users(id),
  record_id varchar(20) NOT NULL REFERENCES source_records(id), UNIQUE(owner_id,record_id)
);
CREATE TABLE calendar_entries (
  id varchar(36) PRIMARY KEY, owner_id varchar(36) NOT NULL REFERENCES app_users(id),
  title varchar(150) NOT NULL, start_date date NOT NULL, end_date date NOT NULL,
  category varchar(40) NOT NULL CHECK(category IN ('个人准备','申报安排（待核验）')),
  note varchar(1000) NOT NULL DEFAULT '', record_id varchar(20) REFERENCES source_records(id),
  created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
  CHECK(end_date >= start_date)
);
CREATE INDEX idx_calendar_owner ON calendar_entries(owner_id);
CREATE TABLE chat_messages (
  id bigserial PRIMARY KEY, owner_id varchar(36) NOT NULL REFERENCES app_users(id),
  role varchar(20) NOT NULL CHECK(role IN ('user','assistant')), content jsonb NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_chat_owner ON chat_messages(owner_id,id);
CREATE TABLE file_assets (
  id varchar(36) PRIMARY KEY, owner_id varchar(36) NOT NULL REFERENCES app_users(id),
  record_id varchar(20) REFERENCES source_records(id), original_name varchar(200) NOT NULL,
  object_key varchar(200) NOT NULL UNIQUE, content_type varchar(100) NOT NULL,
  size_bytes bigint NOT NULL, created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_files_owner ON file_assets(owner_id);
CREATE TABLE audit_logs (
  id bigserial PRIMARY KEY, actor_id varchar(36) NOT NULL REFERENCES app_users(id),
  action varchar(60) NOT NULL, target_id varchar(60) NOT NULL, detail text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_actor ON audit_logs(actor_id,id);
CREATE TABLE app_meta (key varchar(60) PRIMARY KEY, value text NOT NULL);
