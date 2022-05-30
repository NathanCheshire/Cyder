from typing import Tuple
import cv2
import numpy as np
from PIL import ImageFont, Image, ImageDraw

global roboto_bold_font
roboto_bold_font = ImageFont.truetype("roboto-bold.ttf", 34)


def export_stats(code_lines, comment_lines, blank_lines, width: str = 400, height: str = 400, save_name: str = "CyderStats"):
    total = code_lines + comment_lines + blank_lines

    comment_percent = round(comment_lines / float(total) * 100.0, 1)
    code_percent = round(code_lines / float(total) * 100.0, 1)
    blank_percent = round(blank_lines / float(total) * 100.0, 1)

    # for a border if needed
    border_thickness = 0

    export_font = ImageFont.truetype("roboto-bold.ttf", 28)

    blank_image = np.zeros((width, height, 3), np.uint8)
    black_image = cv2.rectangle(
        blank_image, (0, 0), (width, height), (0, 0, 0), -1)
    outlined_image = cv2.rectangle(black_image, (border_thickness, border_thickness),
                                   (width - border_thickness, height - border_thickness), (255, 255, 255), -1)

    code_color = (25, 114, 176)
    code_height = int(height * (code_percent / 100.0))
    image = cv2.rectangle(outlined_image, (border_thickness, border_thickness),
                          (width - border_thickness, code_height - border_thickness), code_color, -1)

    comment_color = (243, 244, 255)
    comment_height = int(height * (blank_percent / 100.0))
    image = cv2.rectangle(image, (border_thickness, code_height - border_thickness),
                          (width - border_thickness, code_height + comment_height), comment_color, -1)

    blank_color = (119, 98, 42)
    blank_height = int(height * (comment_percent / 100.0))
    image = cv2.rectangle(image, (border_thickness, code_height + comment_height - border_thickness),
                          (width - border_thickness, height), blank_color, -1)

    img_pil = Image.fromarray(image)
    draw = ImageDraw.Draw(img_pil)
    code_string = "Code: " + str(code_percent) + \
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
              font=export_font, fill=(25, 25, 25))

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


def export_code_comment_ratio_badge(alpha_string, beta_string, save_name):
    primary = (131, 83, 5)
    secondary = (199, 147, 85)

    padding = 40
    font_size = 28

    alpha_width = get_text_size(alpha_string, font_size, "roboto_bold.ttf")[0]
    beta_width = get_text_size(beta_string, font_size, "roboto_bold.ttf")[0]
    text_height = get_text_size(beta_string, font_size, "roboto_bold.ttf")[1]

    full_width = padding + alpha_width + padding + beta_width + padding
    full_height = padding * 2 + text_height

    blank_image = np.zeros((full_height, full_width, 3), np.uint8)
    alpha_color_drawn = cv2.rectangle(blank_image, (0, 0),
                                      (full_width, full_height), primary, -1)
    beta_color_drawn = cv2.rectangle(alpha_color_drawn, (alpha_width + padding, 0),
                                     (full_width, full_height), secondary, -1)

    text_color = (245, 245, 245)

    base_colors_done = Image.fromarray(beta_color_drawn)

    draw = ImageDraw.Draw(base_colors_done)
    draw.text((padding - text_height / 2, padding),  alpha_string,
              font=roboto_bold_font, fill=text_color)

    draw = ImageDraw.Draw(base_colors_done)
    draw.text((padding * 2 + alpha_width, full_height / 2 - text_height / 2),  beta_string,
              font=roboto_bold_font, fill=text_color)

    cv2.imwrite(save_name + '.png', np.array(base_colors_done))


def get_text_size(text: str, font_size: int, font_name: str) -> Tuple:
    """ Returns a tuple of the size required to hold the provided string with the provided font and point size.
    """
    font = ImageFont.truetype('roboto-bold.ttf', 34)
    return font.getsize(text)


if __name__ == '__main__':
    code_lines = 35346
    comment_lines = 8926
    blank_lines = 11024
    ratio = 2.50
    last_updated = "2022-05-30 14:39"

    export_stats(code_lines=code_lines,comment_lines=comment_lines,blank_lines=blank_lines,save_name="stats")

    export_code_comment_ratio_badge("Code comment ratio", "{:.2f}".format(ratio), "code_comment")
    export_code_comment_ratio_badge("Last updated", last_updated, "last_updated")
