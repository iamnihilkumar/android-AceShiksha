package com.example.edureach1.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edureach1.adapters.StudentListAdapter
import com.example.edureach1.databinding.ActivityTeacherStudentListBinding
import com.example.edureach1.viewmodels.TeacherStudentListViewModel

class TeacherStudentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherStudentListBinding
    private val viewModel: TeacherStudentListViewModel by viewModels()
    private lateinit var adapter: StudentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        binding.btnBack.setOnClickListener { finish() }

        viewModel.loadStudents()
    }

    private fun setupRecyclerView() {
        adapter = StudentListAdapter { student ->
            // Open StudentReportActivity with that student's userId
            val intent = Intent(this, StudentReportActivity::class.java).apply {
                putExtra("VIEW_MODE", "teacher")          // tells the screen it's teacher-view
                putExtra("TARGET_USER_ID", student.userId)
                putExtra("STUDENT_NAME", student.name)
            }
            startActivity(intent)
        }
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterStudents(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.students.observe(this) { students ->
            adapter.submitList(students)
            binding.tvStudentCount.text = "${students.size} student(s) registered"
            binding.layoutEmpty.visibility = if (students.isEmpty()) View.VISIBLE else View.GONE
            binding.rvStudents.visibility = if (students.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                binding.tvStudentCount.text = "Error: $it"
            }
        }
    }
}