:- use_module(library(lists)).

% initial_state(+Size, -(Board, r))
% Sets up the initial state of the game board based on the specified size.

initial_state(Size, (Board, r)) :-
    display_top_row(Size),
    initial_board(Size, Board),
    display_board_with_left_column(Board, 1, Size).

% initial_board(+Size, -Board)
% Generates the initial configuration of the game board.

initial_board(Size, Board) :-
    length(Board, Size),
    maplist(same_length(Board), Board),
    fill_board(Board, Size).

% fill_board(+Board, +Size)
% Recursively fills the board with the red/blue pieces.

fill_board(Board, Size) :-
    fill_board(Board, Size, r).

fill_board([], _, _).
fill_board([Line|Rest], Size, Color) :-
    fill_line(Line, Size, Color),
    switch_color(Color, NextColor),
    fill_board(Rest, Size, NextColor).

% fill_line(+Line, +Size, +Color)
% Recursively fills a line with the red/blue pieces.

fill_line([], _, _).
fill_line([Cell|Rest], Size, Color) :-
    Cell = Color,
    switch_color(Color, NextColor),
    Size1 is Size - 1,
    fill_line(Rest, Size1, NextColor).

% switch_color(+Color, -NextColor)
% Switches the color of a piece.

switch_color(r, b).
switch_color(b, r).

% symbol(+Piece, -Symbol)
% Associates a piece with its corresponding symbol.

symbol(b, '  O  ').
symbol(r, '  X  ').
symbol(_, '     ').

% write_n_times(+X, +N)
% Writes a given input N times.

write_n_times(_, 0). 
write_n_times(X, N) :-
    N > 0,            
    write(X),         
    N1 is N - 1,      
    write_n_times(X, N1).  

% display_top_row(+Size)
% Displays the top row of the board.

display_top_row(Size) :-   
    write('            -'),
    write_n_times('------', Size), nl,
    write('            |'),
    display_top_row(Size, 97),
    write('            -'),
    write_n_times('------', Size), nl, nl,
    write('-------     -'),
    write_n_times('------', Size), nl.

display_top_row(0, _) :- nl.
display_top_row(Size, Letter) :-
    format('  ~c  |', [Letter]),
    Letter1 is Letter + 1,
    Size1 is Size - 1,
    display_top_row(Size1, Letter1).

% display_board_with_left_column(+Board, +N, +Size)
% Displays the board along with the left column.

display_board_with_left_column([], _, Size).
display_board_with_left_column([Line|Rest], N, Size) :-
    display_left_column(N),
    display_line(Line), nl,
    write('-------     -'),
    write_n_times('------', Size), nl,
    N1 is N + 1,
    display_board_with_left_column(Rest, N1, Size).

% display_left_column(+N)
% Displays the left column of the board, with the corresponding number.

display_left_column(N) :-
    format('|  ~w  |     |', [N]).

% display_line(+Line)
% Displays a line of the board.

display_line([]).
display_line([Cell|Tail]) :-
    symbol(Cell, S),
    format('~w|', [S]),
    display_line(Tail).

% display_game(+GameState)
% Displays the current state of the game.

display_game((NewBoard, NewPlayer)) :-
    length(NewBoard, Size),
    display_top_row(Size),
    display_board_with_left_column(NewBoard, 1, Size).

