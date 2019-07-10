package com.cz.code.record.edit

import com.intellij.codeInsight.actions.MultiCaretCodeInsightActionHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile


class FileTypeHandler(runForEachCaret: Boolean) : MultiCaretCodeInsightActionHandler() {
    override fun invoke(p0: Project, p1: Editor, p2: Caret, p3: PsiFile) {
        print(p2)
    }

}