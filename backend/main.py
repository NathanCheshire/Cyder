

from fastapi import FastAPI
from numpy import require
from pydantic import BaseModel
from typing import Optional
import uvicorn
from functions import gaussian_blur, get_unix_gmt_time, get_audio_length, get_usb_devices
import os
import argparse

app = FastAPI()


@app.get("/")
def read_root():
    """ The local backend root.
    """
    return {"ping_time": get_unix_gmt_time()}


class GaussianBlurPost(BaseModel):
    """ The expected schema for a gaussian blur post request.
    """
    image: str
    radius: int
    save_directory: Optional[str] = None


@app.post("/image/blur/")
def post_blur_image(gaussian_blur_post: GaussianBlurPost):
    """ The post location for applying a gaussian blur to a local image file.
    """
    return {"image": str(gaussian_blur(gaussian_blur_post.image, gaussian_blur_post.radius,
                                       save_directory=gaussian_blur_post.save_directory))}


class AudioLengthPost(BaseModel):
    """ The expected schema for an audio length post request.
    """
    audio_path: str


@app.post("/audio/length/")
def post_audio_length(audio_length: AudioLengthPost):
    """ The post location for determing the length in seconds of a local audio file.
    """
    exists = os.path.exists(audio_length.audio_path)

    if exists:
        return {"length": get_audio_length(audio_length.audio_path)}
    else:
        return {"error": "file not found"}


@app.get("/usb/devices/")
def get_usb():
    """ The get location for usb devices.
    """
    return {"usb": str(get_usb_devices())}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument("--port", "-p", required=True,
                    help='an integer for the FastAPI port')
    args = parser.parse_args()
    uvicorn.run("main:app", host="0.0.0.0", port=int(args.port))
