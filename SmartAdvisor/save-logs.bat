@echo off
echo ========================================
echo SmartAdvisor - Save Logs to File
echo ========================================
echo.

REM 清除旧日志
echo [1/5] Clearing old logs...
adb logcat -c
echo Done.
echo.

REM 删除旧的日志文件
if exist logcat.txt (
    echo [2/5] Deleting old logcat.txt...
    del logcat.txt
    echo Done.
) else (
    echo [2/5] No old logcat.txt found.
)
echo.

REM 启动日志记录（后台）
echo [3/5] Starting log capture...
echo Logs will be saved to: logcat.txt
echo.
echo ========================================
echo READY! Please use the app now:
echo ========================================
echo 1. Open SmartAdvisor app on your device
echo 2. Send a message
echo 3. Wait for AI response to complete
echo 4. Press Ctrl+C to stop logging
echo ========================================
echo.
echo Capturing logs...
echo.

REM 捕获日志到文件，同时显示在屏幕上
adb logcat | findstr "ChatScreen DeepThinkingCard ChatServiceImpl" > logcat.txt & adb logcat | findstr "ChatScreen DeepThinkingCard ChatServiceImpl"

echo.
echo ========================================
echo Logs saved to: logcat.txt
echo ========================================

