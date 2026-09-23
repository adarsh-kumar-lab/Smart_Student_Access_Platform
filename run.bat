@echo off
REM Runs the app. Compiles first automatically if "out" folder doesn't exist yet.

if not exist out (
    echo No build found, compiling first...
    call compile.bat
)

java -cp out Main
pause
