package com.nikhil.aceshiksha.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.databinding.ActivityCreateTopicBinding
import com.nikhil.aceshiksha.viewmodels.CreateTopicState
import com.nikhil.aceshiksha.viewmodels.CreateTopicViewModel

class CreateTopicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTopicBinding
    private val viewModel: CreateTopicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTopicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdowns()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupDropdowns() {
        val subjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            viewModel.subjects
        )
        binding.spinnerSubject.adapter = subjectAdapter

        val classAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            viewModel.classLevels
        )
        binding.spinnerClass.adapter = classAdapter
        // Default to class 8
        binding.spinnerClass.setSelection(2)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnCreateTopic.setOnClickListener {
            val title       = binding.etTopicTitle.text.toString()
            val subject     = binding.spinnerSubject.selectedItem.toString()
            val classLevel  = binding.spinnerClass.selectedItem.toString()
            val description = binding.etTopicDescription.text.toString()
            viewModel.createTopic(title, subject, classLevel, description)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is CreateTopicState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateTopic.isEnabled = true
                }
                is CreateTopicState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCreateTopic.isEnabled = false
                }
                is CreateTopicState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Topic created successfully!", Toast.LENGTH_SHORT).show()
                    clearForm()
                    viewModel.resetState()
                }
                is CreateTopicState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateTopic.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
            }
        }
    }

    private fun clearForm() {
        binding.etTopicTitle.text?.clear()
        binding.etTopicDescription.text?.clear()
        binding.spinnerSubject.setSelection(0)
        binding.spinnerClass.setSelection(2)
    }
}