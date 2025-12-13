package com.example.catalogoproductos

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownRegexTest {
    private fun markdownToHtml(md: String): String {
        var s = md
        s = s.replace(Regex("^# (.*)$", RegexOption.MULTILINE), "<h1>$1</h1>")
        s = s.replace(Regex("^## (.*)$", RegexOption.MULTILINE), "<h2>$1</h2>")
        s = s.replace(Regex("\n- (.*)", RegexOption.MULTILINE), "<br/>• $1")
        s = s.replace(Regex("""\*\*(.*?)\*\*""", RegexOption.DOT_MATCHES_ALL), "<b>$1</b>")
        s = s.replace(Regex("_(.*?)_", RegexOption.DOT_MATCHES_ALL), "<i>$1</i>")
        return s
    }

    @Test
    fun `negrita con regex triple comillas`() {
        assertEquals("<b>hola</b>", markdownToHtml("**hola**"))
    }

    @Test
    fun `cursiva`() {
        assertEquals("<i>hola</i>", markdownToHtml("_hola_"))
    }

    @Test
    fun `encabezados y lista`() {
        val html = markdownToHtml("# Titulo\n- item")
        assert(html.contains("<h1>Titulo</h1>"))
        assert(html.contains("• item"))
    }
}
