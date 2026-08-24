package com.project.Controller;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.State;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static com.project.Model.StateType.QUIT;

public abstract class Controller {
    protected Screen screen;
    protected State state;

    public Controller(Screen screen, State state) {
        this.screen = screen;
        this.state = state;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean handleInput(KeyStroke key) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Close screen and break out of loop if key is 'q'
        if (key.getKeyType().equals(KeyType.Character)  && Character.toLowerCase(key.getCharacter()) == 'q') {
            // Update state
            state.setStateType(QUIT);
            return false;
        }

        // Break out of loop if window is closed
        if (key.getKeyType().equals(KeyType.EOF)) {
            // Update state
            state.setStateType(QUIT);
            return false;
        }

        // Process the key
        this.processKey(key);

        return true;
    }

    public abstract boolean play() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    public abstract void processKey (KeyStroke key) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    public abstract void renderView() throws IOException;
}
