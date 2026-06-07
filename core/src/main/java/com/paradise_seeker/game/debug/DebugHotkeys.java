package com.paradise_seeker.game.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.paradise_seeker.game.entity.monster.Monster;
import com.paradise_seeker.game.main.Main;
import com.paradise_seeker.game.object.item.Fragment;
import com.paradise_seeker.game.screen.GameScreen;
import com.paradise_seeker.game.story.RouteType;

public class DebugHotkeys {
    public static final boolean ENABLED = true;

    public static void handle(GameScreen screen) {
        if (!ENABLED) return;

        Main game = (Main) Gdx.app.getApplicationListener();

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            screen.player.hp = screen.player.MAX_HP;
            screen.player.mp = screen.player.MAX_MP;
            screen.hud.showNotification("> HP/MP restored");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            for (Monster monster : screen.mapManager.getCurrentMap().getMonsters()) {
                monster.takeHit(999999);
            }
            screen.hud.showNotification("> All monsters defeated");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            if (screen.mapManager.getCurrentMap().getPortal() != null) {
                screen.player.getBounds().x = screen.mapManager.getCurrentMap().getPortal().getBounds().x;
                screen.player.getBounds().y = screen.mapManager.getCurrentMap().getPortal().getBounds().y;
                screen.hud.showNotification("> Teleported to portal");
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            screen.player.addItemToInventory(new Fragment(screen.player.getBounds().x, screen.player.getBounds().y, 1f, "items/fragment/frag1.png", 1));
            screen.player.addItemToInventory(new Fragment(screen.player.getBounds().x, screen.player.getBounds().y, 1f, "items/fragment/frag2.png", 2));
            screen.player.addItemToInventory(new Fragment(screen.player.getBounds().x, screen.player.getBounds().y, 1f, "items/fragment/frag3.png", 3));
            screen.hud.showNotification("> Key fragments granted");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            screen.mapManager.switchToNextMap();
            screen.switchMusicAndShowMap();
            screen.hud.showNotification("> Switched to next map");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            game.storyState.setCurrentRoute(RouteType.NORMAL);
            screen.hud.showNotification("> Route set to NORMAL");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
            game.storyState.setCurrentRoute(RouteType.TRUE);
            screen.hud.showNotification("> Route set to TRUE");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            game.storyState.setCurrentRoute(RouteType.OBSERVER);
            screen.hud.showNotification("> Route set to OBSERVER");
        }
    }
}