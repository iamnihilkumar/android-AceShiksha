package com.nikhil.aceshiksha.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nikhil.aceshiksha.adapters.QuizAdapter
import com.nikhil.aceshiksha.databinding.ActivityTeacherDashboardBinding
import com.nikhil.aceshiksha.viewmodels.TeacherDashboardViewModel

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherDashboardBinding
    private val viewModel: TeacherDashboardViewModel by viewModels()
    private lateinit var quizAdapter: QuizAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        viewModel.loadMyQuizzes()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMyQuizzes()
    }

    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter { quiz ->
            Toast.makeText(this, "Quiz: ${quiz.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvQuizzes.apply {
            adapter = quizAdapter
            layoutManager = LinearLayoutManager(this@TeacherDashboardActivity)
        }
    }

    private fun setupClickListeners() {
        binding.cardCreateQuiz.setOnClickListener {
            startActivity(Intent(this, CreateQuizActivity::class.java))
        }
        binding.cardViewReports.setOnClickListener {
            startActivity(Intent(this, TeacherStudentListActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.cardGameQuestions.setOnClickListener {
            startActivity(Intent(this, CreateGameQuestionActivity::class.java))
        }
        binding.cardPdfQuiz.setOnClickListener {
            startActivity(Intent(this, PdfQuizActivity::class.java))
        }
        // NEW
        binding.cardManageTopics.setOnClickListener {
            startActivity(Intent(this, CreateTopicActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.quizzes.observe(this) { quizzes ->
            quizAdapter.submitList(quizzes)
            binding.tvEmpty.visibility = if (quizzes.isEmpty()) View.VISIBLE else View.GONE
            binding.rvQuizzes.visibility = if (quizzes.isEmpty()) View.GONE else View.VISIBLE
        }
        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }
    }
}