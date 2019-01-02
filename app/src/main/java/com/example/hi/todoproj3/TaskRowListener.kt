package com.example.hi.todoproj3

interface TaskRowListener {
    fun onTaskChange(objectId: String, isDone: Boolean)
    fun onTaskDelete(objectId: String)
    fun editItemDialog(objectId: String, taskDesc: String)
}