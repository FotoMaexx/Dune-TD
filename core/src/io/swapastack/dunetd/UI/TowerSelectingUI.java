package io.swapastack.dunetd.UI;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.*;
import io.swapastack.dunetd.DuneTD;
import io.swapastack.dunetd.GameScreen;

/** Shows the Tower selection UI.**/
public class TowerSelectingUI extends Actor {
    private final DuneTD parent;
    private final Stage stage;
    private final InputMultiplexer inputMultiplexer;
    private static VisWindow window;
    private VisList<String> towerList;
    private static VisTextButton DestructionModeButton;
    public static VisTextButton visTextButton;
    protected static boolean locationSelectorActive = false;
    protected static boolean buildMode = true;
    public static boolean waveReady = false;

    /** @param inputMultiplexer The widget needs to accept inputs.
     *  @param stage The stage on where to show the UI.**/
    public TowerSelectingUI(DuneTD parent, Stage stage, InputMultiplexer inputMultiplexer){
        this.parent = parent;
        this.stage = stage;
        this.inputMultiplexer = inputMultiplexer;
        setWidgets();
        setListeners();
        configureWidgets();
    }

    /** Initial settings for widgets.**/
     private void setWidgets() {
            window = new VisWindow("Türme platzieren", "noborder");
            towerList = new VisList<String>();
            towerList.setItems("Geschützturm (1 Spice)", "Bombenturm (2 Spice)", "Schallturm (5 Spice)", "Shai-Hulud"," ", "Start-Portal", "End-Portal");
            towerList.setSelectedIndex(-1);
            visTextButton = new VisTextButton("Welle Initiieren");
            DestructionModeButton = new VisTextButton("Turm zerstören");
    }

    /** Configuration for appearance for widgets.**/
    private void configureWidgets() {
        window.add(towerList).row();
        window.add(new Separator()).pad(5).fillX().expandX().row();
        window.add(DestructionModeButton).row();
        window.add(new Separator()).pad(5).fillX().expandX().row();
        window.add(visTextButton);
        stage.addActor(window);
    }

    /** Set listeners for buttons and other elements in the widget.**/
    private void setListeners() {
        visTextButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent inputEvent, float x, float y){
                if(waveReady){
                    window.getTitleLabel().setText("Nur Klopfer verfügbar");
                    DestructionModeButton.setDisabled(true);
                    GameScreen.createEnemies();
                }
                else{
                    if(GameScreen.startPortalPlaced && GameScreen.endPortalPlaced){
                        visTextButton.setText("Welle starten");
                        waveReady = true;
                        GameScreen.createPath();
                    }
                    else{
                        VisDialog fehlendes_portal = new VisDialog("Fehlendes Portal");
                        VisLabel fehlendes_portal_text = new VisLabel("Es muss ein Start- & Endportal paltziert werden!");
                        fehlendes_portal_text.setAlignment(Align.center);
                        fehlendes_portal.text(fehlendes_portal_text);
                        fehlendes_portal.button("OK");
                        fehlendes_portal.show(stage);
                    }
                }

            }
        });
        DestructionModeButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent inputEvent, float x, float y){
                if(DestructionModeButton.isDisabled())
                    return;
                TowerSelectingUI.buildMode = false;
                if(!locationSelectorActive){
                    locationSelectorActive = true;
                    TowerOverviewUI gfo = new TowerOverviewUI(parent,stage,inputMultiplexer, towerList.getSelectedIndex());
                }
            }
        });

        towerList.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent inputEvent, float x, float y){
                if(!locationSelectorActive){
                    if(GameScreen.allowEnemySpawn == (towerList.getSelectedIndex() != 4)){  //5 - 1 = 4
                        towerList.setSelectedIndex(-1);
                        return;
                    }
                    locationSelectorActive = true;
                    TowerOverviewUI gfo = new TowerOverviewUI(parent,stage,inputMultiplexer, towerList.getSelectedIndex());
                }
                towerList.setSelectedIndex(-1);
            }
        });

        inputMultiplexer.addProcessor(stage);
    }

    /** Reactivates the ability to build towers again after one wave has finished**/
    public static void allowBuild(){
        window.getTitleLabel().setText("Place towers:");
        DestructionModeButton.setDisabled(false);
    }

    @Override
    public void setPosition(float x, float y){
        super.setPosition(x,y);
        window.setPosition(x,y);
    }

    @Override
    public void setSize(float width, float height){
        super.setSize(width,height);
        window.setSize(width,height);
    }


}
