:- use_module(library(lists)).
:- use_module(library(system)).

% start_game1(+Size, -GameState)
% Sets up the initial state of the Human vs Human game.

start_game1(Size, GameState) :-
    initial_state(Size, GameState),
    NewGameState = GameState,
    game_loop(GameState, NewGameState).

% game_loop(+GameState, -NewGameState)
% Main game loop, ensures that the game keeps running until a player wins.

game_loop((Board, Player), NewGameState) :-
    move((Board, Player), Move, (NewBoard, NewPlayer)),
    next_player(Player, NewPlayer),
    display_game((NewBoard, NewPlayer)),
    game_loop((NewBoard, NewPlayer), (NewBoard, NewPlayer)).

% next_player(+Player, -NextPlayer)
% Alternates between players.

next_player(r, b).
next_player(b, r).

% move(+GameState, -Move, -NewGameState)
% Encapsulates the logic of choosing a move and updating the game state.

move((Board, Player), Move, (NewBoard, NewPlayer)) :-
    display_human_turn((Board, Player), Move, Board, NewBoard).

% display_human_turn(+GameState, -Move, +Board, -NewBoard)
% Displays the current state of the game and asks the player for a move.

display_human_turn((Board, Player), (Row1, Col1, Row2, Col2), Board, NewBoard) :-
    length(Board, Size), 
    nl,
    (Player = r -> Symbol = 'X'; Player = b -> Symbol = 'O'),
    format('Player ~w. It\'s your turn!~n', [Symbol]), nl,
    write('About the piece that you want to move: '), nl,

    write('- Column (ex: a) '), read(Col1Char),
    validate_column(Board, Col1Char),

    write('- Row (ex: 1) '), read(Row1), 
    validate_row(Board, Row1),

    format_input(Row1, Col1Char, Row1Dec, Col1),
    validate_player(Board, Row1Dec, Col1, Player, NewRow1Dec, NewCol1),

    move_piece(Board, NewRow1Dec, NewCol1, e, AuxBoard),

    write('About the position that you want to move to: '), nl,

    write('- Column (ex: b) '), read(Col2Char),
    validate_column(AuxBoard, Col2Char),

    write('- Row (ex: 2) '), read(Row2), 
    validate_row(AuxBoard, Row2),

    format_input(Row2, Col2Char, Row2Dec, Col2),

    validate_not_empty_position(AuxBoard, Row2Dec, Col2, NewRow2Dec, NewCol2),

    validate_orthogonal_move(Board, AuxBoard, NewRow1Dec, NewCol1, NewRow2Dec, NewCol2, FinalRow2Dec, FinalCol2),

    validate_separating(AuxBoard, FinalRow2Dec, FinalCol2),
    
    move_piece(AuxBoard, FinalRow2Dec, FinalCol2, Player, NewBoard),

    check_win((NewBoard, Player)).

% validate_column(+Board, -Col1Char)
% Validates the column input, making sure that it is a valid letter.

validate_column(Board, Col1Char) :-
    char_code(Col1Char, Col1Code),
    Col1Code >= 97,
    length(Board, Size),
    Col1Code < 97 + Size.

validate_column(Board, Col1Char) :-
    nl,
    write('Invalid column! Choose a valid letter.'), nl,
    write('- Column (ex: a) '), read(NewCol1Char),
    validate_column(Board, NewCol1Char).

% validate_row(+Board, -Row1)
% Validates the row input, making sure that it is a valid number.

validate_row(Board, Row1) :-
    Row1 > 0,
    length(Board, Size),
    Row1 =< Size.

validate_row(Board, Row1) :-
    write('Invalid row! Choose a valid number.'), nl,
    write('- Row (ex: 1) '), read(NewRow1), 
    validate_row(Board, NewRow1).

% validate_player(+Board, -Row1Dec, -Col1, +Player, -NewRow1Dec, -NewCol1)
% Validates the player input, making sure that the player has chosen a piece of his color.

validate_player(Board, Row1Dec, Col1, Player, Row1Dec, Col1) :-
    nth0(Row1Dec, Board, Row),
    nth0(Col1, Row, Player).

validate_player(Board, Row1Dec, Col1, Player, NewRow1Dec, NewCol1) :-
    write('Invalid player! Choose a piece of your color.'), nl,
    write('- Column (ex: a) '), read(NewCol1Char),
    validate_column(Board, NewCol1Char),
    write('- Row (ex: 1) '), read(NewRow1), 
    validate_row(Board, NewRow1),
    format_input(NewRow1, NewCol1Char, NewRow1Dec, NewCol1),
    validate_player(Board, NewRow1Dec, NewCol1, Player, FinalRow1Dec, FinalCol1).

% validate_not_empty_position(+Board, -Row2Dec, -Col2, +NewRow2Dec, +NewCol2)
% Validates the position input, making sure that the position is not empty.

validate_not_empty_position(Board, Row2Dec, Col2, Row2Dec, Col2) :-
    nth0(Row2Dec, Board, Row),
    (nth0(Col2, Row, r); nth0(Col2, Row, b)).

validate_not_empty_position(Board, Row2Dec, Col2, NewRow2Dec, NewCol2) :-
    nl,
    write('Invalid move! Choose a position that is not empty.'), nl,
    write('- Column (ex: b) '), read(NewCol2Char),
    validate_column(Board, NewCol2Char),
    write('- Row (ex: 2) '), read(NewRow2), 
    validate_row(Board, NewRow2),
    format_input(NewRow2, NewCol2Char, NewRow2Dec, NewCol2),
    validate_not_empty_position(Board, NewRow2Dec, NewCol2, NewRow2Dec, NewCol2).

% validate_orthogonal_move(+Board, +AuxBoard, -Row1Dec, -Col1, -Row2Dec, -Col2, +NewRow2Dec, +NewCol2)
% Validates the move input, making sure that it is orthogonal.

validate_orthogonal_move(Board, AuxBoard, Row1Dec, Col1, Row2Dec, Col2, Row2Dec, Col2) :-
    (Row1Dec =:= Row2Dec -> 
        (Col1 =:= Col2 + 1; Col1 =:= Col2 - 1));
    (Col1 =:= Col2 -> 
        (Row1Dec =:= Row2Dec + 1; Row1Dec =:= Row2Dec - 1)).

validate_orthogonal_move(Board, AuxBoard, Row1Dec, Col1, Row2Dec, Col2, NewRow2Dec, NewCol2) :-
    nl,
    write('Invalid move! Choose an orthogonal move.'), nl,
    write('- Column (ex: b) '), read(NewCol2Char),
    validate_column(AuxBoard, NewCol2Char),
    write('- Row (ex: 2) '), read(NewRow2), 
    validate_row(AuxBoard, NewRow2),
    format_input(NewRow2, NewCol2Char, NewRow2Dec, NewCol2),
    validate_orthogonal_move(Board, AuxBoard, Row1Dec, Col1, NewRow2Dec, NewCol2, NewRow2Dec, NewCol2).

% validate_separating(+Board, -Row, -Col)
% Validates the move input, making sure that it doesn't separate from the rest of the pieces.

validate_separating(Board, Row, Col) :-
    AuxColPlus is Col + 1,
    AuxColMinus is Col - 1,
    AuxRowPlus is Row + 1,
    AuxRowMinus is Row - 1,
    (
        is_empty(Board, Row, AuxColPlus), is_empty(Board, Row, AuxColMinus), is_empty(Board, AuxRowPlus, Col), is_empty(Board, AuxRowMinus, Col) -> 
        nl,
        write('Invalid move! Choose a move that doesn\'t separate from the rest.'), nl,
        write('- Column (ex: b) '), read(NewCol2Char),
        validate_column(Board, NewCol2Char),
        write('- Row (ex: 2) '), read(NewRow2),
        validate_row(Board, NewRow2),
        format_input(NewRow2, NewCol2Char, NewRow2Dec, NewCol2),
        validate_separating(Board, NewRow2Dec, NewCol2);
        true
    ).

% is_empty(+Board, +Row, +Col)
% Checks if a given position is empty.

is_empty(Board, Row, Col) :-
    nth0(Row, Board, RowList),
    nth0(Col, RowList, 'e').

% custom_flatten(+List, -Flat)
% Flattens a list.

custom_flatten([], []).
custom_flatten([H | T], Flat) :-
    is_list(H),
    custom_flatten(H, FlatH),
    custom_flatten(T, FlatT),
    append(FlatH, FlatT, Flat).
custom_flatten([H | T], [H | FlatT]) :-
    \+ is_list(H),
    custom_flatten(T, FlatT).

% check_win(+GameState)
% Checks if a player has won.

check_win((Board, Player)) :-
    custom_flatten(Board, FlatBoard),
    (player_has_won(FlatBoard, Player) ->
        game_over((Board, Player));   
        nl
    ).

% player_has_won(+Board, +Player)
% Checks if a player has won.

player_has_won(Board, Player) :-
    all_empty_or_player(Board, Player).

% all_empty_or_player(+List, +Player)
% Checks if a list is all empty or all of the same player.

all_empty_or_player([], _).
all_empty_or_player(['e' | Rest], Player) :-
    all_empty_or_player(Rest, Player).
all_empty_or_player([Player | Rest], Player) :-
    all_empty_or_player(Rest, Player).

% game_over(+GameState)
% Ends the game.

game_over((Board, Player)) :-
    (Player = r -> Symbol = 'X'; Player = b -> Symbol = 'O'),
    nl,
    format('Player ~w has won! Congratulations!~n', [Symbol]),
    wait(3),
    halt.

% format_input(+Row1, +Col1Char, -Row1Dec, -Col1)
% Formats the input of the row and column.

format_input(Row1, Col1Char, Row1Dec, Col1) :-
    Row1Dec is Row1 - 1,
    char_code(Col1Char, Col1Code),
    Col1 is Col1Code - 97.

% move_piece(+Board, +RowIndex, +ColIndex, +NewValue, -NewBoard)
% Moves a piece from one position to another.

move_piece(Board, RowIndex, ColIndex, NewValue, NewBoard) :-
    nth0(RowIndex, Board, Row),
    nth0(ColIndex, Row, _),
    replace_piece(Row, ColIndex, NewValue, NewRow),
    replace_piece(Board, RowIndex, NewRow, NewBoard).

% replace_piece(+List, +Index, +NewValue, -NewList)
% Replaces a piece with another one in a list.

replace_piece([_|T], 0, X, [X|T]). 
replace_piece([H|T], I, X, [H|R]) :-
    I > 0,
    I1 is I - 1,
    replace_piece(T, I1, X, R).

