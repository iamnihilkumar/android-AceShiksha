package com.example.edureach1.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edureach1.adapters.TopicAdapter
import com.example.edureach1.databinding.ActivityTopicListBinding
import com.example.edureach1.viewmodels.TopicListState
import com.example.edureach1.viewmodels.TopicViewModel

class TopicListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicListBinding
    private val viewModel: TopicViewModel by viewModels()
    private lateinit var topicAdapter: TopicAdapter

    private var subjectName = ""
    private var subjectColor = ""
    private var classLevel = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectName  = intent.getStringExtra("SUBJECT_NAME") ?: ""
        subjectColor = intent.getStringExtra("SUBJECT_COLOR") ?: "#4CAF50"
        classLevel   = intent.getStringExtra("CLASS_LEVEL") ?: "8"

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        viewModel.loadTopics(subjectName, classLevel)
    }

    private fun setupToolbar() {
        binding.tvSubjectTitle.text = subjectName
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        topicAdapter = TopicAdapter { topic ->
            val intent = Intent(this, TopicFeedbackActivity::class.java).apply {
                putExtra("TOPIC_ID", topic.id)
                putExtra("TOPIC_TITLE", topic.title)
                putExtra("TOPIC_DESCRIPTION", topic.description)
                putExtra("SUBJECT_NAME", subjectName)
                putExtra("CLASS_LEVEL", classLevel)
            }
            startActivity(intent)
        }
        binding.rvTopics.apply {
            adapter = topicAdapter
            layoutManager = LinearLayoutManager(this@TopicListActivity)
        }
    }

    private fun observeViewModel() {
        viewModel.topicListState.observe(this) { state ->
            when (state) {
                is TopicListState.Loading -> {
                    binding.progressLoading.visibility = View.VISIBLE
                    binding.rvTopics.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is TopicListState.Success -> {
                    binding.progressLoading.visibility = View.GONE
                    binding.rvTopics.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                    topicAdapter.submitList(state.topics)
                }
                is TopicListState.Empty -> {
                    binding.progressLoading.visibility = View.GONE
                    binding.rvTopics.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = state.message
                }
                is TopicListState.Error -> {
                    binding.progressLoading.visibility = View.GONE
                    binding.rvTopics.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = state.message
                }
            }
        }
    }
}