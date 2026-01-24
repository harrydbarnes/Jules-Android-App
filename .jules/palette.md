## 2024-05-22 - Empty State Pattern
**Learning:** RecyclerViews in this project lack native empty state support. The robust pattern is to wrap the RecyclerView and an empty state TextView in a FrameLayout, toggling visibility based on data availability.
**Action:** Reuse this pattern for other lists (e.g. bookmarks, history) to ensure consistency and layout stability.
