package com.paradise_seeker.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.paradise_seeker.game.entity.monster.Monster;
import com.paradise_seeker.game.entity.monster.boss.ParadiseKing;
import com.paradise_seeker.game.entity.npc.gipsy.Gipsy;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.paradise_seeker.game.entity.player.Player;
import com.paradise_seeker.game.ui.DialogueBox;
import com.paradise_seeker.game.ui.HUD;
import com.paradise_seeker.game.main.Main;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.paradise_seeker.game.map.GameMap;
import com.paradise_seeker.game.map.GameMapManager;
import com.paradise_seeker.game.object.*;
import com.paradise_seeker.game.object.item.Item;
import com.paradise_seeker.game.screen.cutscene.EndGame;
import com.paradise_seeker.game.screen.cutscene.EndMap1;
import com.paradise_seeker.game.screen.cutscene.EndMap2;
import com.paradise_seeker.game.screen.cutscene.EndMap3;
import com.paradise_seeker.game.screen.cutscene.EndMap4;
import com.paradise_seeker.game.story.RouteType;
import com.paradise_seeker.game.debug.DebugHotkeys;

public class GameScreen implements Screen {
    private void handleRouteDebugInput() {
        Main game = (Main) Gdx.app.getApplicationListener();

        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            game.storyState.setCurrentRoute(RouteType.NORMAL);
            hud.showNotification("> Route set to NORMAL");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
            game.storyState.setCurrentRoute(RouteType.TRUE);
            hud.showNotification("> Route set to TRUE");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            game.storyState.setCurrentRoute(RouteType.OBSERVER);
            hud.showNotification("> Route set to OBSERVER");
        }
    }

    private final float CAMERA_VIEW_WIDTH = 16f;
    private final float CAMERA_VIEW_HEIGHT = 10f;
    public SpriteBatch batch;
    public BitmapFont font;
    public OrthographicCamera camera;
    public FitViewport viewport;

    public float cameraLerp = 0.1f;// Controls how fast the camera follows the player

    public Music music;
    public Player player;
    public GameMapManager mapManager;// Manages the current map and transitions
    public HUD hud;// Heads-Up Display for player stats, inventory, etc.
    public DialogueBox dialogueBox;
    public Texture dialogueBg;
    public Gipsy currentTalkingNPC = new Gipsy(); // Current NPC the player is interacting with
    public OrthographicCamera gameCamera;// Camera for the game world
    public OrthographicCamera hudCamera;// Camera for the HUD elements
    public ShapeRenderer shapeRenderer;
    public boolean winTriggered = false;
    public float zoom = 1.0f;

    public int[] mapcutsceneIndicesEnd = {0, 0, 0, 0, 0}; // Indices for cutscenes in the map

    public GameScreen(final Main game) {
        this.batch = game.batch; // Use the shared batch
        this.font = game.font; // Use the shared font
        this.camera = game.camera; // Use the shared camera
        this.viewport = game.viewport; // Use the shared viewport
        // Create player, initial position will come from Tiled data by mapManager
		player = new Player();
		currentTalkingNPC = new Gipsy(); // Initialize with a default NPC
        this.mapManager = new GameMapManager(player);
        this.hud = new HUD(player, game.font, mapManager.getCurrentMap());
        this.shapeRenderer = new ShapeRenderer();

        // Reset font color to white when starting new game
        game.font.setColor(Color.WHITE);

        dialogueBg = new Texture(Gdx.files.internal("ui/dialog/dlg_box_bg/dialogboxc.png"));
        float boxHeight = 180f;
        float dialogX = 0;
        float dialogY = 0f;
        float dialogWidth = Gdx.graphics.getWidth();
        float dialogHeight = boxHeight;

        dialogueBox = new DialogueBox(
            "",
            dialogueBg,
            game.font,
            dialogX,
            dialogY,
            dialogWidth,
            dialogHeight
        );

        this.gameCamera = new OrthographicCamera(CAMERA_VIEW_WIDTH, CAMERA_VIEW_HEIGHT);
        // Initial camera will be positioned on first render/update
        this.hudCamera = new OrthographicCamera();
        this.hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void setmusic() {
    	Main game = (Main) Gdx.app.getApplicationListener();
    	String musicPath = mapManager.getCurrentMapMusic();
    	music = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
    	music.setLooping(true);
    	music.setVolume(game.setVolume); // Set volume from Main game settings);
    	music.play();
    	hud.showMapNotification(mapManager.getCurrentMap().getMapName());
	}


    private Gipsy findNearestNPC() {
        Gipsy nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Gipsy npc : mapManager.getCurrentMap().getNPCs()) {
            float dx = player.getBounds().x + player.getBounds().width / 2f
                    - (npc.getBounds().x + npc.getBounds().width / 2f);
            float dy = player.getBounds().y + player.getBounds().height / 2f
                    - (npc.getBounds().y + npc.getBounds().height / 2f);
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= 2.5f && distance < minDistance) {
                minDistance = distance;
                nearest = npc;
            }
        }

        return nearest;
    }

    private boolean hasAliveMonsters() {
        for (Monster monster : mapManager.getCurrentMap().getMonsters()) {
            if (!monster.statusManager.isDead()) {
                return true;
            }
        }
        return false;
    }

    private boolean isFinalBossDead() {
        for (Monster monster : mapManager.getCurrentMap().getMonsters()) {
            if (monster instanceof ParadiseKing) {
                return monster.statusManager.isDead();
            }
        }
        return false;
    }

    @Override
    public void show() {
        if (music != null) music.stop();
        setmusic();
    }

    @Override
    public void render(float delta) {
        // Dialogue logic (unchanged)
    	Main game = (Main) Gdx.app.getApplicationListener();
        DebugHotkeys.handle(this);
		handleRouteDebugInput();
    	player.inputHandler.checkForInteractions(player, this.mapManager.getCurrentMap());
  //  	System.out.println("Current Map: " + mapManager.getCu   rrentMap().getMapName() + "\n NPCInteraction: " + player.inputHandler.showDialogueOptions);
        Gipsy nearestNPC = findNearestNPC();
        if (nearestNPC != null) {
            currentTalkingNPC = nearestNPC;
        }
        player.inputHandler.handleDialogue(this, player);


		// Handle NPC interaction
    	if (currentTalkingNPC != null && currentTalkingNPC.isChestOpenAndFinished()) {
            player.inputHandler.finishNpcInteraction(this, player);
        }
        // Zoom logic
        player.inputHandler.handleZoomInput(this);

        // Game update logic (outside dialogue)
        if (!dialogueBox.isVisible() && !this.currentTalkingNPC.stateManager.showDialogueOptions && !this.currentTalkingNPC.stateManager.isOpeningChest()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                game.setScreen(new PauseScreen(game));
                music.pause();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                if (game.inventoryScreen == null) game.inventoryScreen = new InventoryScreen(game, player);
                game.setScreen(game.inventoryScreen);
                music.pause();
            }
            if (player.hp == 0) {
                game.setScreen(new DeadScreen(game));
                music.stop();
                game.currentGame = null;
            }

            player.act(delta,mapManager.getCurrentMap());
            mapManager.update(delta);

            Chest chest = mapManager.getCurrentMap().getChest();
            if (chest != null) {
            	player.inputHandler.handleChest(this, player);
			}

            player.inputHandler.handleBook(this, player);

            mapManager.getCurrentMap().collisionSystem.checkCollisions(player, hud);

            player.playerSkill2.updatePosition(player);
            player.playerSkill2.updateSkill(delta, mapManager.getCurrentMap().getMonsters());
            player.playerSkill1.updateSkill(delta, mapManager.getCurrentMap().getMonsters());

        } else {
            mapManager.update(delta);
        }

        if (currentTalkingNPC != null && currentTalkingNPC.isChestOpenAndFinished() && currentTalkingNPC.stateManager.pendingPotionToDrop != null) {
            player.inputHandler.finishNpcInteraction(this, player);
        }

        // Camera follows player
        Vector2 playerCenter = new Vector2(player.getBounds().x + player.getBounds().width / 2, player.getBounds().y + player.getBounds().height / 2);
        Vector2 currentCameraPos = new Vector2(gameCamera.position.x, gameCamera.position.y);
        Vector2 newCameraPos = currentCameraPos.lerp(playerCenter, cameraLerp);
        gameCamera.position.set(newCameraPos.x, newCameraPos.y, 0);
        gameCamera.viewportWidth = CAMERA_VIEW_WIDTH * zoom;
        gameCamera.viewportHeight = CAMERA_VIEW_HEIGHT * zoom;
        gameCamera.update();

        // Clear the screen with a black color
        ScreenUtils.clear(Color.BLACK);
        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();

        // Render monsters, etc. from GameMap
        mapManager.render(game.batch); // gọi đến currentMap.render(batch);

        // Render player and skills (independent from map)
        player.playerRenderer.render(player, game.batch); // player không dùng interface Renderable, khác Skill
        player.playerSkill1.getSkill1render().render(game.batch, player.playerSkill1); // gọi tới Renderable interface
        player.playerSkill2.getSkill2render().render(game.batch, player.playerSkill2); // gọi tới Renderable interface
        game.batch.end();

        // Render dialogue box
        hudCamera.update();
        game.batch.setProjectionMatrix(hudCamera.combined);
        game.batch.begin();
        float baseHeight = 720f;
        float fontScale = Math.max(Gdx.graphics.getHeight() / baseHeight, 0.05f);
        dialogueBox.render(game.batch, fontScale);
        game.batch.end();

        // Render HUD
        hud.shapeRenderer.setProjectionMatrix(hudCamera.combined);
        hud.spriteBatch.setProjectionMatrix(hudCamera.combined);
        //hud.gameMap = mapManager.getCurrentMap();
        hud.render(hudCamera.viewportHeight);

        // --- PORTAL & MAP SWITCH ---
        handlePortalsEvent();
    }

    /** All portal/map transition logic, always uses Tiled player spawn! */
    public void handlePortalsEvent() {
        GameMap currentMap = mapManager.getCurrentMap();
        Main game = (Main) Gdx.app.getApplicationListener();
        if (currentMap.portal != null && player.getBounds().overlaps(currentMap.portal.getBounds())) {
            currentMap.portal.onCollision(player);

            int currentIndex = mapManager.getCurrentMapIndex();

            // Map cuối: phải giết boss mới được qua
            if (currentIndex == 4 && !isFinalBossDead()) {
                player.blockMovement();
                hud.showNotification("> Defeat the boss first!");
                return;
            }

            // Các map thường: phải dọn quái mới được qua cổng
            if (currentIndex != 4 && hasAliveMonsters()) {
                player.blockMovement();
                hud.showNotification("> Defeat all enemies first!");
                return;
            }

            switch (mapManager.getCurrentMapIndex()) {
				case 0:
                        // Map 1 -> Map 2: giữ nguyên currentGame để không reset state/checkpoint.
                        if (mapcutsceneIndicesEnd[0] == 0) {
                            mapcutsceneIndicesEnd[0] = 1;
                            game.setScreen(new EndMap1(game));
                        }
                    break;
				case 1:
                        // Map 2 -> Map 3: chuyển liền mạch như các map sau, không load checkpoint ngay khi chạm portal.
                        if (mapcutsceneIndicesEnd[1] == 0) {
                            mapcutsceneIndicesEnd[1] = 1;
                            game.setScreen(new EndMap2(game));
                        }
                    break;
				case 2: // Map 3 to Map 4
					if (mapcutsceneIndicesEnd[2] == 0) {
						mapcutsceneIndicesEnd[2] = 1; // Set cutscene index for Map 3 to Map 4
					    game.setScreen(new EndMap3(game));
					}
					break;
				case 3: // Map 4 to Map 5
					break;
				case 4: // Map 5 to Map 1 (loop back)
					// Kiểm tra boss ParadiseKing đã chết chưa và chuyển sang màn hình chiến thắng
		            for (Monster monster : mapManager.getCurrentMap().getMonsters()) {
		            	if (monster instanceof ParadiseKing && monster.statusManager.isDead() && !winTriggered){
                            winTriggered = true;
                            Gdx.app.postRunnable(() -> {
                                music.stop();
                                if (mapcutsceneIndicesEnd[4] == 0) {
                                    mapcutsceneIndicesEnd[4] = 1;
                                    RouteType routeType = game.storyState.getCurrentRoute();
                                    game.setScreen(new EndGame(game, routeType));
                                }
                            });
                        }
		            }
					break;
			}

            if (mapManager.getCurrentMapIndex() == 4) {
                return;
            }

            if (mapManager.getCurrentMapIndex() != 3 && mapManager.getCurrentMapIndex() != 4) {

            	mapManager.switchToNextMap();
                switchMusicAndShowMap();
                game.saveManager.saveCheckpoint(this, game);
                hud.showNotification("> Game Saved");
			} else {
				Item key = null;
				boolean hasKey = false;
	        	for (Item item : player.getInventory()) {
					if (item.getName().equals("Fragment of the Lost Treasure")) {
						hasKey = true;
						key = item;
						break;
					}
				}
	        	if (hasKey) {
	                // chuyển sang map 5
	        		if (mapcutsceneIndicesEnd[3] == 0) {
						mapcutsceneIndicesEnd[3] = 1; // Set cutscene index for Map 4 to Map 5
					    game.setScreen(new EndMap4(game)); // Reuse EndMap1 for Map 4 to Map 5 transition
					}
	        		if (mapManager.getCurrentMapIndex() == 3) {
	        			player.inventoryManager.removeItem(key);
		                mapManager.switchToNextMap();
		                }

	            } else {
                    player.blockMovement();
	                hud.showNotification("> You need the Key to enter!");
                    return;
	            }
			}

        }
        if (currentMap.getStartPortal() != null && player.getBounds().overlaps(currentMap.getStartPortal().getBounds())) {
            currentMap.getStartPortal().onCollision(player);
            mapManager.switchToPreviousMap();
            switchMusicAndShowMap();
            game.saveManager.saveCheckpoint(this, game);
            hud.showNotification("> Game Saved");
        }
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (music != null) music.dispose();
        hud.dispose();
        dialogueBg.dispose();
    }

    public void switchMusicAndShowMap() {
        if (music != null) {
            music.stop();
            music.dispose();
        }
        setmusic();
    }
}
