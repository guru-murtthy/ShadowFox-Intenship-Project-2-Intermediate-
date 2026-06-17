package com.shadowfox.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shadowfox.todoapp.data.TodoDatabase
import com.shadowfox.todoapp.data.TodoEntity
import com.shadowfox.todoapp.data.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TodoRepository
    
    private val searchQuery = MutableLiveData<String>("")
    private val priorityFilter = MutableLiveData<String>("All")

    val tasks: MediatorLiveData<List<TodoEntity>> = MediatorLiveData()

    private var currentSource: LiveData<List<TodoEntity>>? = null

    init {
        val todoDao = TodoDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)

        tasks.addSource(searchQuery) { query ->
            updateTaskSource(query, priorityFilter.value ?: "All")
        }
        tasks.addSource(priorityFilter) { filter ->
            updateTaskSource(searchQuery.value ?: "", filter)
        }

        updateTaskSource("", "All")
    }

    private fun updateTaskSource(query: String, filter: String) {
        currentSource?.let { tasks.removeSource(it) }
        val newSource = repository.searchAndFilterTasks(query, filter)
        currentSource = newSource
        tasks.addSource(newSource) { list ->
            tasks.value = list
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setPriorityFilter(filter: String) {
        priorityFilter.value = filter
    }

    fun insertTask(todo: TodoEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTask(todo)
    }

    fun updateTask(todo: TodoEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(todo)
    }

    fun deleteTask(todo: TodoEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTask(todo)
    }
}
