import pygame
import sys
import math
import hashlib
import random

from models import GameState, GameLogger
from utils import *

import json
import time
from datetime import datetime
import tracemalloc
import os


# Initialize pygame
pygame.init()

# Constants
WIDTH, HEIGHT = 800, 600
BG_COLOR = (255, 255, 255)  # Set the background color to white
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)  # Set the board color to black
BUTTON_COLOR = (70, 130, 180)
BUTTON_HOVER = (100, 180, 250)
HEX_RADIUS = 40
HEX_WIDTH = math.sqrt(3) * HEX_RADIUS  # Width of a pointy-top hexagon
HEX_HEIGHT = 2 * HEX_RADIUS
# Setup display
screen = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption("Churn Game")

# Fonts
font = pygame.font.Font(None, 50)
small_font = pygame.font.Font(None, 36)

# Board state: None means empty, 'R' means Red, 'B' means Blue
board_layout = [3, 4, 5, 4, 3]
# Player turn
current_player = 'R'  # Red starts first

# Game state storage (use this for Minimax search)
class GameGraph:
    def __init__(self):
        # Dictionary to store game states by their hash
        self.graph = {}
    
    def add_state(self, game_state, evaluation=None, best_move=None):
        """Adds a game state to the graph with optional evaluation and best move."""
        state_hash = game_state.get_hash()
        if state_hash not in self.graph:
            self.graph[state_hash] = {
                'game_state': game_state,
                'evaluation': evaluation,
                'best_move': best_move,
                'children': []
            }
        elif evaluation is not None:
            # Update existing state if new evaluation is provided
            self.graph[state_hash]['evaluation'] = evaluation
            if best_move is not None:
                self.graph[state_hash]['best_move'] = best_move
    
    def get_state(self, game_state):
        """Retrieves stored state data by hash."""
        return self.graph.get(game_state.get_hash(), None)
    
    def add_child(self, parent_state, child_state, move, evaluation):
        """Adds a child state with move and evaluation."""
        parent_hash = parent_state.get_hash()
        child_hash = child_state.get_hash()
        
        # Ensure parent exists
        if parent_hash not in self.graph:
            self.add_state(parent_state)
        
        # Ensure child exists
        if child_hash not in self.graph:
            self.add_state(child_state, evaluation)
        
        # Add child reference to parent
        self.graph[parent_hash]['children'].append({
            'state_hash': child_hash,
            'move': move,
            'evaluation': evaluation
        })
        
        # Update parent's best move if this is better
        current_best = self.graph[parent_hash]['best_move']
        current_eval = self.graph[parent_hash]['evaluation']
        
        if current_eval is None or \
           (evaluation > current_eval if parent_state.current_player == 'B' else evaluation < current_eval):
            self.graph[parent_hash]['best_move'] = move
            self.graph[parent_hash]['evaluation'] = evaluation


# Initialize GameGraph
game_graph = GameGraph()

#-------------------#Game helper functions#-------------------#
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

    

def draw_button(text, x, y, w, h, color, hover_color, action=None):
    """Draws a button and handles clicks."""
    mouse = pygame.mouse.get_pos()
    click = False
    for event in pygame.event.get():
        if event.type == pygame.MOUSEBUTTONDOWN and event.button == 1:
            click = True
    
    rect = pygame.Rect(x, y, w, h)
    if rect.collidepoint(mouse):
        pygame.draw.rect(screen, hover_color, rect)
        if click and action is not None:
            action()  # Execute the action when clicked
    else:
        pygame.draw.rect(screen, color, rect)
    
    text_surf = small_font.render(text, True, WHITE)
    screen.blit(text_surf, (x + (w - text_surf.get_width()) // 2, y + (h - text_surf.get_height()) // 2))
    return click and rect.collidepoint(mouse) and action is not None

def draw_hexagon(x, y, radius, color):
    """Draw a pointy-top hexagon centered at (x, y)."""
    points = []
    for i in range(6):
        angle = math.radians(60 * i + 30)  # Rotate by 30 degrees to point upwards
        px = x + radius * math.cos(angle)
        py = y + radius * math.sin(angle)
        points.append((px, py))
    if color == BLACK:
        pygame.draw.polygon(screen, color, points, 2)
    else:
        pygame.draw.polygon(screen, color, points, 0)
        
def draw_board(game_state):
    """Draws the hexagonal board with proper staggered layout."""
    screen.fill(BG_COLOR)
    
    start_x = WIDTH // 2  # Center horizontally
    start_y = HEIGHT // 2 - (2 * HEX_HEIGHT)  # Center vertically

    for r, row_length in enumerate(board_layout):
        row_x = start_x - (row_length * HEX_WIDTH) / 2  # Center row
        row_y = start_y + r * (HEX_HEIGHT * 0.75)  # Move down in a staggered pattern

        for q in range(row_length):
            x = row_x + q * HEX_WIDTH
            y = row_y

            # Draw the hexagon with the proper color based on board state
            if game_state.board[r][q] == 'R':
                draw_hexagon(x, y, HEX_RADIUS, (255, 0, 0))  # Draw Red
            elif game_state.board[r][q] == 'B':
                draw_hexagon(x, y, HEX_RADIUS, (0, 0, 255))  # Draw Blue
            else:
                draw_hexagon(x, y, HEX_RADIUS, BLACK)  # Draw empty hexagon



def place_stone(game_state, r, q):
    """Places a stone for the current player and switches turns."""
    global current_player
    move = (r, q)

    print(f"Attempting to place stone at position {move} for player {current_player}")
    if game_state.board[r][q] != '':
        print(f"Move {move} is not valid. The spot is either taken or not possible.")
        return game_state
    if move in game_state.get_possible_moves():  # Only place a stone if the spot is empty
        print(f"Move {move} is valid. Applying move...")

        # Apply the move and update the game state
        game_state = game_state.apply_move(r, q)
        
        # Print the updated board after the move
        

        # Switch turns
        current_player = 'B' if current_player == 'R' else 'R'
        print(f"Turn switched to player {current_player}")

        return game_state
    else:
        print(f"Move {move} is not valid. The spot is either taken or not possible.")
        return game_state

#-------------------------------------------------#

def game_loop():
    """Main game loop for 2-player game with undo functionality."""
    global current_player
    game_state = GameState(
        [['', '', ''], ['', '', '', ''], ['', '', '', '', ''], ['', '', '', ''], ['', '', '']], 'R'
    )
    
    move_history = []  # Stores past game states for undo

    while not game_state.is_game_over():
        screen.fill(BG_COLOR)
        draw_board(game_state)

        # Display player turn (use game_state.current_player instead of global)
        turn_text = font.render(f"Player {game_state.current_player}'s Turn", True, BLACK)
        screen.blit(turn_text, (WIDTH // 2 - turn_text.get_width() // 2, 50))

        # Draw Undo button
        undo_button_rect = pygame.Rect(WIDTH - 150, HEIGHT - 100, 120, 50)
        draw_button("Undo", *undo_button_rect.topleft, 120, 50, BUTTON_COLOR, BUTTON_HOVER)

        pygame.display.update()

        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:  # Left click
                    mouse_x, mouse_y = pygame.mouse.get_pos()
                    
                    # Check if Undo button was clicked
                    if undo_button_rect.collidepoint(mouse_x, mouse_y):
                        if move_history:  # Only undo if there’s a move history
                            game_state = move_history.pop()
                            print("Undo successful")
                        else:
                            print("No moves to undo")
                    else:
                        coords = get_board_coordinates(mouse_x, mouse_y)
                        if coords:
                            r, q = coords
                            move_history.append(game_state)  # Save state before move
                            game_state = place_stone(game_state, r, q)  # Update state


def ai_move(game_state, difficulty='medium', ai_type='minimax', game_graph=None, logger=None):
    tracemalloc.start()
    start_time = time.time()
    
    move = None
    metrics = {}
    
    try:
        if ai_type == "minimax":
            depth = {"easy": 2, "medium": 3, "hard": 6}[difficulty]
            _, move, game_graph = minimax_with_gamegraph(
                game_state, depth, -float('inf'), float('inf'), True, game_graph
            )
            metrics["depth"] = depth
            
        elif ai_type == "montecarlo":
            simulations = {"easy": 10, "medium": 25, "hard": 50}[difficulty]
            move, game_graph = monte_carlo_tree_search(game_state, game_graph, simulations)
            metrics["simulations"] = simulations
            
        time_taken = time.time() - start_time
        current, peak = tracemalloc.get_traced_memory()
        tracemalloc.stop()
        
        if logger:
            player_type = f"AI_{ai_type}_{difficulty}"
            logger.log_move(
                player_type,
                move,
                time_taken,
                peak,
                metrics.get("depth"),
                metrics.get("simulations")
            )
            
        return game_state.apply_move(*move), game_graph
        
    except Exception as e:
        tracemalloc.stop()
        print(f"AI move failed: {str(e)}")
        return game_state, game_graph

#-------------------#Minimax#-------------------#

def minimax_with_gamegraph(game_state, depth, alpha, beta, maximizing_player, game_graph):
    """
    Improved minimax with proper state management in GameGraph.
    """
    state_hash = game_state.get_hash()
    
    # Check for stored state
    stored = game_graph.get_state(game_state)
    if stored and stored['evaluation'] is not None and (depth == 0 or game_state.is_game_over()):
        return stored['evaluation'], stored.get('best_move'), game_graph
    
    # Base case
    if depth == 0 or game_state.is_game_over():
        evaluation = game_state.evaluate()
        game_graph.add_state(game_state, evaluation)
        return evaluation, None, game_graph
    
    # Initialize best values
    best_eval = float('-inf') if maximizing_player else float('inf')
    best_move = None
    
    # Explore all possible moves
    for move in game_state.get_possible_moves():
        if not move:  # Skip invalid moves
            continue
            
        new_state = game_state.apply_move(*move)
        
        # Recursive call
        eval_score, _, game_graph = minimax_with_gamegraph(
            new_state, depth-1, alpha, beta, not maximizing_player, game_graph
        )
        
        # Update best values
        if (maximizing_player and eval_score > best_eval) or \
           (not maximizing_player and eval_score < best_eval):
            best_eval = eval_score
            best_move = move
            
            # Update alpha/beta
            if maximizing_player:
                alpha = max(alpha, best_eval)
            else:
                beta = min(beta, best_eval)
            
            # Prune if possible
            if beta <= alpha:
                break
    
    # Store results in graph
    game_graph.add_state(game_state, best_eval, best_move)
    if best_move:
        new_state = game_state.apply_move(*best_move)
        game_graph.add_child(game_state, new_state, best_move, best_eval)
    
    return best_eval, best_move, game_graph

#-------------------#Monte Carlo BS#-------------------#

def simulate_game(game_state, max_depth=50):
    current_state = game_state
    depth = 0
    
    while not current_state.is_game_over() and depth < max_depth:
        moves = current_state.get_possible_moves()
        if not moves:
            break
            
        # Heuristic: prefer moves that create large groups
        if random.random() < 0.7:  # 70% chance to use heuristic
            move_scores = []
            for move in moves:
                new_state = current_state.apply_move(*move)
                # Score based on size of the created group (bigger is better)
                group_size = len(new_state.flood_fill(move[0], move[1], current_state.current_player))
                
                # Additional bonus if this move would eliminate smaller groups
                original_groups = len(current_state.groups_r if current_state.current_player == 'R' else current_state.groups_b)
                new_groups = len(new_state.groups_r if current_state.current_player == 'R' else new_state.groups_b)
                elimination_bonus = (original_groups - new_groups) * 2  # Bonus for each group eliminated
                
                score = group_size + elimination_bonus
                move_scores.append(score)
                
            move = moves[move_scores.index(max(move_scores))]
        else:
            move = random.choice(moves)
            
        current_state = current_state.apply_move(*move)
        depth += 1
    
    # Evaluation with bonus for terminal state
    if current_state.is_game_over():
        return current_state.evaluate() * 1.5  # Bonus for reaching terminal state
    return current_state.evaluate()
    
def monte_carlo_tree_search(game_state, game_graph, num_simulations=100):
    possible_moves = game_state.get_possible_moves()
    if not possible_moves:
        return None, game_graph

    # Initialize move statistics
    move_stats = {
        tuple(move): {'wins': 0, 'plays': 0, 'total_score': 0}
        for move in possible_moves
    }

    total_plays = 0

    for _ in range(num_simulations):
        # Selection phase - UCB1 formula
        best_ucb = -float('inf')
        selected_move = None
        
        for move, stats in move_stats.items():
            if stats['plays'] == 0:
                selected_move = move
                break
                
            exploitation = stats['total_score'] / stats['plays']
            exploration = math.sqrt(2 * math.log(total_plays) / stats['plays'])
            ucb = exploitation + exploration
            
            if ucb > best_ucb:
                best_ucb = ucb
                selected_move = move

        if selected_move is None:
            selected_move = random.choice(list(move_stats.keys()))

        # Simulation phase
        new_state = game_state.apply_move(*selected_move)
        result = simulate_game(new_state)
        total_plays += 1

        # Backpropagation phase
        move_stats[selected_move]['plays'] += 1
        move_stats[selected_move]['total_score'] += result
        if result > 0:  # Assuming positive is good for current player
            move_stats[selected_move]['wins'] += 1

        # Store in game graph
        game_graph.add_state(new_state, result)
        game_graph.add_child(game_state, new_state, selected_move, result)

    # Select best move - fixed max() usage
    def get_win_ratio(item):
        move, stats = item
        return stats['wins'] / stats['plays'] if stats['plays'] > 0 else 0

    best_move = max(move_stats.items(), key=get_win_ratio)[0]
    return best_move, game_graph


def ai_game_loop(difficulty='medium'):
    """Game loop for playing against AI with logging."""
    global current_player
    game_state = GameState(
        [['', '', ''], ['', '', '', ''], ['', '', '', '', ''], ['', '', '', ''], ['', '', '']], 'R'
    )
    move_history = []
    game_graph = GameGraph()
    logger = GameLogger()
    logger.set_players("human", "N/A", "AI", difficulty)

    while not game_state.is_game_over():
        screen.fill(BG_COLOR)
        draw_board(game_state)
        
        # Display player turn
        turn_text = font.render(f"Player {game_state.current_player}'s Turn", True, BLACK)
        screen.blit(turn_text, (WIDTH // 2 - turn_text.get_width() // 2, 50))
        
        # Draw buttons
        draw_button("Undo", WIDTH - 150, HEIGHT - 100, 120, 50, BUTTON_COLOR, BUTTON_HOVER)
        draw_button("Menu", 30, HEIGHT - 100, 120, 50, BUTTON_COLOR, BUTTON_HOVER, main_menu)
        
        pygame.display.update()
        
        if game_state.current_player == 'B':  # AI turn
            pygame.time.delay(500)
            move_history.append(game_state)
            game_state, game_graph = ai_move(game_state, difficulty, "minimax", game_graph, logger)
        else:  # Human turn
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    pygame.quit()
                    sys.exit()
                elif event.type == pygame.MOUSEBUTTONDOWN:
                    if event.button == 1:
                        mouse_x, mouse_y = pygame.mouse.get_pos()
                        
                        if WIDTH - 150 <= mouse_x <= WIDTH - 30 and HEIGHT - 100 <= mouse_y <= HEIGHT - 50:
                            if move_history:
                                game_state = move_history.pop()
                        elif 30 <= mouse_x <= 150 and HEIGHT - 100 <= mouse_y <= HEIGHT - 50:
                            main_menu()
                        else:
                            coords = get_board_coordinates(mouse_x, mouse_y)
                            if coords:
                                r, q = coords
                                move_history.append(game_state)
                                game_state = place_stone(game_state, r, q)
                                # Log human move
                                logger.log_move(
                                    "human",
                                    (r, q),
                                    0,  # Human move time negligible
                                    0   # Human memory usage negligible
                                )
    
    # Record final result
    winner = game_state.get_winner()
    logger.record_result(winner, 5)  # Board size 5
    logger.save_to_file()
    game_over_screen(winner)

def game_over_screen(winner):
    """Displays the game over screen."""
    while True:
        screen.fill(BG_COLOR)
        title = font.render(f"Game Over! Winner: {winner}", True, BLACK)
        screen.blit(title, (WIDTH // 2 - title.get_width() // 2, 100))
        
        # Buttons
        restart_rect = pygame.Rect(WIDTH//2 - 100, 200, 200, 50)
        menu_rect = pygame.Rect(WIDTH//2 - 100, 270, 200, 50)
        
        mouse_pos = pygame.mouse.get_pos()
        pygame.draw.rect(screen, BUTTON_HOVER if restart_rect.collidepoint(mouse_pos) else BUTTON_COLOR, restart_rect)
        pygame.draw.rect(screen, BUTTON_HOVER if menu_rect.collidepoint(mouse_pos) else BUTTON_COLOR, menu_rect)
        
        screen.blit(small_font.render("Restart", True, WHITE), 
                   (restart_rect.x + (restart_rect.w - small_font.size("Restart")[0])//2, 
                    restart_rect.y + (restart_rect.h - small_font.size("Restart")[1])//2))
        screen.blit(small_font.render("Menu", True, WHITE), 
                   (menu_rect.x + (menu_rect.w - small_font.size("Menu")[0])//2, 
                    menu_rect.y + (menu_rect.h - small_font.size("Menu")[1])//2))
        
        pygame.display.update()
        
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:
                    if restart_rect.collidepoint(event.pos):
                        ai_game_loop()
                        return
                    elif menu_rect.collidepoint(event.pos):
                        main_menu()
                        return

def difficulty_menu():
    """Menu to select AI difficulty."""
    while True:
        screen.fill(BG_COLOR)
        title = font.render("Select AI Difficulty", True, BLACK)
        screen.blit(title, (WIDTH // 2 - title.get_width() // 2, 100))

        # Create button rectangles
        easy_rect = pygame.Rect(WIDTH//2 - 100, 200, 200, 50)
        medium_rect = pygame.Rect(WIDTH//2 - 100, 270, 200, 50)
        hard_rect = pygame.Rect(WIDTH//2 - 100, 340, 200, 50)
        back_rect = pygame.Rect(WIDTH//2 - 100, 410, 200, 50)

        # Draw buttons
        mouse_pos = pygame.mouse.get_pos()
        pygame.draw.rect(screen, BUTTON_HOVER if easy_rect.collidepoint(mouse_pos) else BUTTON_COLOR, easy_rect)
        pygame.draw.rect(screen, BUTTON_HOVER if medium_rect.collidepoint(mouse_pos) else BUTTON_COLOR, medium_rect)
        pygame.draw.rect(screen, BUTTON_HOVER if hard_rect.collidepoint(mouse_pos) else BUTTON_COLOR, hard_rect)
        pygame.draw.rect(screen, BUTTON_HOVER if back_rect.collidepoint(mouse_pos) else BUTTON_COLOR, back_rect)

        # Draw button text
        screen.blit(small_font.render("Easy", True, WHITE), 
                   (easy_rect.x + (easy_rect.w - small_font.size("Easy")[0])//2, 
                    easy_rect.y + (easy_rect.h - small_font.size("Easy")[1])//2))
        screen.blit(small_font.render("Medium", True, WHITE), 
                   (medium_rect.x + (medium_rect.w - small_font.size("Medium")[0])//2, 
                    medium_rect.y + (medium_rect.h - small_font.size("Medium")[1])//2))
        screen.blit(small_font.render("Hard", True, WHITE), 
                   (hard_rect.x + (hard_rect.w - small_font.size("Hard")[0])//2, 
                    hard_rect.y + (hard_rect.h - small_font.size("Hard")[1])//2))
        screen.blit(small_font.render("Back", True, WHITE), 
                   (back_rect.x + (back_rect.w - small_font.size("Back")[0])//2, 
                    back_rect.y + (back_rect.h - small_font.size("Back")[1])//2))

        pygame.display.update()

        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:  # Left mouse button
                    if easy_rect.collidepoint(event.pos):
                        ai_game_loop('easy')
                        return
                    elif medium_rect.collidepoint(event.pos):
                        ai_game_loop('medium')
                        return
                    elif hard_rect.collidepoint(event.pos):
                        ai_game_loop('hard')
                        return
                    elif back_rect.collidepoint(event.pos):
                        main_menu()
                        return

def ai_vs_ai_setup():
    """Let user choose AI types and difficulties for both players."""
    red_ai = "minimax"
    blue_ai = "montecarlo"
    red_difficulty = "easy"
    blue_difficulty = "easy"

    ai_types = ["minimax", "montecarlo"]
    difficulties = ["easy", "medium", "hard"]

    red_ai_idx = 0
    blue_ai_idx = 1
    red_diff_idx = 0
    blue_diff_idx = 0

    while True:
        screen.fill(BG_COLOR)
        title = font.render("AI vs AI Setup", True, BLACK)
        screen.blit(title, (WIDTH // 2 - title.get_width() // 2, 50))

        mouse = pygame.mouse.get_pos()
        click = False
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            if event.type == pygame.MOUSEBUTTONDOWN and event.button == 1:
                click = True

        # Define buttons
        def draw_cycle_button(x, y, label, options, index, mouse, click):
            rect = pygame.Rect(x, y, 200, 40)
            hovered = rect.collidepoint(mouse)
            pygame.draw.rect(screen, BUTTON_HOVER if hovered else BUTTON_COLOR, rect)
            text = f"{label}: {options[index]}"
            txt_surface = small_font.render(text, True, WHITE)
            screen.blit(txt_surface, (rect.x + 10, rect.y + 10))
            if hovered and click:
                index = (index + 1) % len(options)
            return index

        red_ai_idx = draw_cycle_button(WIDTH // 2 - 250, 150, "Red AI", ai_types, red_ai_idx, mouse, click)
        red_diff_idx = draw_cycle_button(WIDTH // 2 - 250, 210, "Red Difficulty", difficulties, red_diff_idx, mouse, click)
        blue_ai_idx = draw_cycle_button(WIDTH // 2 + 50, 150, "Blue AI", ai_types, blue_ai_idx, mouse, click)
        blue_diff_idx = draw_cycle_button(WIDTH // 2 + 50, 210, "Blue Difficulty", difficulties, blue_diff_idx, mouse, click)

        # Start match button
        start_rect = pygame.Rect(WIDTH // 2 - 100, 300, 200, 50)
        pygame.draw.rect(screen, BUTTON_HOVER if start_rect.collidepoint(mouse) else BUTTON_COLOR, start_rect)
        screen.blit(small_font.render("Start Match", True, WHITE),
                    (start_rect.x + 50, start_rect.y + 15))

        if start_rect.collidepoint(mouse) and click:
            red_ai = ai_types[red_ai_idx]
            blue_ai = ai_types[blue_ai_idx]
            red_difficulty = difficulties[red_diff_idx]
            blue_difficulty = difficulties[blue_diff_idx]
            ai_vs_ai_game_loop(red_ai, red_difficulty, blue_ai, blue_difficulty)

        pygame.display.update()

def ai_vs_ai_game_loop(red_ai_type='minimax', red_difficulty='medium',
                      blue_ai_type='minimax', blue_difficulty='medium'):
    """Game loop for AI vs AI with logging."""
    global current_player
    game_state = GameState(
        [['', '', ''], ['', '', '', ''], ['', '', '', '', ''], ['', '', '', ''], ['', '', '']], 'R'
    )
    game_graph_r = GameGraph()
    game_graph_b = GameGraph()
    logger = GameLogger()
    logger.set_players(red_ai_type, red_difficulty, blue_ai_type, blue_difficulty)

    while not game_state.is_game_over():
        screen.fill(BG_COLOR)
        draw_board(game_state)

        turn_text = font.render(f"AI {game_state.current_player}'s Turn", True, BLACK)
        screen.blit(turn_text, (WIDTH // 2 - turn_text.get_width() // 2, 50))

        draw_button("Menu", 30, HEIGHT - 100, 120, 50, BUTTON_COLOR, BUTTON_HOVER, main_menu)
        pygame.display.update()

        if game_state.current_player == 'R':
            game_state, game_graph_r = ai_move(
                game_state, red_difficulty, red_ai_type, game_graph_r, logger
            )
        else:
            game_state, game_graph_b = ai_move(
                game_state, blue_difficulty, blue_ai_type, game_graph_b, logger
            )


    winner = game_state.get_winner()
    logger.record_result(winner, 5)  # Board size 5
    logger.save_to_file(f"ai_vs_ai_{red_ai_type}_{red_difficulty}_vs_{blue_ai_type}_{blue_difficulty}")
    game_over_screen(winner)

def main_menu():
    """Displays the main menu.""" 
    while True:
        screen.fill(BG_COLOR)
        title = font.render("CHURN", True, BLACK)
        screen.blit(title, (WIDTH // 2 - title.get_width() // 2, 100))
        
        mouse = pygame.mouse.get_pos()
        click = False
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            if event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:
                    click = True
        
        # Button positions
        two_player_rect = pygame.Rect(WIDTH//2 - 100, 200, 200, 50)
        ai_rect = pygame.Rect(WIDTH//2 - 100, 270, 200, 50)
        ai_vs_ai_rect = pygame.Rect(WIDTH//2 - 100, 340, 200, 50)
        quit_rect = pygame.Rect(WIDTH//2 - 100, 410, 200, 50)

        # Handle button clicks
        if two_player_rect.collidepoint(mouse) and click:
            game_loop()
        if ai_rect.collidepoint(mouse) and click:
            difficulty_menu()
        if ai_vs_ai_rect.collidepoint(mouse) and click:
            ai_vs_ai_setup()
        if quit_rect.collidepoint(mouse) and click:
            pygame.quit()
            sys.exit()

        # Draw buttons
        pygame.draw.rect(screen, BUTTON_HOVER if two_player_rect.collidepoint(mouse) else BUTTON_COLOR, two_player_rect)
        pygame.draw.rect(screen, BUTTON_HOVER if ai_rect.collidepoint(mouse) else BUTTON_COLOR, ai_rect)
        pygame.draw.rect(screen, BUTTON_HOVER if ai_vs_ai_rect.collidepoint(mouse) else BUTTON_COLOR, ai_vs_ai_rect)
        pygame.draw.rect(screen, BUTTON_HOVER if quit_rect.collidepoint(mouse) else BUTTON_COLOR, quit_rect)

        # Draw button text
        screen.blit(small_font.render("2 Player Game", True, WHITE),
                    (two_player_rect.x + (two_player_rect.w - small_font.size("2 Player Game")[0]) // 2,
                     two_player_rect.y + (two_player_rect.h - small_font.size("2 Player Game")[1]) // 2))

        screen.blit(small_font.render("Play vs AI", True, WHITE),
                    (ai_rect.x + (ai_rect.w - small_font.size("Play vs AI")[0]) // 2,
                     ai_rect.y + (ai_rect.h - small_font.size("Play vs AI")[1]) // 2))

        screen.blit(small_font.render("AI vs AI", True, WHITE),
                    (ai_vs_ai_rect.x + (ai_vs_ai_rect.w - small_font.size("AI vs AI")[0]) // 2,
                     ai_vs_ai_rect.y + (ai_vs_ai_rect.h - small_font.size("AI vs AI")[1]) // 2))

        screen.blit(small_font.render("Quit", True, WHITE),
                    (quit_rect.x + (quit_rect.w - small_font.size("Quit")[0]) // 2,
                     quit_rect.y + (quit_rect.h - small_font.size("Quit")[1]) // 2))

        pygame.display.update()


# Start the game
main_menu()
