import os
import re
from typing import Tuple
import cv2
import numpy as np
from PIL import ImageFont, Image, ImageDraw

# a regex used to detect a comment line
IS_COMMENT_REGEX = "\s*[/]{2}.*|\s*[/][*].*|\s*[*].*|\s*.*[*][/]\s*"

# the path to the font to use for all exported pngs
FONT_PATH = os.path.join('actions', 'resources', 'roboto.ttf')

# the color used for java code
JAVA_CODE_COLOR = (25, 114, 176)
COMMENT_COLOR = (75, 71, 60)
BLANK_COLOR = (33, 37, 22)


def export_stats(code_lines: int, comment_lines: int, blank_lines: int,
                 width: int, height: int, save_name: str) -> None:
    """ 
    Exports a stats png using the provided information.

    :param code_lines: the number of code lines in the project
    :param comment_lines: the number of comment lines in the project
    :param blank_lines: the number of blank lines in the project
    :param width: the width of the png to export
    :param height: the height of the png to export
    :param save_name: the name of the png to export
    """

    total = code_lines + comment_lines + blank_lines

    comment_percent = round(comment_lines / float(total) * 100.0, 1)
    code_percent = round(code_lines / float(total) * 100.0, 1)
    blank_percent = round(blank_lines / float(total) * 100.0, 1)

    border_thickness = 0

    export_font = ImageFont.truetype(FONT_PATH, 16)

    blank_image = np.zeros((width, height, 3), np.uint8)
    black_image = cv2.rectangle(blank_image, (0, 0), (width, height), (0, 0, 0), -1)
    outlined_image = cv2.rectangle(black_image, (border_thickness, border_thickness),
                                   (width - border_thickness, height - border_thickness), (255, 255, 255), -1)
    code_height = int(height * (code_percent / 100.0))
    image = cv2.rectangle(outlined_image, (border_thickness, border_thickness),
                          (width - border_thickness, code_height - border_thickness), JAVA_CODE_COLOR, -1)

    comment_height = int(height * (blank_percent / 100.0))
    image = cv2.rectangle(image, (border_thickness, code_height - border_thickness),
                          (width - border_thickness, code_height + comment_height), COMMENT_COLOR, -1)

    blank_height = int(height * (comment_percent / 100.0))
    image = cv2.rectangle(image, (border_thickness, code_height + comment_height - border_thickness),
                          (width - border_thickness, height), BLANK_COLOR, -1)

    img_pil = Image.fromarray(image)
    draw = ImageDraw.Draw(img_pil)
    code_string = "Java: " + str(code_percent) + \
                  "% (" + get_compressed_number(code_lines) + ')'
    w, h = draw.textsize(code_string, font=export_font)
    code_area_center = (width / 2 - w / 2,
                        border_thickness + code_height / 2 - h / 2)
    draw.text(code_area_center, code_string,
              font=export_font, fill=(245, 245, 245))

    draw = ImageDraw.Draw(img_pil)
    blank_string = "Blank lines: " + str(blank_percent) + \
                   "% (" + get_compressed_number(blank_lines) + ")"
    w, h = draw.textsize(blank_string, font=export_font)
    blank_area_center = (width / 2 - w / 2,
                         border_thickness + code_height + comment_height / 2 - h / 2)
    draw.text(blank_area_center, blank_string,
              font=export_font, fill=(245, 245, 245))

    draw = ImageDraw.Draw(img_pil)
    comment_string = "Comment lines: " + str(comment_percent) + \
                     "% (" + get_compressed_number(comment_lines) + ")"
    w, h = draw.textsize(comment_string, font=export_font)
    comment_area_center = (width / 2 - w / 2,
                           border_thickness + code_height + comment_height + blank_height / 2 - h / 2)
    draw.text(comment_area_center, comment_string,
              font=export_font, fill=(245, 245, 245))

    cv2.imwrite('actions/output/' + str(save_name) + '.png', np.array(img_pil))

__thousand_prefix = 'K'

def get_compressed_number(num: int) -> str:
    """ 
    Returns the number of thousands represented by the integer rounded to one decimal place.
    """
    return str(round(num / 1000.0, 1)) + __thousand_prefix


def export_string_badge(alpha_string: str, beta_string: str, save_name: str, font_size: int = 18,
                        horizontal_padding: int = 15, vertical_padding: int = 10,
                        text_color: tuple = (245, 245, 245),
                        left_background_color: tuple = (131, 83, 5),
                        right_background_color: tuple = (199, 147, 85)):
    """ 
    Exports a png to the root directory resembling a badge with the provided parameters.

    :param alpha_string: the string for the left of the badge
    :param beta_string: the string for the right of the badge
    :param save_name: the name to save the png as
    :param font_size: the size for the font
    :param horizontal_padding: the left/right padding between the image borders and words
    :param vertical_padding: the top/bottom padding between the image borders and words
    :param text_color: the color for the text painted by alpha_string and beta_string
    :param left_background_color: the color used for the badge's left background
    :param right_background_color: the color used for the badge's right background
    """

    local_font = ImageFont.truetype(FONT_PATH, font_size)

    alpha_width = get_text_size(alpha_string, font_size, FONT_PATH)[0]
    beta_width = get_text_size(beta_string, font_size, FONT_PATH)[0]
    text_height = get_text_size(beta_string, font_size, FONT_PATH)[1]

    full_width = (horizontal_padding + alpha_width
                  + 2 * horizontal_padding + beta_width + horizontal_padding)
    full_height = vertical_padding + text_height + vertical_padding

    blank_image = np.zeros((full_height, full_width, 3), np.uint8)
    alpha_color_drawn = cv2.rectangle(blank_image, (0, 0),
                                      (alpha_width + 2 *
                                       horizontal_padding, full_height),
                                      left_background_color, -1)
    beta_color_drawn = cv2.rectangle(alpha_color_drawn, (alpha_width + horizontal_padding, 0),
                                     (full_width, full_height), right_background_color, -1)

    base_colors_done = Image.fromarray(beta_color_drawn)

    draw = ImageDraw.Draw(base_colors_done)
    left_anchor = (horizontal_padding / 2, vertical_padding)
    draw.text(left_anchor, alpha_string, font=local_font, fill=text_color)

    draw = ImageDraw.Draw(base_colors_done)
    left_anchor = (alpha_width + horizontal_padding * 2, vertical_padding)
    draw.text(left_anchor, beta_string, font=local_font, fill=text_color)

    cv2.imwrite('actions/output/' + save_name + '.png', np.array(base_colors_done))


def get_text_size(text: str, font_size: int, font_name: str) -> Tuple:
    """ 
    Returns a tuple of the size (width, height) required to hold the provided 
    string with the provided font and point size.
    """
    return ImageFont.truetype(font_name, font_size).getsize(text)


def find_files(starting_dir: str, extensions: list = [], recursive: bool = False) -> list:
    """ 
    Finds all files within the provided directory that end in one of the provided extensions.

    :param starting_dir: the directory to start recursion from
    :param extensions: a list of valid extensions such as [".java"]
    :param recursive: whether to recurse through found subdirectories
    :return: a list of discovered files
    """

    ret = []

    if len(extensions) == 0:
        raise Exception('Error: must provide valid extensions')

    if os.path.isdir(starting_dir):
        for subDir in os.listdir(starting_dir):
            if recursive:
                ret = ret + \
                      find_files(os.path.join(starting_dir, subDir),
                                 extensions, recursive)
            else:
                ret.append(os.path.join(starting_dir, subDir))
    else:
        for extension in extensions:
            if starting_dir.endswith(extension):
                ret.append(starting_dir)

    return ret


def analyze_file(file: str) -> Tuple:
    """ 
    Analyzes the provided file for source code.

    :param file: the file to analyze
    :return: a tuple in the following order (num+code_lines, num_comment_lines, num_blank_lines)
    """

    if not os.path.exists(file):
        print('Error: provided file does not exist: ', file)
        return ()

    file_lines = open(file, 'r').readlines()

    if file.endswith('.java') or file.endswith('.kt'):
        num_code_lines = count_code_lines(file_lines)
        num_comments = count_comment_lines(file_lines)
        num_blank_lines = len(file_lines) - num_code_lines - num_comments

    else:
        raise Exception(
            'Found file that does not end in .java or .kt: ' + file)

    print('---- Found code stats of', file, '----')
    print('Code lines:', num_code_lines)
    print('Comment lines:', num_comments)
    print('Blank lines:', num_blank_lines)

    return num_code_lines, num_comments, num_blank_lines


def count_code_lines(file_lines: list) -> int:
    """ 
    Counts the number of code lines of the provided file lines. 
    A line is a code line if it is not a comment and not empty.

    :param file_lines: the lines of a file
    :return: the number of code lines of the provided file lines
    """
    ret = 0

    for line in file_lines:
        line = line.strip()

        if len(line) > 0 and not is_comment_line(line):
            ret = ret + 1

    return ret


def count_comment_lines(file_lines: list) -> int:
    """ 
    Counts the number of comments of the provided file lines.

    :param file_lines: the lines of a file
    :return: the number of comment lines of the provided file lines
    """
    ret = 0
    block_comment = False

    for line in file_lines:
        if line.strip().startswith('/*') and line.strip().endswith('*/'):
            ret = ret + 1
            continue

        if line.strip().startswith("/*"):
            block_comment = True
        elif line.strip().endswith("*/"):
            block_comment = False

        if block_comment:
            ret = ret + 1
        elif len(line.strip()) > 0 and is_comment_line(line):
            ret = ret + 1

    return ret


def is_comment_line(line: str) -> bool:
    """ 
    Returns whether the provided line is a comment line.
    """
    return re.compile(IS_COMMENT_REGEX).match(line)


def main():
    print("Finding files starting from:", os.getcwd())

    files = find_files(starting_dir="cyder",
                       extensions=['.java'], recursive=True)

    code_lines = 0
    comment_lines = 0
    blank_lines = 0

    for file in files:
        results = analyze_file(file)

        code_lines = code_lines + results[0]
        comment_lines = comment_lines + results[1]
        blank_lines = blank_lines + results[2]

    print('---- Found code stats ----')
    print('Total code lines:', code_lines)
    print('Total comment lines:', comment_lines)
    print('Total blank lines:', blank_lines)

    total = code_lines + comment_lines + blank_lines
    print("Total:",total)

    total_rounded = round(code_lines / 1000.0, 1) + round(comment_lines / 1000.0, 1) + round(blank_lines / 1000.0, 1)


    export_stats(code_lines=code_lines, comment_lines=comment_lines,
                 blank_lines=blank_lines, width=250, height=250, save_name="stats")

    # attempt to regenerate in case one was removed
    export_string_badge("Cyder", "A Programmer's Swiss Army Knife", "tagline")
    export_string_badge("By", "Nate Cheshire", "author")
    export_string_badge("Total lines", get_compressed_number(total_rounded), "total")


if __name__ == '__main__':
    main()
