package com.example.edureach1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.edureach1.databinding.ItemGeneratedQuestionBinding
import com.example.edureach1.models.GameQuestion

class GeneratedQuestionAdapter(
    private val questions: MutableList<GameQuestion>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<GeneratedQuestionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGeneratedQuestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    override fun getItemCount() = questions.size

    inner class ViewHolder(private val binding: ItemGeneratedQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(q: GameQuestion, position: Int) {
            binding.tvQuestionNumber.text = "Q${position + 1}"
            binding.tvQuestionText.text = q.questionText
            binding.tvOptionA.text = "A) ${q.optionA}"
            binding.tvOptionB.text = "B) ${q.optionB}"
            binding.tvOptionC.text = "C) ${q.optionC}"
            binding.tvOptionD.text = "D) ${q.optionD}"
            binding.tvCorrectAnswer.text = "✓ Answer: ${q.correctAnswer}"
            binding.btnDelete.setOnClickListener { onDelete(adapterPosition) }
        }
    }
}
