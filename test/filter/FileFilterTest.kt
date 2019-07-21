package filter

import java.io.File
import java.lang.StringBuilder


/**
 * 文件过滤测试
 */
fun main() {
    //查找到.gitignore文件,检测出所有过滤项,并过滤掉本项目内所有文件
    val elementFilterList= mutableListOf<Regex>()
    File(".gitignore").useLines { lines->
        for(line in lines){
            var regexString=StringBuilder(line)
            if(regexString.startsWith("*")){
                regexString.insert(0,'.')
            } else {
                regexString.insert(0,".*")
            }
            if(regexString.endsWith("*")){
                regexString.insert(regexString.lastIndexOf("*"),".")
            }
            println(regexString)
            elementFilterList.add(regexString.toString().toRegex())
        }
    }
    println("以下是过滤项--------------------------")
    //遍历当前项目所有文件
    File(".").walk().maxDepth(Integer.MAX_VALUE).forEach{ file->
        val regex = elementFilterList.find { it.matches(file.path.replace("\\","/")) }
        if(null!=regex){
            //此目录被过滤
            println("${file.path} ----------- $regex")
        }
    }
}