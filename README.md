# AI Exam Grader - Android Application

A modern Android application for automatically grading exam papers using AI-powered OCR and intelligent scoring algorithms.

## 🌟 Features

### ✅ Implemented
- **📸 Answer Sheet Scanning** - Capture student answer sheets using camera with ML Kit OCR
- **🤖 Automatic Grading** - AI-powered grading for both MCQ and essay-type questions
- **📊 Results Dashboard** - View class statistics, averages, and individual scores
- **📚 Question Bank Management** - Create, edit, and delete questions with answer keys
- **👨‍🎓 Student Management** - Comprehensive student records with profile management
- **🏫 Class/Section Management** - Organize students into classes and sections
- **📋 Scan History** - View and manage all captured answer sheets
- **⚙️ Grading Workflow** - Step-by-step grading process with template support
- **🔐 Authentication System** - Secure login and registration with user roles
- **🎨 Modern UI/UX** - Material Design 3 with dark/light theme support
- **💾 Local Storage** - Persistent data storage using JSON files
- **🔄 Non-blocking OCR** - Asynchronous text recognition with coroutines
- **✨ Smooth Animations** - Staggered card animations, fade-ins, and transitions

### 🎨 Design Highlights
- **Modern Color Palette** - Vibrant primary colors with excellent contrast
- **Smooth Animations** - Theme transitions, card elevations, and crossfades
- **Gradient Backgrounds** - Eye-catching hero sections on main screens
- **Elevated Cards** - Material 3 elevated cards with dynamic shadows
- **Floating Action Button** - Quick access to scan functionality
- **Bottom Navigation** - Easy navigation between main sections

## 🛠️ Technologies Used

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern declarative UI framework
- **Material Design 3** - Latest Material Design components
- **CameraX** - Camera integration for capturing answer sheets
- **ML Kit** - On-device text recognition (OCR)
- **Coroutines** - Asynchronous programming
- **Navigation Compose** - Type-safe navigation
- **Gradle KTS** - Kotlin-based build configuration

## 📋 Prerequisites

- **Android Studio** - Arctic Fox or newer
- **Minimum SDK** - API 28 (Android 9.0)
- **Target SDK** - API 36
- **JDK** - Version 11 or higher

## 🚀 Setup & Installation

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd FinproApp
```

### 2. Open in Android Studio
- Open Android Studio
- Select "Open an Existing Project"
- Navigate to the `FinproApp` folder
- Wait for Gradle sync to complete

### 3. Build the Project
```powershell
# Using Gradle wrapper (Windows)
.\gradlew.bat assembleDebug

# Or build directly in Android Studio
# Build > Make Project (Ctrl+F9)
```

### 4. Run the App
- Connect an Android device or start an emulator
- Click "Run" (Shift+F10) in Android Studio
- Or use command line:
```powershell
.\gradlew.bat installDebug
```

## 📱 Required Permissions

The app requires the following permissions:

- **CAMERA** - For capturing answer sheets
- **WRITE_EXTERNAL_STORAGE** - For saving exported reports (API < 29)

Permissions are requested at runtime when needed.

## 🎯 Usage Guide

### Authentication
1. **First Launch** - Demo account is automatically created
   - Email: `teacher@demo.com`
   - Password: `demo123`
2. **Register** - Create a new account with email and password
3. **Login** - Sign in with your credentials
4. **User Menu** - Access via profile icon in top bar (view info, logout)

### Student Management
1. Navigate to "Students" from the dashboard
2. **Add Student** - Tap the + FAB button
   - Enter name, student number, and email
   - Optionally assign to a class section
3. **Edit Student** - Tap the edit icon on any student card
4. **Delete Student** - Tap the delete icon with confirmation
5. **Search & Filter** - Use search bar or class filter chips
6. **View Stats** - See total students, classes, and assignments in header

### Class Management
1. Navigate to "Classes" from the dashboard
2. **Create Class** - Tap the + FAB button
   - Enter class name (e.g., "CS101-A")
   - Add optional description
3. **Edit Class** - Tap the edit icon on any class card
4. **View Students** - Tap the eye icon to see enrolled students
5. **Delete Class** - Removes class and unassigns all students
6. **Student Count** - Automatically updated when students are assigned

### Scanning Answer Sheets
1. Tap the camera icon (FAB) or navigate to "Scan" tab
2. Grant camera permission if prompted
3. Point camera at the answer sheet
4. Tap "Capture & Recognize"
5. Wait for ML Kit to extract text
6. Scanned data is automatically saved

### Grading Workflow
1. Navigate to "Grade Exam" from dashboard
2. **Select Scan** - Choose from available scanned answer sheets
3. **Enter IDs** - Input student ID and exam ID
4. **Grade** - Tap "Grade Exam" to process
5. **View Result** - Score is displayed with success animation
6. **Auto-Save** - Results are automatically saved to database

### Viewing Results
1. Navigate to "Results" tab
2. **Filter** - Use filter chips to view specific exams
3. **Statistics** - View average score, highest score, and passing rate
4. **Score Distribution** - Visual chart showing grade distribution (A-F)
5. **Individual Results** - Scroll through all exam results

### Managing Questions
1. Navigate to "Question Bank"
2. **Add Question** - Tap the + FAB
   - Enter question text
   - Select type (MCQ or Essay)
   - Add options for MCQ
   - Set correct answer
   - Assign points/weight
   - Add explanation (optional)
3. **Edit Question** - Tap edit icon on any question card
4. **Delete Question** - Tap delete icon with confirmation
5. **Type Badges** - Questions show MCQ/Essay badges with color coding

### Scan History
1. Navigate to "Scan History" from dashboard
2. View all captured scans with timestamps
3. **View Details** - Tap to see full recognized text
4. **Delete Scan** - Remove unwanted scans
5. **Preview** - See first 100 characters of recognized text

## 📂 Project Structure

```
app/src/main/java/com/example/myapplication/
├── MainActivity.kt                      # Main entry point with auth flow and theme
├── data/
│   ├── Models.kt                       # Core data models (Question, ExamResult, ScanRecord)
│   ├── User.kt                         # User authentication models (User, UserSession)
│   ├── StudentModels.kt                # Student management models (Student, ClassSection, AnswerKeyTemplate)
│   └── Repository.kt                   # JSON-based local storage with CRUD operations
├── grading/
│   └── Grader.kt                       # Grading engine (MCQ + essay cosine similarity)
├── ocr/
│   └── TextRecognitionHelper.kt        # ML Kit wrapper with image preprocessing
├── ui/
│   ├── navigation/
│   │   └── AppNavHost.kt               # Navigation graph with Scaffold, TopBar, BottomNav
│   ├── screens/
│   │   ├── LoginScreen.kt              # Authentication - login
│   │   ├── RegisterScreen.kt           # Authentication - registration
│   │   ├── DashboardScreen.kt          # Main dashboard with gradient hero and stats
│   │   ├── StudentManagementScreen.kt  # Student CRUD with search and filtering
│   │   ├── ClassManagementScreen.kt    # Class/Section management with student view
│   │   ├── ScanScreen.kt               # Camera + OCR with handwriting tips
│   │   ├── ScanHistoryScreen.kt        # Scan management with view/delete
│   │   ├── QuestionBankScreen.kt       # Question CRUD with type badges
│   │   ├── GradingWorkflowScreen.kt    # Step-by-step grading process
│   │   └── ResultsScreen.kt            # Analytics with filtering and charts
│   ├── components/
│   │   ├── AddQuestionDialog.kt        # Dialog for creating questions
│   │   └── EditQuestionDialog.kt       # Dialog for editing questions
│   └── theme/
│       ├── Color.kt                    # Material 3 color schemes (light/dark)
│       ├── Shape.kt                    # Rounded corner shapes
│       ├── Theme.kt                    # Material 3 theme configuration
│       └── Type.kt                     # Typography definitions
```

## 🎨 Theming

The app supports both light and dark themes:

- **Toggle theme** - Tap the sun/moon icon in the top app bar
- **Smooth transitions** - Animated crossfade between themes
- **Persistent preference** - Theme choice is saved across app restarts

### Color Palette

**Light Mode:**
- Primary: Bright Blue (#3B82F6)
- Secondary: Purple (#8B5CF6)
- Tertiary: Emerald Green (#10B981)

**Dark Mode:**
- Primary: Light Blue (#60A5FA)
- Secondary: Light Purple (#A78BFA)
- Tertiary: Light Emerald (#34D399)

## 🔄 Data Storage

All data is stored locally in JSON format:

- **users.json** - User accounts with encrypted passwords (SHA-256)
- **students.json** - Student profiles with contact information
- **class_sections.json** - Class/section definitions with teacher assignments
- **answer_keys.json** - Answer key templates for exams
- **questions.json** - Question bank with answers and weights
- **scans.json** - Captured answer sheet scans with OCR text
- **results.json** - Graded exam results with detailed scoring

Location: `/data/data/com.example.myapplication/files/`

### Data Models

**Student:**
- id, name, studentNumber, email
- classSection (optional foreign key)
- createdAt timestamp

**ClassSection:**
- id, name, description
- teacherId (foreign key to User)
- studentCount (auto-calculated)
- createdAt timestamp

**AnswerKeyTemplate:**
- id, name, examId
- questions (list of QuestionAnswer)
- totalPoints, passingScore
- createdAt timestamp

**User:**
- id, name, email, password (hashed)
- role (TEACHER, STUDENT, ADMIN)
- createdAt timestamp

## 🧪 Grading Algorithm

### MCQ Questions
- Exact string match (case-insensitive)
- Full points if correct, zero if incorrect

### Essay Questions
- Cosine similarity over token frequencies
- Score scaled by question weight
- Similarity range: 0.0 to 1.0

## 🚧 Planned Features

- [ ] Answer Key Template UI for quick exam setup
- [ ] Bulk import students from CSV
- [ ] Export results to PDF/CSV
- [ ] Performance analytics charts (student progress over time)
- [ ] Class comparison reports
- [ ] Firebase cloud sync and backup
- [ ] Student profile photos
- [ ] Advanced search and sorting
- [ ] Barcode/QR code scanning for student IDs
- [ ] Email notifications for graded exams
- [ ] Multi-language support
- [ ] Offline mode improvements
- [ ] Answer sheet image cropping and enhancement
- [ ] Batch scanning mode

## 🐛 Known Issues

- Some deprecated APIs (LocalLifecycleOwner) - to be migrated
- Coroutine opt-in warnings - safe to ignore or annotate
- Icon deprecations (ListAlt, TrendingUp) - use AutoMirrored versions

## 📄 License

This project is for educational purposes.

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📞 Support

For issues or questions, please open an issue in the repository.

---

**Built with ❤️ using Jetpack Compose and Material Design 3**
