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
        // 文件变化监听
        val fileEditorListener = FileEditorListener(project)
        project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,fileEditorListener)

    }

    override fun projectOpened() {
        super.projectOpened()
    }
}