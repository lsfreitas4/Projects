:- use_module(library(lists)).
:- use_module(library(system)).
:- use_module(library(random)).

% start_game2(+Size, -GameState)
% Starts a game between a human and a computer.

start_game2(Size, GameState) :-
    write('You will play with the red (X) pieces.'), nl,
    write('The computer will play with the blue (O) pieces.'), nl, nl,
    initial_state(Size, GameState),
    NewGameState = GameState,
    game_loop2(GameState, NewGameState).

% start_game3(+Size, -GameState)
% Starts a game between two computers.

start_game3(Size, GameState) :-
    initial_state(Size, GameState),
    NewGameState = GameState,
    game_loop3(GameState, NewGameState).

% game_loop2(+GameState, -NewGameState)
% Implements the game loop for a game between a human and a computer, ensuring that the game runs until it is over.

game_loop2((Board, Player), NewGameState) :-
    move2((Board, Player), Move, (NewBoard, NewPlayer)),
    next_player(Player, NewPlayer),
    display_game((NewBoard, NewPlayer)),
    game_loop2((NewBoard, NewPlayer), (NewBoard, NewPlayer)).

% game_loop3(+GameState, -NewGameState)
% Implements the game loop for a game between two computers, ensuring that the game runs until it is over.

game_loop3((Board, Player), NewGameState) :-
    move3((Board, Player), Move, (NewBoard, NewPlayer)),
    next_player(Player, NewPlayer),
    display_game((NewBoard, NewPlayer)),
    wait(0.5),
    game_loop3((NewBoard, NewPlayer), (NewBoard, NewPlayer)).

% move2(+GameState, -Move, -NewGameState)
% Encapsulates the logic of choosing a move from either a human or a computer.

move2((Board, Player), Move, (NewBoard, NewPlayer)) :-
    display_player_turn2((Board, Player), Move, Board, NewBoard).

% move3(+GameState, -Move, -NewGameState)
% Encapsulates the logic of choosing a move from a computer.

move3((Board, Player), Move, (NewBoard, NewPlayer)) :-
    display_computer_turn((Board, Player), Move, Board, NewBoard).

% display_player_turn2(+GameState, -Move, +Board, -NewBoard)
% Selects the appropriate game turn and displays it based on the current player.

display_player_turn2((Board, Player), (Row1, Col1, Row2, Col2), Board, NewBoard) :-
    (Player = r -> display_human_turn((Board, Player), (Row1, Col1, Row2, Col2), Board, NewBoard);
    Player = b -> display_computer_turn((Board, Player), (Row1, Col1, Row2, Col2), Board, NewBoard)).

% display_computer_turn(+GameState, -Move, +Board, -NewBoard)
% Displays the computer's turn.

display_computer_turn((Board, Player), (Row1, Col1, Row2, Col2), Board, NewBoard) :-
    % get all possible positions from the board
    possible_pieces(Board, Player, PossiblePieces),
    
    other_player(Player, OtherPlayer),
    
    % If PossiblePieces is empty, other player wins
    (PossiblePieces = [] -> game_over((Board, OtherPlayer)), !; true),
    
    all_valid_moves(Board, Player, PossiblePieces, AllValidMoves),
    
    % If AllValidMoves is empty, other player wins
    (AllValidMoves = [] -> game_over((Board, OtherPlayer)), !;
        % Choose a random piece from the possible pieces
        random_piece((Row, Col), PossiblePieces),
        
        % Get all possible moves for the chosen piece
        valid_moves(Board, Player, (Row, Col), PossibleMoves),
        
        % If PossibleMoves is empty, select another random piece
        (PossibleMoves = [] -> display_computer_turn((Board, Player), (Row1, Col1, Row2, Col2), Board, NewBoard);
            FinalRow is Row + 1,
            FinalCol is Col + 97,
            char_code(Char, FinalCol),
            nl,
            
            (Player = r -> Symbol = 'X'; Player = b -> Symbol = 'O'),
            nl,
            format('Player ~w is thinking...', [Symbol]), nl, nl,
            
            wait(0.5),
            
            % Choose a random move from the possible moves
            random_move((Row2, Col2), PossibleMoves),
            FinalRow2 is Row2 + 1,
            FinalCol2 is Col2 + 97,
            char_code(Char2, FinalCol2),
            write('Moving '), write(FinalRow), write(Char), write(' to '), write(FinalRow2), write(Char2), write('.'), nl,
            
            % Make the move
            move_piece(Board, Row, Col, e, TempBoard),
            move_piece(TempBoard, Row2, Col2, Player, NewBoard),
            
            check_win((NewBoard, Player)),
            
            wait(0.5)
        )
    ).

% all_valid_moves(+Board, +Player, +PossiblePieces, -AllValidMoves)
% Gets all valid moves for all possible pieces. 

all_valid_moves(Board, Player, PossiblePieces, AllValidMoves) :-
    findall((Row1, Col1, Row2, Col2), (
        member((Row1, Col1), PossiblePieces),
        valid_moves(Board, Player, (Row1, Col1), PossibleMoves),
        member((Row2, Col2), PossibleMoves)
    ), AllValidMoves).

% wait(+Seconds)
% Waits for a given number of seconds.

wait(Seconds) :-
    sleep(Seconds).

% other_player(+Player, -OtherPlayer)
% Gets the other player.

other_player(r, b).
other_player(b, r).

% possible_pieces(+Board, +Player, -PossiblePieces)
% Gets all possible pieces for a given player.

possible_pieces(Board, Player, PossiblePieces) :-
    findall((Row, Col), (nth0(Row, Board, RowList), nth0(Col, RowList, Player)), PossiblePieces).

% random_piece(-Piece, +PossiblePieces)
% Gets a random piece from a list of possible pieces.

random_piece((Row, Col), PossiblePieces) :-
    length(PossiblePieces, Length),
    random(0, Length, Index),
    nth0(Index, PossiblePieces, (Row, Col)).

% valid_moves(+Board, +Player, +Piece, -ValidMoves)
% Gets all valid moves for a given piece.

valid_moves(Board, Player, (Row1, Col1), ValidMoves) :-
    findall((Row2, Col2), (
        (Row2 is Row1 - 1, Col2 = Col1, is_valid_move(Board, Player, (Row1, Col1), (Row2, Col2)));
        (Row2 is Row1 + 1, Col2 = Col1, is_valid_move(Board, Player, (Row1, Col1), (Row2, Col2)));
        (Row2 = Row1, Col2 is Col1 - 1, is_valid_move(Board, Player, (Row1, Col1), (Row2, Col2)));
        (Row2 = Row1, Col2 is Col1 + 1, is_valid_move(Board, Player, (Row1, Col1), (Row2, Col2)))
    ), ValidMoves).

% is_valid_move(+Board, +Player, +Piece, +Position)
% Checks if a given position is a valid move for a given piece.

is_valid_move(Board, Player, (Row1, Col1), (Row2, Col2)) :-
    nth0(Row1, Board, RowList1),
    nth0(Row2, Board, RowList2),
    nth0(Col1, RowList1, Piece1),
    nth0(Col2, RowList2, Piece2),
    valid_position(Board, Row2, Col2),
    (Piece2 \= e).

% valid_position(+Board, +Row, +Col)
% Checks if a position is within the board's boundaries.

valid_position(Board, Row, Col) :-
    length(Board, Size),
    Row >= 0, Row < Size,
    Col >= 0, Col < Size.

% random_move(-Move, +PossibleMoves)
% Gets a random move from a list of possible moves.

random_move((Row2, Col2), PossibleMoves) :-
    length(PossibleMoves, Length),
    random(0, Length, Index),
    nth0(Index, PossibleMoves, (Row2, Col2)).



