@echo off
echo ========================================
echo SmartAdvisor - Save ALL Logs to File
echo ========================================
echo.

REM 清除旧日志
echo [1/5] Clearing old logs...
adb logcat -c
echo Done.
echo.

REM 删除旧的日志文件
if exist logcat-all.txt (
    echo [2/5] Deleting old logcat-all.txt...
    del logcat-all.txt
    echo Done.
) else (
    echo [2/5] No old logcat-all.txt found.
)
echo.

REM 启动日志记录
echo [3/5] Starting log capture (ALL logs)...
echo Logs will be saved to: logcat-all.txt
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
echo Capturing ALL logs (this may be verbose)...
echo.

REM 捕获所有日志到文件
adb logcat > logcat-all.txt

echo.
echo ========================================
echo All logs saved to: logcat-all.txt
echo ========================================

