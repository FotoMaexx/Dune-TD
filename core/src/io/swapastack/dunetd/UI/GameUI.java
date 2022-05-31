package io.swapastack.dunetd.UI;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.swapastack.dunetd.DuneTD;
import org.jetbrains.annotations.NotNull;

public class GameUI {
    private final DuneTD parent;
    public static Stage stage;
    public TowerSelectingUI towerSelectingUI;
    public StatsUI statsUI;
    public InputMultiplexer inputMultiplexerUI;

    public GameUI(DuneTD parent){
        this.parent = parent;
        stage = new Stage(new FitViewport(DuneTD.WIDTH,DuneTD.HEIGHT));
        inputMultiplexerUI = new InputMultiplexer();
        VisUI.load();
        setWidgets();
        configureWidgets();
    }

    /** Initial settings for widgets.
     * @see TowerSelectingUI
     * @see StatsUI **/
    private void setWidgets() {
        towerSelectingUI = new TowerSelectingUI(parent, stage, inputMultiplexerUI);
        statsUI = new StatsUI(parent,stage);
    }

    private void configureWidgets() {
        towerSelectingUI.setSize(200,280);
        towerSelectingUI.setPosition(1150, 350);

        statsUI.setSize(200,120);
        statsUI.setPosition(1150,650);
    }

    /** Shows a dialog.
     * @param d The dialog to show. Not Null.**/
    public static void showDialog(@NotNull VisDialog d){
        d.show(stage);
    }

    /** Shows a specific dialog if you don't have enough money.
     * @see io.swapastack.dunetd.GameScreen
     * @see StatsUI **/

    public static void NotEnoughMoneyDialog(){
        VisDialog not_enough_money = new VisDialog("Nicht genug Spice!");
        VisLabel not_enough_money_text = new VisLabel("Du hast nicht genug Spice um diesen Turm zu platzieren!");
        not_enough_money_text.setAlignment(Align.center);
        not_enough_money.text(not_enough_money_text);
        not_enough_money.button("OK");
        showDialog(not_enough_money);
    }

    public void update(float delta){
        stage.act(delta);
    }
    public void render(){
        stage.draw();
    }
    public void dispose(){
        stage.dispose();
    }


}
