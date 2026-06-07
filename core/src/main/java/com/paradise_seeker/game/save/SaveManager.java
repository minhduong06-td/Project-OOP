package com.paradise_seeker.game.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.paradise_seeker.game.main.Main;
import com.paradise_seeker.game.screen.GameScreen;
import com.paradise_seeker.game.story.RouteType;
import com.paradise_seeker.game.story.StoryStateManager;

public class SaveManager {
    private final Preferences prefs = Gdx.app.getPreferences("paradise_seeker_checkpoint");

    public void saveStoryState(StoryStateManager storyState) {
        prefs.putString("route", storyState.getCurrentRoute().name());
        prefs.flush();
    }

    public void loadStoryState(StoryStateManager storyState) {
        String route = prefs.getString("route", "NORMAL");
        storyState.setCurrentRoute(RouteType.valueOf(route));
    }

    public void saveCheckpoint(GameScreen screen, Main game) {
        prefs.putInteger("mapIndex", screen.mapManager.getCurrentMapIndex());
        prefs.putFloat("playerX", screen.player.getBounds().x);
        prefs.putFloat("playerY", screen.player.getBounds().y);
        prefs.putFloat("hp", screen.player.hp);
        prefs.putFloat("mp", screen.player.mp);
        prefs.putString("route", game.storyState.getCurrentRoute().name());
        prefs.flush();
    }

    public boolean hasCheckpoint() {
        return prefs.contains("mapIndex");
    }

    public void loadCheckpoint(GameScreen screen, Main game) {
        if (!hasCheckpoint()) return;

        int mapIndex = prefs.getInteger("mapIndex", 0);
        float playerX = prefs.getFloat("playerX", 0f);
        float playerY = prefs.getFloat("playerY", 0f);
        float hp = prefs.getFloat("hp", screen.player.MAX_HP);
        float mp = prefs.getFloat("mp", screen.player.MAX_MP);
        String route = prefs.getString("route", "NORMAL");

        screen.mapManager.switchToMap(mapIndex);
        screen.player.getBounds().x = playerX;
        screen.player.getBounds().y = playerY;
        screen.player.hp = hp;
        screen.player.mp = mp;

        game.storyState.setCurrentRoute(RouteType.valueOf(route));
    }

    public void clearCheckpoint() {
        prefs.remove("mapIndex");
        prefs.remove("playerX");
        prefs.remove("playerY");
        prefs.remove("hp");
        prefs.remove("mp");
        prefs.remove("route");
        prefs.flush();
    }
}
