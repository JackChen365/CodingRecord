package com.cz.code.record.configurable

import com.cz.code.record.ui.CodingRecordSettingUI
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

class CodingRecordConfigurable:SearchableConfigurable{
    override fun isModified(): Boolean {
        return false
    }

    override fun getId(): String {
        return "coding_record"
    }

    override fun getDisplayName(): String {
        return "Display name"
    }

    override fun apply() {

    }

    override fun createComponent(): JComponent? {
        return CodingRecordSettingUI().container
    }

}