@echo off

if EXIST "\venv" (
    echo venv did not exist, creating...
    call python -m venv venv
    echo entering venv
    call .\venv\Scripts\Activate.bat
    echo installing requirements
    call pip install -r requirements.txt
    echo starting backend
    call python main.py
) else (
    @echo venv found, entering
    call .\venv\Scripts\Activate.bat
    echo starting backend
    call python main.py
)