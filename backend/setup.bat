@echo off

if [%1]==[] goto help

echo creating venv
call python -m venv venv
echo entering venv
call .\venv\Scripts\Activate.bat
echo installing requirements
call pip install -r requirements.txt
echo starting backend on port: %1
call python main.py --port %1
goto done

:help
echo usage: .\setup.bat PORT_NUMBER

:done