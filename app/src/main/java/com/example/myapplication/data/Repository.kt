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
            // Only initialize if data doesn't exist
            val currentQuestions = loadQuestions()
            android.util.Log.d("Repository", "Current questions count: ${currentQuestions.size}")
            
            if (currentQuestions.isEmpty()) {
                android.util.Log.d("Repository", "Initializing demo questions...")
                // Add sample questions
            val sampleQuestions = listOf(
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
                    text = "What is 15 + 27?",
                    type = QuestionType.MCQ,
                    options = emptyList(),
                    answer = "42",
                    weight = 10.0,
                    explanation = "15 + 27 = 42"
                ),
                Question(
                    id = "q3",
                    text = "Indonesia gained independence in which year?",
                    type = QuestionType.MCQ,
                    options = emptyList(),
                    answer = "1945",
                    weight = 10.0,
                    explanation = "Indonesia proclaimed its independence on August 17, 1945"
                ),
                Question(
                    id = "q4",
                    text = "What is the largest planet in our solar system?",
                    type = QuestionType.MCQ,
                    options = listOf("Earth", "Jupiter", "Saturn", "Mars"),
                    answer = "Jupiter",
                    weight = 10.0,
                    explanation = "Jupiter is the largest planet"
                ),
                Question(
                    id = "q5",
                    text = "Water freezes at what temperature in Celsius?",
                    type = QuestionType.MCQ,
                    options = emptyList(),
                    answer = "0",
                    weight = 10.0,
                    explanation = "Water freezes at 0°C (32°F)"
                )
            )
            saveQuestions(sampleQuestions)
            android.util.Log.d("Repository", "Saved ${sampleQuestions.size} demo questions")
        } else {
            android.util.Log.d("Repository", "Questions already exist, skipping initialization")
        }
        
        val currentStudents = loadStudents()
        android.util.Log.d("Repository", "Current students count: ${currentStudents.size}")
        
        if (currentStudents.isEmpty()) {
            android.util.Log.d("Repository", "Initializing demo students...")
            // Add sample students
            val sampleStudents = listOf(
                Student(
                    id = "s1",
                    name = "Ahmad Rizki",
                    studentNumber = "2024001",
                    email = "ahmad@student.com",
                    classSection = "class1"
                ),
                Student(
                    id = "s2",
                    name = "Siti Nurhaliza",
                    studentNumber = "2024002",
                    email = "siti@student.com",
                    classSection = "class1"
                ),
                Student(
                    id = "s3",
                    name = "Budi Santoso",
                    studentNumber = "2024003",
                    email = "budi@student.com",
                    classSection = "class1"
                ),
                Student(
                    id = "s4",
                    name = "Dewi Lestari",
                    studentNumber = "2024004",
                    email = "dewi@student.com",
                    classSection = "class2"
                ),
                Student(
                    id = "s5",
                    name = "Eko Prasetyo",
                    studentNumber = "2024005",
                    email = "eko@student.com",
                    classSection = "class2"
                )
            )
            saveStudents(sampleStudents)
            android.util.Log.d("Repository", "Saved ${sampleStudents.size} demo students")
        } else {
            android.util.Log.d("Repository", "Students already exist, skipping initialization")
        }
        
        val currentClasses = loadClassSections()
        android.util.Log.d("Repository", "Current classes count: ${currentClasses.size}")
        
        if (currentClasses.isEmpty()) {
            android.util.Log.d("Repository", "Initializing demo classes...")
            // Add sample classes
            val sampleClasses = listOf(
                ClassSection(
                    id = "class1",
                    name = "Class 10A",
                    description = "Mathematics and Science",
                    teacherId = "teacher1",
                    studentCount = 3
                ),
                ClassSection(
                    id = "class2",
                    name = "Class 10B",
                    description = "Social Studies",
                    teacherId = "teacher1",
                    studentCount = 2
                )
            )
            saveClassSections(sampleClasses)
            android.util.Log.d("Repository", "Saved ${sampleClasses.size} demo classes")
        } else {
            android.util.Log.d("Repository", "Classes already exist, skipping initialization")
        }
        
        val currentScans = loadScans()
        android.util.Log.d("Repository", "Current scans count: ${currentScans.size}")
        
        if (currentScans.isEmpty()) {
            android.util.Log.d("Repository", "Initializing demo scans...")
            // Add sample scans
            val sampleScans = listOf(
                ScanRecord(
                    id = "scan1",
                    filePath = "",
                    recognizedText = "1. Jakarta\n2. 42\n3. 1945\n4. Jupiter\n5. 0",
                    studentId = "s1",
                    timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                    accuracy = 95.5f,
                    confidenceLevel = "High",
                    enhancedByAI = true
                ),
                ScanRecord(
                    id = "scan2",
                    filePath = "",
                    recognizedText = "1. Jakarta\n2. 43\n3. 1945\n4. Saturn\n5. 0",
                    studentId = "s2",
                    timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                    accuracy = 88.2f,
                    confidenceLevel = "High",
                    enhancedByAI = true
                ),
                ScanRecord(
                    id = "scan3",
                    filePath = "",
                    recognizedText = "Sample handwritten answers for demo",
                    studentId = "s3",
                    timestamp = System.currentTimeMillis() - 259200000, // 3 days ago
                    accuracy = 92.0f,
                    confidenceLevel = "High",
                    enhancedByAI = false
                )
            )
            saveScans(sampleScans)
            android.util.Log.d("Repository", "Saved ${sampleScans.size} demo scans")
        } else {
            android.util.Log.d("Repository", "Scans already exist, skipping initialization")
        }
        
        val currentResults = loadExamResults()
        android.util.Log.d("Repository", "Current results count: ${currentResults.size}")
        
        if (currentResults.isEmpty()) {
            android.util.Log.d("Repository", "Initializing demo results...")
            // Add sample exam results
            val sampleResults = listOf(
                ExamResult(
                    studentId = "s1",
                    examId = "exam1",
                    totalScore = 85.0,
                    details = mapOf("q1" to 10.0, "q2" to 10.0, "q3" to 10.0, "q4" to 10.0, "q5" to 8.0)
                ),
                ExamResult(
                    studentId = "s2",
                    examId = "exam1",
                    totalScore = 78.0,
                    details = mapOf("q1" to 10.0, "q2" to 8.0, "q3" to 10.0, "q4" to 7.0, "q5" to 10.0)
                ),
                ExamResult(
                    studentId = "s3",
                    examId = "exam1",
                    totalScore = 92.0,
                    details = mapOf("q1" to 10.0, "q2" to 10.0, "q3" to 10.0, "q4" to 10.0, "q5" to 10.0)
                )
            )
            sampleResults.forEach { saveExamResult(it) }
            android.util.Log.d("Repository", "Saved ${sampleResults.size} demo results")
        } else {
            android.util.Log.d("Repository", "Results already exist, skipping initialization")
        }
        
        android.util.Log.d("Repository", "Demo data initialization completed successfully")
        
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Error initializing demo data", e)
        }
    }
}
