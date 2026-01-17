# WA Status Saver (Android · Kotlin · Jetpack Compose)

WA Status Saver is a Compose-first Android app that lets you browse, preview, save, share, and delete WhatsApp image and video statuses directly on-device. It is built as a clean, portfolio-ready reference for modern Android UI, MediaStore save flows, and SAF-based storage access.

## Features

- **Status grid:** Photo/video tabs with lazy grids, pull-to-refresh, and multi-select delete.
- **Immersive preview:** Fullscreen pager, pinch-to-zoom images, Media3 video playback, quick share, and one-tap save to Gallery.
- **Theming:** Material 3 + custom palette, light/dark mode toggle, cached image loading via Coil.
- **Storage-safe:** Uses Storage Access Framework (SAF) to request access to the hidden `.Statuses` folder; persists URI permissions across launches.
- **Quality-of-life:** Custom APK naming per build variant, simple cache to avoid redundant reads, basic settings screen with folder re-pick.

## Tech Stack

- Language: Kotlin 1.9.20
- UI: Jetpack Compose, Material 3, Navigation via simple sealed `Screen`
- Media: Coil (with VideoFrameDecoder) for thumbnails, Media3 ExoPlayer for playback
- Storage: SAF + `DocumentFile`, MediaStore for saving to `Pictures/StatusSaver`
- Build: AGP 8.3.0, compileSdk 34, minSdk 24

## Project Structure

- `app/src/main/java/com/deva/statussaver/` core app code
- `MainActivity` sets up theming and screen routing.
- `ui/MainViewModel` handles permission state, loading, saving, deletion, and dark mode prefs.
- `data/StatusRepository` reads statuses via SAF with cursor and a small in-memory cache.
- `data/FileSaver` writes media to MediaStore; avoids duplicates by display name.
- `ui/screens/*` Compose screens for permission, home grid, preview, and settings.
- `ui/components/VideoPlayer` Media3-backed player for videos.

## Setup

1) Clone the repo and open in Android Studio (Hedgehog+ recommended).
2) Use JDK 17 (bundled with recent Android Studio versions).
3) Build debug: `./gradlew assembleDebug` or run from Android Studio.
4) First launch: tap “Select Folder” and choose the WhatsApp Statuses folder. The helper path is pre-filled to `Android/media/com.whatsapp/WhatsApp/Media/.Statuses` (adjust for Business: `com.whatsapp.w4b`).

### Signing (release builds)

The release build type references `app/release-key.jks` with placeholder credentials. Create your own keystore or point the signing config to an existing one before assembling a release APK/AAB.

## Usage

- Open the app → grant folder access via SAF.
- Browse Photos/Videos tabs; tap to preview, long-press to multi-select and delete.
- In preview, use Share or Save (writes to Gallery under `Pictures/StatusSaver`).
- Toggle dark mode in Settings; re-pick the folder anytime.

## Privacy

All operations are local. The app only requests access to the folder you pick and does not upload or transmit media.

## Roadmap Ideas

- Add Room-backed favorites/history
- Add in-app onboarding and empty-state tips for Business/dual WhatsApp installs
- Optional dynamic color theming (Android 12+)

## License

MIT (feel free to adapt for your own portfolio/demo use).
