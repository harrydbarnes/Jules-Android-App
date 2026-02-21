# 🐙 Octopus for Jules

**Octopus for Jules** is a native Android client for Jules, allowing users to view sessions and create tasks efficiently. Built with Kotlin and Material Design 3, it provides a seamless experience for managing your Jules interactions.

## ✨ Features

* **Session Management**: View your active Jules sessions with real-time status updates. 📋
* **Task Creation**: Easily create new tasks directly from the app or share text from other apps to start a task. ✍️
* **Secure Authentication**: Your Jules API Key is stored securely using `EncryptedSharedPreferences`. 🔒
* **Material Design 3**: Modern UI with dynamic color support, adapting to your device's wallpaper. 🎨
* **Dark Mode**: Full support for dark theme. 🌙

## 🛠️ Tech Stack

This project is built using modern Android development practices:

* **Language**: [Kotlin](https://kotlinlang.org/) (v1.9.0)
* **Minimum SDK**: 24 (Android 7.0)
* **Target SDK**: 34 (Android 14)
* **Architecture**: MVVM with Repository Pattern
* **Key Libraries**:
    * [Retrofit](https://square.github.io/retrofit/) (Networking)
    * [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) (Asynchronous programming)
    * [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) (Security)
    * [Material Components](https://github.com/material-components/material-components-android)
    * [Lottie](https://airbnb.io/lottie/#/android) (Animations)

## 🚀 Getting Started

To build and run this app locally, follow these steps:

1.  **Clone the repository**
    ```bash
    git clone https://github.com/harrydbarnes/Jules-Android-App.git
    ```

2.  **Open in Android Studio**
    * Open Android Studio and select "Open an existing Android Studio project".
    * Navigate to the cloned directory.

3.  **Build the project**
    * Let Gradle sync and download dependencies.
    * Run the app on an emulator or connected device.

    *Alternatively, build via command line:*
    ```bash
    ./gradlew assembleDebug
    ```

## 📖 Usage

1.  **Launch the App**: On first launch, you will be guided through the **Onboarding** flow to securely save your Jules API Key.
2.  **View Sessions**: Once set up, you will see a list of your active Jules sessions.
3.  **Create Task**: Tap the **FAB (Floating Action Button)** to create a new task.
4.  **Settings**: Access settings via the top menu to manage preferences.

## 🤝 Contributing

Contributions are welcome! If you have suggestions or bug reports, please open an issue or submit a pull request.

1.  Fork the project 🍴
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request 🔀

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

> Built with ❤️ by Harry Barnes

*Built by Harry Barnes. No affiliation to Jules or Google and any of their trademarks, only praise and admiration for the project.*
