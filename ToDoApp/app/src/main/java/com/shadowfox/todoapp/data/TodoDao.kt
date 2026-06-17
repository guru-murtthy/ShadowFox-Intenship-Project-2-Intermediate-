package com.shadowfox.todoapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(todo: TodoEntity)

    @Update
    suspend fun updateTask(todo: TodoEntity)

    @Delete
    suspend fun deleteTask(todo: TodoEntity)

    @Query("SELECT * FROM todo_table WHERE title LIKE :searchQuery AND (:priorityFilter = 'All' OR priority = :priorityFilter) ORDER BY CASE WHEN priority = 'High' THEN 0 ELSE 1 END ASC, id DESC")
    fun searchAndFilterTasks(searchQuery: String, priorityFilter: String): LiveData<List<TodoEntity>>
}
