# HMI - Mental Health Interface

## Overview
HMI (Mental Health Interface) is an Android application designed to provide mental health support tools for individuals experiencing anxiety, stress, and other mental health conditions. The app features interactive calming exercises and specialized assistance tools for people living with conditions like schizophrenia, helping users manage symptoms and improve their daily functioning.

## Features

### Calming Exercises
- **Breathing Exercise**: Guided breathing techniques to reduce anxiety and promote relaxation
- **Grounding Exercise (5-4-3-2-1)**: A step-by-step grounding technique that helps users reconnect with their surroundings and reduce dissociation or anxiety

### Human Detection Scanner
- **Reality Verification Tool**: Camera-based scanning that helps users with schizophrenia distinguish between real people and potential hallucinations
- **Confidence Indicators**: Visual feedback showing detection confidence levels to support reality testing
- **Privacy-Focused**: All processing happens on-device with no data storage by default
- **Clinician Collaboration**: Optional image saving for reviewing with healthcare providers

## Technical Details

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Computer Vision**: ML Kit for human detection and pose estimation
- **Camera**: CameraX API for camera integration

### Project Structure
- **UI Components**: Located in `app/src/main/java/com/example/hmi/ui/screens/`
- **ViewModels**: Located in `app/src/main/java/com/example/hmi/ui/viewmodel/`
- **Theme Configuration**: Located in `app/src/main/java/com/example/hmi/ui/theme/`
- **ML Models**: Located in `app/src/main/assets/ml/`
- **Utilities**: Located in `app/src/main/java/com/example/hmi/utils/`

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Minimum SDK: Android 8.0 (API level 26)
- Target SDK: Android 14 (API level 34)
- Camera permissions for human detection features
- ML Kit dependencies for computer vision functionality

### Build and Run
1. Clone the repository:
   ```
   https://github.com/SCHIZOS-Group-HMI/Front-end.git
   ```
2. Open the project in Android Studio
3. Sync Gradle files and resolve dependencies
4. Add your ML Kit API key in the `local.properties` file (if required):
   ```
   ml_kit_api_key=your_api_key_here
   ```
5. Run the app on an emulator (for basic features) or physical device (recommended for camera-based features)

## Usage

### Calming Exercise Screen
The main screen provides two exercise options:

1. **Breathing Exercise**:
   - Select "Breathing" from the top menu
   - Follow the on-screen instructions for guided breathing

2. **Grounding Exercise**:
   - Select "Grounding" from the top menu
   - Follow the 5-4-3-2-1 technique steps
   - Use the "Next" button to progress through the steps
   - Use "Restart" to begin the exercise again

### Human Detection Screen
The human detection scanner provides reality verification support for individuals experiencing visual hallucinations:

1. **Using the Scanner**:
   - Access the scanner from the main menu
   - Point the camera at the person or area in question
   - The app will analyze the scene in real-time using on-device machine learning
   - Real human presence will be indicated with a green outline and confidence percentage
   - Multiple detection capability for group settings
   - Empty spaces will show no detection indicators

2. **Reality Testing Features**:
   - Enable verbalized feedback for additional auditory confirmation ("Person detected" / "No person detected")
   - Toggle between high sensitivity and standard detection modes based on environmental conditions
   - Adjustable confidence threshold to reduce false positives
   - Save snapshots (optional and privacy-protected) for later review with healthcare providers
   - Historical log showing detection events with timestamps

## Development

### Adding New Exercises
To add new exercises:
1. Update the `ExerciseType` enum in the `CalmingExerciseViewModel`
2. Add UI components to the `CalmingExerciseScreen.kt` file
3. Implement the exercise logic in the ViewModel

### Enhancing Human Detection
To improve the human detection functionality:
1. Update ML models in the `assets/ml` directory with newer TensorFlow Lite models
2. Modify detection parameters in `HumanDetectionViewModel.kt` to adjust confidence thresholds
3. Calibrate sensitivity settings in the detector configuration for different lighting conditions
4. Implement additional pose estimation features by extending the `PoseDetector` class
5. Add new audio feedback options in `FeedbackManager.kt`

## Privacy & Security

HMI takes user privacy very seriously, especially given the sensitive nature of mental health applications:

- All human detection processing happens on-device
- No images are uploaded to external servers
- Optional image saving requires explicit user consent
- Data is encrypted on-device
- No personal information is collected without consent

## License
MIT License

## Contributing
Contributions to improve HMI are welcome. Please feel free to submit pull requests or create issues for bugs and feature requests.

## Contact
For support or inquiries, please contact the development team at: [Your contact information]
