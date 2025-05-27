# HMI - Mental Health Interface

## Overview
HMI (Mental Health Interface) is an Android application designed to provide calming exercises and mental health support tools. The app features interactive breathing and grounding exercises to help users manage anxiety and stress.

## Features

### Calming Exercises
- **Breathing Exercise**: Guided breathing techniques to reduce anxiety and promote relaxation
- **Grounding Exercise (5-4-3-2-1)**: A step-by-step grounding technique that helps users reconnect with their surroundings and reduce dissociation or anxiety

## Technical Details

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)

### Project Structure
- **UI Components**: Located in `app/src/main/java/com/example/hmi/ui/screens/`
- **ViewModels**: Located in `app/src/main/java/com/example/hmi/ui/viewmodel/`
- **Theme Configuration**: Located in `app/src/main/java/com/example/hmi/ui/theme/`

## Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- Minimum SDK: Android 5.0 (API level 21)
- Target SDK: Android 13 (API level 33)

### Build and Run
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

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

## Development

### Adding New Exercises
To add new exercises:
1. Update the `ExerciseType` enum in the `CalmingExerciseViewModel`
2. Add UI components to the `CalmingExerciseScreen.kt` file
3. Implement the exercise logic in the ViewModel

## License
[Specify your license here]

## Contact
[Your contact information]
