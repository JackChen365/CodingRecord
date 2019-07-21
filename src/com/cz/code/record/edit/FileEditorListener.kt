package com.cz.code.record.edit;

import com.cz.code.record.service.FileEditService
import com.cz.code.record.service.FileFilterService
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
import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import java.text.DecimalFormat
import javax.swing.BorderFactory

/**
 * 文件编辑管理监听对象
 */
class FileEditorListener(private var project: Project) : FileEditorManagerListener {
    /**
     * 文件方档监听对象,如果存在,不需要重复添加,用于防止重复打开,关闭操作引起的重复添加监听问题
     */
    private val fileDocumentListeners= mutableMapOf<VirtualFile,DocumentListener>()

    override fun fileOpened(manager: FileEditorManager, file: VirtualFile) {
        super.fileOpened(manager, file)
        if(!file.name.endsWith(".gitignore")){
            FileFilterService.instance.filterFile(file){
                //文件打开
//                FileEditService.instance.fileOpened(it)
                // 添加文本文监听
                addFileDocumentListener(manager, it)
            }
        }
    }

    /**
     * 添加文本文监听
     */
    private fun addFileDocumentListener(manager: FileEditorManager, file: VirtualFile) {
        val fileEditor = manager.getSelectedEditor(file)
        val document = FileDocumentManager.getInstance().getDocument(file)
        // documents are available for text files only, we do not support image editors, for example
        if (fileEditor == null || document == null) {
            return
        }
        // 顶部提示信息
        val label = JBLabel()
        label.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        //        label.text = statusMessage(document, file)
        //        manager.addTopComponent(fileEditor, label)
        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                //提示信息
                val virtualFile = FileDocumentManager.getInstance().getFile(event.document)
                //查看是否需要过滤
                FileFilterService.instance.filterFile(virtualFile) {
                    FileEditService.instance.fileContentChanged(it, event.newFragment, event.oldFragment)
                }
                //label.text = "file:${file.name} add:${event.newFragment} old:${event.oldFragment}"
            }
        }, fileEditor)
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        super.selectionChanged(event)
        val file = event.newFile
        if(null!=file&&!file.name.endsWith(".gitignore")){
            FileFilterService.instance.filterFile(file,FileEditService.instance::fileSelectionChanged)
        }
    }

    private fun statusMessage(doc: Document, file: VirtualFile?): String {
        val file=file?:return ""
        val format = DecimalFormat.getIntegerInstance()
        return StringUtil.formatFileSize(file.length) + ", " +
                file.charset.displayName() +
                format.format(doc.textLength) + " chars, " +
                format.format(doc.lineCount) + " lines"
    }

    override fun fileClosed(manager: FileEditorManager, file: VirtualFile) {
        super.fileClosed(manager, file)
        //如果关闭.gitignore文件,更新文件
        if(file.name.endsWith(".gitignore")){
            //更新配置
            FileFilterService.instance.updateFilterElement(file.toPsiFile(project))
        } else {
            //文件关闭监听
            FileFilterService.instance.filterFile(file,FileEditService.instance::fileClosed)
        }
        //移除监听
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return
        val documentListener = fileDocumentListeners.get(file)
        if(null!=documentListener){
            document.removeDocumentListener(documentListener)
        }
    }
}