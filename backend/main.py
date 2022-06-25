
from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn
from functions import get_unix_gmt_time, get_audio_length
import os

app = FastAPI()


class AudioLengthPost(BaseModel):
    audio_path: str


@app.get("/")
def read_root():
    return {"ping_time": get_unix_gmt_time()}


class GaussianBlurPost(BaseModel):
    image: str
    radius: int


@app.post("/image/blur/")
def post_blur_image(gaussian_blur_post: GaussianBlurPost):
    return {"image": str(gaussian_blur_post(gaussian_blur_post.image, gaussian_blur_post.radius))}


@app.post("/audio/length/")
def post_audio_length(audio_length: AudioLengthPost):
    exists = os.path.exists(audio_length.audio_path)

    if exists:
        return {"length": get_audio_length(audio_length.audio_path)}
    else:
        return {"error": "file not found"}

if __name__ == '__main__':
    uvicorn.run("main:app", host="0.0.0.0", port=8080)
