import os
import re
import cv2
import numpy as np
from PIL import ImageFont, Image, ImageDraw

FONT_PATH = os.path.join('actions', 'resources', 'oswald-semi-bold.ttf')

CODE_COLOR = (25, 114, 176)
COMMENT_COLOR = (75, 71, 60)
BLANK_COLOR = (33, 37, 22)


def export_stats(code_lines: int, comment_lines: int, blank_lines: int,
                 save_name: str, width: int = 300, height: int = 300,
                 border_length: int = 0, border_color: tuple = (255, 255, 255),
                 text_foreground: tuple = (245, 245, 245),
                 font_size: int = 22) -> None:
    """ 
    Exports a stats png using the provided information.

    :param code_lines: the number of code lines in the project
    :param comment_lines: the number of comment lines in the project
    :param blank_lines: the number of blank lines in the project
    :param save_name: the name of the png to export
    :param width: the width of the png badge to export
    :param height: the height of the png badge to export
    :param border_length: the length of the border to paint on the exported png
    :param border_color: the color the border to paint if border_length is greater than 0
    :param text_foreground: the color of the painted strings
    :param font_size: the size of the font to use for the painted strings
    """

    total = code_lines + comment_lines + blank_lines

    comment_percent = round(comment_lines / float(total) * 100.0, 1)
    code_percent = round(code_lines / float(total) * 100.0, 1)
    blank_percent = round(blank_lines / float(total) * 100.0, 1)

    export_font = ImageFont.truetype(FONT_PATH, font_size)

    # Initial image
    blank_image = np.zeros((width, height, 3), np.uint8)
    black_image = cv2.rectangle(
        blank_image, (0, 0), (width, height), (0, 0, 0), -1)

    # Paint border color border with border length
    outlined_image = cv2.rectangle(black_image, (border_length, border_length),
                                   (width - border_length, height - border_length), border_color, -1)

    code_height = int(height * (code_percent / 100.0))
    comment_height = int(height * (blank_percent / 100.0))
    blank_height = int(height * (comment_percent / 100.0))

    # Paint code background at top
    image = cv2.rectangle(outlined_image, (border_length, border_length),
                          (width - border_length, code_height - border_length), CODE_COLOR, -1)

    # Paint comment background in middle
    image = cv2.rectangle(image, (border_length, code_height - border_length),
                          (width - border_length, code_height + comment_height), COMMENT_COLOR, -1)

    # Paint blank lines background at bottom
    image = cv2.rectangle(image, (border_length, code_height + comment_height - border_length),
                          (width - border_length, height), BLANK_COLOR, -1)

    # Convert to pillow image
    pillow_image = Image.fromarray(image)

    draw = ImageDraw.Draw(pillow_image)
    code_string = get_paint_string("Java", code_percent, code_lines)
    w, h = draw.textsize(code_string, font=export_font)
    code_area_center = (width / 2 - w / 2,
                        border_length + code_height / 2 - h / 2)
    draw.text(code_area_center, code_string,
              font=export_font, fill=text_foreground)

    draw = ImageDraw.Draw(pillow_image)
    blank_string = get_paint_string("Blank", blank_percent, blank_lines)
    w, h = draw.textsize(blank_string, font=export_font)
    blank_area_center = (width / 2 - w / 2,
                         border_length + code_height + comment_height / 2 - h / 2)
    draw.text(blank_area_center, blank_string,
              font=export_font, fill=text_foreground)

    draw = ImageDraw.Draw(pillow_image)
    comment_string = get_paint_string("Comment", comment_percent, comment_lines)
    w, h = draw.textsize(comment_string, font=export_font)
    comment_area_center = (width / 2 - w / 2,
                           border_length + code_height + comment_height + blank_height / 2 - h / 2)
    draw.text(comment_area_center, comment_string,
              font=export_font, fill=text_foreground)

    cv2.imwrite('actions/output/' + str(save_name) +
                '.png', np.array(pillow_image))


def get_paint_string(header: str, percent: float, lines: int) -> str:
    """
    Returns a string to paint on the stat image depicting the number
    of lines associated with a certain line group and the percentage.

    :param header: the header string such as "blank lines"
    :param percent: the percent of the total this line group takes up
    :param lines: the number of lines this group takes up
    """
    return header + " lines: " + str(percent) + "% (" + get_compressed_number(lines) + ")"


THOUSAND = 'K'


def get_compressed_number(num: int) -> str:
    """ 
    Returns the number of thousands represented by the integer rounded to one decimal place.
    """
    return str(round(num / 1000.0, 1)) + THOUSAND


def export_string_badge(alpha_string: str, beta_string: str, save_name: str, font_size: int = 22,
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

    cv2.imwrite('actions/output/' + save_name +
                '.png', np.array(base_colors_done))


def get_text_size(text: str, font_size: int, font_name: str) -> tuple:
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
        for sub_directory in os.listdir(starting_dir):
            if recursive:
                ret = ret + \
                    find_files(os.path.join(starting_dir, sub_directory),
                               extensions, recursive)
            else:
                ret.append(os.path.join(starting_dir, sub_directory))
    else:
        for extension in extensions:
            if starting_dir.endswith(extension):
                ret.append(starting_dir)

    return ret


def analyze_file(file: str) -> tuple:
    """ 
    Analyzes the provided file for source code.

    :param file: the file to analyze
    :return: a tuple in the following order (num_code_lines, num_comment_lines, num_blank_lines)
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


IS_COMMENT_REGEX = "\s*[/]{2}.*|\s*[/][*].*|\s*[*].*|\s*.*[*][/]\s*"


def is_comment_line(line: str) -> bool:
    """ 
    Returns whether the provided line is a comment line.
    """
    return re.compile(IS_COMMENT_REGEX).match(line)


def regenerate_badges(total_rounded):
    export_string_badge("Cyder", "A Programmer's Swiss Army Knife", "tagline")
    export_string_badge("By", "Nate Cheshire", "author")
    export_string_badge(
        "Total lines", str(total_rounded) + THOUSAND, "total")


def main():
    print("Finding files starting from:", os.getcwd())

    files = find_files(starting_dir="cyder",
                       extensions=['.java'], recursive=True)

    code_lines = 0
    comment_lines = 0
    blank_lines = 0

    for file in files:
        results = analyze_file(file)

        code_lines += results[0]
        comment_lines += results[1]
        blank_lines += results[2]

    total = code_lines + comment_lines + blank_lines

    print('Total code lines:', code_lines)
    print('Total comment lines:', comment_lines)
    print('Total blank lines:', blank_lines)
    print('Total:', total)
    
    code_rounded = round(code_lines / 1000.0, 1)
    comment_rounded = round(comment_lines / 1000.0, 1)
    blank_rounded = round(blank_lines / 1000.0, 1)

    total_rounded = round(code_rounded + comment_rounded + blank_rounded, 1)
    print('Total rounded:',total_rounded)

    export_stats(code_lines=code_lines, comment_lines=comment_lines,
                 blank_lines=blank_lines, save_name="stats")
    regenerate_badges(total_rounded)


if __name__ == '__main__':
    main()
