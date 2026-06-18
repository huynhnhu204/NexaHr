# NexaHR QA Checklist

## Auth
- [ ] Login 4 demo accounts
- [ ] Company select, logout, session expiry
- [ ] 403 page for unauthorized routes

## Multi-tenant
- [ ] Switch company updates all modules
- [ ] No cross-company data leak

## Core flows
- [ ] Employee CRUD + upload
- [ ] Attendance check-in/out + reports
- [ ] Leave create/approve/reject
- [ ] Payroll generate/approve/export
- [ ] Recruitment + careers portal
- [ ] Training + assets
- [ ] Settings theme/language

## Quality
- [ ] Loading & empty states
- [ ] API error toasts (Vietnamese)
- [ ] `npm run build` + `mvn package` pass
- [ ] Swagger accessible
