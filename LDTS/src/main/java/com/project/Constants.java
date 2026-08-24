package com.project;

import com.project.Model.Position;

import java.util.Arrays;
import java.util.List;

public class Constants {

    // Terminal size
    /////////////////
    public static final int TERMINAL_WIDTH = 28;
    public static final int TERMINAL_HEIGHT = 21;
    public static final int HEADER_HEIGHT = 4;


    // Game Colours
    ///////////////
    public static final String BACKGROUND_COLOUR = "#000001";
    public static final String PLAYER_COLOUR = "#FFC900";
    public static final String WALLS_COLOUR = "#861EDC";
    public static final String EXIT_COLOUR = "#C2AED7";
    public static final String SPIKES_COLOUR = "#74FAD8";
    public static final String COIN_COLOUR = "#FFC900";
    public static final String STAR_COLOUR = "#FFD700";
    public static final String HEADER_COLOUR = "#000001";
    public static final String OPENBTN_COLOUR = "#861EDC";

    // Game Symbols
    ////////////////////
    public static final String PLAYER_SYMBOL = "P";
    public static final String SPIKE_SYMBOL = "/";
    public static final String COIN_SYMBOL = ".";
    public static final String STAR_SYMBOL = "*";
    public static final String OPENBTN_SYMBOL = "O";


    // Map specifications
    /////////////////////
    public static final Position PLAYER_INIT_POSITION = new Position(1, TERMINAL_HEIGHT - 2);

    // Level 1 Inner Walls
    public static final List<String> MAP_LEVEL1 = Arrays.asList(
            "wwwwwwwwwwwwwwwwwwwwwwwwwwww",
            "w...w..............w.......w",
            "w. .w.          . .w.......-",
            "w. . .     wwwww. .www    .w",
            "w.....wwwwww...w...........w",
            "w.www.w........w...........w",
            "w.www.w...... ./. ..wwwwwwww",
            "w.www.w.www.ww./*...w......w",
            "w.   .w.www.w..wwwwww. /  .w",
            "w.   . .   .w...     . /  .w",
            "w..........*w......... /  .w",
            "wwwwwwwwwwwww ..wwwwwwww  .w",
            "w...........w www*.........w",
            "w.   /w/   .w / ...........w",
            "w.   /w/   .w// .ww.      .w",
            "w    /w/   ......ww........w",
            "wwwwwwwwwwwwwwwwwwwwwwwwwwww"
            );

    public static final List<String> MAP_LEVEL2 = Arrays.asList(
            "wwwwwwwwwwwwwwwwwwwwwwwwwwww",
            "w................w.....o...w",
            "w.    . wwwww.  .w.  *.ww .w",
            "w. www...o..w..........ww .w",
            "w. www  . ..//  ....w..// .w",
            "w. www  .w../wwww...w......w",
            "w........w......w...w...wwww",
            "w.......*w.. . .wwwwww.....w",
            "w.  .mmmmw.. .w......w.....w",
            "w.  .////w....wo..w .wmmm .w",
            "w.  .wm wwwwwwww../ .w/// .w",
            "w..o.w..w......w../ .w/   .w",
            "wwwwww..w..ww .w*./ .w/   .-",
            "w....w...m.ww .wmm/ .w/  mmw",
            "w........w.   .w/// .w/  //w",
            "w ..wo...w...........w/    w",
            "wwwwwwwwwwwwwwwwwwwwwwwwwwww"
    );
}

