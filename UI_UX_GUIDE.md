# UI/UX Guide - New Features

## 📱 Student Management Screen

### Layout Structure
```
┌─────────────────────────────────────┐
│  ← Student Management           ⚙️  │ TopAppBar (Primary Container)
├─────────────────────────────────────┤
│  ╔═══════════════════════════════╗  │
│  ║   GRADIENT HEADER (Pri→Ter)   ║  │
│  ║                               ║  │
│  ║   👤    🏫    👥              ║  │ Stats Row
│  ║   25    5     20              ║  │
│  ║Students Classes Assigned      ║  │
│  ╚═══════════════════════════════╝  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ 🔍 Search students...      ✕  │  │ Search TextField
│  └───────────────────────────────┘  │
│                                     │
│  [All] [CS101] [Math-A] [...]       │ Filter Chips
│                                     │
│  ┌─────────────────────────────────┐│
│  │  ●  John Doe                    ││ Student Card
│  │  J  #20230001                   ││ - Avatar Circle
│  │     john@email.com              ││ - Name (Bold)
│  │     [CS101-A]         ✏️  🗑️   ││ - Student #
│  └─────────────────────────────────┘│ - Email
│                                     │ - Class Chip
│  ┌─────────────────────────────────┐│ - Actions
│  │  ●  Jane Smith                  ││
│  │  J  #20230002          ✏️  🗑️  ││
│  └─────────────────────────────────┘│
│                                     │
│                                     │
│                              [+]    │ FAB (Floating)
└─────────────────────────────────────┘
```

### Color Scheme
- **Header Background**: Horizontal gradient (Primary → Tertiary)
- **Stats Icons**: White (32dp)
- **Stats Numbers**: White, Bold, Headline Medium
- **Avatar Background**: Primary Container
- **Avatar Text**: On Primary Container
- **Class Chip**: Tertiary Container
- **Edit Icon**: Primary
- **Delete Icon**: Error

### Animations
1. **Screen Enter**: Fade in + slide from top (100ms delay)
2. **Stats Appear**: Slide in vertically (sequential)
3. **Student Cards**: Staggered (50ms per item)
4. **FAB Enter**: Scale in + fade (500ms)
5. **Press Effect**: Scale to 0.95f on touch

### Empty State
```
        ┌─────────────┐
        │             │
        │     🎓      │  Large icon (80dp, 30% opacity)
        │             │
        └─────────────┘
      No students yet
   Add your first student
      to get started
```

## 📱 Class Management Screen

### Layout Structure
```
┌─────────────────────────────────────┐
│  ← Class Management             ⚙️  │ TopAppBar (Tertiary Container)
├─────────────────────────────────────┤
│  ╔═══════════════════════════════╗  │
│  ║   GRADIENT HEADER (Ter→Sec)   ║  │
│  ║                               ║  │
│  ║   🏫    👥    👨‍👩‍👧‍👦         ║  │ Stats Row
│  ║    5    25     5              ║  │
│  ║ Classes Total  Avg            ║  │
│  ║         Students Size         ║  │
│  ╚═══════════════════════════════╝  │
│                                     │
│  ┌─────────────────────────────────┐│
│  │  🏫  CS101-A                    ││ Class Card
│  │                                 ││ (Tertiary Container 30%)
│  │      Introduction to CS         ││ - Icon (40dp)
│  │                                 ││ - Name (Large, Bold)
│  │      👥 12 students              ││ - Description
│  │                    👁️ ✏️ 🗑️     ││ - Student count
│  └─────────────────────────────────┘│ - Actions (View/Edit/Delete)
│                                     │
│  ┌─────────────────────────────────┐│
│  │  🏫  Math-A                     ││
│  │      Advanced Mathematics       ││
│  │      👥 8 students  👁️ ✏️ 🗑️   ││
│  └─────────────────────────────────┘│
│                                     │
│                              [+]    │ FAB
└─────────────────────────────────────┘
```

### Color Scheme
- **Header Background**: Horizontal gradient (Tertiary → Secondary)
- **Card Background**: Tertiary Container (30% opacity)
- **Class Icon**: Tertiary (40dp)
- **View Icon**: Tertiary
- **Edit Icon**: Primary
- **Delete Icon**: Error

### View Students Dialog
```
┌─────────────────────────────────────┐
│           👥 CS101-A                │ Dialog Header
│            12 student(s)            │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────────┐│
│  │ 👤  John Doe                    ││ Student Item
│  │     #20230001                   ││ (Surface Variant)
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ 👤  Jane Smith                  ││
│  │     #20230002                   ││
│  └─────────────────────────────────┘│
│                                     │
│  [... more students ...]            │
│                                     │
│                           [Close]   │
└─────────────────────────────────────┘
```

## 🎨 Add/Edit Student Dialog

```
┌─────────────────────────────────────┐
│  👤 Add Student / Edit Student      │ Icon + Title
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Name                            ││ Text Field
│  │ John Doe                        ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Student Number                  ││
│  │ 20230001                        ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Email                           ││
│  │ john@email.com                  ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Class Section            ▼      ││ Dropdown
│  │ CS101-A                         ││
│  └─────────────────────────────────┘│
│  │ No Class                        ││ Dropdown Menu
│  │ CS101-A                    ✓    ││ (when expanded)
│  │ Math-A                          ││
│  └─────────────────────────────────┘│
│                                     │
│                  [Cancel]  [Save]   │ Actions
└─────────────────────────────────────┘
```

### Form Validation
- **All fields required** except class (optional)
- **Save button disabled** when any required field empty
- **Email format** - basic validation (contains @)
- **No duplicates check** - currently not implemented

## 🎨 Add/Edit Class Dialog

```
┌─────────────────────────────────────┐
│  🏫 Add Class / Edit Class          │ Icon + Title
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Class Name                      ││ Text Field
│  │ CS101-A                         ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Description (Optional)          ││ Multi-line
│  │ Introduction to                 ││ (max 3 lines)
│  │ Computer Science                ││
│  └─────────────────────────────────┘│
│                                     │
│                  [Cancel]  [Save]   │
└─────────────────────────────────────┘
```

### Form Validation
- **Class name required**
- **Description optional**
- **Save button disabled** when name is empty

## 🎨 Delete Confirmation Dialog

```
┌─────────────────────────────────────┐
│  ⚠️  Delete Student / Delete Class  │ Warning Icon (Error color)
├─────────────────────────────────────┤
│                                     │
│  Are you sure you want to delete    │
│  John Doe? This action cannot be    │
│  undone.                            │
│                                     │
│  --- OR ---                         │
│                                     │
│  Are you sure you want to delete    │
│  CS101-A? This will unassign        │
│  12 student(s) from this class.     │
│                                     │
│                  [Cancel] [Delete]  │ Delete = Error color
└─────────────────────────────────────┘
```

## 📊 Dashboard Updates

### New Feature Cards (6th and 7th)

```
┌─────────────────────────────────────┐
│  ┌─────┐  Students                  │ Card 6
│  │ 🎓  │  Manage student records     │
│  └─────┘  and information        →  │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  ┌─────┐  Classes                   │ Card 7
│  │ 🏫  │  Organize students into     │
│  └─────┘  class sections          → │
└─────────────────────────────────────┘
```

**Animation Sequence:**
- Card 1 (Scan): 0ms delay
- Card 2 (Grade): 100ms delay
- Card 3 (History): 200ms delay
- Card 4 (Questions): 300ms delay
- Card 5 (Results): 400ms delay
- **Card 6 (Students): 500ms delay** ← NEW
- **Card 7 (Classes): 600ms delay** ← NEW

## 🎯 Interaction Patterns

### Student Management
1. **Search**: Type in search field → Live filtering
2. **Filter**: Tap chip → Toggle class filter
3. **Add**: Tap FAB → Dialog → Fill form → Save
4. **Edit**: Tap edit icon → Dialog with pre-filled data → Save
5. **Delete**: Tap delete icon → Confirmation → Confirm
6. **Clear Search**: Tap X icon in search field

### Class Management
1. **Add**: Tap FAB → Dialog → Fill form → Save
2. **View Students**: Tap eye icon → Student list dialog
3. **Edit**: Tap edit icon → Dialog with pre-filled data → Save
4. **Delete**: Tap delete icon → Warning about unassignment → Confirm

### Navigation Flow
```
Dashboard → Students → [Add/Edit/Delete Student]
                    ↓
                [Search/Filter]
                    ↓
            [Assign to Class]

Dashboard → Classes → [Add/Edit/Delete Class]
                   ↓
             [View Students]
                   ↓
          [See Enrolled List]
```

## 📏 Sizing Reference

### Icons
- **Large Feature Icons**: 80dp (empty states)
- **Class Card Icons**: 40dp
- **Avatar Size**: 56dp
- **Stat Icons**: 32dp
- **Action Icons**: 24dp (default Material3)
- **Chip Leading Icons**: 16dp

### Text Sizes
- **Display Large**: Hero titles (Dashboard header)
- **Headline Medium**: Stat numbers (25, 12, etc.)
- **Title Large**: Screen titles in TopAppBar
- **Title Medium**: Feature card titles, student names
- **Body Medium**: Descriptions, secondary info
- **Body Small**: Tertiary info (email, timestamps)
- **Label Small**: Chip text, stat labels

### Spacing
- **Screen Padding**: 16dp
- **Card Padding**: 16-20dp
- **Card Spacing**: 8-12dp
- **Section Spacing**: 16-24dp
- **Icon-Text Spacing**: 8-16dp
- **Button Spacing**: 8dp

### Elevation
- **Cards Default**: 4dp
- **Cards Pressed**: 2dp
- **FAB**: 6dp (default Material3)
- **TopAppBar**: 3dp (via tonalElevation)
- **Dialog**: 6dp (default Material3)

## 🎨 Color Palette Usage

### Student Management
- **Primary**: Edit icons, search leading icon
- **Tertiary**: Class assignment chips, student count icon
- **Error**: Delete icons
- **Surface**: Card backgrounds
- **Primary Container**: Avatar backgrounds

### Class Management
- **Tertiary**: Main theme color, class icons, view icons
- **Secondary**: Gradient end color
- **Primary**: Edit icons
- **Error**: Delete icons
- **Surface Variant**: Student item backgrounds in dialog

### Common
- **On Surface (60-70% alpha)**: Secondary text
- **On Surface (30-40% alpha)**: Empty state text
- **White (90% alpha)**: Stat labels on gradient
- **White (100%)**: Stat numbers on gradient

## 🔄 State Management

### Student Screen States
```kotlin
var students by remember { mutableStateOf(...) }      // Full list
var classSections by remember { mutableStateOf(...) } // For dropdown
var showAddDialog by remember { mutableStateOf(false) }
var editingStudent by remember { mutableStateOf<Student?>(null) }
var searchQuery by remember { mutableStateOf("") }
var selectedClassFilter by remember { mutableStateOf<String?>(null) }
var showDeleteConfirm by remember { mutableStateOf<Student?>(null) }
var visible by remember { mutableStateOf(false) }     // For animations
```

### Class Screen States
```kotlin
var classes by remember { mutableStateOf(...) }
var showAddDialog by remember { mutableStateOf(false) }
var editingClass by remember { mutableStateOf<ClassSection?>(null) }
var viewingClass by remember { mutableStateOf<ClassSection?>(null) }
var showDeleteConfirm by remember { mutableStateOf<ClassSection?>(null) }
var visible by remember { mutableStateOf(false) }
```

## 🎬 Animation Details

### Entry Animations
```kotlin
// Screen-level fade in
LaunchedEffect(Unit) {
    delay(100)
    visible = true
}

AnimatedVisibility(
    visible = visible,
    enter = slideInVertically() + fadeIn()
)

// List items (staggered)
LaunchedEffect(Unit) {
    delay(index * 50L)
    itemVisible = true
}

AnimatedVisibility(
    visible = itemVisible,
    enter = slideInHorizontally(tween(300)) + fadeIn()
)
```

### Press Animations
```kotlin
val scale by animateFloatAsState(
    targetValue = if (pressed) 0.95f else 1f,
    animationSpec = spring(stiffness = Spring.StiffnessMedium)
)

Card(modifier = Modifier.scale(scale))
```

### FAB Animation
```kotlin
AnimatedVisibility(
    visible = visible,
    enter = scaleIn(tween(500)) + fadeIn(),
    exit = scaleOut() + fadeOut()
)
```

---

**Design System Compliance**: ✅ All screens follow Material Design 3 guidelines
**Accessibility**: ✅ Content descriptions, contrast ratios, touch targets (48dp min)
**Consistency**: ✅ Colors, spacing, typography, animations match existing screens
