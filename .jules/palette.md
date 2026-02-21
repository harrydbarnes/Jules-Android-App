## 2024-05-22 - Empty State Pattern
**Learning:** RecyclerViews in this project lack native empty state support. The robust pattern is to wrap the RecyclerView and an empty state TextView in a FrameLayout, toggling visibility based on data availability.
**Action:** Reuse this pattern for other lists (e.g. bookmarks, history) to ensure consistency and layout stability.

## 2024-06-11 - Settings Interaction: Dynamic vs. Manual Themes
**Learning:** When a global system preference (like Dynamic Colors) overrides a manual app setting (like Theme selection), disabling the overridden setting's UI is critical for user clarity. It visually communicates the hierarchy and prevents frustration from ineffective inputs.
**Action:** Ensure conflicting settings are mutually exclusive or hierarchically disabled in the UI.
