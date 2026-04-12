package com.example.edureach1.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.edureach1.databinding.ActivityCreateGameQuestionBinding
import com.example.edureach1.models.GameQuestion
import com.example.edureach1.repository.GameRepository
import com.example.edureach1.utils.Constants.GAME_TYPE_CAR_RACE
import com.example.edureach1.utils.Constants.GAME_TYPE_MAZE
import com.example.edureach1.utils.Constants.GAME_TYPE_QUIZ_BATTLE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class CreateGameQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGameQuestionBinding
    private val repository = GameRepository()

    // Tracks how many questions have been saved in this session
    private var questionCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGameQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Add Game Questions"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupSpinners()
        updateCounter()

        // Save current question, clear only question fields, stay on screen
        binding.btnAddNextQuestion.setOnClickListener {
            saveQuestion(finishAfter = false)
        }

        // Save current question and close the screen
        binding.btnDone.setOnClickListener {
            saveQuestion(finishAfter = true)
        }
    }

    private fun setupSpinners() {
        val gameTypes = listOf("Maze", "Car Race", "Quiz Battle")
        binding.spinnerGameType.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, gameTypes
        )

        val classes = listOf("6", "7", "8", "9", "10", "11")
        binding.spinnerClassLevel.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, classes
        )

        val answers = listOf("A", "B", "C", "D")
        binding.spinnerCorrectAnswer.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, answers
        )
    }

    private fun updateCounter() {
        binding.tvQuestionCount.text = questionCount.toString()
    }

    /**
     * Validates and saves the current question to Firestore.
     * @param finishAfter if true, closes the activity after saving; otherwise clears
     *                    only the question/options fields so the teacher can add another
     *                    question with the same Game Type / Class / Subject.
     */
    private fun saveQuestion(finishAfter: Boolean) {
        val gameTypeLabel = binding.spinnerGameType.selectedItem.toString()
        val gameType = when (gameTypeLabel) {
            "Maze"       -> GAME_TYPE_MAZE
            "Car Race"   -> GAME_TYPE_CAR_RACE
            "Quiz Battle"-> GAME_TYPE_QUIZ_BATTLE
            else         -> GAME_TYPE_MAZE
        }
        val classLevel    = binding.spinnerClassLevel.selectedItem.toString()
        val subject       = binding.etSubject.text.toString().trim()
        val question      = binding.etQuestion.text.toString().trim()
        val optionA       = binding.etOptionA.text.toString().trim()
        val optionB       = binding.etOptionB.text.toString().trim()
        val optionC       = binding.etOptionC.text.toString().trim()
        val optionD       = binding.etOptionD.text.toString().trim()
        val correctAnswer = binding.spinnerCorrectAnswer.selectedItem.toString()

        if (subject.isEmpty() || question.isEmpty() || optionA.isEmpty()
            || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val teacherUid = Firebase.auth.currentUser?.uid ?: return

        val gameQuestion = GameQuestion(
            teacherUid    = teacherUid,
            classLevel    = classLevel,
            subject       = subject,
            gameType      = gameType,
            questionText  = question,
            optionA       = optionA,
            optionB       = optionB,
            optionC       = optionC,
            optionD       = optionD,
            correctAnswer = correctAnswer,
            createdAt     = System.currentTimeMillis()
        )

        setButtonsEnabled(false)

        lifecycleScope.launch {
            val success = repository.addGameQuestion(gameQuestion)
            if (success) {
                questionCount++
                updateCounter()

                if (finishAfter) {
                    Toast.makeText(
                        this@CreateGameQuestionActivity,
                        "All $questionCount question(s) saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@CreateGameQuestionActivity,
                        "Question $questionCount saved! Add next.",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearQuestionFields()
                    setButtonsEnabled(true)
                }
            } else {
                Toast.makeText(
                    this@CreateGameQuestionActivity,
                    "Failed to save. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
                setButtonsEnabled(true)
            }
        }
    }

    /** Clears only the question/options fields; Game Type, Class and Subject stay intact. */
    private fun clearQuestionFields() {
        binding.etQuestion.setText("")
        binding.etOptionA.setText("")
        binding.etOptionB.setText("")
        binding.etOptionC.setText("")
        binding.etOptionD.setText("")
        binding.spinnerCorrectAnswer.setSelection(0)
        binding.etQuestion.requestFocus()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnAddNextQuestion.isEnabled = enabled
        binding.btnDone.isEnabled            = enabled
    }
}
