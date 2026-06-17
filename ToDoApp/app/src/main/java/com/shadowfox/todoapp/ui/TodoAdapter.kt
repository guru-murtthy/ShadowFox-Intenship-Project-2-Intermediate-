package com.shadowfox.todoapp.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shadowfox.todoapp.R
import com.shadowfox.todoapp.data.TodoEntity
import com.shadowfox.todoapp.databinding.ItemTodoBinding

class TodoAdapter(
    private val onCheckChanged: (TodoEntity, Boolean) -> Unit,
    private val onPlayVoiceNote: (String) -> Unit,
    private val onViewImage: (String) -> Unit
) : ListAdapter<TodoEntity, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: TodoEntity) {
            binding.tvTitle.text = todo.title
            
            // Set strikethrough if completed
            if (todo.isCompleted) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.alpha = 0.6f
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.alpha = 1.0f
            }

            // Priority setup
            if (todo.priority == "High") {
                binding.viewPriorityIndicator.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.priority_high)
                )
                binding.tvPriority.text = "High"
                binding.tvPriority.setTextColor(ContextCompat.getColor(binding.root.context, R.color.priority_high))
            } else {
                binding.viewPriorityIndicator.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.priority_low)
                )
                binding.tvPriority.text = "Low"
                binding.tvPriority.setTextColor(ContextCompat.getColor(binding.root.context, R.color.priority_low))
            }

            // Voice Play setup
            if (!todo.voiceNoteUri.isNullOrEmpty()) {
                binding.btnPlayVoice.visibility = View.VISIBLE
                binding.btnPlayVoice.setOnClickListener {
                    onPlayVoiceNote(todo.voiceNoteUri)
                }
            } else {
                binding.btnPlayVoice.visibility = View.GONE
            }

            // View Image setup
            if (!todo.imageAttachmentUri.isNullOrEmpty()) {
                binding.btnViewImage.visibility = View.VISIBLE
                binding.btnViewImage.setOnClickListener {
                    onViewImage(todo.imageAttachmentUri)
                }
            } else {
                binding.btnViewImage.visibility = View.GONE
            }

            // Setup Checkbox
            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = todo.isCompleted
            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(todo, isChecked)
            }
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<TodoEntity>() {
        override fun areItemsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
            return oldItem == newItem
        }
    }
}
