package io.swapastack.dunetd;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import io.swapastack.dunetd.ObjectUtil.Enemies.BossEnemy;
import io.swapastack.dunetd.ObjectUtil.Enemies.Enemy;
import io.swapastack.dunetd.ObjectUtil.Enemies.Harvester;
import io.swapastack.dunetd.ObjectUtil.Enemies.Infantry;
import io.swapastack.dunetd.ObjectUtil.GameField.Graph;
import io.swapastack.dunetd.ObjectUtil.GameField.Node;
import io.swapastack.dunetd.ObjectUtil.GameField.PathFindingAlgorithm;
import io.swapastack.dunetd.ObjectUtil.Towers.*;
import io.swapastack.dunetd.UI.GameUI;
import io.swapastack.dunetd.UI.TowerSelectingUI;
import io.swapastack.dunetd.UI.StatsUI;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;
import org.decimal4j.util.DoubleRounder;

import java.util.*;

/**
 * The GameScreen class.
 * Main driver class for the game.
 *
 * @author Tim Palm, Dennis Jehle
 */
public class GameScreen implements Screen {

    private final DuneTD parent;

    private static final byte FRAMEINTERVALL = 20;

    private static Vector2 startPos, endPos;

    public static int[][] gameField;
    public static boolean startPortalPlaced = false;
    public static boolean endPortalPlaced = false;
    public static boolean allowEnemySpawn = false;
    private static byte frameInterval;
    private static long frames;

    public static int waveCounter = 0;
    public static int spice = 3;
    public static int lives = 3;

    public static boolean firstKlopferPlaced = false;

    public static Scene firstKlopfer;

    public static LinkedList<Vector2> path;

    private static LinkedList<Enemy> currentWaveEnemyPile;
    private static ArrayList<Enemy> currentWaveEnemiesSpawned;

    private static HashSet<Tower> currentPlacedTowers;

    // GDX GLTF
    private static SceneManager sceneManager;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private SceneSkybox skybox;
    private DirectionalLightEx light;

    // libGDX
    private PerspectiveCamera camera;
    private CameraInputController cameraInputController;

    // 3D models
    String basePath = "kenney_gltf/";
    String kenneyAssetsFile = "kenney_assets.txt";
    String[] kenneyModels;
    public static HashMap<String, SceneAsset> sceneAssetHashMap;

    // Grid Specifications
    public static int minSize = 2;
    public static int maxSize = 3;
    private static int rows = rand(minSize, maxSize);
    private static int cols = rand(minSize, maxSize);

    /** @see https://stackoverflow.com/a/363692 **/
    public static int rand(int min, int max) {
        Random rand = new Random();

        return rand.nextInt((max - min) + 1) + min;
    }

    public static Vector3 groundTileDimensions;

    // SpaiR/imgui-java
    public ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    public ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    long windowHandle;

    //Game UI
    GameUI gameUI;

    //Not Used:
    public GameScreen(DuneTD parent) {
        this.parent = parent;
        frameInterval = 0;
        initGameUI();
    }

    /**
     * Constructor: Initializes the {@link GameScreen}
     *
     * @param parent The {@link DuneTD} parent object
     * @param fieldX The field dimension on the X-axis.
     * @param fieldY The field dimension on the Z-axis.
     **/
    public GameScreen(DuneTD parent, byte fieldX, byte fieldY) {
        this.parent = parent;
        rows = fieldX;
        cols = fieldY;
        frameInterval = 0;
        initGameUI();
    }

    /**
     * Bomb Tower: Medium costly tower that deals area damage to enemies.
     **/
    public void initGameUI() {
        gameUI = new GameUI(parent);
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     **/
    @Override
    public void show() {
        // SpaiR/imgui-java
        ImGui.createContext();
        windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 120");

        // GDX GLTF - Scene Manager
        sceneManager = new SceneManager(64);

        // GDX GLTF - Light
        light = new DirectionalLightEx();
        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);

        // GDX GLTF - Image Based Lighting
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // GDX GLTF - This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        // GDX GLTF - Cubemaps
        sceneManager.setAmbientLight(1f);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // GDX GLTF - Skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);

        // Camera
        camera = new PerspectiveCamera();
        camera.position.set(10.0f, 10.0f, 10.0f);
        camera.lookAt(Vector3.Zero);
        sceneManager.setCamera(camera);

        // Camera Input Controller
        cameraInputController = new CameraInputController(camera);

        // Set Input Processor
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(gameUI.inputMultiplexerUI);
        inputMultiplexer.addProcessor(cameraInputController);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Load all 3D models listed in kenney_assets.txt file in blocking mode
        FileHandle assetsHandle = Gdx.files.internal("kenney_assets.txt");
        String fileContent = assetsHandle.readString();
        kenneyModels = fileContent.split("\\r?\\n");
        for (String kenneyModel : kenneyModels) {
            DuneTD.assetManager.load(basePath + kenneyModel, SceneAsset.class);
        }
        // Load example enemy models
        DuneTD.assetManager.load("faceted_character/scene.gltf", SceneAsset.class);
        DuneTD.assetManager.load("cute_cyborg/scene.gltf", SceneAsset.class);
        DuneTD.assetManager.load("spaceship_orion/scene.gltf", SceneAsset.class);

        // Load all custom 3D models //Thanks to Tim Palm for the Model
        DuneTD.assetManager.load("timpalm/klopfer.glb", SceneAsset.class);

        // Finish up loading 3D models
        DuneTD.assetManager.finishLoading();

        // Create scene assets for all loaded models
        sceneAssetHashMap = new HashMap<>();
        for (String kenneyModel : kenneyModels) {
            SceneAsset sceneAsset = DuneTD.assetManager.get(basePath + kenneyModel, SceneAsset.class);
            sceneAssetHashMap.put(kenneyModel, sceneAsset);
        }
        SceneAsset bossCharacter = DuneTD.assetManager.get("faceted_character/scene.gltf");
        sceneAssetHashMap.put("faceted_character/scene.gltf", bossCharacter);
        SceneAsset enemyCharacter = DuneTD.assetManager.get("cute_cyborg/scene.gltf");
        sceneAssetHashMap.put("cute_cyborg/scene.gltf", enemyCharacter);
        SceneAsset harvesterCharacter = DuneTD.assetManager.get("spaceship_orion/scene.gltf");
        sceneAssetHashMap.put("spaceship_orion/scene.gltf", harvesterCharacter);

        // Create scene assets for all loaded custom models
        SceneAsset klopfer = DuneTD.assetManager.get("timpalm/klopfer.glb");
        sceneAssetHashMap.put("timpalm/klopfer.glb", klopfer);

        //createMapExample(sceneManager);

        createMap(sceneManager);

    }

    /**
     * Called when the screen should render itself.
     *
     * @param delta The time in seconds since the last render.
     **/
    @Override
    public void render(float delta) {
        // OpenGL - clear color and depth buffer
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        try {
            for (Enemy e : currentWaveEnemiesSpawned)
                e.getAnimationController().update(delta);
        } catch (NullPointerException ignored) {
        }

        // SpaiR/imgui-java
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        // GDX GLTF - update scene manager and render scene
        sceneManager.update(delta);
        sceneManager.render();

        ImGui.begin("Performance", ImGuiWindowFlags.AlwaysAutoResize);
        ImGui.text(String.format(Locale.US, "deltaTime: %1.6f", delta));
        ImGui.end();

        // SpaiR/imgui-java
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        //Update UI
        gameUI.update(delta);

        //Draw UI
        gameUI.render();

        if (allowEnemySpawn) {
            moveEnemies();
            shootOnEnemy();
        }

    }

    //Disabled:
    @Override
    public void resize(int width, int height) {
        // GDX GLTF - update the viewport
        sceneManager.updateViewport(width, height);
    }

    //Not implemented
    @Override
    public void pause() {
    }

    //Not implemented
    @Override
    public void resume() {
    }

    //Not implemented
    @Override
    public void hide() {
    }

    /**
     * Creates an empty map with only the ground tiles.
     **/
    private void createMap(SceneManager sceneManager) {
        gameField = new int[rows][cols];
        currentPlacedTowers = new HashSet<>();
        groundTileDimensions = createGameField();
    }

    /**
     * Actually generates the ground tiles.
     **/
    private static Vector3 createGameField() {
        Vector3 groundTileDimensions = new Vector3();

        for (int i = 0; i < rows; i++) {
            for (int k = 0; k < cols; k++) {
                // Create a new Scene object from the tile_dirt gltf model
                Scene gridTile = new Scene(sceneAssetHashMap.get("tile_dirt.glb").scene);
                // Create a new BoundingBox, this is useful to check collisions or to get the model dimensions
                BoundingBox boundingBox = new BoundingBox();
                // Calculate the BoundingBox from the given ModelInstance
                gridTile.modelInstance.calculateBoundingBox(boundingBox);
                // Create Vector3 to store the ModelInstance dimensions
                Vector3 modelDimensions = new Vector3();
                // Read the ModelInstance BoundingBox dimensions
                boundingBox.getDimensions(modelDimensions);
                groundTileDimensions.set(modelDimensions);
                // Set the ModelInstance to the respective row and cell of the map
                gridTile.modelInstance.transform.setToTranslation(i * modelDimensions.x, 0.0f, k * modelDimensions.z);
                // Add the Scene object to the SceneManager for rendering
                sceneManager.addScene(gridTile);
            }
        }

        return groundTileDimensions;
    }

    /**
     * Called whenever a tower is placed.
     *
     * @param coords     The coordinates of the to be placed tower.
     * @param towerIndex The index of the tower.
     * @see Tower
     **/
    public static void addTower(Vector2 coords, int towerIndex) {
        float internalX = coords.x;
        coords.x = gameField.length - 1 - coords.x;
        switch (towerIndex) {
            case 1:
                Cannon cannon = new Cannon(coords, 5);
                if (cannon.getPrice() > spice) {
                    GameUI.NotEnoughMoneyDialog();
                    return;
                }
                StatsUI.changeMoney(-cannon.getPrice());
                currentPlacedTowers.add(cannon);
                sceneManager.addScene(cannon.getTowerModel());
                break;
            case 2:
                BombTower bombTower = new BombTower(coords, 10);
                if (bombTower.getPrice() > spice) {
                    GameUI.NotEnoughMoneyDialog();
                    return;
                }
                StatsUI.changeMoney(-bombTower.getPrice());
                currentPlacedTowers.add(bombTower);
                sceneManager.addScene(bombTower.getTowerModel());
                break;
            case 3:
                SonicTower sonicTower = new SonicTower(coords, 4);
                if (sonicTower.getPrice() > spice) {
                    GameUI.NotEnoughMoneyDialog();
                    return;
                }
                StatsUI.changeMoney(-sonicTower.getPrice());
                currentPlacedTowers.add(sonicTower);
                sceneManager.addScene(sonicTower.getTowerModel());
                break;
            case 5:
                break;
            case 4:
                Scene klopfer = new Scene(sceneAssetHashMap.get("timpalm/klopfer.glb").scene);
                klopfer.modelInstance.transform.setToTranslation(coords.x, groundTileDimensions.y, coords.y);
                klopfer.modelInstance.transform.scale(0.2f, 0.2f, 0.2f);
                klopfer.modelInstance.transform.rotate(new Vector3(0f, 1f, 0f), 30f);
                if (firstKlopferPlaced) {
                    shaiHulud(klopfer);
                    return;
                }
                firstKlopfer = klopfer;
                firstKlopferPlaced = true;
                sceneManager.addScene(firstKlopfer);
                break;
            case 6:
                startPortalPlaced = true;
                startPos = new Vector2(internalX, coords.y);
                Scene startPortal = new Scene(sceneAssetHashMap.get("tile_spawn.glb").scene);
                startPortal.modelInstance.transform.setToTranslation(coords.x, groundTileDimensions.y, coords.y);
                sceneManager.addScene(startPortal);
                break;
            case 7:
                endPortalPlaced = true;
                endPos = new Vector2(internalX, coords.y);
                Scene endPortal = new Scene(sceneAssetHashMap.get("tile_endSpawn.glb").scene);
                endPortal.modelInstance.transform.setToTranslation(coords.x, groundTileDimensions.y, coords.y);
                sceneManager.addScene(endPortal);
                break;
        }

        gameField[(int) internalX][(int) coords.y] = towerIndex;

    }

    /**
     * Removes the tower on the specified coordinates in Destruction Mode.
     *
     * @param coords Coordinates of the to be removed tower.
     * @see Tower
     **/
    public static void removeTower(Vector2 coords) {
        if (gameField[(int) coords.x][(int) coords.y] == 6)
            startPortalPlaced = false;
        else if (gameField[(int) coords.x][(int) coords.y] == 7) {
            endPortalPlaced = false;
        }

        gameField[(int) coords.x][(int) coords.y] = 0;
        coords.x = gameField.length - 1 - coords.x;

        for (Tower t : currentPlacedTowers) {
            if (t.getCoords().equals(coords)) {
                currentPlacedTowers.remove(t);
                StatsUI.changeMoney(t.getPrice());
                System.out.println(t.getClass());
                sceneManager.removeScene(t.getTowerModel());
                return;
            }
        }

        //Remove Objects from GameField
        sceneManager.getRenderableProviders().forEach(s -> {
            Scene current = (Scene) s;
            float x = current.modelInstance.transform.val[12];
            float y = current.modelInstance.transform.val[13];
            float z = current.modelInstance.transform.val[14];
            if (x == coords.x && y != 0f && z == coords.y) {
                sceneManager.removeScene(current);
            }
        });
    }

    /**
     * Creates the path for the enemies based on Dijkstra Shortest Path Alhorithm.
     *
     * @see PathFindingAlgorithm
     **/
    public static void createPath() {
        Graph g = new Graph(gameField);
        path = new LinkedList<>();

        for (Node n : PathFindingAlgorithm.getPath(g, g.getNode(startPos), g.getNode(endPos)))
            path.add(n.getCoords());

        path.forEach(ll -> System.out.print("(" + ll.x + "," + ll.y + "), "));
        System.out.println();

        if (path.size() <= 1) {
            VisDialog pathfinding_fehler = new VisDialog("PATHFINDING FEHLER");
            VisLabel pathfinding_fehler_text = new VisLabel("Es konnte kein Pfad von Start- zum Endportal gefunden werden. Bitte schaue, dass ein Weg zwischen der Portalen existiert.");
            pathfinding_fehler_text.setAlignment(Align.center);
            pathfinding_fehler.text(pathfinding_fehler_text);
            pathfinding_fehler.button("OK");
            GameUI.showDialog(pathfinding_fehler);
            TowerSelectingUI.visTextButton.setText("Welle initiieren");
            TowerSelectingUI.waveReady = false;
            return;
        }
        path.forEach(v -> {
            v.x = gameField.length - 1 - v.x;
        });

        for (int i = sceneManager.getRenderableProviders().size - 1; i >= 0; i--) {
            Scene current = (Scene) sceneManager.getRenderableProviders().get(i);
            float y = current.modelInstance.transform.val[13];
            if (y == 0f)
                sceneManager.removeScene(current);
        }

        createGameField();
    }

    /**
     * Creates the Enemies in the current Wave
     **/
    @SuppressWarnings("unchecked")
    public static void createEnemies() {
        currentWaveEnemyPile = new LinkedList<>();
        for (int i = 0; i < 3 + 2 * waveCounter; i++) {
            Enemy current;
            if (i % 3 == 0 && i > 6)
                current = new Harvester(250, 3, path.get(0), (LinkedList<Vector2>) path.clone());
            else if (i % 4 == 0 && i > 16)
                current = new BossEnemy(500, 2, path.get(0), (LinkedList<Vector2>) path.clone());
            else
                current = new Infantry(100, 5, path.get(0), (LinkedList<Vector2>) path.clone());
            current.setModelToPosition();
            currentWaveEnemyPile.add(current);
        }
        Collections.shuffle(currentWaveEnemyPile);
        StatsUI.setWaveEnemies(currentWaveEnemyPile.size());
        frameInterval = 0;
        frames = -1;
        currentWaveEnemiesSpawned = new ArrayList<Enemy>();
        allowEnemySpawn = true;
    }

    private static void moveEnemies() {
        frameInterval++;
        if (frameInterval % FRAMEINTERVALL != 0) {
            return;
        }
        frameInterval = 0;
        frames++;

        if (currentWaveEnemiesSpawned.size() == 0 && currentWaveEnemyPile.size() == 0) { //Important: wave finished
            allowEnemySpawn = false;
            firstKlopferPlaced = false;
            removeKlopfer();
            StatsUI.changeWaveNo();
            TowerSelectingUI.allowBuild();
        }

        Enemy removedEnemy = null;

        for (Enemy currentEnemy : currentWaveEnemiesSpawned) {
            sceneManager.removeScene(currentEnemy.getEnemyModel());
            float currentX = (float) DoubleRounder.round(currentEnemy.getEnemyCoords().x, 4);
            float currentY = (float) DoubleRounder.round(currentEnemy.getEnemyCoords().y, 4);

            float goalX = currentEnemy.destination.getFirst().x;
            float goalY = currentEnemy.destination.getFirst().y;

            if (currentX < goalX + 0.05f && currentX > goalX - 0.05f && currentY < goalY + 0.05f && currentY > goalY - 0.05f) {
                currentX = goalX;
                currentY = goalY;
                currentEnemy.destination.removeFirst();
            }


            if (currentEnemy.destination.isEmpty()) {
                StatsUI.removeLive();
                removedEnemy = currentEnemy;
                continue;
            }

            byte secondaryVelocity = 100;
            float totalVelocity = (float) currentEnemy.getEnemySpeed() / secondaryVelocity;

            if (currentX < currentEnemy.destination.getFirst().x) {
                currentEnemy.setEnemyCoords(currentX + totalVelocity, currentY);
                currentEnemy.moveEnemy(totalVelocity, 0f);
                currentEnemy.rotateEnemy(270);
            }
            if (currentX > currentEnemy.destination.getFirst().x) {
                currentEnemy.setEnemyCoords(currentX - totalVelocity, currentY);
                currentEnemy.moveEnemy(-totalVelocity, 0f);
                currentEnemy.rotateEnemy(90);
            }
            if (currentY < currentEnemy.destination.getFirst().y) {
                currentEnemy.setEnemyCoords(currentX, currentY + totalVelocity);
                currentEnemy.moveEnemy(0f, totalVelocity);
                currentEnemy.rotateEnemy(0);
            }
            if (currentY > currentEnemy.destination.getFirst().y) {
                currentEnemy.setEnemyCoords(currentX, currentY - totalVelocity);
                currentEnemy.moveEnemy(0f, -totalVelocity);
                currentEnemy.rotateEnemy(180);
            }
            sceneManager.addScene(currentEnemy.getEnemyModel());
        }

        try {
            currentWaveEnemiesSpawned.remove(removedEnemy);
        } catch (NullPointerException ignored) {
        }

        if (lives == 0) {
            allowEnemySpawn = false;
            lostGame();
            return;
        }

        short spawnIntervall = 10;
        if (frames % spawnIntervall == 0 && 0 < currentWaveEnemyPile.size()) {
            Enemy currentEnemy = currentWaveEnemyPile.poll();
            currentWaveEnemiesSpawned.add(currentEnemy);
            sceneManager.addScene(currentEnemy.getEnemyModel());
        }

    }

    private static void shootOnEnemy() {
        if (frameInterval % FRAMEINTERVALL != FRAMEINTERVALL / 2) {
            return;
        }
        for (Tower t : currentPlacedTowers) {
            if (t instanceof Cannon) {
                for (Enemy e : currentWaveEnemiesSpawned) {
                    Enemy damagedEnemy = t.findEnemyInRange(e);
                    if (damagedEnemy == null)
                        continue;

                    t.hitEnemy(damagedEnemy);
                    if (damagedEnemy.getHealthpoints() <= 0) {
                        currentWaveEnemiesSpawned.remove(damagedEnemy);
                        sceneManager.removeScene(damagedEnemy.getEnemyModel());
                        StatsUI.addWaveEnemies();
                        StatsUI.changeMoney(damagedEnemy.getReward());
                        ((Cannon) t).setFocus(false);
                        for (Tower tt : currentPlacedTowers) {
                            if (!(tt instanceof Cannon))
                                continue;
                            try {
                                if (((Cannon) tt).focused.equals(damagedEnemy))
                                    ((Cannon) tt).setFocus(false);
                            } catch (NullPointerException ignored) {
                            }
                        }
                        break;
                    }
                }
            } else { // Schallturm Flächenschaden
                HashSet<Enemy> enemies = new HashSet<>();
                for (Enemy e : currentWaveEnemiesSpawned) {
                    e.setEnemySpeed(e.maxSpeed);
                    Enemy currentEnemy = t.findEnemyInRange(e);
                    if (currentEnemy == null)
                        continue;
                    enemies.add(currentEnemy);
                }
                if (enemies.size() == 0)
                    continue;
                for (Enemy damagedEnemy : enemies) {
                    t.hitEnemy(damagedEnemy);
                    if (damagedEnemy.getHealthpoints() <= 0) {
                        currentWaveEnemiesSpawned.remove(damagedEnemy);
                        sceneManager.removeScene(damagedEnemy.getEnemyModel());
                        StatsUI.addWaveEnemies();
                        StatsUI.changeMoney(damagedEnemy.getReward());
                    }
                }
            }
        }
    }


    private static void lostGame() {
        VisDialog verloren = new VisDialog("DU HAST VERLOREN");
        VisLabel verloren_text = new VisLabel("Du hast verloren. Um erneut zu spielen starte das Spiel neu!");
        verloren_text.setAlignment(Align.center);
        verloren.text(verloren_text);
        verloren.button("OK");
        GameUI.showDialog(verloren);
    }


    private static void shaiHulud(Scene secondKlopfer) {
        if (secondKlopfer.modelInstance.transform.val[12] == firstKlopfer.modelInstance.transform.val[12]) {
            int axis = gameField.length - 1 - (int) secondKlopfer.modelInstance.transform.val[12];
            firstKlopferPlaced = false;
            HashSet<Enemy> removedEnemies = new HashSet<>();
            HashSet<Tower> removedTowers = new HashSet<>();
            for (Enemy e : currentWaveEnemiesSpawned) {
                if (Math.round(e.getEnemyCoords().x) == axis) {
                    removedEnemies.add(e);
                    sceneManager.removeScene(e.getEnemyModel());
                    StatsUI.addWaveEnemies();
                    StatsUI.changeMoney(e.getReward());
                }
            }
            for (Tower t : currentPlacedTowers) {
                if (Math.round(t.getCoords().x) == axis) {
                    removedTowers.add(t);
                    StatsUI.changeMoney(-t.getPrice());
                }
            }
            removedEnemies.forEach(e -> currentWaveEnemiesSpawned.remove(e));
            removedTowers.forEach(t -> removeTower(t.getCoords()));
            removeKlopfer();
        } else if (secondKlopfer.modelInstance.transform.val[14] == firstKlopfer.modelInstance.transform.val[14]) {
            int axis = (int) secondKlopfer.modelInstance.transform.val[14];
            firstKlopferPlaced = false;
            HashSet<Enemy> removedEnemies = new HashSet<>();
            HashSet<Tower> removedTowers = new HashSet<>();
            for (Enemy e : currentWaveEnemiesSpawned) {
                if (Math.round(e.getEnemyCoords().y) == axis) {
                    removedEnemies.add(e);
                    sceneManager.removeScene(e.getEnemyModel());
                    StatsUI.addWaveEnemies();
                    StatsUI.changeMoney(e.getReward());
                }
            }
            for (Tower t : currentPlacedTowers) {
                if (Math.round(t.getCoords().y) == axis) {
                    removedTowers.add(t);
                    StatsUI.changeMoney(-t.getPrice());
                }
            }
            removedEnemies.forEach(e -> currentWaveEnemiesSpawned.remove(e));
            removedTowers.forEach(t -> removeTower(t.getCoords()));
            removeKlopfer();
        } else {
            VisDialog klopfer_falsch_patziert = new VisDialog("Klopfer falsch Patziert");
            VisLabel klopfer_falsch_platziert_text = new VisLabel("Die zwei Klopfer für den Shai-Hulud müssen in einer Reihe oder in einer Spalte platziert werden!");
            klopfer_falsch_platziert_text.setAlignment(Align.center);
            klopfer_falsch_patziert.text(klopfer_falsch_platziert_text);
            klopfer_falsch_patziert.button("OK");
            GameUI.showDialog(klopfer_falsch_patziert);
        }

    }

    private static void removeKlopfer() {
        for (int i = 0; i < gameField.length; i++) {
            for (int j = 0; j < gameField[0].length; j++) {
                if (gameField[i][j] == 5) {
                    removeTower(new Vector2(i, j));
                }
            }
        }
    }

    /**
     * This function acts as a starting point.
     * It generate a simple rectangular map with towers placed on it.
     * It doesn't provide any functionality, but it uses some common ModelInstance specific functions.
     *
     * @author Dennis Jehle
     */

    @Override
    public void dispose() {
        // GDX GLTF - dispose resources
        sceneManager.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        skybox.dispose();
        VisUI.dispose();
        gameUI.dispose();
    }

}
