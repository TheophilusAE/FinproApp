package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object Repository {
    private var baseDir: File? = null

    fun init(context: Context) {
        baseDir = context.filesDir
    }

    private fun questionsFile(): File = File(baseDir, "questions.json")
    private fun scansFile(): File = File(baseDir, "scans.json")
    private fun resultsFile(): File = File(baseDir, "results.json")
    private fun usersFile(): File = File(baseDir, "users.json")
    private fun studentsFile(): File = File(baseDir, "students.json")
    private fun classSectionsFile(): File = File(baseDir, "class_sections.json")
    private fun answerKeysFile(): File = File(baseDir, "answer_keys.json")

    // User Management
    fun saveUser(user: User) {
        val users = loadUsers().toMutableList()
        users.add(user)
        saveUsers(users)
    }
    
    fun saveUsers(list: List<User>) {
        val array = JSONArray()
        list.forEach { u ->
            val obj = JSONObject()
            obj.put("id", u.id)
            obj.put("email", u.email)
            obj.put("password", u.password)
            obj.put("name", u.name)
            obj.put("role", u.role.name)
            obj.put("createdAt", u.createdAt)
            array.put(obj)
        }
        usersFile().writeText(array.toString(2))
    }
    
    fun loadUsers(): List<User> {
        val file = usersFile()
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            val out = mutableListOf<User>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                out.add(User(
                    id = obj.optString("id"),
                    email = obj.optString("email"),
                    password = obj.optString("password"),
                    name = obj.optString("name"),
                    role = try { UserRole.valueOf(obj.optString("role")) } catch (e: Exception) { UserRole.TEACHER },
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                ))
            }
            out
        } catch (e: Exception) { emptyList() }
    }
    
    fun getUserByEmail(email: String): User? {
        return loadUsers().find { it.email.equals(email, ignoreCase = true) }
    }
    
    fun validateLogin(email: String, password: String): User? {
        val user = getUserByEmail(email)
        return if (user?.password == password) user else null
    }

    fun saveQuestions(list: List<Question>) {
        val array = JSONArray()
        list.forEach { q ->
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("text", q.text)
            obj.put("type", q.type.name)
            obj.put("options", JSONArray(q.options))
            obj.put("answer", q.answer)
            obj.put("weight", q.weight)
            obj.put("explanation", q.explanation)
            array.put(obj)
        }
        val file = questionsFile()
        file.writeText(array.toString(2))
    }

    fun loadQuestions(): List<Question> {
        val file = questionsFile()
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            val array = JSONArray(text)
            val out = mutableListOf<Question>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val options = mutableListOf<String>()
                val opts = obj.optJSONArray("options")
                if (opts != null) {
                    for (j in 0 until opts.length()) options.add(opts.getString(j))
                }
                val q = Question(
                    id = obj.optString("id"),
                    text = obj.optString("text"),
                    type = try { QuestionType.valueOf(obj.optString("type")) } catch (e: Exception) { QuestionType.MCQ },
                    options = options,
                    answer = obj.optString("answer"),
                    weight = obj.optDouble("weight", 1.0),
                    explanation = if (obj.has("explanation")) obj.optString("explanation") else null
                )
                out.add(q)
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveScan(record: ScanRecord) {
        val list = loadScans().toMutableList()
        list.add(record)
        saveScans(list)
    }
    
    fun updateScanWithStudent(scanId: String, studentId: String) {
        val scans = loadScans().toMutableList()
        val index = scans.indexOfFirst { it.id == scanId }
        if (index >= 0) {
            scans[index] = scans[index].copy(studentId = studentId)
            saveScans(scans)
        }
    }

    fun saveScans(list: List<ScanRecord>) {
        val arr = org.json.JSONArray()
        list.forEach { r ->
            val o = org.json.JSONObject()
            o.put("id", r.id)
            o.put("filePath", r.filePath)
            o.put("recognizedText", r.recognizedText)
            o.put("studentId", r.studentId)
            o.put("timestamp", r.timestamp)
            o.put("accuracy", r.accuracy)
            o.put("confidenceLevel", r.confidenceLevel)
            o.put("enhancedByAI", r.enhancedByAI)
            arr.put(o)
        }
        scansFile().writeText(arr.toString(2))
    }

    fun loadScans(): List<ScanRecord> {
        val f = scansFile()
        if (!f.exists()) return emptyList()
        return try {
            val arr = org.json.JSONArray(f.readText())
            val out = mutableListOf<ScanRecord>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(ScanRecord(
                    id = o.optString("id"),
                    filePath = o.optString("filePath"),
                    recognizedText = o.optString("recognizedText"),
                    studentId = o.optString("studentId").ifEmpty { null },
                    timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                    accuracy = if (o.has("accuracy") && !o.isNull("accuracy")) o.getDouble("accuracy").toFloat() else null,
                    confidenceLevel = o.optString("confidenceLevel", "Medium"),
                    enhancedByAI = o.optBoolean("enhancedByAI", false)
                ))
            }
            out
        } catch (e: Exception) { emptyList() }
    }

    fun saveExamResult(result: ExamResult) {
        val list = loadExamResults().toMutableList()
        list.add(result)
        val arr = org.json.JSONArray()
        list.forEach { r ->
            val o = org.json.JSONObject()
            o.put("studentId", r.studentId)
            o.put("examId", r.examId)
            o.put("totalScore", r.totalScore)
            val details = org.json.JSONObject()
            r.details.forEach { (k, v) -> details.put(k, v) }
            o.put("details", details)
            arr.put(o)
        }
        resultsFile().writeText(arr.toString(2))
    }

    fun loadExamResults(): List<ExamResult> {
        val f = resultsFile()
        if (!f.exists()) return emptyList()
        return try {
            val arr = org.json.JSONArray(f.readText())
            val out = mutableListOf<ExamResult>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val details = mutableMapOf<String, Double>()
                val d = o.optJSONObject("details")
                if (d != null) {
                    val keys = d.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        details[key] = d.optDouble(key, 0.0)
                    }
                }
                out.add(ExamResult(
                    studentId = o.optString("studentId"),
                    examId = o.optString("examId"),
                    totalScore = o.optDouble("totalScore", 0.0),
                    details = details
                ))
            }
            out
        } catch (e: Exception) { emptyList() }
    }

    // Student Management
    fun saveStudents(list: List<Student>) {
        val arr = JSONArray()
        list.forEach { student ->
            val obj = JSONObject()
            obj.put("id", student.id)
            obj.put("name", student.name)
            obj.put("studentNumber", student.studentNumber)
            obj.put("email", student.email)
            obj.put("classSection", student.classSection)
            obj.put("createdAt", student.createdAt)
            arr.put(obj)
        }
        studentsFile().writeText(arr.toString(2))
    }

    fun loadStudents(): List<Student> {
        val f = studentsFile()
        if (!f.exists()) return emptyList()
        return try {
            val arr = JSONArray(f.readText())
            val out = mutableListOf<Student>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                out.add(Student(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    studentNumber = obj.optString("studentNumber"),
                    email = obj.optString("email"),
                    classSection = obj.optString("classSection").takeIf { it.isNotEmpty() },
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                ))
            }
            out
        } catch (e: Exception) { emptyList() }
    }

    fun saveStudent(student: Student) {
        val list = loadStudents().toMutableList()
        val index = list.indexOfFirst { it.id == student.id }
        if (index >= 0) {
            list[index] = student
        } else {
            list.add(student)
        }
        saveStudents(list)
    }

    fun deleteStudent(studentId: String) {
        val list = loadStudents().filter { it.id != studentId }
        saveStudents(list)
    }

    fun getStudentsByClass(classId: String): List<Student> {
        return loadStudents().filter { it.classSection == classId }
    }

    // Class Section Management
    fun saveClassSections(list: List<ClassSection>) {
        val arr = JSONArray()
        list.forEach { cls ->
            val obj = JSONObject()
            obj.put("id", cls.id)
            obj.put("name", cls.name)
            obj.put("description", cls.description)
            obj.put("teacherId", cls.teacherId)
            obj.put("studentCount", cls.studentCount)
            obj.put("createdAt", cls.createdAt)
            arr.put(obj)
        }
        classSectionsFile().writeText(arr.toString(2))
    }

    fun loadClassSections(): List<ClassSection> {
        val f = classSectionsFile()
        if (!f.exists()) return emptyList()
        return try {
            val arr = JSONArray(f.readText())
            val out = mutableListOf<ClassSection>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                out.add(ClassSection(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    description = obj.optString("description").takeIf { it.isNotEmpty() },
                    teacherId = obj.optString("teacherId"),
                    studentCount = obj.optInt("studentCount", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                ))
            }
            out
        } catch (e: Exception) { emptyList() }
    }

    fun saveClassSection(classSection: ClassSection) {
        val list = loadClassSections().toMutableList()
        val index = list.indexOfFirst { it.id == classSection.id }
        if (index >= 0) {
            list[index] = classSection
        } else {
            list.add(classSection)
        }
        saveClassSections(list)
    }

    fun deleteClassSection(classId: String) {
        val list = loadClassSections().filter { it.id != classId }
        saveClassSections(list)
    }

    fun updateClassStudentCount(classId: String) {
        val count = getStudentsByClass(classId).size
        val list = loadClassSections().toMutableList()
        val index = list.indexOfFirst { it.id == classId }
        if (index >= 0) {
            list[index] = list[index].copy(studentCount = count)
            saveClassSections(list)
        }
    }

    // Answer Key Template Management
    fun saveAnswerKeyTemplates(list: List<AnswerKeyTemplate>) {
        val arr = JSONArray()
        list.forEach { template ->
            val obj = JSONObject()
            obj.put("id", template.id)
            obj.put("name", template.name)
            obj.put("examId", template.examId)
            
            val questionsArr = JSONArray()
            template.questions.forEach { qa ->
                val qaObj = JSONObject()
                qaObj.put("questionId", qa.questionId)
                qaObj.put("correctAnswer", qa.correctAnswer)
                qaObj.put("points", qa.points)
                questionsArr.put(qaObj)
            }
            obj.put("questions", questionsArr)
            
            obj.put("totalPoints", template.totalPoints)
            obj.put("passingScore", template.passingScore)
            obj.put("createdAt", template.createdAt)
            arr.put(obj)
        }
        answerKeysFile().writeText(arr.toString(2))
    }

    fun loadAnswerKeyTemplates(): List<AnswerKeyTemplate> {
        val f = answerKeysFile()
        if (!f.exists()) return emptyList()
        return try {
            val arr = JSONArray(f.readText())
            val out = mutableListOf<AnswerKeyTemplate>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                
                val questions = mutableListOf<QuestionAnswer>()
                val questionsArr = obj.optJSONArray("questions")
                if (questionsArr != null) {
                    for (j in 0 until questionsArr.length()) {
                        val qaObj = questionsArr.getJSONObject(j)
                        questions.add(QuestionAnswer(
                            questionId = qaObj.optString("questionId"),
                            correctAnswer = qaObj.optString("correctAnswer"),
                            points = qaObj.optDouble("points", 1.0)
                        ))
                    }
                }
                
                out.add(AnswerKeyTemplate(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    examId = obj.optString("examId"),
                    questions = questions,
                    totalPoints = obj.optDouble("totalPoints", 0.0),
                    passingScore = obj.optDouble("passingScore", 0.0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                ))
            }
            out
        } catch (e: Exception) { emptyList() }
    }

    fun saveAnswerKeyTemplate(template: AnswerKeyTemplate) {
        val list = loadAnswerKeyTemplates().toMutableList()
        val index = list.indexOfFirst { it.id == template.id }
        if (index >= 0) {
            list[index] = template
        } else {
            list.add(template)
        }
        saveAnswerKeyTemplates(list)
    }

    fun deleteAnswerKeyTemplate(templateId: String) {
        val list = loadAnswerKeyTemplates().filter { it.id != templateId }
        saveAnswerKeyTemplates(list)
    }

    fun getTemplateByExamId(examId: String): AnswerKeyTemplate? {
        return loadAnswerKeyTemplates().firstOrNull { it.examId == examId }
    }
    
    // Initialize demo data for first-time users
    fun initializeDemoData(context: Context) {
        try {
            val prefs = context.getSharedPreferences("finpro_app_prefs", Context.MODE_PRIVATE)
            val currentDataVersion = prefs.getInt("demo_data_version", 0)
            val DEMO_DATA_VERSION = 2 // Increment this when demo data changes
            
            // Force reinitialize if version changed or data is empty
            val currentQuestions = loadQuestions()
            android.util.Log.d("Repository", "Current questions count: ${currentQuestions.size}")
            android.util.Log.d("Repository", "Demo data version: $currentDataVersion (expected: $DEMO_DATA_VERSION)")
            
            if (currentQuestions.isEmpty() || currentDataVersion < DEMO_DATA_VERSION) {
                android.util.Log.d("Repository", "Initializing demo questions (version $DEMO_DATA_VERSION)...")
                // Add comprehensive sample questions with various types
            val sampleQuestions = listOf(
                // MCQ Questions
                Question(
                    id = "q1",
                    text = "What is the capital of Indonesia?",
                    type = QuestionType.MCQ,
                    options = listOf("Jakarta", "Bandung", "Surabaya", "Medan"),
                    answer = "Jakarta",
                    weight = 10.0,
                    explanation = "Jakarta is the capital and largest city of Indonesia"
                ),
                Question(
                    id = "q2",
                    text = "What is the largest planet in our solar system?",
                    type = QuestionType.MCQ,
                    options = listOf("Earth", "Jupiter", "Saturn", "Mars"),
                    answer = "Jupiter",
                    weight = 10.0,
                    explanation = "Jupiter is the largest planet"
                ),
                Question(
                    id = "q3",
                    text = "Who painted the Mona Lisa?",
                    type = QuestionType.MCQ,
                    options = listOf("Leonardo da Vinci", "Michelangelo", "Raphael", "Donatello"),
                    answer = "Leonardo da Vinci",
                    weight = 10.0,
                    explanation = "Leonardo da Vinci painted the Mona Lisa in the early 16th century"
                ),
                Question(
                    id = "q4",
                    text = "What is 25 x 4?",
                    type = QuestionType.MCQ,
                    options = listOf("90", "100", "110", "120"),
                    answer = "100",
                    weight = 10.0,
                    explanation = "25 multiplied by 4 equals 100"
                ),
                
                // Short Text Questions
                Question(
                    id = "q5",
                    text = "Define photosynthesis in your own words.",
                    type = QuestionType.SHORT_TEXT,
                    options = emptyList(),
                    answer = "process where plants convert sunlight into energy using chlorophyll",
                    weight = 15.0,
                    explanation = "Photosynthesis is the process by which plants use sunlight to produce food"
                ),
                Question(
                    id = "q6",
                    text = "Describe the importance of recycling.",
                    type = QuestionType.SHORT_TEXT,
                    options = emptyList(),
                    answer = "reduces waste saves resources protects environment conserves energy",
                    weight = 15.0,
                    explanation = "Recycling helps reduce waste, conserve natural resources, and protect the environment"
                ),
                Question(
                    id = "q7",
                    text = "What are the three states of matter?",
                    type = QuestionType.SHORT_TEXT,
                    options = emptyList(),
                    answer = "solid liquid gas",
                    weight = 12.0,
                    explanation = "The three basic states of matter are solid, liquid, and gas"
                ),
                Question(
                    id = "q8",
                    text = "Name three renewable energy sources.",
                    type = QuestionType.SHORT_TEXT,
                    options = emptyList(),
                    answer = "solar wind hydroelectric geothermal biomass",
                    weight = 12.0,
                    explanation = "Renewable energy sources include solar, wind, and hydroelectric power"
                ),
                
                // Long Text / Essay Questions
                Question(
                    id = "q9",
                    text = "Write a paragraph explaining the water cycle.",
                    type = QuestionType.LONG_TEXT,
                    options = emptyList(),
                    answer = "The water cycle is a continuous process. Water evaporates from oceans and lakes, forms clouds through condensation, falls as precipitation like rain or snow, and returns to water bodies through runoff. This cycle is essential for distributing water across Earth and supporting all life forms.",
                    weight = 20.0,
                    explanation = "The water cycle involves evaporation, condensation, precipitation, and collection"
                ),
                Question(
                    id = "q10",
                    text = "Explain how volcanoes are formed and why they erupt.",
                    type = QuestionType.LONG_TEXT,
                    options = emptyList(),
                    answer = "Volcanoes form when molten rock, called magma, rises from deep within the Earth. This happens at tectonic plate boundaries where plates collide or separate. When pressure builds up from gases in the magma, it forces its way through weak spots in the Earth's crust, causing an eruption. Lava, ash, and gases are expelled during eruptions.",
                    weight = 25.0,
                    explanation = "Volcanoes form at plate boundaries due to rising magma and erupt when pressure builds"
                ),
                Question(
                    id = "q11",
                    text = "Discuss the impact of climate change on our planet.",
                    type = QuestionType.ESSAY,
                    options = emptyList(),
                    answer = "Climate change has significant impacts on Earth. Rising temperatures cause ice caps to melt, leading to sea level rise. Extreme weather events become more frequent. Ecosystems are disrupted as species struggle to adapt. Agricultural patterns change, affecting food security. Addressing climate change requires global cooperation and sustainable practices.",
                    weight = 30.0,
                    explanation = "Climate change affects temperature, sea levels, weather patterns, and ecosystems"
                ),
                Question(
                    id = "q12",
                    text = "Describe the importance of education in society.",
                    type = QuestionType.ESSAY,
                    options = emptyList(),
                    answer = "Education is fundamental to societal progress. It empowers individuals with knowledge and skills necessary for personal and professional growth. Education promotes critical thinking, creativity, and problem-solving abilities. It helps reduce poverty, improves health outcomes, and strengthens democracy. A well-educated population drives innovation and economic development.",
                    weight = 30.0,
                    explanation = "Education is essential for individual empowerment and societal development"
                )
            )
            saveQuestions(sampleQuestions)
            android.util.Log.d("Repository", "Saved ${sampleQuestions.size} demo questions")
            
            // Update version after successful initialization
            prefs.edit().putInt("demo_data_version", DEMO_DATA_VERSION).apply()
            android.util.Log.d("Repository", "Demo data version updated to $DEMO_DATA_VERSION")
        } else {
            android.util.Log.d("Repository", "Questions already exist and version is current, skipping initialization")
        }
        
        val currentStudents = loadStudents()
        android.util.Log.d("Repository", "Current students count: ${currentStudents.size}")
        
        if (currentStudents.isEmpty() || currentDataVersion < DEMO_DATA_VERSION) {
            android.util.Log.d("Repository", "Initializing demo students...")
            // Add sample students
            val sampleStudents = listOf(
                Student(
                    id = "s1",
                    name = "Ahmad Rizki",
                    studentNumber = "2024001",
                    email = "ahmad.rizki@student.com",
                    classSection = "class1"
                ),
                Student(
                    id = "s2",
                    name = "Siti Nurhaliza",
                    studentNumber = "2024002",
                    email = "siti.nur@student.com",
                    classSection = "class1"
                ),
                Student(
                    id = "s3",
                    name = "Budi Santoso",
                    studentNumber = "2024003",
                    email = "budi.santoso@student.com",
                    classSection = "class1"
                ),
                Student(
                    id = "s4",
                    name = "Dewi Lestari",
                    studentNumber = "2024004",
                    email = "dewi.lestari@student.com",
                    classSection = "class1"
                ),
                Student(
                    id = "s5",
                    name = "Eko Prasetyo",
                    studentNumber = "2024005",
                    email = "eko.prasetyo@student.com",
                    classSection = "class2"
                ),
                Student(
                    id = "s6",
                    name = "Rani Wijaya",
                    studentNumber = "2024006",
                    email = "rani.wijaya@student.com",
                    classSection = "class2"
                ),
                Student(
                    id = "s7",
                    name = "Fajar Nugroho",
                    studentNumber = "2024007",
                    email = "fajar.nugroho@student.com",
                    classSection = "class2"
                ),
                Student(
                    id = "s8",
                    name = "Maya Putri",
                    studentNumber = "2024008",
                    email = "maya.putri@student.com",
                    classSection = "class3"
                ),
                Student(
                    id = "s9",
                    name = "Rudi Hartono",
                    studentNumber = "2024009",
                    email = "rudi.hartono@student.com",
                    classSection = "class3"
                ),
                Student(
                    id = "s10",
                    name = "Linda Susanti",
                    studentNumber = "2024010",
                    email = "linda.susanti@student.com",
                    classSection = "class3"
                )
            )
            saveStudents(sampleStudents)
            android.util.Log.d("Repository", "Saved ${sampleStudents.size} demo students")
        } else {
            android.util.Log.d("Repository", "Students already exist and version is current, skipping initialization")
        }
        
        val currentClasses = loadClassSections()
        android.util.Log.d("Repository", "Current classes count: ${currentClasses.size}")
        
        if (currentClasses.isEmpty() || currentDataVersion < DEMO_DATA_VERSION) {
            android.util.Log.d("Repository", "Initializing demo classes...")
            // Add sample classes
            val sampleClasses = listOf(
                ClassSection(
                    id = "class1",
                    name = "Class 10A - Science",
                    description = "Mathematics, Physics, Chemistry, and Biology",
                    teacherId = "teacher1",
                    studentCount = 4
                ),
                ClassSection(
                    id = "class2",
                    name = "Class 10B - Social Studies",
                    description = "History, Geography, Economics, and Sociology",
                    teacherId = "teacher1",
                    studentCount = 3
                ),
                ClassSection(
                    id = "class3",
                    name = "Class 11A - Advanced Science",
                    description = "Advanced topics in Science and Mathematics",
                    teacherId = "teacher1",
                    studentCount = 3
                )
            )
            saveClassSections(sampleClasses)
            android.util.Log.d("Repository", "Saved ${sampleClasses.size} demo classes")
        } else {
            android.util.Log.d("Repository", "Classes already exist and version is current, skipping initialization")
        }
        
        val currentScans = loadScans()
        android.util.Log.d("Repository", "Current scans count: ${currentScans.size}")
        
        if (currentScans.isEmpty() || currentDataVersion < DEMO_DATA_VERSION) {
            android.util.Log.d("Repository", "Initializing demo scans...")
            // Add realistic sample scans with various answer types
            val sampleScans = listOf(
                ScanRecord(
                    id = "scan1",
                    filePath = "",
                    recognizedText = """MCQ Answers:
1. Jakarta
2. Jupiter
3. Leonardo da Vinci
4. 100

Short Text:
5. Photosynthesis is the process where plants use sunlight to make food with chlorophyll
6. Recycling reduces waste and helps protect our environment

Essay:
9. The water cycle begins when water evaporates from oceans. The water vapor rises and condenses into clouds. Then it falls back as rain or snow.""",
                    studentId = "s1",
                    timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                    accuracy = 96.5f,
                    confidenceLevel = "High",
                    enhancedByAI = true
                ),
                ScanRecord(
                    id = "scan2",
                    filePath = "",
                    recognizedText = """Multiple Choice:
1. Jakarta
2. Saturn
3. Leonardo da Vinci
4. 100

Short Answers:
5. Process where plants convert sunlight into energy
6. Recycling saves resources and reduces pollution
7. solid, liquid, gas""",
                    studentId = "s2",
                    timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                    accuracy = 91.8f,
                    confidenceLevel = "High",
                    enhancedByAI = true
                ),
                ScanRecord(
                    id = "scan3",
                    filePath = "",
                    recognizedText = """Q1: Jakarta
Q2: Jupiter
Q3: Michelangelo
Q4: 100

Short Text:
Q7: Solid, liquid, and gas are the three states
Q8: Solar, wind, and hydroelectric power

Long Answer:
Volcanoes form at plate boundaries where magma rises. When pressure builds up, the volcano erupts releasing lava and ash.""",
                    studentId = "s3",
                    timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                    accuracy = 93.4f,
                    confidenceLevel = "High",
                    enhancedByAI = true
                ),
                ScanRecord(
                    id = "scan4",
                    filePath = "",
                    recognizedText = """Answers:
1. Jakarta - capital of Indonesia
2. Jupiter - largest planet
3. Leonardo da Vinci
4. 25 x 4 = 100

Essay on Water Cycle:
The water cycle is a continuous process that circulates water on Earth. Water evaporates from bodies of water, condenses into clouds, and falls as precipitation.""",
                    studentId = "s4",
                    timestamp = System.currentTimeMillis() - 259200000, // 3 days ago
                    accuracy = 94.2f,
                    confidenceLevel = "High",
                    enhancedByAI = true
                ),
                ScanRecord(
                    id = "scan5",
                    filePath = "",
                    recognizedText = """MCQ Section:
1. Jakarta
2. Jupiter
3. Leonardo da Vinci
4. 100

Written Responses:
Photosynthesis: Plants use sunlight and chlorophyll to produce energy
Recycling: Helps environment by reducing waste
Three states: solid, liquid, gas""",
                    studentId = "s5",
                    timestamp = System.currentTimeMillis() - 345600000, // 4 days ago
                    accuracy = 89.7f,
                    confidenceLevel = "Medium",
                    enhancedByAI = false
                ),
                ScanRecord(
                    id = "scan6",
                    filePath = "",
                    recognizedText = """Student Answer Sheet

Multiple Choice Questions:
1. Jakarta is the capital
2. Jupiter is largest
3. Leonardo da Vinci painted Mona Lisa
4. 100

Essay: Climate change impacts our planet through rising temperatures and extreme weather events. We need sustainable practices to address this global challenge.""",
                    studentId = "s6",
                    timestamp = System.currentTimeMillis() - 432000000, // 5 days ago
                    accuracy = 95.1f,
                    confidenceLevel = "High",
                    enhancedByAI = true
                )
            )
            saveScans(sampleScans)
            android.util.Log.d("Repository", "Saved ${sampleScans.size} demo scans")
        } else {
            android.util.Log.d("Repository", "Scans already exist and version is current, skipping initialization")
        }
        
        val currentResults = loadExamResults()
        android.util.Log.d("Repository", "Current results count: ${currentResults.size}")
        
        if (currentResults.isEmpty() || currentDataVersion < DEMO_DATA_VERSION) {
            android.util.Log.d("Repository", "Initializing demo results...")
            // Add comprehensive sample exam results
            val sampleResults = listOf(
                ExamResult(
                    studentId = "s1",
                    examId = "midterm_2024",
                    totalScore = 92.0,
                    details = mapOf(
                        "q1" to 10.0, "q2" to 10.0, "q3" to 10.0, "q4" to 10.0,
                        "q5" to 14.0, "q6" to 13.0, "q7" to 10.0, "q8" to 11.0,
                        "q9" to 18.0
                    )
                ),
                ExamResult(
                    studentId = "s2",
                    examId = "midterm_2024",
                    totalScore = 85.5,
                    details = mapOf(
                        "q1" to 10.0, "q2" to 8.0, "q3" to 10.0, "q4" to 10.0,
                        "q5" to 13.0, "q6" to 12.0, "q7" to 9.0, "q8" to 10.0,
                        "q9" to 17.0
                    )
                ),
                ExamResult(
                    studentId = "s3",
                    examId = "midterm_2024",
                    totalScore = 88.0,
                    details = mapOf(
                        "q1" to 10.0, "q2" to 10.0, "q3" to 8.0, "q4" to 10.0,
                        "q5" to 14.0, "q6" to 11.0, "q7" to 10.0, "q8" to 12.0,
                        "q9" to 16.0
                    )
                ),
                ExamResult(
                    studentId = "s4",
                    examId = "midterm_2024",
                    totalScore = 95.5,
                    details = mapOf(
                        "q1" to 10.0, "q2" to 10.0, "q3" to 10.0, "q4" to 10.0,
                        "q5" to 15.0, "q6" to 15.0, "q7" to 12.0, "q8" to 12.0,
                        "q9" to 19.0
                    )
                ),
                ExamResult(
                    studentId = "s5",
                    examId = "quiz_01",
                    totalScore = 78.0,
                    details = mapOf(
                        "q1" to 10.0, "q2" to 10.0, "q3" to 10.0, "q4" to 10.0,
                        "q5" to 12.0, "q6" to 11.0, "q7" to 10.0, "q8" to 9.0
                    )
                ),
                ExamResult(
                    studentId = "s6",
                    examId = "quiz_01",
                    totalScore = 91.0,
                    details = mapOf(
                        "q1" to 10.0, "q2" to 10.0, "q3" to 10.0, "q4" to 10.0,
                        "q5" to 15.0, "q6" to 14.0, "q7" to 11.0, "q8" to 12.0
                    )
                ),
                ExamResult(
                    studentId = "s7",
                    examId = "quiz_01",
                    totalScore = 82.5,
                    details = mapOf(
                        "q1" to 10.0, "q2" to 9.0, "q3" to 10.0, "q4" to 10.0,
                        "q5" to 13.0, "q6" to 12.0, "q7" to 10.0, "q8" to 10.0
                    )
                ),
                ExamResult(
                    studentId = "s8",
                    examId = "final_2024",
                    totalScore = 89.0,
                    details = mapOf(
                        "q1" to 10.0, "q2" to 10.0, "q3" to 10.0, "q4" to 10.0,
                        "q9" to 18.0, "q10" to 22.0, "q11" to 27.0
                    )
                )
            )
            sampleResults.forEach { saveExamResult(it) }
            android.util.Log.d("Repository", "Saved ${sampleResults.size} demo results")
        } else {
            android.util.Log.d("Repository", "Results already exist and version is current, skipping initialization")
        }
        
        android.util.Log.d("Repository", "Demo data initialization completed successfully")
        
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Error initializing demo data", e)
        }
    }
}
