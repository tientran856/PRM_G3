@echo off
REM Script test deep link cho Windows
REM Sử dụng: test_deep_link.bat {recipeId}

echo ========================================
echo Test Deep Link - PRM Recipe App
echo ========================================
echo.

if "%1"=="" (
    echo ERROR: Vui long nhap Recipe ID
    echo.
    echo Su dung: test_deep_link.bat {recipeId}
    echo Vi du: test_deep_link.bat -N1234567890
    echo.
    pause
    exit /b 1
)

set RECIPE_ID=%1
set DEEP_LINK=prmrecipe://recipe/%RECIPE_ID%

echo Recipe ID: %RECIPE_ID%
echo Deep Link: %DEEP_LINK%
echo.

REM Kiem tra thiet bi da ket noi
echo Kiem tra thiet bi...
adb devices
echo.

REM Test deep link
echo Dang mo deep link...
adb shell am start -a android.intent.action.VIEW -d "%DEEP_LINK%"

echo.
echo ========================================
echo Hoan thanh! Kiem tra ung dung da mo chua.
echo ========================================
echo.
pause

