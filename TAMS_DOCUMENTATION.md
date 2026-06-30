# TAMS (The Aspirant Management System)
### Complete App Architecture, Feature Documentation, Workflows, Validation & Requirements

Welcome to the official comprehensive documentation of **TAMS (The Aspirant Management System)**. This document outlines the system architecture, features, user roles, security, operational workflows, system validations, and data structures.

---

## 1. Executive Summary

**TAMS** is an offline-first, high-performance, SaaS-ready classroom and tuition manager designed to streamline educational business administration. From single-tutor centers to multi-branch academies, it handles scheduling, attendance tracking, fee payments, double-entry financial ledgering, interactive study halls, physical library desks, digital exams, and automated SMS/WhatsApp alerts for parents. It features an advanced integration with the **Gemini API** for automatic homework feedback and doubt-solving.

---

## 2. Technical Stack & Architecture

- **Runtime:** Android OS (Mobile & Tablet)
- **Programming Language:** Kotlin (100% Type-Safe)
- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **State Management:** Jetpack ViewModel + Kotlin Coroutines & StateFlow
- **Data Persistence:** SQLite database via **Room Persistence Library** (KSP-processed, fully relational, with Foreign Key Cascades)
- **External Integration Services:**
  - **Firebase Firestore/Realtime DB:** SaaS tenant synchronization and cloud compliance backup.
  - **Google ML Kit:** High-speed barcode/QR scanner processing for touchless attendance.
  - **Gemini API:** Generative AI for academic grading feedback, automated doubt-solving bot, and smart query assistance.
  - **Google Drive Android API:** Automated file-based manual and scheduling backup models.

---

## 3. User Roles & Permission Matrix (Roles)

TAMS employs a strict role-based access control (RBAC) mechanism. The system transitions dynamically across five distinct authorization modes:

| Feature / Workspace | GUEST | STUDENT | PARENT | TEACHER / TUTOR | STAFF / CLERK | ADMIN (App Owner) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Walkthrough & Demos** | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| **View Study Material** | ❌ | ✔ | ❌ | ✔ | ✔ | ✔ |
| **Attempt Quizzes & Exams** | ❌ | ✔ | ❌ | Preview Only | Preview Only | ✔ |
| **Ask Gemini AI Bot** | ❌ | ✔ | ❌ | ✔ | ✔ | ✔ |
| **Manual & QR Attendance** | ❌ | QR Only | ❌ | ✔ | ✔ | ✔ |
| **View Fee Invoices & Dues** | ❌ | Own Only | ✔ | ❌ | ✔ | ✔ |
| **Edit Batches & Scheduling** | ❌ | ❌ | ❌ | ✔ (Read/Suggest) | ✔ | ✔ |
| **Manage Admission Forms** | ❌ | ❌ | ❌ | ❌ | ✔ | ✔ |
| **Ledger (Income & Expenses)** | ❌ | ❌ | ❌ | ❌ | ✔ (Restricted) | ✔ |
| **Staff/Teacher Accounts** | ❌ | ❌ | ❌ | ❌ | ❌ | ✔ |
| **SaaS Approvals & System Logs** | ❌ | ❌ | ❌ | ❌ | ❌ | ✔ |

---

## 4. Unified Services Directory (Core Features)

### A. Batch Scheduling & Classroom Manager
- **Dynamic Creation:** Configure custom batch names, center location partitions, and assigned subject lines.
- **Timings Configuration:** Define explicit hourly intervals (e.g., `04:00 PM - 05:00 PM`) and weekly days (e.g., `Mon, Wed, Fri`).
- **Billing Assignment:** Bind flat-rate fees structure directly to batch databases (Monthly/Course duration metrics).

### B. Student admission & Profile Engine
- **Profile Fields:** Captures roll numbers, birth dates, phone coordinates, sibling details, and school contexts.
- **Media Attachments:** Stores local URIs of student profile photos and digital qualification certificates.
- **Dynamic Attributes:** Utilizes non-rigid metadata lines (`custom_field_1`, `custom_field_2`, `custom_field_3`) to record customized logs (e.g., Blood Group, Bus Route, Physical Height).

### C. Attendance & Dual-Mode Logging
- **QR Automated Attendance:** Generates interactive student cards with personalized QR codes. The high-speed camera scanner powered by Google ML Kit scans QR cards and automatically posts a "Present" status with a timestamp.
- **Manual Sheet Matrix:** A structured grid allowing administrators to toggle student logs among *Present*, *Absent*, or *Leave* states with custom note annotations.

### D. Tuition Fees Management & PDF Exports
- **Fee Configuration:** Tracks payment forms (UPI, CASH, CARD, NET BANKING).
- **Grace Allowances:** Allows custom discount credits to record fee waivers under a single ledger entry.
- **Reciept Exporter:** Formulates clean visual payment certificates with local Android PDF print drivers. This includes UPI transactional sequence codes and verification markers.

### E. Financial Ledger (Double-Entry Bookkeeping)
- **Ledger Inflow/Outflow:** Categories for income heads (e.g., Course fees, donations) and expense heads (e.g., rent, utility bills, teacher salaries).
- **Net Profits:** Active computation of balances, automatically rendering active financial warnings or monthly growth reports.

### F. Online Exam Suite & Interactive Quizzes
- **Locked Examination Papers:** Teachers construct questions with four choices (A, B, C, D). Exams can be scheduled and locked to prevent early attempts.
- **Study Material Micro-Quizzes:** Every digital booklet supports contextual quizzes. Students receive automatic grading alongside step-by-step reasoning guides compiled by Gemini AI.

### G. Physical Library Seat Desk (Library Desks)
- **Book Loans:** Monitors book names, ISBN targets, borrowing students, expected return dates, and late-fee calculations.
- **Interactive Seat Layout:** A visual booking grid allowing students to reserve study hall cubes or reading chairs.

### H. Context-Aware Parent Alerts Message Broadcaster
- **Platform Selection:** Sends templates using secondary cellular networks (Sms Sim 1 vs. Sim 2) or direct WhatsApp intent handshakes.
- **Smart Placeholders:** Dynamic parsing turns messages like `Dear [ParentName], [StudentName] had a pending fee of ₹[Amount]` into direct texts.

---

## 5. Workflows & Lifecycle Sequences

### Workflow 1: Multi-Tenant SaaS Registration & Subscription
```
[Unlicensed Academy]
         │
         ▼
[Registers via App Walkthrough] ──► [Input Academy Info & UPI Payment]
                                                │
                                                ▼
[Pending Manual/Auto Admin Check] ◄── [Record Sub-Transaction log]
         │
         ├──► Approved ──► [Unlocks all ADMIN features & local partitions]
         │
         └──► Suspended ─► [Saves databases locally, restricts operational access]
```

### Workflow 2: Attendance Tracking Cycle
```
[Student Presents QR-Code Card] ──► [ML Kit Camera Scanner in App]
                                               │
                                               ▼
[System Queries Student ID] ◄──────── [Validates Roll Number & Batch ID]
         │
         ├──► Success ──► [Posts "Present" Status + Records Check-In Timestamps]
         │
         └──► Fallback ─► [Launch Sheet Matrix for Manual Attendance Overrides]
```

### Workflow 3: Examination and AI Feedback Cycle
```
[Teacher Configures Exam & Locks It]
                 │
                 ▼
[Teacher Unlocks Exam at Schedule]
                 │
                 ▼
[Student Submits MCQ Selections within Timer]
                 │
                 ▼
[Instant Auto-Grading Engine Scores Attempt]
                 │
                 ▼
[System Dispatches Prompt to Gemini API] ──► [GenAI Formulates Study Guide]
                                                       │
                                                       ▼
[Student Dashboard Displays Score, Correct Answers & AI Recommendation Reports]
```

---

## 6. System Validations & Integrity Constraints

Strict constraints are implemented in the Room database models, ViewModel flow handles, and Compose view triggers:

1. **Batch Capacity Check:**
   `if (currentBatchStudentCount >= maxCapacity) { Throw "Batch Limit Exceeded!" }`
   Validates admissions dynamically to prevent overcrowding classroom directories.

2. **Roll Number Uniqueness:**
   Enforces unique constraints on the combination of `rollNumber` and `instituteEmail` to avoid duplicate registrations.

3. **Double-Attendance Protection:**
   Blocks secondary attendance postings on the same student record for the same calendar date. Custom check-ins are restricted to a single primary scan per day.

4. **Fee Discount Boundaries:**
   Waivers cannot exceed the total billing cost:
   `discountAmount` must satisfy `0.0 <= discountAmount <= totalFeesValue`.

5. **UPI Transaction Check:**
   UPI reference ID strings are subjected to regular expression syntax checking to prevent typing garbage values for digital receipt approvals.

6. **Library Seat Collisions:**
   A seat coordinate `(Row, Column)` can only hold one active student allocation at a time. Trying to book an occupied seat returns a prompt to selects another desk.

---

## 7. Requirement Diagnostics

For production environments, ensure these capabilities are ready:
- **Camera Permission:** Must be permitted internally for QR scanner capabilities.
- **Secrets Setup:** Make sure the **Gemini API Key** is configured within the AI Studio secrets configuration menu (via `BuildConfig.GEMINI_API_KEY`) to power the doubt solver bot.
- **Android SMS Services:** Sim card permissions must be granted to invoke physical message broadcast tools.

---

TAMS is built to scale. Read through the technical variables mapped in `AppViewModel.kt` and UI layouts in `DashboardScreen.kt`/`OnboardingAndOverlays.kt` to understand or modify further features.
