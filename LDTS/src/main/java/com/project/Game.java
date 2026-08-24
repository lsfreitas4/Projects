package com.project;

import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.project.Controller.Controller;
import com.project.Model.*;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;

import static com.project.Model.StateType.*;

public class Game {
    private final Terminal terminal;
    private final State state;

    private static Game gameInstance = null;

    private Game() throws IOException, URISyntaxException, FontFormatException {
        AWTTerminalFontConfiguration fontConfig = loadSquareFont();

        TerminalSize terminalSize = new TerminalSize(Constants.TERMINAL_WIDTH, Constants.TERMINAL_HEIGHT);
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory().setInitialTerminalSize(terminalSize);
        terminalFactory.setForceAWTOverSwing(true);
        terminalFactory.setTerminalEmulatorFontConfiguration(fontConfig);

        terminal = terminalFactory.createTerminal();
        Screen screen = new TerminalScreen(terminal);
        state = new State(screen);
    }

    public static Game getGameInstance() throws IOException, URISyntaxException, FontFormatException {
        if (gameInstance == null) {
            gameInstance = new Game();
        }
        return gameInstance;
    }

    public void run() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        state.setStateType(MENU);
        state.handleStateController();
        Controller controller = state.getController();

        while (!state.getStateType().equals(QUIT)) {
            if (!state.getController().equals(controller)) controller = state.getController();
            controller.play();
        }

        terminal.close();
    }

    private AWTTerminalFontConfiguration loadSquareFont() throws URISyntaxException, FontFormatException, IOException {
        URL resource = getClass().getClassLoader().getResource("font/squareFont.ttf");
        if (resource == null) throw new IOException("Font file not found: Font/squareFont.ttf");
        File fontFile = new File(resource.toURI());
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);

        Font loadedFont = font.deriveFont(Font.PLAIN, 25);
        return AWTTerminalFontConfiguration.newInstance(loadedFont);
    }

}
