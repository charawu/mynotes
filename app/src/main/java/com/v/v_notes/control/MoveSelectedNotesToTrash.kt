package com.v.v_notes.control

import android.util.Log
import com.v.v_notes.data.model.Note
import com.v.v_notes.viewmodel.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun moveSelectedNotesToTrash(
    context: android.content.Context,
    noteIds: List<String>,
    coroutineScope: CoroutineScope,
    noteViewModel: NoteViewModel,
    allNotes: List<Note>
) {
    if (noteIds.isEmpty()) {
        Log.d("NoteDeletion", "没有选中任何笔记")
        return
    }

    coroutineScope.launch(Dispatchers.IO) {
        try {
            Log.d("NoteDeletion", "开始将选中的 ${noteIds.size} 条笔记移到回收站")

            // 从allNotes中获取选中的笔记
            val selectedNotes = allNotes.filter { note -> noteIds.contains(note.id) }

            // 对每个选中的笔记，调用 moveNoteToTrash（软删除）
            selectedNotes.forEach { note ->
                noteViewModel.moveNoteToTrash(note)
            }

            Log.d("NoteDeletion", "移动到回收站操作完成")
        } catch (e: Exception) {
            Log.e("NoteDeletion", "移动笔记到回收站过程中出错: ${e.message}", e)
        }
    }
}