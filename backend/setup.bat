@echo off

echo creating venv
call python -m venv venv
echo entering venv
call .\venv\Scripts\Activate.bat
echo installing requirements
call pip install -r requirements.txt
echo starting backend on port: %1
call python main.py --port %1