package io.swapastack.dunetd.UI;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.swapastack.dunetd.DuneTD;
import io.swapastack.dunetd.GameScreen;

public class StatsUI extends Actor {

    private final DuneTD parent;
    private final Stage stage;
    private VisWindow window;
    private static VisLabel wave, currentEnemyCount, spice, lives;
    private static int currentWaveMaxEnemies, currentWaveEnemies;

    /**
     * Sets up a UI window with basic information about the current game stats.
     **/
    public StatsUI(DuneTD parent, Stage stage) {
        this.parent = parent;
        this.stage = stage;
        setWidgets();
    }

    /**
     * Initial settings for widgets.
     **/
    private void setWidgets() {
        wave = new VisLabel("0");
        spice = new VisLabel(Integer.toString(GameScreen.spice));
        lives = new VisLabel(Integer.toString(GameScreen.lives));

        window = new VisWindow("Scoreboard", "noborder");
        window.add(new VisLabel("Welle:")).left();
        window.add(wave).right().padLeft(15).row();
        window.add(new VisLabel("Spice:")).left();
        window.add(spice).right().padLeft(15).row();
        window.add(new VisLabel("Übrige Leben:")).left();
        window.add(lives).right().padLeft(15).row();

        stage.addActor(window);
    }

    // Updates the Money Counter
    public static void changeMoney(int money) {
        GameScreen.spice += money;
        spice.setText(GameScreen.spice);
    }

    // Updates the Wave Counter
    public static void changeWaveNo() {
        GameScreen.waveCounter++;
        wave.setText(GameScreen.waveCounter);
    }

    // Defines the total amount of Enemies in this Wave
    public static void setWaveEnemies(int maxEnemies) {
        currentWaveMaxEnemies = maxEnemies;
        currentWaveEnemies = 0;
    }

    // Increases the Kill Counter by One
    public static void addWaveEnemies() {
        currentWaveEnemies++;
    }

    // Subtratcts one Live from the Live Counter
    public static void removeLive() {
        GameScreen.lives--;
        lives.setText(Integer.toString(GameScreen.lives));
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        window.setPosition(x, y);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        window.setSize(width, height);
    }
}
