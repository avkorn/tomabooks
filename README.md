# TomaBooks

TomaBooks is a lightweight and accessible audiobook player for Android, specifically designed for playing `.m4b` files from local storage.

## Motivation & Design Philosophy

This app was developed for my mother, who is almost blind. For users with severe visual impairment, the greatest challenge with modern smartphones is often the risk of **accidental clicks**. A single misplaced touch can navigate away from the app, stop playback, or change settings, which is difficult to recover from without sight.

TomaBooks is built with a "Safety First" approach for accessibility:
- **Prevention of Accidental Input**: The UI is designed to ignore stray touches. Critical navigation is hidden behind specific multi-tap gestures.
- **Extreme Simplification**: In its primary mode, the app eliminates complex menus in favor of massive, high-contrast interactive zones.
- **Intentionality**: Every interaction is designed to be deliberate, ensuring the user stays in control of their listening experience.

## Features

- **Blind Mode**: A specialized UI where:
    - The entire screen acts as a giant Play/Pause button.
    - High-contrast colors (Green for "Ready to Play", Red for "Playing") assist users with residual vision.
    - Settings are protected by a **quadruple-tap** requirement to prevent accidental configuration changes.
- **High-Visibility Home Screen Widget**:
    - Features a **giant adaptive clock** that scales its size based on the widget's dimensions for maximum readability.
    - Acts as a large, easy-to-hit launcher for the main application.
    - Displays the current app version for quick reference.
- **Folder-based Library**: Scan specific folders on your device to build your audiobook library using the Storage Access Framework.
- **Metadata Support**: Automatically extracts book titles, authors, and cover art from `.m4b` files.
- **Library Management**:
    - **Move to Done**: Automatically moves finished books to a `_Done` folder while preserving the original subfolder hierarchy.
    - **Permanent Delete**: Option to delete files directly from storage.
- **Modern Tech Stack**: Built with Jetpack Compose, Material 3, Media3 (ExoPlayer), and **Jetpack Glance** for the widget.
- **Localization**: Supports English and Russian.

## Getting Started

1. **Select Folder**: On first launch, enter settings to select the root folder containing your audiobooks.
2. **Toggle Blind Mode**: Enable "Blind Mode" in settings for the simplified UI.
3. **Navigation in Blind Mode**:
    - **Play/Pause**: Tap or long-press anywhere on the large central area.
    - **Settings**: To prevent accidental exit, the settings icon requires a **quadruple-tap** (4 quick taps) to open.
4. **Library**: Use the standard mode to manage your books, then switch to Blind Mode for daily listening.
5. **Widget**: Add the TomaBooks widget to your home screen to provide a massive, readable clock and a quick way to launch the player.

## Development

### Requirements
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 34+

### Build
```bash
./gradlew assembleDebug
```

## License
[Insert License Here - e.g., MIT]
