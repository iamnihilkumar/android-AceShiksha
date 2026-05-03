package com.nikhil.aceshiksha.activities

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.databinding.ActivityTopicFeedbackBinding
import com.nikhil.aceshiksha.viewmodels.TopicFeedbackState
import com.nikhil.aceshiksha.viewmodels.TopicViewModel

class TopicFeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicFeedbackBinding
    private val viewModel: TopicViewModel by viewModels()

    private var topicTitle = ""
    private var subjectName = ""
    private var topicDescription = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        topicTitle       = intent.getStringExtra("TOPIC_TITLE") ?: ""
        subjectName      = intent.getStringExtra("SUBJECT_NAME") ?: ""
        topicDescription = intent.getStringExtra("TOPIC_DESCRIPTION") ?: ""

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvTopicTitle.text = topicTitle
        binding.tvSubjectName.text = subjectName
        binding.tvTopicDescription.text = topicDescription.ifEmpty {
            "Write what you know about $topicTitle below."
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        binding.btnGetFeedback.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etStudentInput.windowToken, 0)
            val input = binding.etStudentInput.text.toString()
            viewModel.getAiFeedback(topicTitle, subjectName, input)
        }

        binding.btnTryAgain.setOnClickListener {
            viewModel.resetFeedback()
            binding.etStudentInput.text?.clear()
            binding.etStudentInput.requestFocus()
        }
    }

    private fun observeViewModel() {
        viewModel.feedbackState.observe(this) { state ->
            when (state) {
                is TopicFeedbackState.Idle -> {
                    binding.cardInput.visibility = View.VISIBLE
                    binding.cardFeedback.visibility = View.GONE
                    binding.btnGetFeedback.isEnabled = true
                    binding.btnGetFeedback.text = "Get AI Feedback"
                }
                is TopicFeedbackState.Loading -> {
                    binding.btnGetFeedback.isEnabled = false
                    binding.btnGetFeedback.text = "Analyzing..."
                    binding.cardFeedback.visibility = View.VISIBLE
                    binding.progressFeedback.visibility = View.VISIBLE
                    binding.tvFeedback.visibility = View.GONE
                    binding.tvXpEarned.visibility = View.GONE
                    binding.btnTryAgain.visibility = View.GONE
                }
                is TopicFeedbackState.Success -> {
                    binding.cardInput.visibility = View.VISIBLE
                    binding.cardFeedback.visibility = View.VISIBLE
                    binding.progressFeedback.visibility = View.GONE
                    binding.tvFeedback.visibility = View.VISIBLE
                    binding.tvFeedback.text = state.feedback
                    binding.tvXpEarned.visibility = View.VISIBLE
                    binding.btnTryAgain.visibility = View.VISIBLE
                    binding.btnGetFeedback.isEnabled = true
                    binding.btnGetFeedback.text = "Get AI Feedback"
                }
                is TopicFeedbackState.Error -> {
                    binding.cardFeedback.visibility = View.VISIBLE
                    binding.progressFeedback.visibility = View.GONE
                    binding.tvFeedback.visibility = View.VISIBLE
                    binding.tvFeedback.text = state.message
                    binding.tvXpEarned.visibility = View.GONE
                    binding.btnTryAgain.visibility = View.GONE
                    binding.btnGetFeedback.isEnabled = true
                    binding.btnGetFeedback.text = "Get AI Feedback"
                }
            }
        }
    }
}