package com.example.edureach1.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edureach1.adapters.GeneratedQuestionAdapter
import com.example.edureach1.databinding.ActivityPdfQuizBinding
import com.example.edureach1.models.GameQuestion
import com.example.edureach1.network.GeminiContent
import com.example.edureach1.network.GeminiPart
import com.example.edureach1.network.GeminiRequest
import com.example.edureach1.network.RetrofitInstance
import com.example.edureach1.repository.AuthRepository
import com.example.edureach1.repository.GameRepository
import com.example.edureach1.utils.Constants
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PdfQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfQuizBinding
    private val gameRepository = GameRepository()
    private val authRepository = AuthRepository()

    private val generatedQuestions = mutableListOf<GameQuestion>()
    private lateinit var adapter: GeneratedQuestionAdapter

    private var teacherUid = ""
    private var selectedClassLevel = "6"
    private var selectedSubject = "General"
    private var selectedGameType = Constants.GAME_TYPE_QUIZ_BATTLE

    // Holds the properly extracted, clean text from the PDF
    private var cleanedPdfText = ""

    private val PDF_PICK_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // IMPORTANT: Initialize PDFBox resource loader once, in onCreate
        PDFBoxResourceLoader.init(applicationContext)

        teacherUid = authRepository.getCurrentUser()?.uid ?: ""

        setupSpinners()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupSpinners() {
        val classLevels = listOf("6", "7", "8", "9", "10", "11")
        binding.spinnerClass.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, classLevels
        )
        val subjects = listOf(
            "General", "Mathematics", "Science",
            "History", "Geography", "English", "Hindi",
            "Physics", "Chemistry", "Biology", "Computer Science", "DBMS"
        )
        binding.spinnerSubject.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, subjects
        )
        val gameTypeLabels = listOf("Quiz Battle", "Maze", "Car Race")
        binding.spinnerGameType.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, gameTypeLabels
        )
    }

    private fun setupRecyclerView() {
        adapter = GeneratedQuestionAdapter(generatedQuestions) { position ->
            generatedQuestions.removeAt(position)
            adapter.notifyItemRemoved(position)
            updateSaveButtonVisibility()
        }
        binding.rvGeneratedQuestions.layoutManager = LinearLayoutManager(this)
        binding.rvGeneratedQuestions.adapter = adapter
        // ADD THIS — lets NestedScrollView measure all items at once
        binding.rvGeneratedQuestions.isNestedScrollingEnabled = false
    }

    private fun setupClickListeners() {
        binding.btnPickPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
            }
            startActivityForResult(intent, PDF_PICK_CODE)
        }

        binding.btnGenerate.setOnClickListener {
            selectedClassLevel = binding.spinnerClass.selectedItem.toString()
            selectedSubject = binding.spinnerSubject.selectedItem.toString()
            selectedGameType = when (binding.spinnerGameType.selectedItemPosition) {
                1 -> Constants.GAME_TYPE_MAZE
                2 -> Constants.GAME_TYPE_CAR_RACE
                else -> Constants.GAME_TYPE_QUIZ_BATTLE
            }
            if (cleanedPdfText.isBlank()) {
                Toast.makeText(this, "Please select a PDF first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            callGemini(cleanedPdfText)
        }

        binding.btnSaveAll.setOnClickListener {
            saveAllQuestions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            binding.tvPdfName.text =
                uri.lastPathSegment?.substringAfterLast("/") ?: "selected.pdf"
            extractTextFromPdf(uri)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  LAYER 1 — PROPER PDF TEXT EXTRACTION via PDFBox
    // ─────────────────────────────────────────────────────────────────

    /**
     * Uses Apache PDFBox (Android port) to properly decode compressed PDF streams.
     * This handles FlateDecode, standard fonts, and multi-page documents correctly —
     * unlike raw byte reading which only works on ancient uncompressed PDFs.
     *
     * Runs on IO dispatcher so it never blocks the UI thread.
     */
    private fun extractTextFromPdf(uri: Uri) {
        binding.tvPdfText.text = "Extracting text…"
        binding.btnGenerate.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Reading PDF…"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open file")

                // PDFBox loads the full document and decodes all compressed streams
                val document = PDDocument.load(inputStream)
                val stripper = PDFTextStripper().apply {
                    // Sort by position so text flows top→bottom, left→right
                    sortByPosition = true
                }
                val rawText = stripper.getText(document)
                document.close()
                inputStream.close()

                // Post-process: clean whitespace, remove junk lines
                val cleaned = postProcessExtractedText(rawText)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (cleaned.length < 80) {
                        binding.tvPdfText.text =
                            "⚠ Not enough readable text found. Make sure this is a text-based PDF (not a scanned image)."
                        binding.btnGenerate.isEnabled = false
                        binding.tvStatus.text = "Extraction failed."
                        cleanedPdfText = ""
                        Toast.makeText(
                            this@PdfQuizActivity,
                            "Could not extract text. Use a text-based (not scanned) PDF.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Show first 4 lines as a preview in the TextView
                        cleanedPdfText = cleaned.take(4000)
                        val previewLines = cleanedPdfText
                            .lines()
                            .filter { it.isNotBlank() }
                            .take(4)
                            .joinToString("\n")
                        binding.tvPdfText.text = previewLines
                        binding.btnGenerate.isEnabled = true
                        binding.tvStatus.text =
                            "✅ Extracted ${cleanedPdfText.length} characters. Ready to generate."
                        Toast.makeText(
                            this@PdfQuizActivity,
                            "PDF loaded! ${cleanedPdfText.length} characters extracted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvPdfText.text = "Error reading PDF."
                    binding.tvStatus.text = "Error: ${e.message}"
                    cleanedPdfText = ""
                    Toast.makeText(
                        this@PdfQuizActivity,
                        "PDF Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Cleans the raw text output from PDFBox:
     *  1. Removes lines that are purely numeric (page numbers)
     *  2. Removes lines shorter than 20 chars (headers/footers/stray tokens)
     *  3. Removes lines that contain no letters at all
     *  4. Collapses multiple blank lines into one
     *  5. Trims each line
     */
    private fun postProcessExtractedText(raw: String): String {
        val lines = raw.lines()
        val kept = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()

            // Skip blank lines (we'll add one separator later)
            if (trimmed.isBlank()) continue

            // Skip lines with no letters (pure numbers, symbols, page markers)
            if (!trimmed.any { it.isLetter() }) continue

            // Skip very short lines — likely headers, footers, or stray PDF tokens
            if (trimmed.length < 20) continue

            // Skip lines that are >70% non-letter characters (binary/encoding artifacts)
            val letterRatio = trimmed.count { it.isLetter() }.toDouble() / trimmed.length
            if (letterRatio < 0.40) continue

            kept.add(trimmed)
        }

        return kept.joinToString("\n")
    }

    // ─────────────────────────────────────────────────────────────────
    //  LAYER 2 — GEMINI PROMPT
    // ─────────────────────────────────────────────────────────────────

    private fun callGemini(pdfText: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGenerate.isEnabled = false
        binding.tvStatus.text = "Generating questions with AI…"
        generatedQuestions.clear()
        adapter.notifyDataSetChanged()
        binding.btnSaveAll.visibility = View.GONE

        val prompt = buildPrompt(pdfText)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )
                val response = RetrofitInstance.geminiApi.generateContent(
                    apiKey = Constants.GEMINI_API_KEY,
                    request = request
                )
                val generatedText = response.candidates
                    ?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

                withContext(Dispatchers.Main) {
                    parseAndShowQuestions(generatedText)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGenerate.isEnabled = true
                    binding.tvStatus.text = "Error. Please try again."
                    Toast.makeText(
                        this@PdfQuizActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * The prompt is now strict about using ONLY the PDF content.
     * The fallback instruction is removed — if the PDF text is good,
     * Gemini must use it; it must NOT invent questions from general knowledge.
     */
    private fun buildPrompt(pdfText: String): String {
        return """
You are a quiz generator for Indian school students (Class $selectedClassLevel).

Below is text extracted from a PDF document. Your job is to generate exactly 5 multiple-choice questions STRICTLY based on the information in this text.

CRITICAL RULES — follow every one:
1. ONLY use facts, concepts, and definitions present in the PDF text below. Do NOT use outside knowledge.
2. Every question MUST be directly answerable from the PDF text.
3. Every question MUST end with a question mark (?).
4. NEVER write a heading, title, or formula as a question. Questions must be proper sentences.
5. Each question must have exactly 4 options (A, B, C, D). Only one is correct.
6. The 3 wrong options must be plausible but clearly incorrect based on the text.
7. Output ONLY the 5 question blocks. No introduction, no explanation, no extra text.

OUTPUT FORMAT:
Q1: <question text ending with ?>
A) <option>
B) <option>
C) <option>
D) <option>
Answer: <A or B or C or D>

Q2: <question text ending with ?>
A) <option>
B) <option>
C) <option>
D) <option>
Answer: <A or B or C or D>

Q3: <question text ending with ?>
A) <option>
B) <option>
C) <option>
D) <option>
Answer: <A or B or C or D>

Q4: <question text ending with ?>
A) <option>
B) <option>
C) <option>
D) <option>
Answer: <A or B or C or D>

Q5: <question text ending with ?>
A) <option>
B) <option>
C) <option>
D) <option>
Answer: <A or B or C or D>

---BEGIN PDF TEXT---
$pdfText
---END PDF TEXT---
        """.trimIndent()
    }

    // ─────────────────────────────────────────────────────────────────
    //  LAYER 3 — RESPONSE PARSER
    // ─────────────────────────────────────────────────────────────────

    private fun parseAndShowQuestions(text: String) {
        binding.progressBar.visibility = View.GONE
        binding.btnGenerate.isEnabled = true

        val questions = parseGeminiResponse(text)

        if (questions.isEmpty()) {
            binding.tvStatus.text = "⚠ Could not parse response. Please try again."
            Toast.makeText(this, "Parsing failed. Try again.", Toast.LENGTH_SHORT).show()
            return
        }

        generatedQuestions.clear()
        generatedQuestions.addAll(questions)
        adapter.notifyDataSetChanged()

        binding.tvStatus.text = "✅ ${questions.size} questions generated. Review and save."
        updateSaveButtonVisibility()
    }

    private fun parseGeminiResponse(text: String): List<GameQuestion> {
        val result = mutableListOf<GameQuestion>()

        // Split on "Q1:" / "Q1." / "Q 1:" patterns
        val blocks = text.split(Regex("(?i)Q\\s*\\d+[.:]\\s*")).filter { it.isNotBlank() }

        for (block in blocks) {
            try {
                val lines = block.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (lines.size < 5) continue

                val questionText = lines[0].trim()

                // Must end with "?" and be a real sentence (≥10 chars)
                if (!questionText.endsWith("?") || questionText.length < 10) continue

                var optA = ""; var optB = ""; var optC = ""; var optD = ""
                var correctAnswer = ""

                // Matches: A) / A. / (A) / A: / A -
                val optionRegex = Regex(
                    "^\\(?([ABCD])[).:\\-]\\s*(.+)$",
                    RegexOption.IGNORE_CASE
                )
                // Matches: Answer: A / Correct Answer: B / ANSWER:C
                val answerRegex = Regex("(?i)(?:correct\\s+)?answer[:\\s]+([ABCD])[).\\s]?")

                for (line in lines.drop(1)) {
                    val ansMatch = answerRegex.find(line)
                    val optMatch = optionRegex.matchEntire(line)
                    when {
                        ansMatch != null -> {
                            correctAnswer = ansMatch.groupValues[1].uppercase()
                        }
                        optMatch != null -> {
                            val letter = optMatch.groupValues[1].uppercase()
                            val value  = optMatch.groupValues[2].trim()
                            when (letter) {
                                "A" -> optA = value
                                "B" -> optB = value
                                "C" -> optC = value
                                "D" -> optD = value
                            }
                        }
                    }
                }

                // All 4 options + a valid answer letter are required
                if (optA.isBlank() || optB.isBlank() || optC.isBlank() || optD.isBlank()) continue
                if (correctAnswer !in listOf("A", "B", "C", "D")) continue

                result.add(
                    GameQuestion(
                        questionId    = UUID.randomUUID().toString(),
                        teacherUid    = teacherUid,
                        classLevel    = selectedClassLevel,
                        subject       = selectedSubject,
                        gameType      = selectedGameType,
                        questionText  = questionText,
                        optionA       = optA,
                        optionB       = optB,
                        optionC       = optC,
                        optionD       = optD,
                        correctAnswer = correctAnswer
                    )
                )
            } catch (e: Exception) {
                // skip malformed block
            }
        }
        return result
    }

    // ─────────────────────────────────────────────────────────────────
    //  SAVE
    // ─────────────────────────────────────────────────────────────────

    private fun saveAllQuestions() {
        if (generatedQuestions.isEmpty()) return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveAll.isEnabled = false
        binding.tvStatus.text = "Saving to Firestore…"

        CoroutineScope(Dispatchers.IO).launch {
            var saved = 0
            for (question in generatedQuestions) {
                try {
                    gameRepository.addGameQuestion(question)
                    saved++
                } catch (e: Exception) { /* continue */ }
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveAll.isEnabled = true
                Toast.makeText(
                    this@PdfQuizActivity,
                    "$saved / ${generatedQuestions.size} questions saved!",
                    Toast.LENGTH_LONG
                ).show()
                if (saved > 0) finish()
            }
        }
    }

    private fun updateSaveButtonVisibility() {
        binding.btnSaveAll.visibility =
            if (generatedQuestions.isNotEmpty()) View.VISIBLE else View.GONE
    }
}