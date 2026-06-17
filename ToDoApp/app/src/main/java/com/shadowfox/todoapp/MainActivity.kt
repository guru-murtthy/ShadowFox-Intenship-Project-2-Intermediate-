package com.shadowfox.todoapp

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.shadowfox.todoapp.data.TodoEntity
import com.shadowfox.todoapp.databinding.ActivityMainBinding
import com.shadowfox.todoapp.databinding.DialogAddTaskBinding
import com.shadowfox.todoapp.ui.TodoAdapter
import com.shadowfox.todoapp.viewmodel.TodoViewModel
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TodoViewModel by viewModels()
    private lateinit var adapter: TodoAdapter

    // Recording fields
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    // Attachments tracking for dialog
    private var voiceNoteUri: String? = null
    private var imageAttachmentUri: String? = null
    private var currentDialogRecordButton: MaterialButton? = null
    private var currentDialogVoiceStatus: TextView? = null
    private var currentDialogImageStatus: TextView? = null

    // Media Player for playback
    private var mediaPlayer: MediaPlayer? = null

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            toggleRecording()
        } else {
            showSnackbar("Permission denied. Cannot record voice notes.")
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Grant read URI permission persistable
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            imageAttachmentUri = uri.toString()
            currentDialogImageStatus?.text = "Image attachment ready"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        setupFilters()
        setupAddButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TodoAdapter(
            onCheckChanged = { todo, isChecked ->
                viewModel.updateTask(todo.copy(isCompleted = isChecked))
            },
            onPlayVoiceNote = { uriString ->
                playVoiceNote(uriString)
            },
            onViewImage = { uriString ->
                showImagePreview(uriString)
            }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter

        // Swipe-to-delete handler
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val todo = adapter.currentList[position]
                viewModel.deleteTask(todo)

                Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewModel.insertTask(todo)
                    }.show()
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvTasks)
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.firstOrNull()) {
                binding.chipHigh.id -> viewModel.setPriorityFilter("High")
                binding.chipLow.id -> viewModel.setPriorityFilter("Low")
                else -> viewModel.setPriorityFilter("All")
            }
        }
    }

    private fun setupAddButton() {
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.tasks.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    private fun showAddTaskDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        val dialogBinding = DialogAddTaskBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Reset attachment tracking
        voiceNoteUri = null
        imageAttachmentUri = null
        currentDialogRecordButton = dialogBinding.btnRecordVoice
        currentDialogVoiceStatus = dialogBinding.tvVoiceStatus
        currentDialogImageStatus = dialogBinding.tvImageStatus

        dialogBinding.btnRecordVoice.setOnClickListener {
            checkRecordPermissionAndToggle()
        }

        dialogBinding.btnAttachImage.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        dialogBinding.btnCancel.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTaskTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                if (isRecording) {
                    stopRecording()
                }

                val priority = if (dialogBinding.rbHigh.isChecked) "High" else "Low"
                val task = TodoEntity(
                    title = title,
                    priority = priority,
                    isCompleted = false,
                    voiceNoteUri = voiceNoteUri,
                    imageAttachmentUri = imageAttachmentUri
                )

                viewModel.insertTask(task)
                dialog.dismiss()
            } else {
                dialogBinding.tilTaskTitle.error = "Title cannot be empty"
            }
        }

        dialog.setOnDismissListener {
            if (isRecording) {
                stopRecording()
            }
        }

        dialog.show()
    }

    private fun checkRecordPermissionAndToggle() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            toggleRecording()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun toggleRecording() {
        if (!isRecording) {
            audioFile = File(cacheDir, "voice_note_${System.currentTimeMillis()}.3gp")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                try {
                    prepare()
                    start()
                    isRecording = true
                    currentDialogRecordButton?.text = "Stop Recording"
                    currentDialogRecordButton?.icon = ContextCompat.getDrawable(this@MainActivity, android.R.drawable.ic_media_pause)
                    currentDialogVoiceStatus?.text = "Recording in progress..."
                } catch (e: Exception) {
                    e.printStackTrace()
                    showSnackbar("Failed to start recording")
                }
            }
        } else {
            stopRecording()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
        isRecording = false
        voiceNoteUri = Uri.fromFile(audioFile).toString()
        currentDialogRecordButton?.text = "Record Voice"
        currentDialogRecordButton?.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_btn_speak_now)
        currentDialogVoiceStatus?.text = "Recorded voice note successfully"
    }

    private fun playVoiceNote(uriString: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(this@MainActivity, Uri.parse(uriString))
                prepare()
                start()
                showSnackbar("Playing voice memo...")
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackbar("Failed to play recording")
            }
        }
    }

    private fun showImagePreview(uriString: String) {
        val previewDialog = Dialog(this)
        previewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val imageView = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            try {
                setImageURI(Uri.parse(uriString))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        previewDialog.setContentView(imageView)
        previewDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        previewDialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        previewDialog.show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaRecorder?.release()
    }
}
