package com.paradise_seeker.game.story;

public class EndingResolver {

    public String resolveEndingId(RouteType routeType) {
        switch (routeType) {
            case NORMAL:
                return "ending_normal";
            case TRUE:
                return "ending_true";
            case OBSERVER:
                return "ending_observer";
            default:
                return "ending_normal";
        }
    }
}