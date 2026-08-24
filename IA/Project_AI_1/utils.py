def get_board_coordinates(mouse_x, mouse_y):
    """Converts mouse position to board coordinates (r, q) on the hexagonal grid.""" 
    x_offset = HEX_RADIUS * 1.5 
    y_offset = HEX_RADIUS * math.sqrt(3)
    
    start_x = (WIDTH - (x_offset * 5)) // 2
    start_y = (HEIGHT - (y_offset * 5)) // 2
    
    # Check the mouse's position against each row
    for r, row_length in enumerate([3, 4, 5, 4, 3]):
        row_start_x = start_x + (x_offset * (5 - row_length) // 2)
        
        for q in range(row_length):
            x = row_start_x + x_offset * q
            y = start_y + y_offset * r

            # Calculate the distance from the mouse to the hexagon's center
            dist = math.sqrt((mouse_x - x) ** 2 + (mouse_y - y) ** 2)
            if dist < HEX_RADIUS:
                return r, q  # Return the board coordinates
    return None

    
        

def print_board(board):
    """Helper function to print the board."""
    for row in board:
        print(" ".join(cell if cell != '' else '.' for cell in row))
    print()