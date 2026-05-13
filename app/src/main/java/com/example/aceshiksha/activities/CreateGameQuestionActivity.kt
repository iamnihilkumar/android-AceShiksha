package com.nikhil.aceshiksha.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nikhil.aceshiksha.databinding.ActivityCreateGameQuestionBinding
import com.nikhil.aceshiksha.models.GameQuestion
import com.nikhil.aceshiksha.repository.GameRepository
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_CAR_RACE
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_MAZE
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_MEMORY_MATCH
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_QUIZ_BATTLE
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_TRUE_OR_FALSE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class CreateGameQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGameQuestionBinding
    private val repository = GameRepository()
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

        // Show/hide hint when game type changes
        binding.spinnerGameType.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>, view: View?,
                    position: Int, id: Long
                ) {
                    updateHintForGameType(parent.getItemAtPosition(position).toString())
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }

        binding.btnAddNextQuestion.setOnClickListener { saveQuestion(finishAfter = false) }
        binding.btnDone.setOnClickListener { saveQuestion(finishAfter = true) }
    }

    private fun setupSpinners() {
        val gameTypes = listOf("Maze", "Car Race", "Quiz Battle", "Memory Match", "True or False")
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

    /**
     * Shows a context-aware hint card below the Game Type spinner
     * for Memory Match and True or False, hidden for other types.
     */
    private fun updateHintForGameType(gameTypeLabel: String) {
        when (gameTypeLabel) {
            "Memory Match" -> {
                binding.tvGameTypeHint.visibility = View.VISIBLE
                binding.tvGameTypeHint.text =
                    "📌 Memory Match format:\n" +
                            "• Question = the TERM  (e.g. \"Photosynthesis\")\n" +
                            "• Option A = the DEFINITION  (e.g. \"Process plants use to make food\")\n" +
                            "• Options B, C, D and Correct Answer are ignored."
            }
            "True or False" -> {
                binding.tvGameTypeHint.visibility = View.VISIBLE
                binding.tvGameTypeHint.text =
                    "📌 True or False format:\n" +
                            "• Question = the STATEMENT students must judge\n" +
                            "• Correct Answer = A  if the statement is TRUE\n" +
                            "• Correct Answer = B  if the statement is FALSE\n" +
                            "• Options A, B, C, D text fields are ignored."
            }
            else -> {
                binding.tvGameTypeHint.visibility = View.GONE
            }
        }
    }

    private fun updateCounter() {
        binding.tvQuestionCount.text = questionCount.toString()
    }

    private fun saveQuestion(finishAfter: Boolean) {
        val gameTypeLabel = binding.spinnerGameType.selectedItem.toString()
        val gameType = when (gameTypeLabel) {
            "Maze"          -> GAME_TYPE_MAZE
            "Car Race"      -> GAME_TYPE_CAR_RACE
            "Quiz Battle"   -> GAME_TYPE_QUIZ_BATTLE
            "Memory Match"  -> GAME_TYPE_MEMORY_MATCH
            "True or False" -> GAME_TYPE_TRUE_OR_FALSE
            else            -> GAME_TYPE_MAZE
        }

        val classLevel    = binding.spinnerClassLevel.selectedItem.toString()
        val subject       = binding.etSubject.text.toString().trim()
        val question      = binding.etQuestion.text.toString().trim()
        val optionA       = binding.etOptionA.text.toString().trim()
        val optionB       = binding.etOptionB.text.toString().trim()
        val optionC       = binding.etOptionC.text.toString().trim()
        val optionD       = binding.etOptionD.text.toString().trim()
        val correctAnswer = binding.spinnerCorrectAnswer.selectedItem.toString()

        // For Memory Match: only question + optionA required
        // For True or False: only question + correctAnswer required
        // For all others: all fields required
        val isValid = when (gameType) {
            GAME_TYPE_MEMORY_MATCH  -> subject.isNotEmpty() && question.isNotEmpty() && optionA.isNotEmpty()
            GAME_TYPE_TRUE_OR_FALSE -> subject.isNotEmpty() && question.isNotEmpty()
            else -> subject.isNotEmpty() && question.isNotEmpty()
                    && optionA.isNotEmpty() && optionB.isNotEmpty()
                    && optionC.isNotEmpty() && optionD.isNotEmpty()
        }

        if (!isValid) {
            val msg = when (gameType) {
                GAME_TYPE_MEMORY_MATCH  -> "Please fill Subject, Question (Term) and Option A (Definition)"
                GAME_TYPE_TRUE_OR_FALSE -> "Please fill Subject and Question (Statement)"
                else -> "Please fill in all fields"
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@CreateGameQuestionActivity, "Failed to save. Try again.", Toast.LENGTH_SHORT).show()
                setButtonsEnabled(true)
            }
        }
    }

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
        binding.btnDone.isEnabled = enabled
    }
}
