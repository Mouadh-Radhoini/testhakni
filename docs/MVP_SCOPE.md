# MVP Scope vs Future Scope

## Functional Completion Rule

The project is functionally complete for MVP when a customer can authenticate, find services, create requests, receive and accept offers, track bookings to completion, and submit a review; while providers and admins can perform minimum required governance actions.

## In-Scope for MVP (Must Have)

- **SET-01** — Initialize backend and frontend workspaces
- **SET-02** — Configure development environments and branching workflow
- **AUTH-01** — Create user domain model and repository
- **AUTH-02** — Implement customer registration API
- **AUTH-03** — Implement provider registration API
- **AUTH-04** — Implement login API
- **AUTH-05** — Implement JWT generation and refresh strategy
- **AUTH-06** — Add JWT authorization filter for protected endpoints
- **AUTH-07** — Implement password policy and hashing standards
- **AUTH-08** — Implement role-based authorization rules
- **PROV-01** — Create provider professional profile model
- **PROV-02** — Implement provider profile create/update APIs
- **PROV-03** — Expose provider public profile endpoint
- **SERV-01** — Create service domain model and categories
- **SERV-02** — Implement provider service CRUD APIs
- **SEARCH-01** — Implement service listing endpoint for customers
- **SEARCH-02** — Add search and filter capabilities
- **SEARCH-03** — Build Angular service discovery UI
- **REQ-01** — Create service request domain model
- **REQ-02** — Implement customer service request creation/list APIs
- **REQ-03** — Expose relevant open requests to providers
- **OFFER-01** — Create offer domain model
- **OFFER-02** — Implement provider offer submission endpoint
- **OFFER-03** — Implement customer offer comparison and acceptance APIs
- **BOOK-01** — Create booking domain model and status workflow
- **BOOK-02** — Implement booking retrieval APIs for customer/provider
- **BOOK-03** — Implement provider booking status update API
- **BOOK-04** — Implement customer completion confirmation API
- **REV-01** — Create review model and customer review submission API
- **SEC-01** — Implement global exception and secure error response policy
- **SEC-03** — Harden CORS, headers, and JWT secret management
- **TEST-01** — Create backend unit tests for auth and core domain services
- **TEST-02** — Create integration/API tests for core marketplace workflow
- **DOC-01** — Publish OpenAPI/Swagger documentation for all REST endpoints

## Secondary / Future Enhancements

- **SET-03** — Establish base CI workflow and quality gates
- **AUTH-09** — Implement user profile read/update endpoints
- **SERV-03** — Implement service moderation workflow hooks
- **REV-02** — Expose reviews in provider profile/service detail
- **NOTIF-01** — Design notification model and event triggers
- **NOTIF-02** — Implement in-app notification listing and read status APIs
- **ADMIN-01** — Implement admin user management APIs
- **ADMIN-02** — Implement moderation APIs for services and reviews
- **ADMIN-03** — Create admin dashboard statistics endpoint
- **PAY-01** — Define payment architecture and booking-payment linkage
- **PAY-02** — Implement payment intent and status endpoints (internal)
- **SEC-02** — Implement API rate limiting for auth and sensitive endpoints
- **TEST-03** — Implement frontend component/integration tests for key pages
- **DOC-02** — Create technical architecture documentation
- **DOC-03** — Prepare final user guide and demo script
- **DEP-01** — Containerize backend and frontend services
- **DEP-02** — Create docker-compose stack with MongoDB
- **DEP-03** — Prepare deployment checklist and release procedure

## Exit Criteria for MVP Sign-off

- End-to-end core workflow passes integration/API tests (TEST-02).
- JWT security and role authorization are enforced (AUTH-06, AUTH-08, SEC-03).
- OpenAPI docs are complete for implemented endpoints (DOC-01).
- No unresolved critical defects or critical security findings remain.