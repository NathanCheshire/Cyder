
from datetime import datetime
import calendar
from mutagen.mp3 import MP3


def gaussian_blur(image_path: str, radius: int = 7):
    return "stuff will be here"


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
