## 2024-05-22 - Missing Android SDK Environment
**Learning:** The current environment lacks `ANDROID_HOME` and the Android SDK, making it impossible to compile or run Android instrumentation tests or even unit tests that depend on `android.jar` (like `org.json.JSONArray`) without mocking frameworks that are not present.
**Action:** When working in this environment, rely on code analysis and write tests that *would* pass in a proper CI environment, but do not attempt to run them if they depend on Android classes. Focus on logic that can be isolated or manually verified where possible.
