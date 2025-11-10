@echo off
setlocal

REM 设置日志文件路径
set LOG_FILE=%~dp0logcat.txt

echo ========================================
echo SmartAdvisor - Capture Logs
echo ========================================
echo.
echo Log file location:
echo %LOG_FILE%
echo.

REM 清除旧日志
echo Clearing old logs...
adb logcat -c
if exist "%LOG_FILE%" del "%LOG_FILE%"
echo.

echo ========================================
echo INSTRUCTIONS:
echo ========================================
echo 1. Open SmartAdvisor app on your device
echo 2. Send a message
echo 3. Wait for AI response to complete
echo 4. Press Ctrl+C when done
echo ========================================
echo.
echo Starting capture...
echo.

REM 使用 PowerShell 的 Tee-Object 同时显示和保存
powershell -Command "& {adb logcat | Select-String 'ChatScreen|DeepThinkingCard|ChatServiceImpl' | Tee-Object -FilePath '%LOG_FILE%'}"

echo.
echo ========================================
echo Logs saved to:
echo %LOG_FILE%
echo ========================================
echo.
echo Opening log file...
notepad "%LOG_FILE%"

