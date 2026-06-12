import os
import shutil

from fastapi import UploadFile

from config import UPLOAD_DIR


def save_upload_file(file: UploadFile) -> str:
    filename = os.path.basename(file.filename)
    file_path = os.path.join(UPLOAD_DIR, filename)

    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    return file_path
