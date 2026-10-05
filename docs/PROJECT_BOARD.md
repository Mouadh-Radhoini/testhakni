# Recommended GitHub Project Board

## Board Columns
1. BACKLOG
2. TODO
3. IN PROGRESS
4. CODE REVIEW
5. TESTING
6. DONE

## Recommended Labels
- backend, frontend, database, authentication, security
- customer, provider, services, search, requests, offers, booking, reviews, notifications, admin, payments
- testing, documentation, devops, docker, deployment, bug
- P0, P1, P2

## Milestones (8-week)
- Week 1
- Week 2
- Week 3
- Week 4
- Week 5
- Week 6
- Week 7
- Week 8

## Dependency Map (Execution Order)
Setup → Authentication → Profiles → Services → Search → Requests → Offers → Bookings → Reviews → Notifications/Admin → Testing/Security → Docker/Deployment → Final Delivery

## Intake and Flow Rules
- New items start in **BACKLOG** with full acceptance criteria and dependencies.
- Move to **TODO** only when dependencies are completed or explicitly waived by product owner.
- Move to **IN PROGRESS** only with assignee and branch/PR reference.
- Move to **CODE REVIEW** after implementation + local tests pass.
- Move to **TESTING** after code review approval and CI checks pass.
- Move to **DONE** only when Definition of Done is fully satisfied.

## Suggested Priority Swimlanes
- **P0 (MVP core):** auth, profiles, services, search, requests, offers, booking, completion, reviews.
- **P1 (important):** notifications, admin moderation, Docker/deployment readiness, non-core UX/testing enhancements.
- **P2 (future):** extended analytics/dashboard and incremental payments capabilities.