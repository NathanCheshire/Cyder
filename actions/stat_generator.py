import os
import re
from typing import Tuple
import cv2
import numpy as np
from PIL import ImageFont, Image, ImageDraw


def export_stats(code_lines, comment_lines, blank_lines, width: str = 250,
                 height: str = 250, save_name: str = "CyderStats"):
    total = code_lines + comment_lines + blank_lines

    comment_percent = round(comment_lines / float(total) * 100.0, 1)
    code_percent = round(code_lines / float(total) * 100.0, 1)
    blank_percent = round(blank_lines / float(total) * 100.0, 1)

    # for a border if needed
    border_thickness = 0

    export_font = ImageFont.truetype("actions/roboto-bold.ttf", 16)

    blank_image = np.zeros((width, height, 3), np.uint8)
    black_image = cv2.rectangle(
        blank_image, (0, 0), (width, height), (0, 0, 0), -1)
    outlined_image = cv2.rectangle(black_image, (border_thickness, border_thickness),
                                   (width - border_thickness, height - border_thickness), (255, 255, 255), -1)

    code_color = (25, 114, 176)
    code_height = int(height * (code_percent / 100.0))
    image = cv2.rectangle(outlined_image, (border_thickness, border_thickness),
                          (width - border_thickness, code_height - border_thickness), code_color, -1)

    comment_color = (75, 71, 60)
    comment_height = int(height * (blank_percent / 100.0))
    image = cv2.rectangle(image, (border_thickness, code_height - border_thickness),
                          (width - border_thickness, code_height + comment_height), comment_color, -1)

    blank_color = (33, 37, 22)
    blank_height = int(height * (comment_percent / 100.0))
    image = cv2.rectangle(image, (border_thickness, code_height + comment_height - border_thickness),
                          (width - border_thickness, height), blank_color, -1)

    img_pil = Image.fromarray(image)
    draw = ImageDraw.Draw(img_pil)
    code_string = "Java: " + str(code_percent) + \
        "% (" + get_compressed_number(code_lines) + ')'
    w, h = draw.textsize(code_string, font=export_font)
    code_area_center = (width / 2 - w / 2,
                        border_thickness + code_height / 2 - h / 2)
    draw.text(code_area_center,  code_string,
              font=export_font, fill=(245, 245, 245))

    draw = ImageDraw.Draw(img_pil)
    blank_string = "Blank lines: " + str(blank_percent) + \
        "% (" + get_compressed_number(blank_lines) + ")"
    w, h = draw.textsize(blank_string, font=export_font)
    blank_area_center = (width / 2 - w / 2,
                         border_thickness + code_height + comment_height / 2 - h / 2)
    draw.text(blank_area_center,  blank_string,
              font=export_font, fill=(245, 245, 245))

    draw = ImageDraw.Draw(img_pil)
    comment_string = "Comment lines: " + str(blank_percent) + \
        "% (" + get_compressed_number(comment_lines) + ")"
    w, h = draw.textsize(comment_string, font=export_font)
    comment_area_center = (width / 2 - w / 2,
                           border_thickness + code_height + comment_height + blank_height / 2 - h / 2)
    draw.text(comment_area_center,  comment_string,
              font=export_font, fill=(245, 245, 245))

    cv2.imwrite(str(save_name) + '.png', np.array(img_pil))


def get_compressed_number(num: int) -> str:
    """ Returns the number of thousands represented by the int with one decimal place.
    """
    return str(round(num / 1000.0, 1)) + 'K'


def export_string_badge(alpha_string, beta_string, save_name):
    primary = (131, 83, 5)
    secondary = (199, 147, 85)

    padding = 15
    font_size = 18

    local_font = ImageFont.truetype("roboto-bold.ttf", font_size)

    alpha_width = get_text_size(alpha_string, font_size, "roboto_bold.ttf")[0]
    beta_width = get_text_size(beta_string, font_size, "roboto_bold.ttf")[0]
    text_height = get_text_size(beta_string, font_size, "roboto_bold.ttf")[1]

    full_width = padding + alpha_width + padding + padding + beta_width + padding
    full_height = padding + text_height + padding

    blank_image = np.zeros((full_height, full_width, 3), np.uint8)
    alpha_color_drawn = cv2.rectangle(blank_image, (0, 0),
                                      (alpha_width + padding + padding, full_height), primary, -1)
    beta_color_drawn = cv2.rectangle(alpha_color_drawn, (alpha_width + padding, 0),
                                     (full_width, full_height), secondary, -1)

    text_color = (245, 245, 245)

    base_colors_done = Image.fromarray(beta_color_drawn)

    draw = ImageDraw.Draw(base_colors_done)
    start = (padding / 2, padding)
    draw.text(start,  alpha_string,
              font=local_font, fill=text_color)

    draw = ImageDraw.Draw(base_colors_done)
    start = (alpha_width + padding * 2, padding)
    draw.text(start,  beta_string,
              font=local_font, fill=text_color)

    cv2.imwrite(save_name + '.png', np.array(base_colors_done))


def get_text_size(text: str, font_size: int, font_name: str) -> Tuple:
    """ Returns a tuple of the size required to hold the provided string with the provided font and point size.
    """
    font = ImageFont.truetype('roboto-bold.ttf', font_size)
    return font.getsize(text)


def find_files(starting_dir, extensions=[], recursive=False):
    '''
    Finds all files within the provided directory that 
    end in one of the provided extensions.
    '''

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


def analyze_file(file):
    """
    Returns a tuple of the number of code lines, 
    comments lines, and blank lines in that order
    """

    # don't change me
    blockMode = False

    num_comments = 0
    num_code_lines = 0
    num_blank_lines = 0

    if not os.path.exists(file):
        print('Error: provided file does not exist: ', file)
        return

    file_lines = open(file, 'r').readlines()

    if file.endswith('.java') or file.endswith('.kt'):
        num_code_lines = count_code_lines(file_lines)
        num_comments = count_comment_lines(file_lines)
        num_blank_lines = len(file_lines) - num_code_lines - num_comments

    else:
        raise Exception(
            'Found file that does not end in .java or .kt: ' + file)

    return (num_code_lines, num_comments, num_blank_lines)


def count_code_lines(file_lines):
    ret = 0

    for line in file_lines:
        line = line.strip()

        if len(line) > 0 and not is_comment(line):
            ret = ret + 1

    return ret


def count_comment_lines(file_lines):
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
        elif len(line.strip()) > 0 and is_comment(line):
            ret = ret + 1

    return ret


def is_comment(line) -> bool:
    return re.compile("\s*[/]{2}.*|\s*[/][*].*|\s*[*].*|\s*.*[*][/]\s*").match(line)


if __name__ == '__main__':
    print("Finding files from directory:",os.getcwd())
    print("Can find font file at actions/roboto-bold.ttf:", os.path.exists('actions/roboto-bold.ttf'))

    files = find_files(starting_dir="cyder",
                       extensions=['.java'], recursive=True)

    code_lines = 0
    comment_lines = 0
    blank_lines = 0

    for file in files:
        tuple = analyze_file(file)

        code_lines = code_lines + tuple[0]
        comment_lines = comment_lines + tuple[1]
        blank_lines = blank_lines + tuple[2]

    print('Found code stats:')
    print('Code lines:', code_lines)
    print('Comment lines:', comment_lines)
    print('Blank lines:', blank_lines)

    export_stats(code_lines=code_lines, comment_lines=comment_lines,
                 blank_lines=blank_lines, save_name="stats")

    export_string_badge(
        "Cyder", "A Programmer's Swiss Army Knife", "tagline")
    export_string_badge(
        "By", "Nate Cheshire", "author")
