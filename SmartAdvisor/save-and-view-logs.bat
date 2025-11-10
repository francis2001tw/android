@echo off
echo ========================================
echo SmartAdvisor - Save and View Logs
echo ========================================
echo.

REM 清除旧日志
echo [1/6] Clearing old logs...
adb logcat -c
echo Done.
echo.

REM 删除旧的日志文件
if exist logcat.txt (
    echo [2/6] Deleting old logcat.txt...
    del logcat.txt
    echo Done.
) else (
    echo [2/6] No old logcat.txt found.
)
echo.

REM 显示文件保存位置
echo [3/6] Log file will be saved to:
echo %CD%\logcat.txt
echo.

REM 启动日志记录
echo [4/6] Starting log capture...
echo.
echo ========================================
echo READY! Please use the app now:
echo ========================================
echo 1. Open SmartAdvisor app on your device
echo 2. Send a message (e.g., "你好，请介绍一下自己")
echo 3. Wait for AI response to complete
echo 4. Press Ctrl+C to stop logging
echo ========================================
echo.
echo Capturing logs (showing on screen and saving to file)...
echo.

REM 捕获日志到文件并显示
adb logcat | findstr "ChatScreen DeepThinkingCard ChatServiceImpl" | tee logcat.txt

REM 如果 tee 命令不可用，使用这个替代方案
REM adb logcat > logcat.txt & adb logcat | findstr "ChatScreen DeepThinkingCard ChatServiceImpl"

echo.
echo ========================================
echo [5/6] Logs saved to: %CD%\logcat.txt
echo ========================================
echo.

REM 询问是否打开文件
echo [6/6] Opening log file...
timeout /t 2 /nobreak >nul
notepad logcat.txt

echo.
echo Done!
pause

