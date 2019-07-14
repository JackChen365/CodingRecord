package com.cz.code.record.service

import com.cz.code.record.checkDispatchThread
import com.intellij.ide.util.PackageUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
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
     * 插件Project信息对象
     */
    private var project: Project?=null
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
        val project=project?:throw IllegalThreadStateException("You should use startService to start this service!")
        //检测project对象
        val timeFormatter = SimpleDateFormat("yyyy_MM_dd")
        //缓存文件
        val projectDir = File(project.guessProjectDir()?.path,".record")
        val cacheFile=File(projectDir,"${timeFormatter.format(Date())}.txt")
        try {
            //遍历消息
            while(!interrupted()){
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
            checkAndWriteMessage(cacheFile)
        }
        println("写入服务终止！")
    }

    /**
     * 检测并写入消息
     */
    private fun checkAndWriteMessage(cacheFile: File) {
        if (messageQueue.isNotEmpty()) {
            synchronized(LOCK) {
                println("开始将文件写入文件,当前消息个数:${messageQueue.size}")
                //取出所有消息并记录
                FileOutputStream(cacheFile,true).bufferedWriter().use { writer ->
                    while (!messageQueue.isEmpty()) {
                        val message = messageQueue.pollFirst()
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
     * 创建操作目录
     */
    private fun createSubdirectory(project: Project) {
        val projectDir = project.guessProjectDir()
        if (null != projectDir) {
            val directory = PsiManager.getInstance(project).findDirectory(projectDir)
            if (null != directory) {
                WriteCommandAction.runWriteCommandAction(project) {
                    PackageUtil.findOrCreateSubdirectory(directory, ".record")
                }
            }
        }
    }

    /**
     * 启动服务
     */
    fun startService(project: Project){
        this.project=project
        //创建操作目录
        createSubdirectory(project)
        //记录启动时间
        activeTimeMillis=System.currentTimeMillis()
        //设置运行标记
        isRunning=true
        //启动服务
        this.start()
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
            //添加到队列
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
}