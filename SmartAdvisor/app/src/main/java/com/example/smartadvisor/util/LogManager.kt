package com.example.smartadvisor.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志管理器 - 自动保存应用日志到文件
 */
object LogManager {
    private const val TAG = "LogManager"
    private const val LOG_FILE_NAME = "smartadvisor_latest.log"
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024 // 5MB
    
    private var isCapturing = false
    private var logProcess: Process? = null
    
    /**
     * 开始捕获日志
     */
    fun startCapture(context: Context) {
        if (isCapturing) {
            Log.d(TAG, "Already capturing logs")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isCapturing = true
                val logFile = getLogFile(context)
                
                // 清空旧日志
                logFile.writeText("")
                
                Log.d(TAG, "Starting log capture to: ${logFile.absolutePath}")
                
                // 清除旧的 logcat 缓冲区
                Runtime.getRuntime().exec("logcat -c").waitFor()
                
                // 启动 logcat 进程，只捕获我们关心的标签
                val command = arrayOf(
                    "logcat",
                    "-v", "time",
                    "ChatServiceImpl:D",
                    "ChatScreen:D",
                    "MessageBubble:D",
                    "DeepThinkingCard:D",
                    "ChatViewModel:D",
                    "DeepSeekProvider:D",
                    "ChatActivity:D",
                    "*:S"  // 静默其他所有标签
                )
                
                logProcess = Runtime.getRuntime().exec(command)
                val reader = BufferedReader(InputStreamReader(logProcess!!.inputStream))
                
                logFile.bufferedWriter().use { writer ->
                    writer.write("=".repeat(80) + "\n")
                    writer.write("SmartAdvisor Debug Log\n")
                    writer.write("Started at: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                    writer.write("=".repeat(80) + "\n\n")
                    writer.flush()
                    
                    var line: String?
                    while (reader.readLine().also { line = it } != null && isCapturing) {
                        writer.write(line + "\n")
                        writer.flush()
                        
                        // 检查文件大小，防止过大
                        if (logFile.length() > MAX_LOG_SIZE) {
                            writer.write("\n[LOG FILE SIZE LIMIT REACHED - STOPPING CAPTURE]\n")
                            break
                        }
                    }
                }
                
                Log.d(TAG, "Log capture stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing logs", e)
            } finally {
                isCapturing = false
                logProcess?.destroy()
                logProcess = null
            }
        }
    }
    
    /**
     * 停止捕获日志
     */
    fun stopCapture() {
        isCapturing = false
        logProcess?.destroy()
        logProcess = null
        Log.d(TAG, "Stopping log capture")
    }
    
    /**
     * 获取日志文件
     */
    fun getLogFile(context: Context): File {
        val logDir = File(context.getExternalFilesDir(null), "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        return File(logDir, LOG_FILE_NAME)
    }
    
    /**
     * 获取日志文件路径
     */
    fun getLogFilePath(context: Context): String {
        return getLogFile(context).absolutePath
    }
    
    /**
     * 读取日志内容
     */
    fun readLog(context: Context): String {
        val logFile = getLogFile(context)
        return if (logFile.exists()) {
            logFile.readText()
        } else {
            "No log file found"
        }
    }
    
    /**
     * 清除日志
     */
    fun clearLog(context: Context) {
        val logFile = getLogFile(context)
        if (logFile.exists()) {
            logFile.delete()
        }
        Log.d(TAG, "Log file cleared")
    }
    
    /**
     * 检查是否正在捕获
     */
    fun isCapturing(): Boolean = isCapturing
}

