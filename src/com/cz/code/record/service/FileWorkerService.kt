package com.cz.code.record.service

import com.cz.code.record.Event
import com.cz.code.record.checkDispatchThread
import com.intellij.ide.util.PackageUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件写入服务对象
 */
class FileWorkerService : Thread("File worker") {

    companion object {
        private const val CODING_RECORD=".CodingRecord"
        /**
         * 最大只存100条,100条之后阻塞
         */
        private const val MAX_CAPACITY=100
        /**
         * 最长记录等级时间,保证记录时间偏差不长
         */
        private const val MAX_WAIT_TIME=2*60*1000L
        /**
         * 同步锁对象
         */
        private val LOCK = Object()

        private fun checkThread() = checkDispatchThread(FileWorkerService::class.java)
        val instance: FileWorkerService
            get() = ServiceManager.getService(FileWorkerService::class.java)
    }
    /**
     * 消息队列
     */
    private val messageQueue = LinkedList<String>()
    /**
     * 操作时间,用于比较最大时间
     */
    private var activeTimeMillis:Long=0L
    /**
     * 当前服务是否运行
     */
    @Volatile
    private var isRunning=false

    override fun run() {
        super.run()
        //检测project对象
        val timeFormatter = SimpleDateFormat("yyyy_MM_dd")
        //缓存文件
        val folder = File(System.getProperty("java.io.tmpdir"),CODING_RECORD)
        try {
            //遍历消息
            while(!interrupted()){
                val cacheFile=File(folder,"${timeFormatter.format(Date())}.txt")
                //检测并写入消息
                checkAndWriteMessage(cacheFile)
                //如果检测到停止任务，弹出
                if(!isRunning){
                    break
                }
            }
        } catch (e:Exception){
            //此处可以采集异常
            e.printStackTrace()
        } finally {
            //写入剩下所有文件
            writeAllMessage(File(folder,"${timeFormatter.format(Date())}.txt"))
        }
        println("写入服务终止！")
    }

    /**
     * 检测并写入消息
     */
    private fun checkAndWriteMessage(cacheFile: File) {
        if (messageQueue.isNotEmpty()) {
            synchronized(LOCK) {
                println("开始将文件写入文件:${cacheFile.path},当前消息个数:${messageQueue.size}")
                //取出所有消息并记录
                FileOutputStream(cacheFile,true).bufferedWriter().use { writer ->
                    while (!messageQueue.isEmpty()) {
                        val message = messageQueue.pollFirst()
                        println("消息:$message")
                        writer.append(message + "\n")
                    }
                }
                //记录操作时间
                activeTimeMillis = System.currentTimeMillis()
                //等待
                LOCK.wait(MAX_WAIT_TIME)
            }
        }
    }

    /**
     * 检测并写入消息
     */
    private fun writeAllMessage(cacheFile: File) {
        if (messageQueue.isNotEmpty()) {
            println("开始写入文件:${cacheFile.path},剩余消息个数:${messageQueue.size}")
            //取出所有消息并记录
            FileOutputStream(cacheFile,true).bufferedWriter().use { writer ->
                while (!messageQueue.isEmpty()) {
                    val message = messageQueue.pollFirst()
                    writer.append(message + "\n")
                }
            }
        }
    }

    /**
     * 创建操作目录
     */
    private fun createSubdirectory() {
        val folder = File(System.getProperty("java.io.tmpdir"),CODING_RECORD)
        if(!folder.exists()){
            val defaultProject = ProjectManager.getInstance().defaultProject
            WriteCommandAction.runWriteCommandAction(defaultProject) { folder.mkdir() }
        }
    }

    /**
     * 启动服务
     */
    fun startService(){
        //启动服务
        if(!isAlive){
            //记录启动时间
            activeTimeMillis=System.currentTimeMillis()
            //检测目录是否存在
            createSubdirectory()
            //设置运行标记
            isRunning=true
            this.start()
        }
    }

    /**
     * 停止服务
     */
    fun stopService(){
        synchronized(LOCK){
            isRunning=false
            LOCK.notify()
        }
    }

    /**
     * 添加消息到队列
     */
    fun postMessage(message:String){
        synchronized(LOCK){
            //添加消息到队列
            println("消息:$message")
            messageQueue.offerLast(message)
            if(System.currentTimeMillis()-activeTimeMillis>MAX_WAIT_TIME){
                //超过最大时间通知
                LOCK.notify()
            } else if(messageQueue.size> MAX_CAPACITY){
                //超过最大个数通知
                LOCK.notify()
            }
        }
    }

    /**
     * 添加消息到队列
     */
    fun postMessage(event:Event, file:VirtualFile?){
        //添加到队列
        var filePath= file?.path
        //发送消息
        postMessage("$event $filePath ${System.currentTimeMillis()}")
    }

    /**
     * 主动通知唤醒
     */
    fun notifyService(){
        synchronized(LOCK){
            LOCK.notify()
        }
    }
}