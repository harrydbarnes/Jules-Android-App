## 2025-01-24 - Unrestricted WebView File Access
**Vulnerability:** WebView was configured with JavaScript enabled but without explicitly disabling file and content access.
**Learning:** Even though newer Android versions disable file access by default, older versions (within the supported minSdk range) leave it enabled. Explicitly setting these to false is crucial for defense-in-depth across all supported API levels.
**Prevention:** Always explicitly configure WebView settings for `allowFileAccess`, `allowContentAccess`, `allowFileAccessFromFileURLs`, and `allowUniversalAccessFromFileURLs` to `false` when `javaScriptEnabled` is true.
