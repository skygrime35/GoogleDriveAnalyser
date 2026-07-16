# Presentation & UI Documentation

The presentation layer is built entirely using **Jetpack Compose** and **Material Design 3**, utilizing a dark-mode theme by default.

## Screens

### 1. LoginScreen
* **Location:** `com.skygrime.googledriveanalyser.presentation.screens.LoginScreen`
* **Purpose:** Initial screen shown if the user is not authenticated.
* **UI Features:**
  * Modern deep blue gradient background.
  * Sleek login button that initiates the Google Auth Flow.
  * Informational notes emphasizing read-only access and local privacy.

### 2. MainDashboardScreen
* **Location:** `com.skygrime.googledriveanalyser.presentation.screens.MainDashboardScreen`
* **Purpose:** The main application screen.
* **Layout Structure:**
  * **Top Bar:** Displays application title, logged-in email, sync trigger button, and logout button.
  * **Controls:** Search text field and a sorting dropdown menu.
  * **Tab Layout:**
    * **Overview Tab:** Shows the space distribution chart, statistics, and top 10 largest files list.
    * **File Tree Tab:** Displays the interactive tree.

---

## Custom UI Components

### 1. DonutChart
* **Location:** `com.skygrime.googledriveanalyser.presentation.components.DonutChart`
* **Implementation:** Canvas-based rendering using `drawArc` with `StrokeCap.Round` and gap/spacing angles between categories.
* **Visuals:** Displays category names, percentages, and total space consumed inside a modern donut ring.

### 2. InteractiveTreeView
* **Location:** `com.skygrime.googledriveanalyser.presentation.components.InteractiveTreeView`
* **Performance Optimization:** Rather than nested rendering which degrades Compose performance, the tree is flattened on-the-fly into a list based on which node IDs are present in an `expandedNodeIds` set.
* **Visual Hierarchy:**
  * Indentation based on depth (`depth * 16.dp`).
  * Expand/collapse arrows for folders.
  * Color-coded icons based on `FileCategory` (e.g., Image/Blue, Video/Red, Document/Green, Folder/Amber).

---

## State Management

* **DriveViewModel:** Extends `AndroidViewModel` to access Application context for SQLite database initialization.
* **Reactive Pipeline:** Employs Kotlin Flow `combine` to merge flat files, search queries, and sorting criteria reactively. The final sorted and filtered tree structure is recalculated whenever any of these inputs change, providing an instantaneous UI updates.
