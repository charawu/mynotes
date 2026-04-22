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
import com.v.v_notes.addlist.RichTextEditorActivity
import com.v.v_notes.components.NoteDetailScreen
import com.v.v_notes.ui.theme.MyNotesTheme

/**
 * 笔记详情Activity
 * 承载NoteDetailScreen组件
 */
class NoteDetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_NOTE_ID = "note_id"

        /**
         * 创建跳转到笔记详情页面的Intent
         * @param context 上下文
         * @param noteId 笔记ID
         * @return 配置好的Intent
         */
        fun newIntent(context: Context, noteId: String): Intent {
            return Intent(context, NoteDetailActivity::class.java).apply {
                putExtra(EXTRA_NOTE_ID, noteId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 从Intent中获取笔记ID
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: ""

        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    //使用NoteDetailScreen组件
                    NoteDetailScreen(
                        noteId = noteId,
                        onBackClick = { finish() },
                        onEditClick = { noteId ->
                            // 启动编辑器Activity，并传递数据
                            val intent = Intent(this, RichTextEditorActivity::class.java).apply {
                            }
                            startActivity(intent)
                        },
                    )
                }
            }
        }
    }
}
