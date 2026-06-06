# Tài liệu mô tả ban đầu (Project Specification)

Tài liệu này là bản mô tả ban đầu cho dự án — giả định chưa có code hay assets. Mục tiêu: cung cấp cốt truyện (hero tiêu diệt nhà vua độc ác), phạm vi tính năng, và khung class/object để bắt đầu triển khai.

1) Tóm tắt ý tưởng (elevator pitch)
- Một game hành động/phiêu lưu 2D top-down. Người chơi thu thập mảnh, chiến đấu quái, tương tác với NPC và quyết định route dựa trên hành động/flags.

1) Tóm tắt ý tưởng (elevator pitch)
- Một game hành động/phiêu lưu 2D top-down: người chơi vào vai một hero vượt ải, di chuyển qua nhiều chương, tiêu diệt tay chân và cuối cùng thâm nhập phá hoại lâu đài để đối đầu và tiêu diệt nhà vua độc ác.

2) Cốt truyện sơ bộ

2) Cốt truyện sơ bộ (hero -> defeat evil king)
- Prologue: Hero, một chiến binh lưu lạc, nhận được lời kêu cứu từ một làng bị đàn áp.
- Chương 1 (Làng bị đàn áp): Hero giải cứu dân làng, phát hiện manh mối về tay sai của nhà vua.
- Chương 2 (Rừng/Đồn lính): Hero xuyên qua lãnh địa lính canh, tiêu diệt lính canh và thu thập tài liệu bí mật.
- Chương 3 (Mỏ/Bến tàu): Hero phá hủy nguồn cung ứng của quân đội nhà vua, đối đầu elite.
- Chương 4 (Đường vào lâu đài): Hero vượt qua cạm bẫy, giải các câu đố nhỏ và đánh bại tướng lĩnh.
- Chương 5 (Lâu đài): Thâm nhập lâu đài, chiến đấu với quân cận vệ, đối đầu nhà vua độc ác.
- Epilogue / Endings:
  - Hero hạ nhà vua, thiết lập hòa bình tạm thời.

3) MVP (tối thiểu cần làm để có bản chơi được)
- Playable map 1 với:
  - Di chuyển nhân vật, tấn công cơ bản.
  - 1 loại quái và combat đơn giản.
  - 1 fragment để thu thập.
  - 1 portal (sang map kế hoặc kết thúc thử).
  - UI cơ bản: HP/MP, inventory slot, dialogue box.
  - StoryState: lưu route và flags; EndingResolver chuyển sang `ending_normal`.

  Ghi chú: cho phiên bản ban đầu chỉ cần thực hiện flow từ Chương 1 đến Chương 3 rồi vào thử Lâu đài để test combat/ending.

4) Core features (phải có)
- Player: di chuyển, tấn công, HP/MP, inventory (fragments).
- Enemy: spawn, di chuyển, nhận sát thương, rơi item.
- Maps: tile-based maps, portal, collidable objects.
- UI: HUD (HP/MP), DialogueBox (wrap text), inventory màn hình.
- Story system: StoryState, RouteResolver, EndingResolver, Save/Load.
- Cutscenes: hỗ trợ hiển thị chuỗi ảnh (chapter frames) cho prologue/ending.

5) High-level class / object design (gợi ý khung)

- Game (App)
  - Tạo window, vòng lặp chính, quản lý scene/screens.

- Screen / Scene
  - `MainMenuScreen`, `GameScreen`, `InventoryScreen`, `SettingScreen` — quản lý input, update, render cho từng chế độ.

- World / Map
  - `GameMap` (tilemap, objects, portals)
  - `MapObject` (vị trí, collidable?)

- Entity
  - `Entity` (base): position, velocity, HP, update(), render(), receiveDamage()
  - `Player` extends `Entity`: input handling, skills, inventory.
  - `Monster` extends `Entity`: AI state, drop table.

  - `Boss` extends `Monster`: multi-phase behavior, special attacks (ví dụ: `King` class sẽ là Boss cuối cùng)

- Inventory & Items
  - `Inventory` interface
  - `PlayerInventory` implements Inventory: add/remove items, fragments count.
  - `Item` class (type, id, sprite)

- Combat & Skills
  - `Skill` interface, `PlayerSkill` implementations (Skill1, Skill2)
  - `SkillRenderer`, `SkillAnimationManager` để xử lý hiệu ứng.

- UI
  - `HUD`: draw HP/MP, icons, hints (reads from InteractionState)
  - `DialogueBox`: show/hide text, wrap logic

- Story
  - `StoryState`: currentRoute, flags map
  - `RouteResolver`: quyết định route từ flags
  - `EndingResolver`: map route -> ending id

Additional story elements specific to plot:
- `Quest` / `Objective` class: biểu diễn nhiệm vụ nhỏ (giải cứu NPC, phá nguồn cung), kèm flag hoàn thành.
- `NPC` class: NPC tương tác (give quest, dialogue) — có `isQuestGiver` flag.

- Save/Load
  - `SaveManager`: save story state, player checkpoint (map, pos, HP/MP), settings

6) Data model / flags (gợi ý)
- `flags` (key:boolean): examples: `tamper_detected`, `true_condition_met`, `boss_defeated_chapter3`, `fragment_all_collected`.

Gợi ý flags cho cốt truyện mới:
- `king_guard_defeated` (boolean)
- `village_rescued` (boolean)
- `supply_disrupted` (boolean)
- `final_boss_exposed` (boolean)

7) Assets (thô)
- Sprites: player, monster, tileset, UI icons.
- Cutscene frames (chapter 1..5, ending) — có thể dùng placeholder PNG khi chưa có artwork.
- Sounds: music per chapter, SFX attack/hit/pickup.

8) Folder layout (gợi ý)
- `src/` — code
  - `main/` — entry
  - `game/` — gameplay packages: entity, map, ui, story, save, interaction
- `assets/` — images/audio/cutscene
- `docs/` — spec, design, assets list

9) Lộ trình phát triển (milestones)
- Milestone 1 (Week 0.5): scaffolding project, game window, input, camera, tileset render.
- Milestone 2 (Week 1): Player entity, movement, basic enemy, collision.
- Milestone 3 (Week 2): Inventory, fragment pickup, portal transition, 1 chapter flow.
- Milestone 4 (Week 3): Story flags, save/load, route resolver, ending display.
- Milestone 5 (Week 4): Polish UI, skills, cutscene pipeline, audio.

Gợi ý ưu tiên cho cốt truyện hero:
- Phase A (scaffold): window, tile rendering, player movement, simple enemy.
- Phase B (gameplay loop): quest system, chapter flow, boss skeleton (King placeholder).
- Phase C (polish): cutscenes, sound, multiple endings.

10) Acceptance criteria (MVP)
- Launch game, control player, defeat enemy, pick fragment, open portal, reach ending screen.
- Save/load restores player position, HP/MP and story route.

11) Testing plan
- Unit test core pure logic (StoryState, RouteResolver, EndingResolver, SaveManager logic).
- Manual playtest for gameplay, input, collisions, UI.

12) Next steps 
- Tạo skeleton project (Gradle + main runner) và cấu trúc thư mục.
- Tạo `GameScreen` minimal, `Player` movement, tile renderer.
- Tạo placeholder assets (player.png, enemy.png, tileset.png, ui placeholders, cutscene placeholders).


---


