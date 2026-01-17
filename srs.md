# Software Requirements Specification (SRS)

## WhatsApp Status Saver – Android Application

---

### 1. Introduction

#### 1.1 Purpose

This document defines the functional, non-functional, and technical requirements for the **WhatsApp Status Saver** Android application. The app allows users to view and save WhatsApp statuses (images and videos) from their device storage into a local gallery for offline access.

#### 1.2 Scope

The application will:

* Work only on Android devices.
* Read WhatsApp status media from official WhatsApp directories using Android’s Storage Access Framework (SAF).
* Allow users to preview, save, and manage status media.
* Store saved media locally on the device.

The app is designed to be simple, lightweight, and usable by non-technical users.

#### 1.3 Target Users

* Android users who want to save WhatsApp statuses.
* Non-technical users, including elderly users.

---

### 2. Overall Description

#### 2.1 Product Perspective

This is a standalone Android application that integrates with the device file system to access WhatsApp status media. It does not integrate directly with WhatsApp APIs.

#### 2.2 User Classes

* Primary User: Casual Android user
* Secondary User: Elderly or low-tech user

#### 2.3 Operating Environment

* Android 10 and above
* Requires WhatsApp installed
* Requires file access permissions via SAF

#### 2.4 Design Constraints

* Must comply with Android scoped storage rules
* No direct file system access outside SAF
* No modification of WhatsApp data

---

### 3. Functional Requirements

#### 3.1 Initial Setup

* On first launch, app must ask user to select WhatsApp status folder using SAF.
* App must persist folder permission for future access.
* If permission is revoked, app must request again.

#### 3.2 View Statuses

* App must display all image and video statuses from selected folder.
* App must support grid and list views.
* App must support pull-to-refresh.

#### 3.3 Preview Status

* User must be able to tap a status to preview it full screen.
* Images must support zoom.
* Videos must support play, pause, and seek.

#### 3.4 Save Status

* User must be able to save selected status to local gallery.
* App must prevent duplicate saving.
* App must notify user when save is complete.

#### 3.5 Manage Saved Media

* App must have a “Saved” section.
* User must be able to view, share, and delete saved items.

#### 3.6 Error Handling

* App must show friendly error messages for:

  * Missing WhatsApp
  * Missing folder permission
  * Empty status folder

---

### 4. Non-Functional Requirements

#### 4.1 Usability

* UI must be simple and readable.
* Buttons must be large and clearly labeled.
* App must support dark and light mode.

#### 4.2 Performance

* App must load statuses in under 2 seconds for up to 500 items.
* App must not freeze during file operations.

#### 4.3 Reliability

* App must not crash on missing permissions.
* App must handle corrupted media gracefully.

#### 4.4 Security

* App must not upload or share data automatically.
* All data remains on the user’s device.

#### 4.5 Compatibility

* Supports Android 10+.
* Works with latest WhatsApp folder structure.

---

### 5. Technical Requirements

#### 5.1 Tech Stack

* Language: Kotlin
* UI: Jetpack Compose
* Architecture: MVVM + StateFlow
* Storage: Room + MediaStore
* Image Loading: Coil
* Video Playback: Media3 / ExoPlayer

#### 5.2 Permissions

* Storage Access Framework (SAF)
* Persisted URI permissions

#### 5.3 Data Handling

* Metadata stored in Room database
* Media stored in app-managed directory via MediaStore

---

### 6. User Interface Requirements

#### 6.1 Main Screen

* Tabs: Statuses | Saved
* Grid of thumbnails
* Refresh button

#### 6.2 Preview Screen

* Fullscreen view
* Save button
* Share button

#### 6.3 Settings Screen

* Change WhatsApp folder
* Clear cache
* About section

---

### 7. Future Enhancements (Optional)

* Auto-save selected contacts’ statuses
* Status categories
* Favorites
* Backup to cloud

---

### 8. Acceptance Criteria

* App can read statuses using SAF.
* App can preview images and videos.
* App can save statuses to gallery.
* App handles missing permissions safely.
* App is usable by non-technical users.
