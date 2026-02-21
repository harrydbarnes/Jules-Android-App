# 🐙 Octopus for Jules

**Octopus for Jules** is a native Android application designed to interface directly with the [Jules API](https://jules.google.com). Built with Kotlin and Material Design 3, it allows users to manage sessions, create tasks, and view live activity logs efficiently on mobile devices.

## ✨ Features

* **Session Management**: View and manage your active Jules sessions in a clean list interface. 📋
* **Task Creation**: Create new tasks with custom prompts and optional source contexts directly from the app. ➕
* **Live Activity Logs**: Monitor task progress in real-time with live activity logs and status updates. ⚡
* **Repository Integration**: Easily access and attach your connected repositories to new tasks. 📦
* **Material Design 3**: A modern, cohesive UI that adapts to your device's theme. 🎨

## 📱 Screenshots

| Session List | Task Detail |
|:---:|:---:|
| 📷 | 📷 |
| *View Active Sessions* | *Monitor Live Logs* |

## 🛠️ Tech Stack

This project is built using modern Android development practices:

* **Language**: [Kotlin](https://kotlinlang.org/) (v1.9.0)
* **Minimum SDK**: 24 (Android 7.0)
* **Target SDK**: 34 (Android 14)
* **Architecture**: MVVM / Repository Pattern
* **Key Libraries**:
    * [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) (Network)
    * [Gson](https://github.com/google/gson) (JSON Parsing)
    * [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) (Async)
    * [AndroidX AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat)
    * [Material Components](https://github.com/material-components/material-components-android)
    * [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) (Security)

## 🚀 Getting Started

To build and run this app locally, follow these steps:

1.  **Clone the repository**
    ```bash
    git clone https://github.com/harrydbarnes/Jules-Android-App.git
    ```

2.  **Open in Android Studio**
    * Open Android Studio and select "Open an existing Android Studio project".
    * Navigate to the cloned directory.

3.  **Configure API Key**
    * Launch the app on a device or emulator.
    * Enter your Jules API Key on the onboarding screen.

4.  **Build the project**
    * Let Gradle sync and download dependencies.
    * Run the app.

    *Alternatively, build via command line:*
    ```bash
    ./gradlew assembleDebug
    ```

## 📖 Usage

1.  **Launch the App**: Authenticate with your API key if it's your first time.
2.  **View Sessions**: Browse your list of active and past sessions.
3.  **Create a Task**: Tap the FAB (Floating Action Button) to start a new task. Enter a prompt and select a repository context if needed.
4.  **Monitor Progress**: Tap on any session to view detailed activity logs and status updates in real-time.

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
