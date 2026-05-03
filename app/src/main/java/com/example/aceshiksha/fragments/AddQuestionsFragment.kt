package com.nikhil.aceshiksha.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nikhil.aceshiksha.databinding.FragmentAddQuestionBinding
import com.nikhil.aceshiksha.viewmodels.CreateQuizViewModel
import com.nikhil.aceshiksha.viewmodels.QuizCreateState

class AddQuestionsFragment : Fragment() {

    private var _binding: FragmentAddQuestionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateQuizViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateQuestionCount()

        binding.btnAddAnother.setOnClickListener { submitQuestion(publishAfter = false) }
        binding.btnPublish.setOnClickListener { submitQuestion(publishAfter = true) }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QuizCreateState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnAddAnother.isEnabled = false
                    binding.btnPublish.isEnabled = false
                }
                is QuizCreateState.QuestionAdded -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddAnother.isEnabled = true
                    binding.btnPublish.isEnabled = true
                    clearFields()
                    updateQuestionCount()
                    Toast.makeText(context, "Question added!", Toast.LENGTH_SHORT).show()
                }
                is QuizCreateState.Published -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Quiz published successfully! 🎉", Toast.LENGTH_LONG).show()
                    requireActivity().finish()
                }
                is QuizCreateState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddAnother.isEnabled = true
                    binding.btnPublish.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun submitQuestion(publishAfter: Boolean) {
        val questionText = binding.etQuestion.text.toString().trim()
        val optionA = binding.etOptionA.text.toString().trim()
        val optionB = binding.etOptionB.text.toString().trim()
        val optionC = binding.etOptionC.text.toString().trim()
        val optionD = binding.etOptionD.text.toString().trim()

        val selectedId = binding.rgCorrectAnswer.checkedRadioButtonId
        if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() ||
            optionC.isEmpty() || optionD.isEmpty() || selectedId == -1) {
            Toast.makeText(context, "Please fill all fields and select correct answer", Toast.LENGTH_SHORT).show()
            return
        }

        val correctAnswer = when (selectedId) {
            binding.rbA.id -> "A"
            binding.rbB.id -> "B"
            binding.rbC.id -> "C"
            binding.rbD.id -> "D"
            else -> ""
        }

        viewModel.addQuestion(questionText, optionA, optionB, optionC, optionD, correctAnswer)

        if (publishAfter) {
            // Observe QuestionAdded, then publish
            viewModel.state.observe(viewLifecycleOwner) { state ->
                if (state is QuizCreateState.QuestionAdded) {
                    viewModel.publishQuiz()
                }
            }
        }
    }

    private fun clearFields() {
        binding.etQuestion.text?.clear()
        binding.etOptionA.text?.clear()
        binding.etOptionB.text?.clear()
        binding.etOptionC.text?.clear()
        binding.etOptionD.text?.clear()
        binding.rgCorrectAnswer.clearCheck()
    }

    private fun updateQuestionCount() {
        binding.tvQuestionCount.text = "Question ${viewModel.questionCount + 1}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}