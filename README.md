
# Study Shield — Android starter project

## Included
- Native Android Kotlin project
- Beautiful dark/Naruto-inspired home/session UI
- Adjustable timer: 1–720 minutes
- Pause/resume and restart
- PIN-protected early exit
- Study streak counter
- Undated daily planner + to-do notes
- Theme selector foundation
- AccessibilityService foundation for app blocking
- Music-app allowlist storage
- Education-channel allowlist storage
- Education website/channel architecture notes

## Build
Open this folder in Android Studio and let Gradle sync. Build the debug APK from:
Build > Build Bundle(s) / APK(s) > Build APK(s)

## Important technical limitation
Android does not give an ordinary third-party app a supported API to modify the official YouTube app so that arbitrary channels are the only channels it can display. A robust design should block the normal YouTube app during study mode and provide an education-only video surface that you control.

The AccessibilityService is intentionally limited to detecting blocked app windows and returning the user to Study Shield. The user must explicitly enable the accessibility service in Android settings.

This is a starter implementation rather than a signed Play Store release. Before public distribution, the permission declarations, privacy disclosures, accessibility use, background behavior, and device-specific behavior should be reviewed against current Android/Play policies.
