package com.cz.code.record.edit;

import com.cz.code.record.service.FileEditService
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

/**
 * 文件编辑管理监听对象
 */
class FileEditorListener(private var project: Project) : FileEditorManagerListener {
    override fun selectionChanged(event: FileEditorManagerEvent) {
        super.selectionChanged(event)
        var file = event.newFile
        if(null!=file){
            FileEditService.instance.fileOpened(file)
        }
    }

    override fun fileOpened(manager: FileEditorManager, file: VirtualFile) {
        super.fileOpened(manager, file)
        val fileEditor = manager.getSelectedEditor(file)
        val document = FileDocumentManager.getInstance().getDocument(file)
        // documents are available for text files only, we do not support image editors, for example
        if (fileEditor == null || document == null) {
            return
        }
        // 顶部提示信息
        val label = JBLabel()
        label.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        label.text = statusMessage(document, file)
        manager.addTopComponent(fileEditor, label)
        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                //提示信息
                val virtualFile = FileDocumentManager.getInstance().getFile(event.document)
                if(null!=virtualFile){
                    FileEditService.instance.fileContentChanged(virtualFile,event.newFragment,event.oldFragment)
                }
                //label.text = "file:${file.name} add:${event.newFragment} old:${event.oldFragment}"
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
        //文件关闭监听
        FileEditService.instance.fileClosed(file)
    }
}