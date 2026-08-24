package com.project.Controller.Menus;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.State;
import com.project.Model.StateType;
import com.project.View.Menus.SelectLevelView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SelectLevelController extends MenuController <SelectLevelView> {

    public SelectLevelController(Screen screen, State state) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(screen, state, new SelectLevelView(screen));
        menuView.setMaps(state.getMaps());
    }

    @Override
    public boolean play() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke key = menuView.inputListener();
        return handleInput(key);
    }

    @Override
    public void processKey(KeyStroke key) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!key.getKeyType().equals(KeyType.Character)) return;

        switch (Character.toLowerCase(key.getCharacter())) {
            case 'm':
                // Back to main menu
                state.setStateType(StateType.MENU);
                state.handleStateController();
                break;
            case '1':
                // Play level 1
                state.setStateType(StateType.LEVEL);
                state.setActiveLevel(1);
                state.handleStateController();
                break;
            case '2':
                // Play level 2
                state.setStateType(StateType.LEVEL);
                state.setActiveLevel(2);
                state.handleStateController();
                break;
            default :
                break;
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
