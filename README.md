# 🐙 Jules Loader

**Jules Loader** is a native Android application designed to provide a seamless, multi-tabbed browsing experience for [jules.google.com](https://jules.google.com). Built with Kotlin and Material Design, it allows users to navigate repositories and web content efficiently on mobile devices.

## ✨ Features

* **Multi-Tab Interface**: Browse multiple pages or repositories simultaneously using a dynamic tabbed view powered by `ViewPager2` and `TabLayout`. 📑
* **Quick Access**: Instantly load the Jules Home page with a single tap from the "New Tab" screen. 🏠
* **Recent History**: Automatically tracks and saves your recently visited repositories/URLs. The list is managed locally using `SharedPreferences`, ensuring your history persists between sessions. 🕒
* **Full WebView Support**: Features a robust `WebView` implementation with JavaScript, DOM storage, and database support enabled. It also includes a progress bar for page load status. 🌐
* **Material Design**: Clean and modern UI using Android's Material Components. 🎨

## 📱 Screenshots

| New Tab | Web View |
|:---:|:---:|
| 📷 | 📷 |
| *Recent Repos & Load Home* | *Tabbed Browsing Interface* |

## 🛠️ Tech Stack

This project is built using modern Android development practices:

* **Language**: [Kotlin](https://kotlinlang.org/) (v1.9.0)
* **Minimum SDK**: 24 (Android 7.0)
* **Target SDK**: 34 (Android 14)
* **Architecture**: Fragments & Activities
* **Key Libraries**:
    * [AndroidX AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat)
    * [Material Components](https://github.com/material-components/material-components-android)
    * [ConstraintLayout](https://developer.android.com/reference/androidx/constraintlayout/widget/ConstraintLayout)
    * [ViewPager2](https://developer.android.com/jetpack/androidx/releases/viewpager2) (for tabs)
    * [WebKit](https://developer.android.com/reference/androidx/webkit/WebView)

## 🚀 Getting Started

To build and run this app locally, follow these steps:

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/yourusername/jules-loader.git](https://github.com/yourusername/jules-loader.git)
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

1.  **Launch the App**: You will start on a "New Tab" page.
2.  **Load Content**: Click **"Load Jules Home"** to start browsing or select a repo from the "Recent Repos" list.
3.  **Manage Tabs**:
    * Use the **Top Menu** (three dots) and select **"Add Tab"** to open a new browsing session.
    * Swipe left or right to switch between open tabs.
4.  **Navigation**: The app supports standard back navigation within the WebView.

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
