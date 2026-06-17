package com.shadowfox.todoapp.data

import androidx.lifecycle.LiveData

class TodoRepository(private val todoDao: TodoDao) {

    fun searchAndFilterTasks(query: String, priorityFilter: String): LiveData<List<TodoEntity>> {
        return todoDao.searchAndFilterTasks("%$query%", priorityFilter)
    }

    suspend fun insertTask(todo: TodoEntity) {
        todoDao.insertTask(todo)
    }

    suspend fun updateTask(todo: TodoEntity) {
        todoDao.updateTask(todo)
    }

    suspend fun deleteTask(todo: TodoEntity) {
        todoDao.deleteTask(todo)
    }
}
