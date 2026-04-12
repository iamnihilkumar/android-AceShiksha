package com.example.edureach1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edureach1.databinding.ItemTopicBinding
import com.example.edureach1.models.SubjectTopic

class TopicAdapter(
    private val onTopicClick: (SubjectTopic) -> Unit
) : ListAdapter<SubjectTopic, TopicAdapter.TopicViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTopicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TopicViewHolder(
        private val binding: ItemTopicBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: SubjectTopic) {
            binding.tvTopicTitle.text = topic.title
            binding.tvTopicDescription.text = topic.description.ifEmpty {
                "Tap to explore this topic"
            }
            binding.tvTopicNumber.text = (adapterPosition + 1).toString()
            binding.root.setOnClickListener { onTopicClick(topic) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SubjectTopic>() {
        override fun areItemsTheSame(a: SubjectTopic, b: SubjectTopic) = a.id == b.id
        override fun areContentsTheSame(a: SubjectTopic, b: SubjectTopic) = a == b
    }
}