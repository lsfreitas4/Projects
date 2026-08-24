# constants.py

import math

WIDTH, HEIGHT = 800, 600
BG_COLOR = (255, 255, 255)  # Set the background color to white
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)  # Set the board color to black
BUTTON_COLOR = (70, 130, 180)
BUTTON_HOVER = (100, 180, 250)
HEX_RADIUS = 40
HEX_WIDTH = math.sqrt(3) * HEX_RADIUS  # Width of a pointy-top hexagon
HEX_HEIGHT = 2 * HEX_RADIUS

board_layout = [3, 4, 5, 4, 3]  # Initial board layout
