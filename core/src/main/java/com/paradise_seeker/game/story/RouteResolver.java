package com.paradise_seeker.game.story;

public class RouteResolver {

    public RouteType resolveRoute(StoryStateManager storyState) {
        if (storyState.getFlag("tamper_detected")) {
            return RouteType.OBSERVER;
        }

        if (storyState.getFlag("true_condition_met")) {
            return RouteType.TRUE;
        }

        return RouteType.NORMAL;
    }
}