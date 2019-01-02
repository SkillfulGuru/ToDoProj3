package com.example.hi.todoproj3

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), TaskRowListener {
    lateinit var _db: DatabaseReference
    var _taskList: MutableList<Task>? = null
    lateinit var _adapter: TaskAdapter
    private var listViewTask:ListView?=null
    var newTaskDesc:String?=null


    var _taskListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            loadTaskList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        _db = FirebaseDatabase.getInstance().reference
        _taskList = mutableListOf<Task>()

        _adapter = TaskAdapter(this, _taskList!!)

        listviewTask!!.setAdapter(_adapter)

        //mAdminDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Admins")

        fab.setOnClickListener { view ->

            showFooter()
        }

        btnAdd.setOnClickListener{ view ->

            addTask()
        }

        _db.orderByKey().addValueEventListener(_taskListener)



        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showFooter(){
        footer.visibility = View.VISIBLE
        fab.visibility = View.GONE
    }

    fun addTask(){

        //Declare and Initialise the Task
        val task = Task.create()

        //Set Task Description and isDone Status
        task.taskDesc = txtNewTaskDesc.text.toString()
        task.done = false

        //Get the object id for the new task from the Firebase Database
        val newTask = _db.child(Statics.FIREBASE_TASK).push()
        task.objectId = newTask.key

        //Set the values for new task in the firebase using the footer form
        newTask.setValue(task)

        //Hide the footer and show the floating button
        footer.visibility = View.GONE
        fab.visibility = View.VISIBLE

        //Reset the new task description field for reuse.
        txtNewTaskDesc.setText("")

        Toast.makeText(this, "New Task added to the List successfully" + task.objectId, Toast.LENGTH_SHORT).show()
    }

    private fun loadTaskList(dataSnapshot: DataSnapshot) {
        Log.d("MainActivity", "loadTaskList")

        val tasks = dataSnapshot.children.iterator()

        //Check if current database contains any collection
        if (tasks.hasNext()) {

            _taskList!!.clear()


            val listIndex = tasks.next()
            val itemsIterator = listIndex.children.iterator()

            //check if the collection has any task or not
            while (itemsIterator.hasNext()) {

                //get current task
                val currentItem = itemsIterator.next()
                val task = Task.create()

                //get current data in a map
                val map = currentItem.getValue() as HashMap<String, Any>

                //key will return the Firebase ID
                task.objectId = currentItem.key
                task.done = map.get("done") as Boolean?
                task.taskDesc = map.get("taskDesc") as String?
                _taskList!!.add(task)
            }
        }

        _adapter.notifyDataSetChanged()

    }

    override fun onTaskChange(objectId: String, isDone: Boolean) {
        val task = _db.child(Statics.FIREBASE_TASK).child(objectId)
        task.child("done").setValue(isDone)
        _adapter.notifyDataSetChanged()
        listviewTask!!.setAdapter(_adapter)

    }


    override fun onTaskDelete(objectId: String) {
        val task = _db.child(Statics.FIREBASE_TASK).child(objectId)
        task.removeValue()
        _adapter.notifyDataSetChanged()
        listviewTask!!.setAdapter(_adapter)
    }

    override fun editItemDialog(objectId: String, taskDesc: String) {
        val alert = AlertDialog.Builder(this)
        val itemEditText = EditText(this)

        alert.setMessage("Enter new task")
        alert.setTitle("Edit task")
        alert.setView(itemEditText)

        alert.setPositiveButton("Change") { dialog, positiveButton ->
            newTaskDesc = itemEditText.text.toString()
            val taskChild = FirebaseDatabase.getInstance().getReference().child("task").child(objectId)
            taskChild.child("taskDesc").setValue(newTaskDesc)
            _adapter.notifyDataSetChanged()
        }

        alert.show()
    }



}
