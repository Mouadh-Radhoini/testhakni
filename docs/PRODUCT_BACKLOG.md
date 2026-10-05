# Product Backlog — Local Services Marketplace

## Scope and Source of Truth

- Repository currently contains only a minimal README and no separate specification files; this backlog is built from the provided Local Services Marketplace specification.
- Functional terminology and actor workflows are preserved from the specification.
- Status defaults to `BACKLOG` for all items until implementation begins.


## EPIC 1 — Project Setup

### SET-01 — Initialize backend and frontend workspaces
- **Unique ID:** SET-01
- **Epic:** EPIC 1 — Project Setup
- **Title:** Initialize backend and frontend workspaces
- **User Story:** As a developer team, I want a clean Angular and Spring Boot project structure so that the team can develop in parallel with consistent foundations.
- **Description:** Create repository baseline structure for frontend, backend, shared docs, and environment configuration conventions.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** None
- **Labels:** backend, frontend, documentation, P0
- **Acceptance Criteria:**
  - Repository contains clear backend/frontend directory structure.
  - Build tooling for Angular and Spring Boot is initialized.
  - Common coding/contribution conventions are documented.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 1
- **Status:** BACKLOG

### SET-02 — Configure development environments and branching workflow
- **Unique ID:** SET-02
- **Epic:** EPIC 1 — Project Setup
- **Title:** Configure development environments and branching workflow
- **User Story:** As a developer team, I want a reproducible local development setup so that onboarding is fast and delivery risk is reduced.
- **Description:** Define local setup steps, environment variables, and Git workflow conventions for team collaboration.
- **Priority:** P0
- **Story Points:** 3
- **Dependencies:** SET-01
- **Labels:** documentation, devops, P0
- **Acceptance Criteria:**
  - Environment variables required for local run are documented.
  - Branch naming and PR conventions are documented.
  - A new contributor can run both apps following the README/docs only.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 1
- **Status:** BACKLOG

### SET-03 — Establish base CI workflow and quality gates
- **Unique ID:** SET-03
- **Epic:** EPIC 1 — Project Setup
- **Title:** Establish base CI workflow and quality gates
- **User Story:** As a project manager, I want automated checks on pull requests so that core quality is enforced from the start.
- **Description:** Create CI workflow for backend/frontend linting/testing/build checks and basic PR validation.
- **Priority:** P1
- **Story Points:** 3
- **Dependencies:** SET-01
- **Labels:** testing, devops, P1
- **Acceptance Criteria:**
  - CI runs automatically on pull requests.
  - Failed checks block merge according to repository policy.
  - CI status is visible and traceable in GitHub Actions.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 2
- **Status:** BACKLOG


## EPIC 2 — Authentication & Users

### AUTH-01 — Create user domain model and repository
- **Unique ID:** AUTH-01
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Create user domain model and repository
- **User Story:** As a developer, I want a user entity with role support so that authentication and authorization can be built reliably.
- **Description:** Define User model in MongoDB with core fields, role mapping, status flags and persistence repository.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** SET-01
- **Labels:** backend, database, authentication, P0
- **Acceptance Criteria:**
  - User model includes identity, credentials, role(s), and account status fields.
  - Unique constraints are enforced for login identifier (email/username).
  - User repository supports role/status-aware lookups.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### AUTH-02 — Implement customer registration API
- **Unique ID:** AUTH-02
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Implement customer registration API
- **User Story:** As a customer, I want to create an account so that I can request services on the platform.
- **Description:** Provide validated registration endpoint for customer accounts with secure password storage.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-01
- **Labels:** backend, authentication, customer, P0
- **Acceptance Criteria:**
  - Valid customer registration creates an active customer account.
  - Duplicate identity values are rejected with clear error response.
  - Passwords are hashed and never returned in any response.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### AUTH-03 — Implement provider registration API
- **Unique ID:** AUTH-03
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Implement provider registration API
- **User Story:** As a provider, I want to create a provider account so that I can publish and sell services.
- **Description:** Provide validated registration endpoint for provider accounts with provider role assignment.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-01
- **Labels:** backend, authentication, provider, P0
- **Acceptance Criteria:**
  - Valid provider registration creates account with provider role.
  - Registration enforces required provider-specific fields.
  - Sensitive credential data is not exposed in API responses.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### AUTH-04 — Implement login API
- **Unique ID:** AUTH-04
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Implement login API
- **User Story:** As a registered user, I want to log in securely so that I can access protected marketplace features.
- **Description:** Authenticate credentials and issue secure JWT token with identity and role claims.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-02, AUTH-03
- **Labels:** backend, authentication, security, P0
- **Acceptance Criteria:**
  - Valid credentials authenticate successfully.
  - Invalid credentials return unauthorized response.
  - JWT is returned on successful authentication.
  - JWT contains required identity/role claims.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### AUTH-05 — Implement JWT generation and refresh strategy
- **Unique ID:** AUTH-05
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Implement JWT generation and refresh strategy
- **User Story:** As a registered user, I want secure session tokens so that I can stay authenticated safely.
- **Description:** Configure JWT issuance parameters, signing, expiration and optional refresh-token flow.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-04
- **Labels:** backend, authentication, security, P0
- **Acceptance Criteria:**
  - Access token expires according to configured TTL.
  - Token signature is validated by server-side secret/key.
  - Expired/invalid tokens are rejected consistently.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### AUTH-06 — Add JWT authorization filter for protected endpoints
- **Unique ID:** AUTH-06
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Add JWT authorization filter for protected endpoints
- **User Story:** As a system, I want request authentication to be enforced so that only authorized users can access protected data.
- **Description:** Integrate Spring Security JWT filter that resolves user identity and roles for secured routes.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-05
- **Labels:** backend, authentication, security, P0
- **Acceptance Criteria:**
  - Protected endpoints require valid JWT tokens.
  - Unauthenticated requests are denied with proper status code.
  - Security context contains authenticated user and role data.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### AUTH-07 — Implement password policy and hashing standards
- **Unique ID:** AUTH-07
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Implement password policy and hashing standards
- **User Story:** As a administrator, I want strong credential handling so that platform accounts are better protected.
- **Description:** Apply password complexity validation and BCrypt hashing strategy for all user credentials.
- **Priority:** P0
- **Story Points:** 3
- **Dependencies:** AUTH-02
- **Labels:** security, authentication, P0
- **Acceptance Criteria:**
  - Password complexity rules are validated on registration/update.
  - Stored passwords use one-way salted hashing.
  - Weak passwords are rejected with clear validation errors.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### AUTH-08 — Implement role-based authorization rules
- **Unique ID:** AUTH-08
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Implement role-based authorization rules
- **User Story:** As a administrator, I want access controls by role so that each actor sees only allowed capabilities.
- **Description:** Define endpoint authorization matrix for customer/provider/admin access rights.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-06
- **Labels:** backend, security, authentication, admin, P0
- **Acceptance Criteria:**
  - Customer-only endpoints are inaccessible to unauthorized roles.
  - Provider/admin protected routes enforce role checks.
  - Forbidden access returns standardized authorization error.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### AUTH-09 — Implement user profile read/update endpoints
- **Unique ID:** AUTH-09
- **Epic:** EPIC 2 — Authentication & Users
- **Title:** Implement user profile read/update endpoints
- **User Story:** As a registered user, I want to manage my account profile so that my profile information remains accurate.
- **Description:** Add authenticated profile retrieval and update endpoints with field-level validation.
- **Priority:** P1
- **Story Points:** 3
- **Dependencies:** AUTH-06
- **Labels:** backend, authentication, customer, provider, P1
- **Acceptance Criteria:**
  - Authenticated user can retrieve own profile.
  - User can update allowed fields only.
  - Unauthorized profile access attempts are blocked.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG


## EPIC 3 — Provider Profiles

### PROV-01 — Create provider professional profile model
- **Unique ID:** PROV-01
- **Epic:** EPIC 3 — Provider Profiles
- **Title:** Create provider professional profile model
- **User Story:** As a provider, I want a structured professional profile so that customers can evaluate my credibility.
- **Description:** Design provider profile data model including skills, categories, location, and availability indicators.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-03
- **Labels:** backend, database, provider, P0
- **Acceptance Criteria:**
  - Provider profile schema supports required business fields.
  - Profile is linked one-to-one with provider account.
  - Validation enforces mandatory profile information.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG

### PROV-02 — Implement provider profile create/update APIs
- **Unique ID:** PROV-02
- **Epic:** EPIC 3 — Provider Profiles
- **Title:** Implement provider profile create/update APIs
- **User Story:** As a provider, I want to publish and maintain my profile so that I can attract relevant customer requests.
- **Description:** Allow providers to create/update professional profile content via secured endpoints.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** PROV-01, AUTH-06
- **Labels:** backend, provider, P0
- **Acceptance Criteria:**
  - Provider can create own profile once authenticated.
  - Provider can update own profile fields with validation.
  - Provider cannot modify another provider profile.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG

### PROV-03 — Expose provider public profile endpoint
- **Unique ID:** PROV-03
- **Epic:** EPIC 3 — Provider Profiles
- **Title:** Expose provider public profile endpoint
- **User Story:** As a customer, I want to view provider profile details so that I can make informed offer/booking decisions.
- **Description:** Provide public/read endpoint for provider profile display in service details and offers context.
- **Priority:** P0
- **Story Points:** 3
- **Dependencies:** PROV-02
- **Labels:** backend, provider, customer, P0
- **Acceptance Criteria:**
  - Customer can fetch provider profile by provider identifier.
  - Response excludes sensitive private account fields.
  - Non-existing provider profile returns clear not-found response.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG


## EPIC 4 — Service Management

### SERV-01 — Create service domain model and categories
- **Unique ID:** SERV-01
- **Epic:** EPIC 4 — Service Management
- **Title:** Create service domain model and categories
- **User Story:** As a provider, I want a standard service structure so that I can list clear offers for customers.
- **Description:** Define service model with title, description, category, pricing hints, status and provider ownership.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** PROV-01
- **Labels:** backend, database, services, provider, P0
- **Acceptance Criteria:**
  - Service schema includes required fields and provider link.
  - Service category field supports marketplace taxonomy.
  - Service status supports moderation lifecycle states.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG

### SERV-02 — Implement provider service CRUD APIs
- **Unique ID:** SERV-02
- **Epic:** EPIC 4 — Service Management
- **Title:** Implement provider service CRUD APIs
- **User Story:** As a provider, I want to create and manage my services so that I can maintain an up-to-date catalog.
- **Description:** Provide secured endpoints for providers to create, update, list, and deactivate own services.
- **Priority:** P0
- **Story Points:** 8
- **Dependencies:** SERV-01, AUTH-08
- **Labels:** backend, services, provider, P0
- **Acceptance Criteria:**
  - Provider can create service with valid payload.
  - Provider can edit/deactivate only own services.
  - Validation errors return clear, consistent API responses.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG

### SERV-03 — Implement service moderation workflow hooks
- **Unique ID:** SERV-03
- **Epic:** EPIC 4 — Service Management
- **Title:** Implement service moderation workflow hooks
- **User Story:** As a administrator, I want to approve/reject inappropriate services so that marketplace quality and trust are protected.
- **Description:** Add moderation states and admin actions for approving, suspending or rejecting service listings.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** SERV-01, ADMIN-01
- **Labels:** admin, services, security, P1
- **Acceptance Criteria:**
  - Admin can change service moderation status.
  - Non-admin users cannot execute moderation actions.
  - Moderation status affects customer-facing visibility.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 6
- **Status:** BACKLOG


## EPIC 5 — Search & Discovery

### SEARCH-01 — Implement service listing endpoint for customers
- **Unique ID:** SEARCH-01
- **Epic:** EPIC 5 — Search & Discovery
- **Title:** Implement service listing endpoint for customers
- **User Story:** As a customer, I want to browse available services so that I can discover providers matching my needs.
- **Description:** Expose public service listing endpoint with pagination and baseline sorting.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** SERV-02
- **Labels:** backend, services, search, customer, P0
- **Acceptance Criteria:**
  - Endpoint returns only visible/approved services.
  - Response supports pagination metadata.
  - Empty results are handled without server errors.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG

### SEARCH-02 — Add search and filter capabilities
- **Unique ID:** SEARCH-02
- **Epic:** EPIC 5 — Search & Discovery
- **Title:** Add search and filter capabilities
- **User Story:** As a customer, I want to filter services by category/location/keyword so that I can quickly find relevant offers.
- **Description:** Provide query parameters for keyword, category, location, and price-related filters.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** SEARCH-01
- **Labels:** backend, search, customer, P0
- **Acceptance Criteria:**
  - Keyword search narrows service results by title/description.
  - Category/location filters can be combined.
  - Invalid filter values return validation errors.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG

### SEARCH-03 — Build Angular service discovery UI
- **Unique ID:** SEARCH-03
- **Epic:** EPIC 5 — Search & Discovery
- **Title:** Build Angular service discovery UI
- **User Story:** As a customer, I want an intuitive search interface so that I can discover and compare services efficiently.
- **Description:** Implement Angular pages/components for service listing, filtering, and detail navigation.
- **Priority:** P0
- **Story Points:** 8
- **Dependencies:** SEARCH-01, SEARCH-02
- **Labels:** frontend, search, customer, P0
- **Acceptance Criteria:**
  - User can search/filter from UI and see updated result list.
  - Selecting a service opens service detail view.
  - UI handles loading, empty, and error states.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG


## EPIC 6 — Service Requests

### REQ-01 — Create service request domain model
- **Unique ID:** REQ-01
- **Epic:** EPIC 6 — Service Requests
- **Title:** Create service request domain model
- **User Story:** As a customer, I want to formalize my service need so that providers can respond with suitable offers.
- **Description:** Define request model including customer, category, description, location, schedule preferences and status.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-02, SEARCH-03
- **Labels:** backend, database, requests, customer, P0
- **Acceptance Criteria:**
  - Request schema captures required customer need details.
  - Each request is linked to requesting customer.
  - Request status lifecycle is clearly represented.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG

### REQ-02 — Implement customer service request creation/list APIs
- **Unique ID:** REQ-02
- **Epic:** EPIC 6 — Service Requests
- **Title:** Implement customer service request creation/list APIs
- **User Story:** As a customer, I want to create and track my requests so that I can manage service demand in one place.
- **Description:** Add secured endpoints for customers to create requests and list own requests by status.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** REQ-01, AUTH-06
- **Labels:** backend, requests, customer, P0
- **Acceptance Criteria:**
  - Authenticated customer can create new request.
  - Customer can list only own requests.
  - Request validation failures are returned with explicit fields/messages.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 4
- **Status:** BACKLOG

### REQ-03 — Expose relevant open requests to providers
- **Unique ID:** REQ-03
- **Epic:** EPIC 6 — Service Requests
- **Title:** Expose relevant open requests to providers
- **User Story:** As a provider, I want to see matching customer requests so that I can submit targeted offers.
- **Description:** Provide provider-facing endpoint to discover open requests by category/location relevance.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** REQ-02, PROV-02
- **Labels:** backend, requests, provider, P0
- **Acceptance Criteria:**
  - Provider can list open requests relevant to profile/categories.
  - Closed/cancelled requests are excluded by default.
  - Provider cannot access private customer data beyond allowed fields.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 5
- **Status:** BACKLOG


## EPIC 7 — Offers

### OFFER-01 — Create offer domain model
- **Unique ID:** OFFER-01
- **Epic:** EPIC 7 — Offers
- **Title:** Create offer domain model
- **User Story:** As a provider, I want to send structured offers to requests so that customers can compare proposals consistently.
- **Description:** Define offer model with request link, provider link, proposed amount, timeline, message and status.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** REQ-03
- **Labels:** backend, database, offers, provider, P0
- **Acceptance Criteria:**
  - Offer is linked to one request and one provider.
  - Offer status supports submitted/accepted/rejected lifecycle.
  - Business rules prevent malformed offer values.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 5
- **Status:** BACKLOG

### OFFER-02 — Implement provider offer submission endpoint
- **Unique ID:** OFFER-02
- **Epic:** EPIC 7 — Offers
- **Title:** Implement provider offer submission endpoint
- **User Story:** As a provider, I want to submit offers on requests so that I can compete for customer jobs.
- **Description:** Allow providers to submit offers on eligible open requests while enforcing ownership/eligibility rules.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** OFFER-01, AUTH-08
- **Labels:** backend, offers, provider, P0
- **Acceptance Criteria:**
  - Provider can submit offer for eligible open request.
  - Duplicate conflicting offers from same provider are controlled by rule.
  - Unauthorized submissions are rejected.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 5
- **Status:** BACKLOG

### OFFER-03 — Implement customer offer comparison and acceptance APIs
- **Unique ID:** OFFER-03
- **Epic:** EPIC 7 — Offers
- **Title:** Implement customer offer comparison and acceptance APIs
- **User Story:** As a customer, I want to compare offers and accept one so that I can choose best proposal confidently.
- **Description:** Provide endpoints to list offers per request and accept one, triggering booking creation workflow.
- **Priority:** P0
- **Story Points:** 8
- **Dependencies:** OFFER-02
- **Labels:** backend, offers, customer, booking, P0
- **Acceptance Criteria:**
  - Customer can list offers for own request.
  - Customer can accept one offer and others become rejected/closed.
  - Accepting offer triggers booking creation event/flow.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 5
- **Status:** BACKLOG


## EPIC 8 — Bookings

### BOOK-01 — Create booking domain model and status workflow
- **Unique ID:** BOOK-01
- **Epic:** EPIC 8 — Bookings
- **Title:** Create booking domain model and status workflow
- **User Story:** As a system, I want a booking lifecycle model so that service execution can be tracked end-to-end.
- **Description:** Model booking entity linked to accepted offer and include status transitions from created to completed.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** OFFER-03
- **Labels:** backend, database, booking, P0
- **Acceptance Criteria:**
  - Booking is created from accepted offer with immutable linkage.
  - Booking status transitions are defined and validated.
  - Booking stores both customer and provider references.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 5
- **Status:** BACKLOG

### BOOK-02 — Implement booking retrieval APIs for customer/provider
- **Unique ID:** BOOK-02
- **Epic:** EPIC 8 — Bookings
- **Title:** Implement booking retrieval APIs for customer/provider
- **User Story:** As a customer/provider, I want to view my bookings so that I can follow service progress.
- **Description:** Add role-aware endpoints for both actors to list and view booking details they are part of.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** BOOK-01, AUTH-06
- **Labels:** backend, booking, customer, provider, P0
- **Acceptance Criteria:**
  - Customer sees only own bookings.
  - Provider sees only bookings assigned to them.
  - Unauthorized booking access is blocked.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 5
- **Status:** BACKLOG

### BOOK-03 — Implement provider booking status update API
- **Unique ID:** BOOK-03
- **Epic:** EPIC 8 — Bookings
- **Title:** Implement provider booking status update API
- **User Story:** As a provider, I want to update booking progress so that customers receive transparent service progress.
- **Description:** Allow provider to move booking through allowed progress states (e.g., in-progress, completed).
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** BOOK-02
- **Labels:** backend, booking, provider, P0
- **Acceptance Criteria:**
  - Provider can apply only allowed forward transitions.
  - Invalid transitions are rejected with validation message.
  - Status updates are auditable (timestamp/user).
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 5
- **Status:** BACKLOG

### BOOK-04 — Implement customer completion confirmation API
- **Unique ID:** BOOK-04
- **Epic:** EPIC 8 — Bookings
- **Title:** Implement customer completion confirmation API
- **User Story:** As a customer, I want to confirm work completion so that booking can be officially closed and reviewed.
- **Description:** Add endpoint for customer to confirm completed booking and transition to closed/completed state.
- **Priority:** P0
- **Story Points:** 3
- **Dependencies:** BOOK-03
- **Labels:** backend, booking, customer, reviews, P0
- **Acceptance Criteria:**
  - Only booking customer can confirm completion.
  - Confirmation allowed only after provider marks work completed.
  - Confirmed booking becomes eligible for review creation.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 5
- **Status:** BACKLOG


## EPIC 9 — Reviews

### REV-01 — Create review model and customer review submission API
- **Unique ID:** REV-01
- **Epic:** EPIC 9 — Reviews
- **Title:** Create review model and customer review submission API
- **User Story:** As a customer, I want to rate completed bookings so that future customers can trust provider quality.
- **Description:** Implement review entity and endpoint for customers to leave review on completed booking only.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** BOOK-04
- **Labels:** backend, reviews, customer, P0
- **Acceptance Criteria:**
  - Customer can create review only for own completed booking.
  - One booking can receive at most one customer review.
  - Review payload validates rating range and required feedback fields.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 6
- **Status:** BACKLOG

### REV-02 — Expose reviews in provider profile/service detail
- **Unique ID:** REV-02
- **Epic:** EPIC 9 — Reviews
- **Title:** Expose reviews in provider profile/service detail
- **User Story:** As a customer, I want to read provider feedback so that I can choose providers based on reputation.
- **Description:** Return aggregated review information in profile/service views and display in Angular UI.
- **Priority:** P1
- **Story Points:** 3
- **Dependencies:** REV-01, PROV-03
- **Labels:** backend, frontend, reviews, provider, P1
- **Acceptance Criteria:**
  - Provider profile endpoint includes review summary data.
  - Service detail UI shows review list/summary.
  - Hidden/moderated reviews are not publicly shown.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 6
- **Status:** BACKLOG


## EPIC 10 — Notifications

### NOTIF-01 — Design notification model and event triggers
- **Unique ID:** NOTIF-01
- **Epic:** EPIC 10 — Notifications
- **Title:** Design notification model and event triggers
- **User Story:** As a system, I want standard notification events so that users are informed about important workflow updates.
- **Description:** Define notification entity/channels and map triggers for request, offer, booking and review lifecycle events.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** OFFER-03, BOOK-03, REV-01
- **Labels:** backend, notifications, P1
- **Acceptance Criteria:**
  - Notification events are defined for core workflow state changes.
  - Notification payload includes recipient, type, message, and reference object.
  - Event-to-notification mapping is documented.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 6
- **Status:** BACKLOG

### NOTIF-02 — Implement in-app notification listing and read status APIs
- **Unique ID:** NOTIF-02
- **Epic:** EPIC 10 — Notifications
- **Title:** Implement in-app notification listing and read status APIs
- **User Story:** As a customer/provider/admin, I want to track my notifications so that I stay updated without external tools.
- **Description:** Provide APIs and Angular components to show user notifications and mark them as read.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** NOTIF-01, AUTH-06
- **Labels:** backend, frontend, notifications, P1
- **Acceptance Criteria:**
  - Authenticated user can list own notifications.
  - User can mark selected notifications as read.
  - Unread/read states are visible in UI.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 6
- **Status:** BACKLOG


## EPIC 11 — Administration

### ADMIN-01 — Implement admin user management APIs
- **Unique ID:** ADMIN-01
- **Epic:** EPIC 11 — Administration
- **Title:** Implement admin user management APIs
- **User Story:** As a administrator, I want to view and manage platform users so that I can enforce platform governance.
- **Description:** Create admin endpoints for listing users, viewing details, and toggling active/deactivated status.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** AUTH-08
- **Labels:** backend, admin, security, P1
- **Acceptance Criteria:**
  - Admin can list users with role/status filters.
  - Admin can activate/deactivate user accounts.
  - Non-admin users cannot access these endpoints.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 6
- **Status:** BACKLOG

### ADMIN-02 — Implement moderation APIs for services and reviews
- **Unique ID:** ADMIN-02
- **Epic:** EPIC 11 — Administration
- **Title:** Implement moderation APIs for services and reviews
- **User Story:** As a administrator, I want to remove harmful content so that platform trust and quality are maintained.
- **Description:** Allow admins to moderate and hide/reject reported services and reviews with audit reason.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** SERV-03, REV-01
- **Labels:** backend, admin, services, reviews, P1
- **Acceptance Criteria:**
  - Admin can flag/hide/unhide service or review content.
  - Moderation action stores reason and moderator identity.
  - Moderated content visibility changes immediately in public APIs.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 6
- **Status:** BACKLOG

### ADMIN-03 — Create admin dashboard statistics endpoint
- **Unique ID:** ADMIN-03
- **Epic:** EPIC 11 — Administration
- **Title:** Create admin dashboard statistics endpoint
- **User Story:** As a administrator, I want marketplace KPIs so that I can monitor platform health and growth.
- **Description:** Provide aggregated counts/metrics for users, services, requests, offers, bookings and reviews.
- **Priority:** P2
- **Story Points:** 3
- **Dependencies:** ADMIN-01, BOOK-01, REV-01
- **Labels:** backend, admin, P2
- **Acceptance Criteria:**
  - Dashboard endpoint returns key platform aggregates.
  - Metrics are restricted to admin role.
  - Response performance is acceptable for dashboard usage.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 8
- **Status:** BACKLOG


## EPIC 12 — Payments

### PAY-01 — Define payment architecture and booking-payment linkage
- **Unique ID:** PAY-01
- **Epic:** EPIC 12 — Payments
- **Title:** Define payment architecture and booking-payment linkage
- **User Story:** As a project owner, I want a clear payment domain plan so that future payment implementation is low-risk and compliant.
- **Description:** Document payment flow, states, and integration boundaries aligned with booking lifecycle.
- **Priority:** P2
- **Story Points:** 3
- **Dependencies:** BOOK-01
- **Labels:** payments, documentation, P2
- **Acceptance Criteria:**
  - Payment lifecycle states are defined and mapped to booking stages.
  - Required payment data fields are documented.
  - Security/compliance assumptions are explicitly listed.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG

### PAY-02 — Implement payment intent and status endpoints (internal)
- **Unique ID:** PAY-02
- **Epic:** EPIC 12 — Payments
- **Title:** Implement payment intent and status endpoints (internal)
- **User Story:** As a system, I want basic payment records so that bookings can be prepared for future transaction processing.
- **Description:** Add internal payment entity/endpoints to create intents and track statuses without full gateway integration.
- **Priority:** P2
- **Story Points:** 8
- **Dependencies:** PAY-01, BOOK-02
- **Labels:** backend, payments, security, P2
- **Acceptance Criteria:**
  - Payment intent can be created for eligible booking.
  - Payment status transitions are validated and auditable.
  - Payment data access is role-restricted and secure.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 8
- **Status:** BACKLOG


## EPIC 13 — Security

### SEC-01 — Implement global exception and secure error response policy
- **Unique ID:** SEC-01
- **Epic:** EPIC 13 — Security
- **Title:** Implement global exception and secure error response policy
- **User Story:** As a platform user, I want safe and consistent API errors so that security details are not leaked and debugging is reliable.
- **Description:** Standardize API error payload and sanitize security-sensitive error disclosures.
- **Priority:** P0
- **Story Points:** 3
- **Dependencies:** AUTH-04
- **Labels:** backend, security, P0
- **Acceptance Criteria:**
  - All APIs return standardized error response schema.
  - Sensitive internals (stack traces, secrets) are not exposed.
  - Validation/auth errors return correct HTTP status codes.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 3
- **Status:** BACKLOG

### SEC-02 — Implement API rate limiting for auth and sensitive endpoints
- **Unique ID:** SEC-02
- **Epic:** EPIC 13 — Security
- **Title:** Implement API rate limiting for auth and sensitive endpoints
- **User Story:** As a administrator, I want abuse protection so that brute-force and abuse risk is reduced.
- **Description:** Apply configurable rate limiting strategy for login and other sensitive operations.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** AUTH-04
- **Labels:** backend, security, authentication, P1
- **Acceptance Criteria:**
  - Rate limits are enforced on protected endpoint group(s).
  - Exceeding limits returns explicit throttling response.
  - Rate limiting configuration is environment-driven.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG

### SEC-03 — Harden CORS, headers, and JWT secret management
- **Unique ID:** SEC-03
- **Epic:** EPIC 13 — Security
- **Title:** Harden CORS, headers, and JWT secret management
- **User Story:** As a security reviewer, I want secure runtime defaults so that common web/API attack surfaces are reduced.
- **Description:** Configure secure HTTP headers, controlled CORS, and secret handling for JWT configuration.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-06
- **Labels:** backend, security, devops, P0
- **Acceptance Criteria:**
  - CORS allows only configured origins.
  - Security headers are present on API responses.
  - JWT secret/key is externalized and never hardcoded.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG


## EPIC 14 — Testing

### TEST-01 — Create backend unit tests for auth and core domain services
- **Unique ID:** TEST-01
- **Epic:** EPIC 14 — Testing
- **Title:** Create backend unit tests for auth and core domain services
- **User Story:** As a developer, I want fast regression checks so that core logic changes are caught early.
- **Description:** Implement unit test suites for authentication, authorization, requests/offers/bookings and validation rules.
- **Priority:** P0
- **Story Points:** 8
- **Dependencies:** AUTH-08, BOOK-04
- **Labels:** testing, backend, security, P0
- **Acceptance Criteria:**
  - Critical service-layer logic is unit tested.
  - Authorization rules have positive and negative tests.
  - Tests run in CI and pass consistently.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG

### TEST-02 — Create integration/API tests for core marketplace workflow
- **Unique ID:** TEST-02
- **Epic:** EPIC 14 — Testing
- **Title:** Create integration/API tests for core marketplace workflow
- **User Story:** As a product owner, I want end-to-end API confidence so that MVP flow is validated before delivery.
- **Description:** Cover the main customer-provider journey through API tests from auth to review submission.
- **Priority:** P0
- **Story Points:** 8
- **Dependencies:** OFFER-03, BOOK-04, REV-01
- **Labels:** testing, backend, requests, offers, booking, reviews, P0
- **Acceptance Criteria:**
  - Test scenario covers request → offer → accept → booking → completion → review.
  - Failing authorization and validation paths are included.
  - All workflow integration tests pass in CI pipeline.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG

### TEST-03 — Implement frontend component/integration tests for key pages
- **Unique ID:** TEST-03
- **Epic:** EPIC 14 — Testing
- **Title:** Implement frontend component/integration tests for key pages
- **User Story:** As a developer, I want stable Angular user workflows so that UI regressions are detected automatically.
- **Description:** Add Angular tests for auth screens, service discovery, request/offer/booking views and notifications.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** SEARCH-03, NOTIF-02
- **Labels:** testing, frontend, P1
- **Acceptance Criteria:**
  - Critical customer/provider screens have automated tests.
  - State handling (loading/error/empty) is validated.
  - Frontend tests run in CI and pass.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG


## EPIC 15 — Documentation

### DOC-01 — Publish OpenAPI/Swagger documentation for all REST endpoints
- **Unique ID:** DOC-01
- **Epic:** EPIC 15 — Documentation
- **Title:** Publish OpenAPI/Swagger documentation for all REST endpoints
- **User Story:** As a developer, I want up-to-date API documentation so that frontend integration and jury review are easier.
- **Description:** Document API paths, schemas, auth requirements, and sample responses using OpenAPI/Swagger.
- **Priority:** P0
- **Story Points:** 5
- **Dependencies:** AUTH-08, BOOK-04, REV-01
- **Labels:** documentation, backend, P0
- **Acceptance Criteria:**
  - All implemented REST endpoints are represented in OpenAPI.
  - Security schemes (JWT) are correctly documented.
  - API docs can be viewed in Swagger UI.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG

### DOC-02 — Create technical architecture documentation
- **Unique ID:** DOC-02
- **Epic:** EPIC 15 — Documentation
- **Title:** Create technical architecture documentation
- **User Story:** As a technical jury, I want clear architecture visibility so that system design quality can be evaluated objectively.
- **Description:** Provide architecture views covering frontend/backend layers, MongoDB domain model, and security boundaries.
- **Priority:** P1
- **Story Points:** 3
- **Dependencies:** SET-03, SEC-03
- **Labels:** documentation, backend, frontend, database, P1
- **Acceptance Criteria:**
  - Architecture document includes component and data flow diagrams.
  - Actor permissions and trust boundaries are documented.
  - Deployment and environment assumptions are documented.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 8
- **Status:** BACKLOG

### DOC-03 — Prepare final user guide and demo script
- **Unique ID:** DOC-03
- **Epic:** EPIC 15 — Documentation
- **Title:** Prepare final user guide and demo script
- **User Story:** As a project team, I want clear presentation materials so that project can be demonstrated smoothly.
- **Description:** Create concise user guide for main actors and a structured demo scenario for final presentation.
- **Priority:** P1
- **Story Points:** 2
- **Dependencies:** TEST-02, DEP-03
- **Labels:** documentation, P1
- **Acceptance Criteria:**
  - Guide covers customer, provider, and admin core actions.
  - Demo script follows MVP workflow end-to-end.
  - Documentation is accessible from repository docs folder.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 8
- **Status:** BACKLOG


## EPIC 16 — Docker & Deployment

### DEP-01 — Containerize backend and frontend services
- **Unique ID:** DEP-01
- **Epic:** EPIC 16 — Docker & Deployment
- **Title:** Containerize backend and frontend services
- **User Story:** As a devops engineer, I want portable runtime environments so that setup and delivery are consistent.
- **Description:** Create Dockerfiles for Spring Boot and Angular applications with environment-based configuration.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** SET-02
- **Labels:** docker, deployment, backend, frontend, P1
- **Acceptance Criteria:**
  - Backend image builds and runs via Docker.
  - Frontend image builds and runs via Docker.
  - Images use externalized runtime configuration.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG

### DEP-02 — Create docker-compose stack with MongoDB
- **Unique ID:** DEP-02
- **Epic:** EPIC 16 — Docker & Deployment
- **Title:** Create docker-compose stack with MongoDB
- **User Story:** As a developer, I want one-command local orchestration so that integration testing and demos are simplified.
- **Description:** Define docker-compose for frontend, backend, and MongoDB with networking/volumes/env wiring.
- **Priority:** P1
- **Story Points:** 5
- **Dependencies:** DEP-01
- **Labels:** docker, deployment, database, P1
- **Acceptance Criteria:**
  - Compose starts all required services successfully.
  - Backend connects to MongoDB container via compose network.
  - Documented command brings stack up/down reliably.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 7
- **Status:** BACKLOG

### DEP-03 — Prepare deployment checklist and release procedure
- **Unique ID:** DEP-03
- **Epic:** EPIC 16 — Docker & Deployment
- **Title:** Prepare deployment checklist and release procedure
- **User Story:** As a project manager, I want a repeatable release process so that final delivery is controlled and auditable.
- **Description:** Define release checklist including pre-release tests, environment config, backup, and rollback considerations.
- **Priority:** P1
- **Story Points:** 3
- **Dependencies:** DEP-02, TEST-02, DOC-01
- **Labels:** deployment, documentation, devops, P1
- **Acceptance Criteria:**
  - Checklist includes technical and validation gates.
  - Release steps identify responsible role and order.
  - Rollback and post-deployment verification steps are documented.
- **Definition of Done:**
  - Implementation completed according to story scope.
  - Input validation and error handling covered where relevant.
  - Automated tests for the story pass (unit/integration/API/security as applicable).
  - OpenAPI/Swagger documentation updated for exposed API changes.
  - Code reviewed and no unresolved critical defects remain.
- **Target Week:** Week 8
- **Status:** BACKLOG
