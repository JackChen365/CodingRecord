package com.cz.code.record.component

import com.cz.code.record.edit.FileEditorListener
import com.cz.code.record.edit.FileTypeHandler
import com.cz.code.record.service.FileWorkerService
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
        //启动写入服务
        FileWorkerService.instance.startService(project)

        // 文件切换,以文件变化监听
        project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,FileEditorListener(project))
        // 文件操作变化监听
        VirtualFileManager.getInstance().addVirtualFileListener(object : VirtualFileListener{
            override fun fileDeleted(event: VirtualFileEvent) {
                super.fileDeleted(event)
                val file = event.file
                FileWorkerService.instance.postMessage("REMOVE ${file.path} ${System.currentTimeMillis()}")
            }

            override fun fileCreated(event: VirtualFileEvent) {
                super.fileCreated(event)
                val file = event.file
                FileWorkerService.instance.postMessage("CREATE ${file.path} ${System.currentTimeMillis()}")
            }
        })

    }

    override fun projectOpened() {
        super.projectOpened()
    }
}