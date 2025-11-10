@echo off
echo ========================================
echo SmartAdvisor Debug Logs
echo ========================================
echo.
echo Clearing old logs...
adb logcat -c
echo.
echo Watching for debug logs...
echo Press Ctrl+C to stop
echo.
adb logcat | findstr "ChatScreen DeepThinkingCard ChatServiceImpl"

