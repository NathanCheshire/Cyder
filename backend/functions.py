
import numpy as np
from sklearn.cluster import KMeans
from datetime import datetime
import calendar
from mutagen.mp3 import MP3


def get_colorfulness(r: int, g: int, b: int):
    """Returns a colorfulness index of given RGB combination.

        Parameters:
            r: Red component.
            g: Green component.
            b: Blue component.

        Returns:
            float: a colorfulness metric.
    """

    rg = np.absolute(r - g)
    yb = np.absolute(0.5 * (r + g) - b)

    rg_mean, rg_std = (np.mean(rg), np.std(rg))
    yb_mean, yb_std = (np.mean(yb), np.std(yb))

    std_root = np.sqrt((rg_std ** 2) + (yb_std ** 2))
    mean_root = np.sqrt((rg_mean ** 2) + (yb_mean ** 2))

    return std_root + (0.3 * mean_root)


def find_best_color(img, k: int = 8, color_tol: int = 10) -> str:
    """Returns a suitable background color for the given image.

        Parameter:
            k: Number of clusters to form.
            color_tol: Tolerance for a colorful color.
            plot: Plot the original image, k-means result and
                calculated background color. Only used for testing.
        Returns:
            tuple: (R, G, B). The calculated background color.
    """

    img = img.reshape((img.shape[0]*img.shape[1], 3))

    clt = KMeans(n_clusters=k)
    clt.fit(img)
    centroids = clt.cluster_centers_

    colorfulness_res = [get_colorfulness(
        color[0], color[1], color[2]) for color in centroids]
    max_colorful = np.max(colorfulness_res)

    if max_colorful < color_tol:
        best_color = [230, 230, 230]
    else:
        best_color = centroids[np.argmax(colorfulness_res)]

    return f"({round(best_color[0])},{round(best_color[1])},{round(best_color[2])})"


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
