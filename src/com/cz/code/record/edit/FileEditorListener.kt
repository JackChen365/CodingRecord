package com.cz.code.record.edit;

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import java.text.DecimalFormat
import javax.swing.BorderFactory


class FileEditorListener(private var project: Project) : FileEditorManagerListener {
    override fun selectionChanged(event: FileEditorManagerEvent) {
        super.selectionChanged(event)
        var file = event.newFile
        println("打开${file?.name}")
    }

    override fun fileOpened(manager: FileEditorManager, file: VirtualFile) {
        super.fileOpened(manager, file)
        val fileEditor = manager.getSelectedEditor(file)
        val document = FileDocumentManager.getInstance().getDocument(file)
        // documents are available for text files only, we do not support image editors, for example
        if (fileEditor == null || document == null) {
            return
        }
        val label = JBLabel()
        label.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        label.text = statusMessage(document, file)
        manager.addTopComponent(fileEditor, label)
        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                label.text = "file:${file.name} add:${event.newFragment} old:${event.oldFragment}"
            }
        }, fileEditor)
    }

    private fun statusMessage(doc: Document, file: VirtualFile?): String {
        val file=file?:return ""
        val format = DecimalFormat.getIntegerInstance()
        return StringUtil.formatFileSize(file.length) + ", " +
                file.charset.displayName() +
                format.format(doc.textLength) + " chars, " +
                format.format(doc.lineCount) + " lines"
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        super.fileClosed(source, file)
    }
}