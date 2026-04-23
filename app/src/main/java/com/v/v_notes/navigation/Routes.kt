package com.v.v_notes.navigation

sealed class Route(val route: String) {
    object Home : Route("home")
    object Archive : Route("archive")
    object Trash : Route("trash")

    // 不在底部菜单中的项目
    object Setting : Route("setting")
    object Alert : Route("alert")

    // 详情页
    object NoteDetail : Route("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }

    // 编辑页
    object RichTextEditor : Route("rich_text_editor")
}