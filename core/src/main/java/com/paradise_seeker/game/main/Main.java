package com.paradise_seeker.game.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.paradise_seeker.game.interaction.InteractionState;
import com.paradise_seeker.game.meta.MetaDetector;
import com.paradise_seeker.game.save.SaveManager;
import com.paradise_seeker.game.screen.ControlScreen;
import com.paradise_seeker.game.screen.GameScreen;
import com.paradise_seeker.game.screen.InventoryScreen;
import com.paradise_seeker.game.screen.MainMenuScreen;
import com.paradise_seeker.game.screen.SettingScreen;
import com.paradise_seeker.game.story.EndingResolver;
import com.paradise_seeker.game.story.RouteResolver;
import com.paradise_seeker.game.story.RouteType;
import com.paradise_seeker.game.story.StoryStateManager;

public class Main extends Game {

    public static final float WORLD_WIDTH = 16;
    public static final float WORLD_HEIGHT = 10;

    public SpriteBatch batch;
    public BitmapFont font;
    public OrthographicCamera camera;
    public FitViewport viewport;

    public float setVolume = 0.5f;

    // Screen cache cũ
    public GameScreen currentGame = null;
    public MainMenuScreen mainMenu = null;
    public SettingScreen settingMenu = null;
    public InventoryScreen inventoryScreen = null;
    public ControlScreen controlScreen = null;

    // Manager mới cho hệ 3 route
    public StoryStateManager storyState;
    public RouteResolver routeResolver;
    public EndingResolver endingResolver;
    public SaveManager saveManager;
    public MetaDetector metaDetector;
    public InteractionState interactionState;

    @Override
    public void create() {
        batch = new SpriteBatch();

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/MinecraftStandard.otf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 16;
        font = generator.generateFont(parameter);
        generator.dispose();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        font.setUseIntegerPositions(false);
        font.getData().setScale(WORLD_HEIGHT / Gdx.graphics.getHeight());

        // Khởi tạo state/manager mới
        storyState = new StoryStateManager();
        routeResolver = new RouteResolver();
        endingResolver = new EndingResolver();
        saveManager = new SaveManager();
        metaDetector = new MetaDetector();
        interactionState = new InteractionState();

        // Load save route cũ nếu có
        saveManager.loadStoryState(storyState);

        // Tạm thời nếu phát hiện can thiệp thì bật route Observer
        if (metaDetector.detectTamper()) {
            storyState.setFlag("tamper_detected", true);
            storyState.setCurrentRoute(routeResolver.resolveRoute(storyState));
        } else if (storyState.getCurrentRoute() == null) {
            storyState.setCurrentRoute(RouteType.NORMAL);
        }

        // Khởi tạo menu
        this.settingMenu = new SettingScreen(this);
        this.mainMenu = new MainMenuScreen(this);
        this.setScreen(mainMenu);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}