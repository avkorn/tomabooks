# TomaBooks

TomaBooks is a lightweight and accessible audiobook player for Android, specifically designed for playing `.m4b` files from local storage.

## Features

- **Folder-based Library**: Scan specific folders on your device to build your audiobook library.
- **Metadata Support**: Automatically extracts book titles, authors, and cover art from `.m4b` files.
- **Advanced Playback**:
    - Precise seeking and playback position memory.
    - Customizable rewind and forward steps.
    - Playback speed control (handled via ExoPlayer).
- **Blind Mode**: A specialized, high-contrast, simplified UI with large interactive areas designed for users with limited vision.
- **Library Management**:
    - **Move to Done**: Automatically moves finished books to a `_Done` folder while preserving the original subfolder hierarchy.
    - **Permanent Delete**: Option to delete files directly from storage.
- **Modern Tech Stack**: Built with Jetpack Compose, Material 3, and Media3 (ExoPlayer).
- **Localization**: Supports English and Russian.

## Getting Started

1. **Select Folder**: On first launch, use the settings to select the root folder containing your audiobooks.
2. **Permissions**: The app uses the Storage Access Framework (SAF) to request persistent access to your chosen directory.
3. **Playback**: Tap a book in the list to start listening. Use the main screen for controls.
4. **Settings**: Triple-tap the settings icon (in Blind Mode) or tap it (in Standard Mode) to adjust step intervals or toggle Blind Mode.

## Development

### Requirements
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 34+

### Build
Run the following command to build the project:
```bash
./gradlew assembleDebug
```

## License
[Insert License Here - e.g., MIT]
