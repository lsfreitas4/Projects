% game_menu 
% Displays the game menu and reads the user's option.

game_menu :-
    repeat,
    clear_screen,

    display_menu_title('Welcome to the game of Clusterfuss!'),
    display_menu_empty_line,
    display_menu_options([
        1- 'Human vs Human',
        2- 'Human vs Computer',
        3- 'Computer vs Computer',
        4- 'Game description',
        0- 'Quit'
    ]),
    display_menu_empty_line,
    display_menu_last_line,
    
    read_option(Option),
    game_menu_option(Option).

% clear_screen
% Clears the screen.

clear_screen :- write('\e[2J]').

% display_menu_title(+Title)
% Displays the title of the menu.

display_menu_title(Title) :- format('~n~`*t ~w ~`*t~75|~n', [Title]).

% display_menu_empty_line
% Displays an empty line.

display_menu_empty_line :- format('*~t*~75|~n', []).

% display_menu_options(+Options)
% Displays the menu options.

display_menu_options(Options) :-
    display_menu_subtitle('Input', 'Option'),
    display_menu_empty_line,
    display_menu_option_lines(Options).

% display_menu_subtitle(+Input, +Option)
% Displays the subtitle of the menu.

display_menu_subtitle(Input, Option) :- format('*~t~w~t~37+~t~w~t~37+~t*~75|~n', [Input, Option]).

% display_menu_option_lines(+Options)
% Displays the menu options' lines.

display_menu_option_lines([]).
display_menu_option_lines([Key-Label | Rest]) :-
    format('*~t~w~t~37|~t~w~t~37+~t*~75|~n', [Key, Label]),
    display_menu_option_lines(Rest).

% display_menu_last_line
% Displays the last line of the menu.

display_menu_last_line :- format('~`*t~75|~n', []).

% display_text(+Text)
% Displays the text.

display_text(Text) :- format('* ~w~t~73| *~n', [Text]).

% read_option(-Option)
% Reads the user's option.

read_option(Option) :-
    write('Choose an option: '),
    read(Option),
    integer(Option),
    Option >= 0,
    Option =< 4,
    !.

read_option(Option) :-
    write('Invalid option! Choose a number between 0 and 4.'), nl,
    read_option(Option).

% read_size(-Size)
% Reads the input board size.

read_size(Size) :-
    write('What size do you want the board to have? (4-8) '),
    read(Size),
    integer(Size),
    Size >= 4,
    Size =< 8,
    !.

read_size(Size) :-
    write('Invalid size! Choose a number between 4 and 8.'), nl,
    read_size(Size).

% game_menu_option(+Option)
% Executes the user's option.

game_menu_option(1) :- 
    read_size(Size), nl,
    start_game1(Size, GameState).
game_menu_option(2) :- 
    read_size(Size), nl,
    start_game2(Size, GameState).
game_menu_option(3) :- 
    read_size(Size), nl,
    start_game3(Size, GameState).
game_menu_option(4) :- display_game_description.
game_menu_option(0) :- !.

% display_game_description
% Displays the game description.

display_game_description :-
    display_menu_title('Clusterfuss Game Instructions'),
    display_menu_empty_line,
    display_menu_subtitle('Game board',''),
    display_text('- The board is a squared 8x8 board with a Red and Blue checkered'),
    display_text('pattern.'),
    display_menu_empty_line,
    display_menu_subtitle('Gameplay',''),
    display_text('- The players are Red and Blue, with Red starting.'),
    display_text('- Passing is not allowed, but if there are no available moves, your'),
    display_text('turn is skipped.'),
    display_text('- On each turn, a player must move to capture a checker immediately'),
    display_text('above, below, to the right, or to the left.'),
    display_text('- You can capture an enemy or friendly checker.'),
    display_text('- Groups are comprised of checkers interconnected horizontally or'),
    display_text('vertically, or both.'),
    display_text('- You can only make a move such that after your move there will only be'),
    display_text('one group containing your checkers'),   
    display_text('(it can also contain enemy checkers).'),    
    display_menu_empty_line,
    display_menu_subtitle('How to win',''),
    display_text('- You can win by detaching enemy-only groups.'),
    display_text('- The objective is to remove all enemy checkers from the board.'),
    display_menu_empty_line,
    display_menu_last_line,
    write('Press Enter to return to the main menu.'),
    skip_line,
    get_char(_),
    fail.