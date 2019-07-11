package com.cz.code.record.model

class RecordInfo{
    /**
     * 打开时间
     */
    var startTimeMillis=0L
    /**
     * 当前工作时间
     */
    var stepStartTimeMillis=0L
    /**
     * 工作时间步
     */
    var codingTimeSteps= mutableMapOf<Long,Long>()

    init {
        startTimeMillis=System.currentTimeMillis()
        stepStartTimeMillis=System.currentTimeMillis()
    }

    /**
     * 开始编程
     */
    fun startCoding(){
        stepStartTimeMillis=System.currentTimeMillis()
    }

    /**
     * 添加一个编程时间段
     */
    fun addCodingTimeStep(endTimeMillis:Long){
        codingTimeSteps.put(stepStartTimeMillis,endTimeMillis)
    }
}