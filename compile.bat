@echo off
REM Compiles all Java source files into the "out" folder.
REM -encoding UTF-8 is REQUIRED so the emoji icons in titles/menus don't turn into "?"

if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java

if %errorlevel%==0 (
    echo.
    echo ✅ Build successful! Run run.bat to start the app.
) else (
    echo.
    echo ❌ Build failed. Scroll up to see the compiler error.
)
pause
