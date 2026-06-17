package com.shadowfox.todoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val priority: String, // "High" or "Low"
    val isCompleted: Boolean = false,
    val voiceNoteUri: String? = null,
    val imageAttachmentUri: String? = null
)
