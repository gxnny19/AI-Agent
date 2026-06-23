#!/usr/bin/env bash
# POSIX helper to create venv, install deps and run uvicorn
# Usage: ./run.sh
set -euo pipefail
VENV_DIR=.venv
if [ ! -d "$VENV_DIR" ]; then
  python -m venv "$VENV_DIR"
fi

# shellcheck source=/dev/null
source "$VENV_DIR/bin/activate"

pip install -r requirements.txt

uvicorn main:app --host 127.0.0.1 --port 8000 --reload
