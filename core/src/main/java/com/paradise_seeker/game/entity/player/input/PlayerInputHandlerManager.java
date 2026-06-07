package com.paradise_seeker.game.entity.player.input;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.paradise_seeker.game.collision.Collidable;
import com.paradise_seeker.game.entity.monster.Monster;
import com.paradise_seeker.game.entity.npc.gipsy.Gipsy;
import com.paradise_seeker.game.entity.player.Player;
import com.paradise_seeker.game.main.Main;
import com.paradise_seeker.game.map.GameMap;
import com.paradise_seeker.game.object.Book;
import com.paradise_seeker.game.object.Chest;
import com.paradise_seeker.game.object.item.Item;
import com.paradise_seeker.game.screen.ControlScreen;
import com.paradise_seeker.game.screen.GameScreen;

public class PlayerInputHandlerManager implements PlayerInputHandler {

    @Override
    public void handleInput(Player player, float deltaTime, GameMap gameMap) {
        if (player.statusManager.isDoNothing() || player.statusManager.isAttacking() || player.statusManager.isDead()) return;

        // Check for interaction opportunities and set showInteractMessage
        checkForInteractions(player, gameMap);
        handleMovement(player, deltaTime, gameMap);
        handleDash(player, gameMap);
        handleAttack(player, gameMap);
        handleSkills(player);
        handleNPCInteraction(player, gameMap);
    }

    public void checkForInteractions(Player player, GameMap gameMap) {
        if (gameMap == null) return;

        Main game = (Main) Gdx.app.getApplicationListener();
        game.interactionState.clear();

        for (Gipsy npc : gameMap.getNPCs()) {
            float distance = calculateDistance(player, npc);
            if (distance <= 2.5f) {
                game.interactionState.setShowInteractHint(true);
                game.interactionState.setInteractText("> Press F to talk");
                game.interactionState.setInteractionType("npc");
                return;
            }
        }

        Book book = gameMap.getBook();
        if (book != null && isNearBook(player, book, 1.6f) && !book.isOpened()) {
            game.interactionState.setShowInteractHint(true);
            game.interactionState.setInteractText("> Press F to read");
            game.interactionState.setInteractionType("book");
            return;
        }

        Chest chest = gameMap.getChest();
        if (chest != null && isNearChest(player, chest, 1.6f) && !chest.isOpened()) {
            game.interactionState.setShowInteractHint(true);
            game.interactionState.setInteractText("> Press F to open chest");
            game.interactionState.setInteractionType("chest");
        }
    }

    @Override
    public void handleMovement(Player player, float deltaTime, GameMap gameMap) {
        float dx = 0, dy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;

        float len = (float) Math.sqrt(dx * dx + dy * dy);
        player.statusManager.setMoving(len > 0);
        if (len > 0) {
            dx /= len;
            dy /= len;
            float moveX = dx * player.speed * deltaTime;
            float moveY = dy * player.speed * deltaTime;
            float nextX = player.getBounds().x + moveX;
            float nextY = player.getBounds().y + moveY;
            Rectangle nextBounds = new Rectangle(nextX, nextY, player.getBounds().width, player.getBounds().height);
            boolean blocked = false;
            if (gameMap != null && gameMap.collidables != null) {
                for (Collidable c : gameMap.collidables) {
                    if (c != player && nextBounds.overlaps(c.getBounds())) {
                        c.onCollision(player);

                        boolean shouldBlock = c.isSolid() && !(c instanceof Monster);
                        if (shouldBlock) {
                            player.blockMovement();
                            blocked = true;
                            break;
                        }
                    }
                }
            }
            if (!blocked) {
                player.getBounds().x = nextX;
                player.getBounds().y = nextY;
                // Cập nhật hướng di chuyển
                if (Math.abs(dx) > Math.abs(dy)) {
                    player.statusManager.setDirection(dx > 0 ? "right" : "left");
                } else {
                    player.statusManager.setDirection(dy > 0 ? "up" : "down");
                }
            } else {
                player.statusManager.setMoving(false);
            }
        }
        clampToMapBounds(player, gameMap);
    }
    @Override
    public void handleDash(Player player, GameMap gameMap) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && player.smokeManager.getDashTimer() <= 0 && player.statusManager.isMoving()) {
            float dx = 0, dy = 0;

            // Xác định hướng dash
            if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;

            float len = (float) Math.sqrt(dx * dx + dy * dy);

            if (len > 0) {
                float stepSize = 0.1f;
                float totalDash = 0f;
                float maxDash = player.smokeManager.getDashDistance();
                float prevX = player.getBounds().x;
                float prevY = player.getBounds().y;

                // Try to move in increments until hit something or finished full dash
                while (totalDash < maxDash) {
                    float nextX = player.getBounds().x + (dx / len) * stepSize;
                    float nextY = player.getBounds().y + (dy / len) * stepSize;
                    Rectangle nextBounds = new Rectangle(nextX, nextY, player.getBounds().width, player.getBounds().height);

                    if (gameMap == null || gameMap.collisionSystem == null || !gameMap.collisionSystem.isBlocked(nextBounds)) {
                        player.getBounds().x = nextX;
                        player.getBounds().y = nextY;
                        totalDash += stepSize;
                    } else {
                        break;
                    }
                }
                player.smokeManager.setDashTimer(player.smokeManager.getDashCooldown());
                player.smokeManager.addSmoke(prevX, prevY);
            }
            clampToMapBounds(player, gameMap);
        }
    }

    @Override
    public void handleAttack(Player player, GameMap gameMap) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!player.statusManager.isAttacking() && !player.statusManager.isHit() && !player.statusManager.isDead()) {
                player.statusManager.setAttacking(true);
                player.statusManager.setStateTime(0f); // Reset lại thời gian để animation luôn chạy từ frame đầu
                if (gameMap != null) {
                    float centerX = player.getBounds().x + player.getBounds().width / 2;
                    float centerY = player.getBounds().y + player.getBounds().height / 2;
                    damageMonstersInRange(centerX, centerY, 3f, player.getAtk(), gameMap);
                }
            }
        }
    }
    public void damageMonstersInRange(float x, float y, float radius, float damage, GameMap gameMap) {
        for (Monster m : gameMap.getMonsters()) {
            if (!m.statusManager.isDead() && isInRange(x, y, m.getBounds(), radius)) m.takeHit(damage);
        }
    }

    public boolean isInRange(float x, float y, Rectangle bounds, float radius) {
        float centerX = bounds.x + bounds.width / 2;
        float centerY = bounds.y + bounds.height / 2;
        float dx = centerX - x;
        float dy = centerY - y;
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    public void handleSkills(Player player) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            if (player.getMp() >= 2 && player.getPlayerSkill1().canUse(System.currentTimeMillis())) {
                player.setMp(player.getMp() - 2);
                player.getPlayerSkill1().castSkill(player.getAtk(), player.getBounds(), player.statusManager.getDirection());
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            if (player.getMp() >= 2 && player.getPlayerSkill2().canUse(System.currentTimeMillis())) {
                player.setMp(player.getMp() - 2);
                player.getPlayerSkill2().castSkill(player.getAtk(), player.getBounds(), player.statusManager.getDirection());
            }
        }
    }

    @Override
    public void handleNPCInteraction(Player player, GameMap gameMap) {
        if (gameMap == null || !Gdx.input.isKeyJustPressed(Input.Keys.F)) return;

        Gipsy nearestNPC = null;
        float minDistance = Float.MAX_VALUE;

        for (Gipsy npc : gameMap.getNPCs()) {
            float distance = calculateDistance(player, npc);
            if (distance <= 2.5f && distance < minDistance) {
                minDistance = distance;
                nearestNPC = npc;
            }
        }

        if (nearestNPC != null) {
            // Đã nói chuyện và mở rương rồi thì không cho farm lại
            if (nearestNPC.stateManager.hasTalked() || nearestNPC.stateManager.isChestOpened()) {
                return;
            }

            nearestNPC.resetDialogue();
            nearestNPC.setTalking(true);
            nearestNPC.stateManager.showDialogueOptions = true;
        }
    }

    public void handleDialogue(GameScreen gameScreen, Player player) {
        if (gameScreen.currentTalkingNPC == null) return;
        if (!gameScreen.currentTalkingNPC.stateManager.showDialogueOptions) return;
        if (!Gdx.input.isKeyJustPressed(Input.Keys.F)) return;

        Gipsy npc = gameScreen.currentTalkingNPC;

        // Nhấn F lần đầu: hiện dòng đầu
        if (!gameScreen.dialogueBox.isVisible()) {
            npc.setTalking(true);
            gameScreen.dialogueBox.show(npc.getCurrentLine());
            return;
        }

        // Còn dòng tiếp theo thì sang dòng tiếp
        if (npc.hasNextLine()) {
            npc.nextLine();
            gameScreen.dialogueBox.show(npc.getCurrentLine());
            return;
        }

        // Hết thoại
        gameScreen.dialogueBox.hide();
        npc.setTalking(false);
        npc.stateManager.showDialogueOptions = false;
        npc.stateManager.setHasTalked(true);

        // Chọn potion một lần duy nhất
        Random random = new Random();
        int randomIndex = random.nextInt(3);
        npc.stateManager.pendingPotionToDrop = npc.stateManager.options[randomIndex];

        // Mở rương NPC sau khi hội thoại kết thúc
        if (!npc.stateManager.isChestOpened() && !npc.stateManager.isOpeningChest()) {
            npc.openChest();
        }
    }

    public void finishNpcInteraction(GameScreen gameScreen, Player player) {
        if (gameScreen.currentTalkingNPC == null) return;

        if (gameScreen.currentTalkingNPC.stateManager.pendingPotionToDrop != null) {
            gameScreen.currentTalkingNPC.dropPotionNextToPlayer(
                gameScreen.mapManager,
                gameScreen.currentTalkingNPC.stateManager.pendingPotionToDrop,
                player
            );
            gameScreen.currentTalkingNPC.stateManager.pendingPotionToDrop = null;
        }

        gameScreen.currentTalkingNPC.setTalking(false);
        gameScreen.currentTalkingNPC.stateManager.showDialogueOptions = false;
        gameScreen.currentTalkingNPC.stateManager.selectedOptionIndex = 0;

        // Quan trọng: giữ trạng thái đã mở, không reset về false nữa
        gameScreen.currentTalkingNPC.stateManager.setHasTalked(true);
        gameScreen.currentTalkingNPC.stateManager.setChestOpened(true);

        gameScreen.dialogueBox.hide();
    }

	@Override
    public void handleZoomInput(GameScreen gameScreen) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) gameScreen.zoom = Math.min(3.0f, gameScreen.zoom + 0.1f);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) || Gdx.input.isKeyJustPressed(Input.Keys.PLUS))
        	gameScreen.zoom = Math.max(0.5f, gameScreen.zoom - 0.1f);
    }

	@Override
    public void handleChest(GameScreen gameScreen, Player player) {
        Chest chest = gameScreen.mapManager.getCurrentMap().getChest();

        if (chest != null && isNearChest(player, chest, 1.6f)) {
            if (!chest.isOpened()) {
                gameScreen.hud.showNotification("[F] Open Chest?");
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                if (!chest.isOpened()) {
                    chest.onPlayerCollision(player);
                    Array<Item> items = chest.getItems();

                    StringBuilder itemListMessage = new StringBuilder("You received:\n");
                    for (Item item : items) {
                        itemListMessage.append("- ").append(item.getName()).append("\n");
                    }
                    gameScreen.hud.showNotification(itemListMessage.toString());
                }
            }
        }
    }

	@Override
    public void handleBook(GameScreen gameScreen, Player player) {
        Book book = gameScreen.mapManager.getCurrentMap().getBook();
        Main game = (Main) Gdx.app.getApplicationListener();

        if (book != null && isNearBook(player, book, 1.6f)) {
            gameScreen.hud.showNotification("[F] Read Book");

            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                if (!book.isOpened()) {
                    book.onCollision(player);
                    if (game.controlScreen == null) {
                        game.controlScreen = new ControlScreen(game);
                    }
                    game.setScreen(game.controlScreen);
                }
            }
        }
    }

    public float calculateDistance(Player player, Gipsy npc) {
        return (float) Math.sqrt(
            Math.pow(player.getBounds().x + player.getBounds().width/2 - (npc.getBounds().x + npc.getBounds().width/2), 2) +
            Math.pow(player.getBounds().y + player.getBounds().height/2 - (npc.getBounds().y + npc.getBounds().height/2), 2)
        );
    }

    public void clampToMapBounds(Player player, GameMap gameMap) {
        if (gameMap == null) return;

        float minX = 0;
        float minY = 0;
        float maxX = gameMap.getMapWidth() - player.getBounds().width;
        float maxY = gameMap.getMapHeight() - player.getBounds().height;

        player.getBounds().x = Math.max(minX, Math.min(player.getBounds().x, maxX));
        player.getBounds().y = Math.max(minY, Math.min(player.getBounds().y, maxY));
    }

        private boolean isNearRect(Player player, Rectangle rect, float range) {
        float playerCenterX = player.getBounds().x + player.getBounds().width / 2f;
        float playerCenterY = player.getBounds().y + player.getBounds().height / 2f;
        float rectCenterX = rect.x + rect.width / 2f;
        float rectCenterY = rect.y + rect.height / 2f;

        float dx = playerCenterX - rectCenterX;
        float dy = playerCenterY - rectCenterY;
        return dx * dx + dy * dy <= range * range;
    }

    private boolean isNearBook(Player player, Book book, float range) {
        return book != null && isNearRect(player, book.getBounds(), range);
    }

    private boolean isNearChest(Player player, Chest chest, float range) {
        return chest != null && isNearRect(player, chest.getBounds(), range);
    }
}
