package com.nikhil.aceshiksha.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.aceshiksha.databinding.ItemQuizBinding
import com.nikhil.aceshiksha.models.Quiz

class QuizAdapter(
    private val onQuizClick: (Quiz) -> Unit
) : ListAdapter<Quiz, QuizAdapter.QuizViewHolder>(DiffCallback()) {

    inner class QuizViewHolder(private val binding: ItemQuizBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(quiz: Quiz) {
            binding.tvQuizTitle.text = quiz.title
            binding.tvQuizSubject.text = quiz.subject
            binding.tvQuizClass.text = "Class ${quiz.classLevel}"
            binding.tvPublishStatus.text = if (quiz.isPublished) "✅ Published" else "⏳ Draft"
            binding.root.setOnClickListener { onQuizClick(quiz) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Quiz>() {
        override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz) =
            oldItem.quizId == newItem.quizId
        override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz) =
            oldItem == newItem
    }
}