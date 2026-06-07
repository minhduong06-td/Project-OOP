package com.paradise_seeker.game.screen.cutscene;

import java.util.Arrays;
import java.util.List;

import com.paradise_seeker.game.main.Main;
import com.paradise_seeker.game.screen.WinScreen;
import com.paradise_seeker.game.story.RouteType;

public class EndGame extends CutScene {

    private final RouteType routeType;

    public EndGame(Main game) {
        this(game, RouteType.NORMAL);
    }

    public EndGame(Main game, RouteType routeType) {
        super(
                game,
                getBackgrounds(routeType),
                getTexts(routeType),
                5f
        );
        this.routeType = routeType;
    }

    private static List<String> getBackgrounds(RouteType routeType) {
        switch (routeType) {
            case TRUE:
                // Tạm thời dùng lại ảnh ending cũ để dựng khung.
                // Sau này nhóm bạn chỉ cần thay path ở đây.
                return Arrays.asList(
                        "cutscene/Ending/6.1.png",
                        "cutscene/Ending/6.2.png",
                        "cutscene/Ending/6.3.png",
                        "cutscene/Ending/6.4.png"
                );
            case OBSERVER:
                // Tạm thời dùng lại ảnh ending cũ để dựng khung.
                return Arrays.asList(
                        "cutscene/Ending/6.1.png",
                        "cutscene/Ending/6.2.png",
                        "cutscene/Ending/6.3.png",
                        "cutscene/Ending/6.4.png"
                );
            case NORMAL:
            default:
                return Arrays.asList(
                        "cutscene/Ending/6.1.png",
                        "cutscene/Ending/6.2.png",
                        "cutscene/Ending/6.3.png",
                        "cutscene/Ending/6.4.png"
                );
        }
    }

    private static List<String> getTexts(RouteType routeType) {
        switch (routeType) {
            case TRUE:
                return Arrays.asList(
                        "The truth behind Paradise is finally revealed...",
                        "What once looked like salvation was only an illusion.",
                        "You chose to face the truth instead of a beautiful lie.",
                        "TRUE ENDING"
                );
            case OBSERVER:
                return Arrays.asList(
                        "Something is wrong. This world has noticed your presence.",
                        "You were not meant to reach this layer of the system.",
                        "Paradise is no longer speaking to the hero alone.",
                        "OBSERVER ENDING"
                );
            case NORMAL:
            default:
                return Arrays.asList(
                        "After countless battles, the final enemy has fallen.",
                        "The path to Paradise is finally open before you.",
                        "Peace returns, and the long journey comes to an end.",
                        "NORMAL ENDING"
                );
        }
    }

    @Override
    protected void onCutsceneEnd() {
        game.setScreen(new WinScreen(game, routeType));
    }
}