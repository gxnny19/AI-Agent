# PowerShell helper to create venv, install deps and run uvicorn
# Usage: Right-click -> Run with PowerShell or from PowerShell: .\run.ps1

$venvDir = ".venv"
if (-not (Test-Path $venvDir)) {
    python -m venv $venvDir
}

# Activate the venv in this session
. "$venvDir\Scripts\Activate.ps1"

# Install requirements (no-op if already satisfied)
pip install -r requirements.txt

# Run the app (reload enabled for dev)
uvicorn main:app --host 127.0.0.1 --port 8000 --reload
