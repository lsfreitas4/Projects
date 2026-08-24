package com.project.Controller.Menus;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.State;
import com.project.Model.StateType;
import com.project.View.Menus.HelpView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class HelpController extends MenuController <HelpView> {

    public HelpController(Screen screen, State state) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(screen, state, new HelpView(screen));
    }

    @Override
    public boolean play() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke key = menuView.inputListener();
        return handleInput(key);
    }

    @Override
    public void processKey(KeyStroke key) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!key.getKeyType().equals(KeyType.Character)) return;

        //Back to main menu
        if (Character.toLowerCase(key.getCharacter()) == 'm') {
            state.setStateType(StateType.MENU);
            state.handleStateController();
        }
    }

    @Override
    public void renderView() throws IOException {
        screen.setCursorPosition(null);
        screen.startScreen();
        screen.doResizeIfNecessary();
        menuView.draw();
    }
}
