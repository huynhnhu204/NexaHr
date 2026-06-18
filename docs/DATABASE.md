# NexaHR Database

MySQL 8 — `nexahr_db` (utf8mb4)

## Tables (35+)

Companies, users, employees, departments, positions, attendance, leave_requests, payrolls, job_postings, candidates, interviews, courses, assets, notifications, audit_logs, ...

## Seed

1. `DataSeeder.seedCore()` — when users empty
2. `DataSeeder.seedFeatureData()` — when leaves empty
3. `DemoDataExpander.expandIfNeeded()` — when employees < 30 for NEXA-DEMO

Reset: `DROP DATABASE nexahr_db; CREATE DATABASE ...` then restart backend.
