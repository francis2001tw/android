@echo off
echo ============================================================
echo 通用对话模块 - 构建测试
echo ============================================================
echo.

echo [1/3] 检查 Gradle...
where gradle >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Gradle 未安装或不在 PATH 中
    echo 请安装 Gradle 或使用 gradlew.bat
    pause
    exit /b 1
)
echo ✅ Gradle 已安装
echo.

echo [2/3] 清理项目...
call gradle clean
if %ERRORLEVEL% neq 0 (
    echo ❌ 清理失败
    pause
    exit /b 1
)
echo ✅ 清理完成
echo.

echo [3/3] 构建项目...
call gradle build --info
if %ERRORLEVEL% neq 0 (
    echo ❌ 构建失败
    pause
    exit /b 1
)
echo ✅ 构建成功
echo.

echo ============================================================
echo 构建测试完成!
echo ============================================================
echo.
echo 要运行示例程序，请执行:
echo   gradle run
echo.
pause

