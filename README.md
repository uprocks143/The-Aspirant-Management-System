# TAMS — Tuition & Academy Management System 🎓

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Room DB](https://img.shields.io/badge/Storage-Room%20SQLite%20%2B%20Supabase-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%20API-blueviolet.svg)](https://ai.google.dev)

**TAMS (Tuition & Academy Management System)** ek modern, offline-first aur complete Android application hai jo Coaching Centers, Tuition Academies, Private Tutors aur Schools ke daily administration ko aasan banata hai.

---

## 🚀 Key Features / मुख्य विशेषताएं

### 1. 📌 Notice Board & Pinned Announcements (Teacher & Student)
- **Teacher Broadcast**: Teachers urgent updates, holiday notices, aur examination schedules post kar sakte hain.
- **Pinned Dashboard Banner**: High-priority notices students ke dashboard par top par highlight hote hain.
- **Batch-Wise Filtering**: Notices ko sabhi batches ke liye ya kisi specific batch ke liye targeted banaya ja sakta hai.
- **Dedicated Notice Board Tab**: Students aur teachers categories (Urgent, Exam Schedule, General, Holiday) ke according filter aur search kar sakte hain.

### 2. 💳 WhatsApp Payment Reminders with Direct UPI Links
- **One-Click WhatsApp Reminders**: Parents ko pending tuition fee dues ke automated messages bheje ja sakte hain.
- **Dynamic UPI Payment Links**:
  - `https://upiqr.in/pay/<upi_id>/<amount>` — Kisi bhi browser ya UPI app (GPay, PhonePe, Paytm, BHIM) se one-tap payment.
  - `upi://pay?pa=...` — Native deep-link instant intent launch.
- **Intelligent Phone Sanitization**: Indian mobile numbers (`+91`, `0`, spaces, dashes) automatically clean aur normalize hote hain bina kisi URL error ke.
- **Safe Fallback**: WhatsApp na hone par system share chooser aur SMS ka graceful fallback.

### 3. 📊 Monthly Payment & Revenue Dashboard
- **Financial Analytics**: Total expected revenue, collected amount, aur pending student dues ka real-time visual breakdown.
- **Payment Method Distribution**: UPI / Digital, Cash, aur Bank / Cheque collection metrics.
- **Dual Storage Engine**: Local Room SQLite database ke sath optional Supabase PostgreSQL cloud sync.
- **Biometric Lock**: Financial reports, student records aur payment collection data fingerprint ya face recognition se password-protected rehte hain.

### 4. 👥 Student Directory & Admission Management
- **Complete Profiles**: Roll number, class, batch, parents' contact details, registration date, aur active/inactive status.
- **Smart Search & Filters**: Name, roll number, phone number ya batch ke through instant filtering.
- **Instant Actions**: Single-tap phone call, WhatsApp chat, aur fee status management.

### 5. 📅 Batch Scheduling & Timetable
- Multiple batches banayein alag-alag timings (e.g., `04:00 PM - 05:00 PM`), days, aur fee structure ke sath.
- Students ko unke respective batch me enroll karein.

### 6. 📱 QR Code Attendance Matrix
- **Touchless QR Attendance**: Har student ka unique QR code generate hota hai.
- **High-Speed Scanner**: Google ML Kit scanner camera se touchless fast attendance mark karta hai.
- **Manual Sheet**: Present, Absent, aur Leave toggle karne ke liye simple tabular sheet.

### 7. 📄 Digital Fee Receipts & Identity Cards
- Printable high-resolution PDF receipts with institute header, watermark, receipt number, aur payment breakdown.
- Student ID cards with photo, barcode/QR standee, aur center branding.

### 8. 📝 Online Exams & Interactive Quizzes
- Multiple choice question (MCQ) papers schedule aur evaluate karne ka system.
- Digital study materials ke sath chapter-wise micro-quizzes.

### 9. 🤖 AI Academic Assistant (Google Gemini)
- Integrated Gemini API bot homework feedback, doubt solving, aur automated explanation provide karta hai.

### 10. ☁️ Google Drive Disaster Recovery
- WhatsApp-style automatic aur manual disaster recovery backups directly institute ke Google Drive me store hote hain.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Platform** | Android 8.0+ (API 26+) |
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose + Material Design 3 (M3) |
| **Architecture** | MVVM (Model-View-ViewModel) + Clean Architecture |
| **Local Database** | Room (SQLite) with KSP |
| **Cloud Sync** | Supabase REST + Firebase Firestore (Dual Engine) |
| **AI Integration** | Google Gemini API (Firebase AI Logic) |
| **Vision & Scanning** | Google ML Kit Barcode Scanning |
| **Security** | AndroidX Biometric (Fingerprint / Face ID / Device Credential) |
| **Testing** | JUnit 4 + Robolectric |

---

## 📁 Project Architecture & Structure

```
app/src/main/java/com/example/
├── data/
│   ├── dao/             # Room Data Access Objects (Student, Batch, FeePayment, Notice, etc.)
│   ├── database/        # Room Database configuration & Migrations
│   └── model/           # Kotlin Data Classes & Entities
├── repository/          # Unified Repository Layer (Room DB + Cloud operations)
├── security/            # BiometricSecurityManager & Screen Lock Barrier
├── services/            # Gemini AI, Supabase, Firebase, Google Drive & Notification helpers
└── ui/
    ├── components/      # Reusable UI widgets:
    │   ├── NoticeBoardComponent.kt          # Pinned notices banner & notice management
    │   ├── PaymentLoggingAndDuesComponent.kt # Dues tracking & UPI payment dialogs
    │   ├── TcmsTuitionFeesComponent.kt      # Fees billing & receipt printing
    │   ├── AdvancedCoachingSuiteComponent.kt
    │   └── StudyMaterialsGridComponent.kt
    ├── screens/         # Main application screens:
    │   ├── DashboardScreen.kt               # Central coordinator & multi-tab navigation
    │   ├── MonthlyPaymentDashboardScreen.kt # Revenue charts, dues list & WhatsApp reminders
    │   ├── StudentDirectoryScreen.kt        # Searchable roster & contact actions
    │   ├── ExamSchedulerOverlay.kt          # Test creation & evaluation
    │   ├── OnboardingAndOverlays.kt         # Registration & biometric dialogs
    │   └── CardExporter.kt                  # PDF/Image receipt & ID card generation
    ├── theme/           # Material 3 Color Schemes, Typography & Shapes
    └── viewmodel/       # AppViewModel with StateFlow reactive data bindings
```

---

## 🔨 Build & Testing

### Build APK
```bash
gradle :app:assembleDebug
```

### Run Unit Tests
```bash
gradle :app:testDebugUnitTest
```

---

## 🔒 Privacy & Permissions
- **Biometric Authentication**: Sensitive financial ledgers and student contact information can be biometrically protected.
- **Offline First**: All student rosters, batches, payment records, and notices are securely stored locally on the user's device in SQLite.
- **Least Privilege**: Only necessary Android permissions (Internet, Biometric, Camera for QR scanning) are declared.

---

## 📝 License
Proprietary software for coaching institutes, tuition teachers, and educational academy administrators.
