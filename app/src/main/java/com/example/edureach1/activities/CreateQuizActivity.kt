package com.example.edureach1.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.edureach1.databinding.ActivityCreateQuizBinding
import com.example.edureach1.fragments.AddQuestionsFragment
import com.example.edureach1.viewmodels.CreateQuizViewModel
import com.example.edureach1.viewmodels.QuizCreateState
import com.example.edureach1.R

class CreateQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateQuizBinding
    private val viewModel: CreateQuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            val title = binding.etQuizTitle.text.toString().trim()
            val subject = binding.etSubject.text.toString().trim()
            val classLevel = binding.etClassLevel.text.toString().trim()

            if (title.isEmpty() || subject.isEmpty() || classLevel.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createQuiz(title, subject, classLevel)
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                is QuizCreateState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnNext.isEnabled = false
                }
                is QuizCreateState.QuizCreated -> {
                    binding.progressBar.visibility = View.GONE
                    // Navigate to AddQuestionsFragment
                    binding.root.visibility = View.GONE
                    supportFragmentManager.beginTransaction()
                        .replace(android.R.id.content, AddQuestionsFragment())
                        .commit()
                }
                is QuizCreateState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnNext.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}