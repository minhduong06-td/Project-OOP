package com.paradise_seeker.game.story;

import java.util.HashMap;
import java.util.Map;

public class StoryStateManager {
    private RouteType currentRoute = RouteType.NORMAL;
    private final Map<String, Boolean> flags = new HashMap<>();

    public RouteType getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(RouteType currentRoute) {
        this.currentRoute = currentRoute;
    }

    public void setFlag(String key, boolean value) {
        flags.put(key, value);
    }

    public boolean getFlag(String key) {
        return flags.getOrDefault(key, false);
    }

    public void resetFlags() {
        flags.clear();
        currentRoute = RouteType.NORMAL;
    }
}