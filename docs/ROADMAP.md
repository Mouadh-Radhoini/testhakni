# 8-Week Roadmap — Local Services Marketplace

## MVP Definition

MVP is complete when the end-to-end core workflow is functional:

**Authentication → Provider Profile → Services → Search → Service Requests → Offers → Booking → Completion Confirmation → Reviews**

## Dependency Map

Setup → Authentication → Profiles → Services → Search → Requests → Offers → Bookings → Reviews → Notifications/Admin → Testing/Security → Docker/Deployment → Final Delivery

## Milestones by Week

### Week 1
- Requirements validation, repo setup, architecture baseline, team workflow setup (SET-01, SET-02).

### Week 2
- CI quality gates and foundation hardening (SET-03).
- Architecture/domain preparation for implementation start.

### Week 3
- Authentication, JWT, roles and security foundation (AUTH-01..AUTH-08, SEC-01).

### Week 4
- Provider profiles, service management, search/discovery, service requests (AUTH-09, PROV-01..03, SERV-01..02, SEARCH-01..03, REQ-01..02).

### Week 5
- Offers and full booking workflow (REQ-03, OFFER-01..03, BOOK-01..04).

### Week 6
- Reviews, notifications, and administration moderation/governance (REV-01..02, NOTIF-01..02, ADMIN-01..02).

### Week 7
- Testing, security hardening, OpenAPI docs and Docker stack (SEC-02..03, TEST-01..03, DOC-01, DEP-01..02, PAY-01).

### Week 8
- Deployment readiness, extended admin/payments, final docs and demo preparation (ADMIN-03, PAY-02, DOC-02..03, DEP-03).

## Cross-Epic Dependencies

- AUTH-01 is prerequisite for AUTH-02/03 and all secured workflows.
- AUTH-08 role model gates admin/provider/customer access across all epics.
- OFFER-03 acceptance drives BOOK-01 creation.
- BOOK-04 completion confirmation gates REV-01 review submission.
- SEC-03 + TEST-02 + DOC-01 + DEP-02 are required before DEP-03 release readiness.

## Global Definition of Done

- Implementation completed according to story scope.
- Input validation and error handling covered where relevant.
- Automated tests for the story pass (unit/integration/API/security as applicable).
- OpenAPI/Swagger documentation updated for exposed API changes.
- Code reviewed and no unresolved critical defects remain.