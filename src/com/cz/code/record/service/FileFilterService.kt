package com.cz.code.record.service

import com.cz.code.record.checkDispatchThread
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.ini4j.Reg
import java.lang.StringBuilder


/**
 * 文件编缉服务对象
 */
class FileFilterService{
    companion object {
        private fun checkThread() = checkDispatchThread(FileFilterService::class.java)
        val instance: FileFilterService
            get() = ServiceManager.getService(FileFilterService::class.java)
    }

    /**
     * 元素过滤列表
     */
    private val elementFilterList= mutableMapOf<Project,MutableList<Regex>>()

    /**
     * 启动服务
     */
    fun startService() {
        // 监听全局文件操作,如果打开.gitignore文件,监听文件变化
        ApplicationManager.getApplication().runReadAction {
            val defaultProject = ProjectManager.getInstance().defaultProject
            val findFileArray = FilenameIndex.getFilesByName(defaultProject, ".gitignore", GlobalSearchScope.allScope(defaultProject))
            //获得.gitignore
            if (findFileArray.isNotEmpty()) {
                //添加过滤数据
                val filterElement = getFilterElement(findFileArray.first())
                elementFilterList.put(defaultProject,filterElement)
            }
        }
    }

    /**
     * 更新过滤信息,当.gitignore文件内容发生变化时,触发更新
     */
    fun updateFilterElement(file: PsiFile?){
        if(null!= file){
            //更新过滤元素
            val defaultProject = ProjectManager.getInstance().defaultProject
            val filterElement = getFilterElement(file)
            elementFilterList.put(defaultProject,filterElement)
        }
    }

    /**
     * 是否过滤
     */
    fun filterFile(file: VirtualFile?, callback:(VirtualFile)->Unit){
        if(null!=file){
            //如果未被过滤掉,回调闭包
            val defaultProject = ProjectManager.getInstance().defaultProject
            val elementList = elementFilterList.get(defaultProject)
            val regex = elementList?.find { it.matches(file.path.replace("\\","/")) }
            if(null==regex){
                callback.invoke(file)
            }
        }
    }

    /**
     * 添加过滤元素
     */
    private fun getFilterElement(file: PsiFile):MutableList<Regex> {
        //清除己存在的数据列
        val elementFilterList= mutableListOf<Regex>()
        //获得内容
        val content = file.text
        val lineArray = content.lines()
        for (line in lineArray) {
            var regexString = StringBuilder(line)
            if (regexString.startsWith("*")) {
                regexString.insert(0, '.')
            } else {
                regexString.insert(0, ".*")
            }
            if (regexString.endsWith("*")) {
                regexString.insert(regexString.lastIndexOf("*"), ".")
            } else {
                regexString.append(".*")
            }
            elementFilterList.add(regexString.toString().toRegex())
        }
        //添加.gitignore文件
        elementFilterList.add(".*\\.gitignore".toRegex())
        return elementFilterList
    }

}