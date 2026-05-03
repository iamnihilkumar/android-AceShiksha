package com.nikhil.aceshiksha.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.databinding.ActivityQuizAttemptBinding
import com.nikhil.aceshiksha.viewmodels.QuizAttemptState
import com.nikhil.aceshiksha.viewmodels.StudentQuizViewModel

class QuizAttemptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizAttemptBinding
    private val viewModel: StudentQuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizAttemptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val quizId = intent.getStringExtra("QUIZ_ID") ?: return
        val quizTitle = intent.getStringExtra("QUIZ_TITLE") ?: "Quiz"
        supportActionBar?.title = quizTitle

        observeViewModel()
        viewModel.startQuiz(quizId)
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is QuizAttemptState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    setOptionsEnabled(false)
                }
                is QuizAttemptState.AttemptReady -> {
                    binding.progressBar.visibility = View.GONE
                    showQuestion()
                }
                is QuizAttemptState.AttemptFinished -> {
                    val intent = Intent(this, QuizResultActivity::class.java)
                    intent.putExtra("SCORE", state.score)
                    intent.putExtra("TOTAL", state.total)
                    startActivity(intent)
                    finish()
                }
                is QuizAttemptState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun showQuestion() {
        val questions = viewModel.currentQuestions
        val index = viewModel.currentIndex

        if (index >= questions.size) {
            viewModel.finishQuiz()
            return
        }

        val question = questions[index]
        val total = questions.size
        val progressPercent = ((index + 1) * 100) / total

        binding.tvProgress.text = "Question ${index + 1} of $total"
        binding.pbQuiz.progress = progressPercent
        binding.tvQuestion.text = question.questionText
        binding.btnOptionA.text = "A.  ${question.optionA}"
        binding.btnOptionB.text = "B.  ${question.optionB}"
        binding.btnOptionC.text = "C.  ${question.optionC}"
        binding.btnOptionD.text = "D.  ${question.optionD}"

        setOptionsEnabled(true)

        binding.btnOptionA.setOnClickListener { handleAnswer("A") }
        binding.btnOptionB.setOnClickListener { handleAnswer("B") }
        binding.btnOptionC.setOnClickListener { handleAnswer("C") }
        binding.btnOptionD.setOnClickListener { handleAnswer("D") }
    }

    private fun handleAnswer(selected: String) {
        setOptionsEnabled(false)
        viewModel.submitAnswer(selected)

        if (viewModel.currentIndex >= viewModel.currentQuestions.size) {
            viewModel.finishQuiz()
        } else {
            showQuestion()
        }
    }

    private fun setOptionsEnabled(enabled: Boolean) {
        binding.btnOptionA.isEnabled = enabled
        binding.btnOptionB.isEnabled = enabled
        binding.btnOptionC.isEnabled = enabled
        binding.btnOptionD.isEnabled = enabled
    }
}