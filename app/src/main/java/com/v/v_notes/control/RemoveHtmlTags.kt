package com.v.v_notes.control

fun removeHtmlTags(html: String): String {
    return html.replace(Regex("<[^>]*>"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
}