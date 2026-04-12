package com.example.edureach1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edureach1.databinding.ItemStudentBinding
import com.example.edureach1.models.TeacherStudentReport

class StudentListAdapter(
    private val onStudentClick: (TeacherStudentReport) -> Unit
) : ListAdapter<TeacherStudentReport, StudentListAdapter.StudentViewHolder>(DiffCallback()) {

    inner class StudentViewHolder(private val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: TeacherStudentReport) {
            binding.tvStudentName.text = student.name
            binding.tvStudentEmail.text = student.email
            binding.tvStudentClass.text = "Class ${student.classLevel}"
            binding.tvStudentXp.text = "${student.xp} XP"
            binding.root.setOnClickListener { onStudentClick(student) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<TeacherStudentReport>() {
        override fun areItemsTheSame(a: TeacherStudentReport, b: TeacherStudentReport) =
            a.userId == b.userId
        override fun areContentsTheSame(a: TeacherStudentReport, b: TeacherStudentReport) =
            a == b
    }
}