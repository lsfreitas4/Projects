package com.project.Controller.Menus;

import com.googlecode.lanterna.screen.Screen;
import com.project.Controller.Controller;
import com.project.Model.State;
import com.project.View.Menus.MenuView;

public abstract class MenuController<T extends MenuView> extends Controller {
    public T menuView;

    public MenuController(Screen screen, State state, T menuView) {
        super(screen, state);
        this.menuView = menuView;
    }

    public void setMenuView(T menuView) {
        this.menuView = menuView;
    }
}
