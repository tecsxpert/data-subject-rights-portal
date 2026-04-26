# Data Subject Rights Portal
# Java Developer 2 — Work Completed (Day 1 to Day 5)

### Day 1 (Mon Apr 14) — Database Schema Initialization
* Initialized database versioning using **Flyway**.
* Created `V1__init_schema.sql` defining the core `dsr_requests` table.
* Implemented **UUID** primary keys and optimized performance with indexes on `email` and `status` columns.

### Day 2 (Tue Apr 15) — Frontend Environment Setup
* Initialized the frontend using **React 18 + Vite**.
* Configured **Tailwind CSS v4** for utility-first styling.
* Setup **Axios** services with a centralized API base configuration for backend communication.
* Established standard folder structure: `src/components`, `src/pages`, `src/services`, and `src/context`.

### Day 3 (Wed Apr 16) — Request List View & Audit Schema
* Built the primary **Request List** page with dynamic data fetching, loading states, and empty states.
* Wrote **Flyway V2 migration** to implement the `audit_logs` table to track all CUD (Create, Update, Delete) operations.
* Styled data tables using Tailwind for high readability and responsiveness.

### Day 4 (Thu Apr 17) — Forms, Validation & Soft Delete Logic
* Developed the **Create/Edit Request Form** with custom React state management.
* Implemented client-side **validation**.
* Added **Soft Delete** capability by adding to **Flyway V1 migration** to add the `is_deleted` boolean flag to the database.
* Integrated the frontend form with the backend REST API via Axios `POST` requests.

### Day 5 (Fri Apr 18) — Authentication & Route Security
* Implemented **AuthContext** using React Context API for global JWT state management.
* Developed a **ProtectedRoute** wrapper component to secure private routes and handle unauthorized redirects.
* Configured **Axios Interceptors** to automatically attach `Authorization: Bearer <token>` to every outgoing request.
