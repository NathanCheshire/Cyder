

from fastapi import FastAPI
from numpy import require
from pydantic import BaseModel
from typing import Optional
import uvicorn
from functions import gaussian_blur, get_unix_gmt_time, get_audio_length, get_usb_devices
import os
import argparse

app = FastAPI()

class GaussianBlurPost(BaseModel):
    """ 
    The expected schema for a gaussian blur post request.
    """
    image: str
    radius: int
    save_directory: Optional[str] = None


@app.post("/image/blur/")
def post_blur_image(gaussian_blur_post: GaussianBlurPost):
    """ 
    The post location for applying a gaussian blur to a local image file.

    :param gaussian_blur_post: the gaussian blur post response
    """
    return {"image": str(gaussian_blur(gaussian_blur_post.image, gaussian_blur_post.radius,
                                       save_directory=gaussian_blur_post.save_directory))}


class AudioLengthPost(BaseModel):
    """ 
    The expected schema for an audio length post request.
    """
    audio_path: str


@app.post("/audio/length/")
def post_audio_length(audio_length: AudioLengthPost):
    """ 
    The post location for determing the length in seconds of a local audio file.

    :param audio_length: the audio length post
    """
    exists = os.path.exists(audio_length.audio_path)

    if exists:
        return {"length": get_audio_length(audio_length.audio_path)}
    else:
        return {"error": "file not found"}

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument("--port", "-p", required=True,
                    help='an integer for the FastAPI port')
    args = parser.parse_args()
    uvicorn.run("main:app", host="0.0.0.0", port=int(args.port))
