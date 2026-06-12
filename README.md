# Barta Messenger App

A real-time Android messaging application built with Java and Firebase. **Barta** enables users to securely connect, chat one-to-one or in groups, share files/media, and sign in quickly using Google authentication.

---

## 1) Project Overview

**Purpose:** Build a modern messaging experience for Android with authentication, real-time communication, contact discovery, media/file sharing, notifications, and group collaboration.

---

## 2) Key Features

- 🔐 **Authentication**
  - Email/password login and signup
  - Google Sign-In (Firebase Authentication)
  - Password reset flow
- 💬 **Messaging**
  - Real-time one-to-one messaging
  - Group chat support
  - Message forwarding and deletion flows
- 📎 **File & Media Sharing**
  - Image sharing
  - Document/file sharing
  - Voice message recording and sending
- 👥 **Contacts & Social Flow**
  - Contact discovery from phone contacts
  - Add contacts and manage friend requests
- 🔔 **Notifications**
  - Firebase Cloud Messaging integration for chat notifications
- 🛡️ **Security Support**
  - Local encryption helper and key management utilities for message handling

---

## 3) Tech Stack

- **Language:** Java
- **Platform:** Android (SDK 33 target)
- **Backend / BaaS:** Firebase
  - Firebase Authentication
  - Firebase Realtime Database
  - Firebase Storage
  - Firebase Cloud Messaging
  - Firebase Firestore (included in dependencies)
- **Google Services:**
  - Google Sign-In
  - Google Drive API helpers (for integration flows)
- **UI & Libraries:**
  - AndroidX, Material Components, RecyclerView/ViewPager
  - Picasso, CircleImageView, RoundedImageView

---

## 4) Architecture (High-Level)

The app follows a practical Android app structure organized around:

- **UI Layer:** Activities, Fragments, RecyclerView Adapters
  - Examples: `HomeScreen`, `InboxActivity`, `LoginPageActivity`, `GroupInboxActivity`
- **Data & Service Layer:** Firebase auth/database/storage operations and notification services
  - Example: `FCMNotificationService`
- **Model Layer:** POJO-style domain models
  - Examples: `User`, `MessageModel`, `Contact`, `Group`
- **Utility Layer:** Encryption, local DB, and external integration helpers
  - Examples: `CryptoHelper`, `EncryptionKeyManager`, `DBHelper`, `DriveServiceHelper`

This separation keeps UI interactions, backend communication, and shared utilities modular and easier to maintain.

---

## 5) Installation & Setup

### Prerequisites

- Android Studio (latest stable recommended)
- Android SDK:
  - `compileSdk`: **33**
  - `targetSdk`: **33**
  - `minSdk`: **24**
- JDK 8+ (project uses Java 8 compatibility)
- A Firebase project
- A Google Cloud project with OAuth credentials

### Step 1: Clone the Repository

```bash
git clone https://github.com/Sohelkhan26/Barta_a_Messenger_App.git
cd Barta_a_Messenger_App
```

### Step 2: Open in Android Studio

1. Open Android Studio
2. Select **Open**
3. Choose the project directory
4. Let Gradle sync complete

### Step 3: Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create or select a Firebase project
3. Add Android app package:
   - `com.example.barta_a_messenger_app`
4. Download `google-services.json`
5. Place it at:

```text
app/google-services.json
```

6. In Firebase, enable required services:
   - Authentication (Email/Password + Google)
   - Realtime Database
   - Storage
   - Cloud Messaging

### Step 4: Google Sign-In Setup

1. In Firebase Authentication → **Sign-in method**, enable **Google**
2. Add SHA-1 (and SHA-256 if required) fingerprints for your debug/release keystore
3. In Google Cloud Console, verify OAuth 2.0 client configuration
4. Ensure web client ID is available in `app/src/main/res/values/strings.xml`:

```xml
<string name="web_client_id">YOUR_WEB_CLIENT_ID</string>
```

### Step 5: Build & Run

From Android Studio:

- Select emulator/device
- Click **Run**

From terminal (optional):

```bash
bash ./gradlew assembleDebug
```

---

## 6) Screenshots

Available project screenshots:

| Screenshot | Preview |
|---|---|
| Google/Firebase setup reference 1 | ![Setup Screenshot 1](Image/Screenshot%20(13).png) |
| Google/Firebase setup reference 2 | ![Setup Screenshot 2](Image/Screenshot%20(14).png) |

> Tip: Add chat/home/profile app UI screenshots for stronger portfolio impact.

---

## 7) Usage

1. Launch the app and create an account or log in
2. Optionally use **Google Sign-In** for quick authentication
3. Discover/add contacts and manage friend requests
4. Open a chat to send text, image, file, or voice messages
5. Create groups and chat with multiple members
6. Receive push notifications for incoming messages

---

## 8) Challenges & Solutions

- **Google Sign-In + Firebase OAuth setup complexity**
  - Solved by configuring SHA fingerprints, OAuth client, and `web_client_id` correctly.
- **Real-time sync and local message continuity**
  - Combined Firebase real-time listeners with local SQLite helpers for smoother chat persistence.
- **Media/file compatibility and dependency conflicts**
  - Managed AndroidX/support-library compatibility via dependency resolution strategy and exclusions.
- **Message security handling**
  - Added utility classes for encryption/decryption and key management flows.

---

## 9) Future Improvements

- End-to-end encryption hardening and key exchange UX improvements
- Better delivery/read receipts and typing indicators
- Rich media previews and upload progress UX
- Search across chats/messages
- Improved test coverage (unit + instrumentation)
- CI/CD pipeline for automated build and quality checks

---

## 10) Author

**Sohel Khan**  
GitHub: [@Sohelkhan26](https://github.com/Sohelkhan26)

---

If you are reviewing this project for hiring, this repository demonstrates Android app development, Firebase integration, authentication workflows, real-time data handling, and production-oriented feature integration.
