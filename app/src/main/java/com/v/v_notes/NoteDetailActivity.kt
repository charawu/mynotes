package com.v.v_notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.v.v_notes.components.NoteDetailScreen
import com.v.v_notes.ui.theme.MyNotesTheme

class NoteDetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_NOTE_ID = "note_id"

        fun newIntent(context: Context, noteId: String): Intent {
            return Intent(context, NoteDetailActivity::class.java).apply {
                putExtra(EXTRA_NOTE_ID, noteId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //从intent中获取笔记id
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: ""

        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NoteDetailScreen(
                        noteId = noteId,
                        onBackClick = { finish() },
                    )
                }
            }
        }
    }
}
