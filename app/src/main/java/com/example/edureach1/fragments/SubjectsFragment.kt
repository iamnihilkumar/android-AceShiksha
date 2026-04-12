package com.example.edureach1.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.edureach1.activities.TopicListActivity
import com.example.edureach1.adapters.SubjectsAdapter
import com.example.edureach1.databinding.FragmentSubjectsBinding
import com.example.edureach1.viewmodels.SubjectsViewModel

class SubjectsFragment : Fragment() {

    private var _binding: FragmentSubjectsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubjectsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSubjects.layoutManager = GridLayoutManager(requireContext(), 2)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.classLevel.observe(viewLifecycleOwner) { level ->
            binding.tvSubjectsClass.text = "Class $level"
        }

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            if (subjects.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvSubjects.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvSubjects.visibility = View.VISIBLE
                val adapter = SubjectsAdapter(subjects) { subject ->
                    // Pass classLevel from viewModel to TopicListActivity
                    val classLevel = viewModel.classLevel.value ?: "8"
                    val intent = Intent(requireContext(), TopicListActivity::class.java).apply {
                        putExtra("SUBJECT_ID", subject.id)
                        putExtra("SUBJECT_NAME", subject.name)
                        putExtra("SUBJECT_COLOR", subject.color)
                        putExtra("CLASS_LEVEL", classLevel)
                    }
                    startActivity(intent)
                }
                binding.rvSubjects.adapter = adapter
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.tvEmpty.text = error
            binding.tvEmpty.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}