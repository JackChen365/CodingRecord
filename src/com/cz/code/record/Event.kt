package com.cz.code.record

enum class Event{
    /**
     * 打开项目
     */
    OPEN_PROJECT,
    /**
     * 关闭项目
     */
    CLOSE_PROJECT,
    /**
     * 打开文件
     */
    OPEN_FILE,
    /**
     * 创建一个文件
     */
    CREATE_FILE,
    /**
     * 删除一个文件
     */
    DELETE_FILE,
    /**
     * 关闭文件
     */
    CLOSE_FILE,
    /**
     * 开始编写
     */
    START_CODING,
    /**
     * 停止编写
     */
    STOP_CODING,


}