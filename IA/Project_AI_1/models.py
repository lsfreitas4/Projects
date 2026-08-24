import hashlib
from collections import deque
from utils import print_board
import json
import time
from datetime import datetime
import tracemalloc
import os

class GameState:
    def __init__(self, board, current_player):
        self.board = board  # 2D list representing the board
        self.current_player = current_player  # 'R' or 'B'
        self.groups_r = []  # List of groups for player 'R'
        self.groups_b = []  # List of groups for player 'B'
        self.directions_up = {
            'up_left': (-1, -1), 'up_right': (-1, 0), 'right': (0, 1),
            'down_right': (1, 1), 'down_left': (1, 0), 'left': (0, -1)
        }
        self.directions_down = {
            'up_left': (-1, 0), 'up_right': (-1, 1), 'right': (0, 1),
            'down_right': (1, 0), 'down_left': (1, -1), 'left': (0, -1)
        }
        self.directions_middle = {
            'up_left': (-1, -1), 'up_right': (-1, 0), 'right': (0, 1),
            'down_right': (1, 0), 'down_left': (1, -1), 'left': (0, -1)
        }
        self.history = []  # History of moves for undo functionality
        
    def flood_fill(self, r, q, player):
        if self.board[r][q] != player:  # Make sure the function only starts on valid pieces
            return set()
        
        group = set()
        queue = deque([(r, q)])
        group.add((r, q))
    
    
        while queue:
            cr, cq = queue.popleft()

            aux = (len(self.board) + 1) / 2
            if cr + 1 == aux:
                directions = self.directions_middle
            elif cr > len(self.board) / 2:
                directions = self.directions_down
            else:
                directions = self.directions_up
            for dr, dq in directions.values():
                nr, nq = cr + dr, cq + dq
                if 0 <= nr < len(self.board) and 0 <= nq < len(self.board[nr]):
                    if self.board[nr][nq] == player and (nr, nq) not in group:
                        group.add((nr, nq))
                        queue.append((nr, nq))
    
        return group


    def is_adjacent(self, r, q):
        """Check if the position (r, q) is adjacent to any of the current player's stones."""
        
        # Determine the directions based on row parity (even vs odd)
        aux = (len(self.board)+1) / 2
        
        if (r+1 == aux):
            directions = self.directions_middle
        elif r > len(self.board)/2:
            directions = self.directions_down
        else:
            directions = self.directions_up
    
        # Iterate over the directions and check each one
        for direction, (dr, dq) in directions.items():
            nr, nq = r + dr, q + dq
    
            # Check if the new position is within bounds and not out of bounds
            if 0 <= nr < len(self.board) and 0 <= nq < len(self.board[nr]):
    
                # If the board position contains the current player's piece, it's adjacent
                if self.board[nr][nq] == self.current_player:
                    return True
    
        return False



    def get_possible_moves(self):
        possible_moves = []
        isolated_moves = []
        move_to_group_size = {}  # Store group sizes for adjacent moves
        move_to_group = {}  # Store actual groups formed for each move

        # If it's player 'R's first move, allow placing on any empty spot
        if self.current_player == 'R' and not any(cell == 'R' for row in self.board for cell in row):
            for r, row in enumerate(self.board):
                for q, cell in enumerate(row):
                    if cell == '':  # Empty spot
                        possible_moves.append((r, q))
        else:
            for r, row in enumerate(self.board):
                for q, cell in enumerate(row):
                    if cell == '':  # Empty spot
                        is_adjacent_to_current_player = self.is_adjacent(r, q)

                        if not is_adjacent_to_current_player:
                            isolated_moves.append((r, q))
                        else:
                            # Simulate the move and determine the new group size
                            temp_board = [row[:] for row in self.board]
                            temp_board[r][q] = self.current_player
                            simulated_game = GameState(temp_board, self.current_player)
                            new_group = simulated_game.flood_fill(r, q, self.current_player)
                            group_size = len(new_group)
                            move_to_group_size[(r, q)] = group_size
                            move_to_group[(r, q)] = new_group

        # If isolated moves are available, use those
        if isolated_moves:
            possible_moves = isolated_moves
        elif move_to_group_size:
            # Find the smallest group size
            min_group_size = min(move_to_group_size.values())
            possible_moves = [move for move, size in move_to_group_size.items() if size == min_group_size]

        return possible_moves

    def apply_move(self, r, q):
        """Applies a move to the game board, updates groups, and handles clearing of smaller groups if necessary."""
        new_board = [row[:] for row in self.board]
        new_board[r][q] = self.current_player  # Apply the move
        next_player = 'B' if self.current_player == 'R' else 'R'
        # Create a new GameState after the move
        new_game_state = GameState(new_board, next_player)
       
        new_game_state.update_groups()

        groups = self.groups_r if self.current_player == 'R' else self.groups_b
        # Check if a new group is formed and if it's larger than the current largest group
        new_group = new_game_state.flood_fill(r, q, self.current_player)

        if groups and len(new_group) > len(min(groups, key=len)):
            new_game_state.clear_smaller_groups(new_group, self.current_player)

        # Store the move in history
        new_game_state.history.append((r, q))

        return new_game_state

    def is_adjacent_to_player(self, r, q, player):
        """Check if the position (r, q) is adjacent to any of the given player's stones."""
        aux = (len(self.board) + 1) / 2

        if (r + 1 == aux):
            directions = self.directions_middle
        elif r > len(self.board) / 2:
            directions = self.directions_down
        else:
            directions = self.directions_up

        for dr, dq in directions.values():
            nr, nq = r + dr, q + dq
            if 0 <= nr < len(self.board) and 0 <= nq < len(self.board[nr]):
                if self.board[nr][nq] == player:
                    return True
        return False
    
    def clear_smaller_groups(self, new_group, player):
        groups = self.groups_r if player == 'R' else self.groups_b
        
        for group in groups[:]:
            if len(group) < len(new_group):
                for r, q in group:
                    self.board[r][q] = ''
        
        self.update_groups()

    def update_groups(self):
        self.groups_r = []
        self.groups_b = []
        visited = set()

        for r in range(len(self.board)):
            for q in range(len(self.board[r])):
                if (r, q) in visited:
                    continue
                
                if self.board[r][q] == 'R':
                    group = self.flood_fill(r, q, 'R')
                    if group:
                        self.groups_r.append(group)
                        visited.update(group)
                elif self.board[r][q] == 'B':
                    group = self.flood_fill(r, q, 'B')
                    if group:
                        self.groups_b.append(group)
                        visited.update(group)


    def is_game_over(self):
        """Checks if the game is over (win, draw, etc.)."""
        return all(cell != '' for row in self.board for cell in row)

    def evaluate(self):
        """Evaluate the current game state from the perspective of the current player."""
        current_player = self.current_player
        opponent = 'B' if current_player == 'R' else 'R'

        # Cell count difference (current player's cells - opponent's cells)
        current_count = sum(row.count(current_player) for row in self.board)
        opponent_count = sum(row.count(opponent) for row in self.board)
        cell_diff = current_count - opponent_count

        # Group stability (sum of squares of group sizes)
        current_groups = self.groups_r if current_player == 'R' else self.groups_b
        opponent_groups = self.groups_b if current_player == 'R' else self.groups_r

        # Find the size of the largest group for the current player and opponent
        current_largest_group = max(len(g) for g in current_groups) if current_groups else 0
        opponent_largest_group = max(len(g) for g in opponent_groups) if opponent_groups else 0

        # Difference in largest group size
        group_diff = current_largest_group - opponent_largest_group
    
        # Isolated move opportunities (current player's isolated moves - opponent's)
        current_isolated = 0
        opponent_isolated = 0
        for r in range(len(self.board)):
            for q in range(len(self.board[r])):
                if self.board[r][q] == '':
                    if not self.is_adjacent_to_player(r, q, current_player):
                        current_isolated += 1
                    if not self.is_adjacent_to_player(r, q, opponent):
                        opponent_isolated += 1
        isolated_diff = current_isolated - opponent_isolated

        # Weighted sum of factors (weights can be adjusted based on testing)
        weight_cell = 10.0
        weight_group = 100.0
        weight_isolated = 0.1
        evaluation = (weight_cell * cell_diff) + (weight_group * group_diff) + (weight_isolated * isolated_diff)
    
        return evaluation
    
    def get_winner(self):
        red_player = 'R'
        blue_player = 'B'

        # Cell count difference (current player's cells - opponent's cells)
        red_count = sum(row.count(red_player) for row in self.board)
        blue_count = sum(row.count(blue_player) for row in self.board)
        if (blue_count > red_count):
            return "Blue"
        else:
            return "Red"
        
    def undo_move(self):
        """Reverts the last move if possible."""
        if self.history:
            last_move = self.history.pop()
            r, q = last_move
            self.board[r][q] = ''  # Remove last move
            self.update_groups()
            self.current_player = 'B' if self.current_player == 'R' else 'R'  # Switch turns back
            return True  # Move undone
        return False  # No move to undo

    def get_hash(self):
        """Generate a unique hash for this game state."""
        # Generate a string representation of the board, replacing empty cells with ''
        state_str = ''.join([str(cell) if cell != '' else 'E' for row in self.board for cell in row])

        return hashlib.md5(state_str.encode()).hexdigest()


class GameLogger:
    def __init__(self, log_dir="game_logs"):
        self.game_data = {
            "start_time": datetime.now().isoformat(),
            "moves": [],
            "players": {},
            "result": None,
            "performance_metrics": {},
            "board_size": None
        }
        self.start_time = time.time()
        self.log_dir = log_dir
        os.makedirs(log_dir, exist_ok=True)
    
    def log_move(self, player_type, move, time_taken, memory_used, depth=None, simulations=None):
        move_data = {
            "player": player_type,
            "move": move,
            "time_taken": time_taken,
            "memory_used": memory_used,
            "timestamp": datetime.now().isoformat()
        }
        if depth is not None:
            move_data["depth"] = depth
        if simulations is not None:
            move_data["simulations"] = simulations
        self.game_data["moves"].append(move_data)
    
    def set_players(self, player1_type, player1_difficulty, player2_type, player2_difficulty):
        self.game_data["players"] = {
            "player1": {
                "type": player1_type,
                "difficulty": player1_difficulty
            },
            "player2": {
                "type": player2_type,
                "difficulty": player2_difficulty
            }
        }
    
    def record_result(self, result, board_size):
        self.game_data["result"] = result
        self.game_data["board_size"] = board_size
        self.game_data["duration"] = time.time() - self.start_time
    
    def add_performance_metrics(self):
        if not self.game_data["moves"]:
            return
            
        self.game_data["performance_metrics"] = {
            "total_moves": len(self.game_data["moves"]),
            "avg_move_time": sum(m["time_taken"] for m in self.game_data["moves"]) / len(self.game_data["moves"]),
            "max_memory": max(m["memory_used"] for m in self.game_data["moves"]),
            "min_memory": min(m["memory_used"] for m in self.game_data["moves"]),
            "first_move_time": self.game_data["moves"][0]["time_taken"] if len(self.game_data["moves"]) > 0 else 0,
            "last_move_time": self.game_data["moves"][-1]["time_taken"] if len(self.game_data["moves"]) > 0 else 0
        }
    
    def save_to_file(self, filename_prefix="game"):
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{self.log_dir}/{filename_prefix}_{timestamp}.json"
        self.add_performance_metrics()
        with open(filename, 'w') as f:
            json.dump(self.game_data, f, indent=2)
        return filename