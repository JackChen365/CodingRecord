package com.cz.code.record.service

import com.cz.code.record.cancelRequest
import com.cz.code.record.checkDispatchThread
import com.cz.code.record.invokeLater
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection

/**
 * 文件编缉服务对象
 */
class FileEditService{
    companion object {
        private fun checkThread() = checkDispatchThread(FileEditService::class.java)
        val instance: FileEditService
            get() = ServiceManager.getService(FileEditService::class.java)

    }

    /**
     * 间隔时长,停止操作超过此时长,代表暂停编码
     */
    private var suspendTimeMillis=30*1000
    /**
     * 活动时间
     */
    private var activeTimeMillis=0L
    /**
     * 当前操作文件
     */
    private var currentFile:VirtualFile?=null
    /**
     * 配置变化通知
     */
    private var messageBus: MessageBusConnection? = null
    /**
     * 观察狗事件对象,用于每隔1秒,检测有没有产生新动作
     */
    private val watchdogRunnable= object :Runnable {
        override fun run() {
            if(System.currentTimeMillis()-activeTimeMillis>suspendTimeMillis){
                //超时,记录间隔
                FileWorkerService.instance.postMessage("TIME-CLOSE ${currentFile?.path} ${System.currentTimeMillis()}")
            } else {
                //继续轮询
                println("轮询:${System.currentTimeMillis()-activeTimeMillis} $suspendTimeMillis")
                invokeLater(1000, this)
            }
        }
    }


    fun install() {
        checkThread()
//        if (messageBus != null) {
////            return
////        }
        //配置变化监听
//        messageBus = ApplicationManager
//                .getApplication()
//                .messageBus
//                .connect()
//                .apply {
//                    subscribe(SettingsChangeListener.TOPIC, object : SettingsChangeListener {
//                        override fun onTranslatorChanged(settings: Settings, translatorId: String) {
//                            setTranslator(translatorId)
//                        }
//                    })
//                }
    }

    /**
     * 文件打开
     */
    fun fileOpened(file: VirtualFile){
        println("打开文件:${file.name}")
        FileWorkerService.instance.postMessage("TIME-END ${currentFile?.path} ${System.currentTimeMillis()}")
        //打开新文件
        FileWorkerService.instance.postMessage("OPEN ${file.path} ${System.currentTimeMillis()}")
        FileWorkerService.instance.postMessage("TIME-START ${file.path} ${System.currentTimeMillis()}")
        //记录当前打开文件
        this.currentFile=file
        //记录操作时间
        activeTimeMillis=System.currentTimeMillis()
        //取消未执行的事件
        cancelRequest(watchdogRunnable)
        //继续观察
        invokeLater(1000, watchdogRunnable)
    }

    /**
     * 获得以当前项目为根目录的文件地址
     */
    private fun getProjectFilePath(file:VirtualFile):String{
        return file.path
    }

    /**
     * 文件内容变化,此处可以细化
     * 当前每隔一段时间检测一下,比如10秒内,未编辑,则增加一个编程时间段.
     */
    fun fileContentChanged(file: VirtualFile,newText:CharSequence,oldText:CharSequence){
        if(System.currentTimeMillis()-activeTimeMillis>suspendTimeMillis){
            //如果超时
            val currentFile=this.currentFile
            if(null!=currentFile){
                //开始编程
                FileWorkerService.instance.postMessage("OPEN ${file.path} ${System.currentTimeMillis()}")
                FileWorkerService.instance.postMessage("TIME-START ${file.path} ${System.currentTimeMillis()}")
            }
        }
        activeTimeMillis=System.currentTimeMillis()
        cancelRequest(watchdogRunnable)
        invokeLater(1000, watchdogRunnable)
    }

    /**
     * 当文件关闭时
     */
    fun fileClosed(file: VirtualFile){
        println("当前文件:${currentFile?.name} 关闭文件:${file.name} 是否是当前文件:${file==currentFile}")
        if(file == currentFile){
            FileWorkerService.instance.postMessage("TIME-END ${file.path} ${System.currentTimeMillis()}")
            FileWorkerService.instance.postMessage("CLOSE ${file.path} ${System.currentTimeMillis()}")
            cancelRequest(watchdogRunnable)
        }
    }

    fun uninstall() {
        checkThread()
        messageBus?.disconnect()
        messageBus = null
    }
}