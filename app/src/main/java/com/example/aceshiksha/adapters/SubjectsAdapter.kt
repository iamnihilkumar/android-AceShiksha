package com.nikhil.aceshiksha.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.aceshiksha.databinding.ItemSubjectBinding
import com.nikhil.aceshiksha.models.Subject

class SubjectsAdapter(
    private val subjects: List<Subject>,
    private val onSubjectClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    inner class SubjectViewHolder(val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        with(holder.binding) {
            tvSubjectName.text = subject.name
            tvSubjectDesc.text = subject.description
            tvSubjectInitial.text = subject.name.first().toString()

            // Set background color dynamically
            val color = try { Color.parseColor(subject.color) }
            catch (e: Exception) { Color.parseColor("#4CAF50") }

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 40f
                setColor(color)
            }
            layoutSubjectCard.background = drawable

            root.setOnClickListener { onSubjectClick(subject) }
        }
    }

    override fun getItemCount() = subjects.size
}