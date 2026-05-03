package com.nikhil.aceshiksha.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.databinding.ActivityCreateQuizBinding
import com.nikhil.aceshiksha.fragments.AddQuestionsFragment
import com.nikhil.aceshiksha.viewmodels.CreateQuizViewModel
import com.nikhil.aceshiksha.viewmodels.QuizCreateState

class CreateQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateQuizBinding
    private val viewModel: CreateQuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

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