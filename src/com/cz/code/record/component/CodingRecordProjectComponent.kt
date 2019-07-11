package com.cz.code.record.component

import com.cz.code.record.edit.FileEditorListener
import com.cz.code.record.edit.FileTypeHandler
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.codeInsight.intention.IntentionManager
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.vfs.*


class CodingRecordProjectComponent(private val project:Project):ProjectComponent{

    override fun getComponentName(): String {
        return "CodingRecord"
    }

    override fun disposeComponent() {
        super.disposeComponent()
    }

    override fun projectClosed() {
        super.projectClosed()
    }

    override fun initComponent() {
        super.initComponent()
        // 文件切换,以文件变化监听
        val fileEditorListener = FileEditorListener(project)
        project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,fileEditorListener)
        // 文件操作变化监听
        VirtualFileManager.getInstance().addVirtualFileListener(object : VirtualFileListener{
            override fun fileDeleted(event: VirtualFileEvent) {
                super.fileDeleted(event)
                val file = event.file
                println("文件删除:${file.name}")
            }

            override fun fileCreated(event: VirtualFileEvent) {
                super.fileCreated(event)
                val file = event.file
                println("文件创建:${file.name}")
            }
        })

    }

    override fun projectOpened() {
        super.projectOpened()
    }
}