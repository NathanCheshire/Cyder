@echo off

if [%1]==[] goto help

echo Creating virtual environment
call python -m venv venv
echo Entering virtual environment
call .\venv\Scripts\Activate.bat
echo Installing Python requirements
call pip install -r requirements.txt
echo Starting FastAPI backend on port: %1
call python main.py --port %1
goto done

:help
echo Usage: .\setup.bat PORT_NUMBER

:done