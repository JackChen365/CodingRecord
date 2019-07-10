package com.cz.code.record.action

import com.cz.code.record.edit.FileTypeHandler
import com.intellij.codeInsight.actions.MultiCaretCodeInsightAction
import com.intellij.codeInsight.actions.MultiCaretCodeInsightActionHandler
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class FileEditorAction: MultiCaretCodeInsightAction() {
    private val typeHandler= FileTypeHandler(true)

    override fun getHandler(): MultiCaretCodeInsightActionHandler {
        return typeHandler
    }

    override fun actionPerformed(e: AnActionEvent) {
        super.actionPerformed(e)
        val project = e.getData(CommonDataKeys.PROJECT)
        val editor = e.getData(CommonDataKeys.EDITOR)
        val currentCaret = editor?.caretModel?.currentCaret
        print(currentCaret)
    }
}