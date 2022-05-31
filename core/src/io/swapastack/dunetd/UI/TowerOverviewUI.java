package io.swapastack.dunetd.UI;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.swapastack.dunetd.DuneTD;
import io.swapastack.dunetd.GameScreen;


public class TowerOverviewUI extends Actor {
    private final DuneTD parent;
    private final Stage stage;
    private final InputMultiplexer inputMultiplexer;
    private VisWindow window;
    private final int selectedTower;

    /** Constructor:
     *  @param inputMultiplexer The widget needs to accept inputs.
     *  @param stage The stage on where to show the UI.
     *  @param selectedTower The index of the selected tower.**/
    public TowerOverviewUI(DuneTD parent, Stage stage, InputMultiplexer inputMultiplexer, int selectedTower){
        this.parent = parent;
        this.stage = stage;
        this.inputMultiplexer = inputMultiplexer;
        this.selectedTower = selectedTower + 1;
        setWidgets();
    }

    private void setWidgets() {
        int dimX = GameScreen.gameField.length;
        int dimY = GameScreen.gameField[0].length;
        window = new VisWindow("Spielfeld", "noborder");
        for(int x = 0; x < dimX; x++){ //Add image buttons
            for(int y = 0; y < dimY; y++){
                Texture checkedTexture = new Texture("gamefieldoverview/checked.png");
                Texture uncheckedTexture = new Texture("gamefieldoverview/unchecked.png");
                TowerPlacingUI towerPlacingUI;

                // Tower
                if (GameScreen.gameField[x][y] > 0 && GameScreen.gameField[x][y] <= 4) {
                    towerPlacingUI = new TowerPlacingUI(uncheckedTexture,uncheckedTexture,new Vector2(x,y));
                    if(TowerSelectingUI.buildMode)
                        towerPlacingUI.setDisabled(true);
                }
                // Klopfer
                else if (GameScreen.gameField[x][y] == 5) {
                    Texture tex = new Texture ("gamefieldoverview/HammerCircle.png");
                    towerPlacingUI = new TowerPlacingUI(tex,tex,new Vector2(x,y));
                    if(TowerSelectingUI.buildMode)
                        towerPlacingUI.setDisabled(true);
                }
                //Start Portal
                else if (GameScreen.gameField[x][y] == 6) {
                    Texture tex = new Texture ("gamefieldoverview/StartPortal.png");
                    towerPlacingUI = new TowerPlacingUI(tex,tex,new Vector2(x,y));
                    if(TowerSelectingUI.buildMode)
                        towerPlacingUI.setDisabled(true);
                }
                //End Portal
                else if (GameScreen.gameField[x][y] == 7){
                    Texture tex = new Texture ("gamefieldoverview/EndPortal.png");
                    towerPlacingUI = new TowerPlacingUI(tex,tex,new Vector2(x,y));
                    if(TowerSelectingUI.buildMode)
                        towerPlacingUI.setDisabled(true);
                }
                // Free
                else{
                    towerPlacingUI = new TowerPlacingUI(checkedTexture,checkedTexture,new Vector2(x,y));
                    if(!TowerSelectingUI.buildMode)
                        towerPlacingUI.setDisabled(true);
                }
                towerPlacingUI.setSize(20f,20f);
                towerPlacingUI.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent inputEvent, float x, float y){
                        if(towerPlacingUI.isChecked()){
                            byte cordX = (byte) towerPlacingUI.getID().x;
                            byte cordY = (byte) towerPlacingUI.getID().y;
                            window.remove();
                            TowerSelectingUI.waveReady = false;
                            TowerSelectingUI.visTextButton.setText("Initialize wave");
                            if(TowerSelectingUI.buildMode){
                                if(GameScreen.startPortalPlaced && selectedTower == 6 || GameScreen.endPortalPlaced && selectedTower == 7){
                                    VisDialog portal_error = new VisDialog("Zu viele Portale");
                                    VisLabel portal_error_text = new VisLabel("Du darst nicht mehr als ein Portal von der gleichen Kategorie platzieren!");
                                    portal_error_text.setAlignment(Align.center);
                                    portal_error.text(portal_error_text);
                                    portal_error.button("OK");
                                    portal_error.show(stage);
                                }
                                else
                                    GameScreen.addTower(new Vector2(cordX,cordY), selectedTower);
                            }
                            else
                                GameScreen.removeTower(new Vector2(cordX,cordY));
                            TowerSelectingUI.buildMode = true;
                            TowerSelectingUI.locationSelectorActive = false;
                        }
                    }
                });
                window.add(towerPlacingUI);
            }
            window.row();
        }
        VisTextButton b = new VisTextButton("Cancel"); //Add cancel Button
        b.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                window.remove();
                TowerSelectingUI.locationSelectorActive = false;
                TowerSelectingUI.buildMode = true;
            }
        });
        window.add(b).right().pad(10).colspan(dimY).row();
        window.setSize(dimY*40f, dimX*40f+b.getHeight()+10f);
        window.setPosition(DuneTD.WIDTH / 2f - window.getWidth() / 2f, DuneTD.HEIGHT / 2f - window.getHeight() / 2f);
        inputMultiplexer.addProcessor(stage);
        stage.addActor(window);
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
