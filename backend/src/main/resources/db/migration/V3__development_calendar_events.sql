CREATE TABLE development_calendar_events (
  id varchar(36) PRIMARY KEY,
  title varchar(150) NOT NULL,
  event_date date NOT NULL,
  category varchar(40) NOT NULL
);

CREATE INDEX idx_development_calendar_date ON development_calendar_events(event_date);

INSERT INTO development_calendar_events (id, title, event_date, category) VALUES
  ('development-event-20261119', '上海市辅导员能力素质大赛', '2026-11-19', '上海市'),
  ('development-event-20261221', '上海市最美辅导员', '2026-12-21', '上海市'),
  ('development-event-20270525', '复旦大学十佳辅导员评选', '2027-05-25', '复旦大学'),
  ('development-event-20270312', '复旦大学辅导员工作案例大赛', '2027-03-12', '复旦大学'),
  ('development-event-20270612', '复旦大学辅导员心理谈心谈话能力大赛', '2027-06-12', '复旦大学');
