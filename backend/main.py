
from fastapi import FastAPI
from pydantic import BaseModel
from functions import get_unix_gmt_time, get_audio_length, find_best_color
import numpy as np
from PIL import Image
import os

app = FastAPI()

class AudioLengthPost(BaseModel):
    audio_path: str


@app.get("/")
def read_root():
    return {"ping_time": get_unix_gmt_time()}

class ColorPost(BaseModel):
    image: str

@app.post("/image/find-color")
def post_best_color(color: ColorPost):
    return {"color": str(find_best_color(np.array(Image.open(color.image))))}

@app.post("/audio/length/")
def get_audio_length(audio_length: AudioLengthPost):
    print(audio_length)
    if (os.path.exists(audio_length.audio_path)):
        return {"length": get_audio_length(audio_length.audio_path)}
    