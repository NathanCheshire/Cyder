
from datetime import datetime
import calendar
from PIL import Image
from PIL import ImageFilter
from mutagen.mp3 import MP3


def gaussian_blur(image_path: str, radius: int = 7) -> str:
    """ Returns the path to the blurred and saved image
    :param image_path: the path to the image to blur
    :type image_path: str
    :param radius: the radius of the gaussian blur to apply
    :type radius: int
    :return: the path to the blurred image
    :rtype: str
    """
    name, extension = image_path.rsplit('.', 1)
    save_name = name + "_blurred_radius_" + str(radius) + "." + extension
    print("save name:",save_name)
    gaussImage = Image.open(image_path).filter(
        ImageFilter.GaussianBlur(radius))
    
    gaussImage.save(save_name)

    return save_name


def get_unix_gmt_time() -> int:
    """ Returns the current unix gmt time.
    :return: the current unix gmt time
    :rtype: int
    """
    return calendar.timegm(datetime.utcnow().utctimetuple())


def get_audio_length(path: str) -> float:
    """ Returns the length of the provided audio file in seconds.
    :param path: the path to the audio file
    :type path: str
    :return: how long the audio file is in seconds
    :rtype: float
    """
    return MP3(path).info.length
