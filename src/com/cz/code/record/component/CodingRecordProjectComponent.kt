package com.cz.code.record.component

import com.cz.code.record.Event
import com.cz.code.record.edit.FileEditorListener
import com.cz.code.record.service.FileFilterService
import com.cz.code.record.service.FileWorkerService
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager


class CodingRecordProjectComponent(private val project:Project):ProjectComponent{

    override fun getComponentName(): String {
        return "CodingRecord"
    }

    override fun disposeComponent() {
        super.disposeComponent()
    }

    override fun projectClosed() {
        println("关闭项目!")
        //关闭项目
        FileWorkerService.instance.postMessage("${Event.CLOSE_PROJECT} 关闭项目 ${System.currentTimeMillis()}")
        //关闭服务
        FileWorkerService.instance.stopService()
        super.projectClosed()
    }

    override fun initComponent() {
        super.initComponent()
        //启动写入服务
        FileWorkerService.instance.startService()
        //文件过滤监听服务
        FileFilterService.instance.startService()
        //打开项目
        FileWorkerService.instance.postMessage("${Event.OPEN_PROJECT} 打开项目 ${System.currentTimeMillis()}")
        // 文件切换,以文件变化监听
        project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,FileEditorListener(project))
        // 文件操作变化监听
        VirtualFileManager.getInstance().addVirtualFileListener(object : VirtualFileListener{
            override fun fileDeleted(event: VirtualFileEvent) {
                super.fileDeleted(event)
                val file = event.file
                FileWorkerService.instance.postMessage(Event.DELETE_FILE,file)
            }

            override fun fileCreated(event: VirtualFileEvent) {
                super.fileCreated(event)
                val file = event.file
                FileWorkerService.instance.postMessage(Event.CREATE_FILE,file)
            }
        })

    }

    override fun projectOpened() {
        super.projectOpened()
    }
}