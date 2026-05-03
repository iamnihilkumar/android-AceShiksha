package com.nikhil.aceshiksha.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nikhil.aceshiksha.activities.QuizAttemptActivity
import com.nikhil.aceshiksha.adapters.QuizAdapter
import com.nikhil.aceshiksha.databinding.FragmentStudentQuizListBinding
import com.nikhil.aceshiksha.viewmodels.QuizAttemptState
import com.nikhil.aceshiksha.viewmodels.StudentQuizViewModel

class StudentQuizListFragment : Fragment() {

    private var _binding: FragmentStudentQuizListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentQuizViewModel by viewModels()
    private lateinit var quizAdapter: QuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentQuizListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quizAdapter = QuizAdapter { quiz ->
            val intent = Intent(requireContext(), QuizAttemptActivity::class.java)
            intent.putExtra("QUIZ_ID", quiz.quizId)
            intent.putExtra("QUIZ_TITLE", quiz.title)
            startActivity(intent)
        }

        binding.rvQuizzes.apply {
            adapter = quizAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QuizAttemptState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is QuizAttemptState.QuizzesLoaded -> {
                    binding.progressBar.visibility = View.GONE
                    quizAdapter.submitList(state.quizzes)
                    binding.tvEmpty.visibility =
                        if (state.quizzes.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvQuizzes.visibility =
                        if (state.quizzes.isEmpty()) View.GONE else View.VISIBLE
                }
                is QuizAttemptState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        viewModel.loadQuizzesForStudent()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadQuizzesForStudent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}