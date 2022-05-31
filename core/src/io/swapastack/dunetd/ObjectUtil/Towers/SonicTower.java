package io.swapastack.dunetd.ObjectUtil.Towers;

import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.GameScreen;
import io.swapastack.dunetd.ObjectUtil.Enemies.Enemy;
import net.mgsx.gltf.scene3d.scene.Scene;

import static io.swapastack.dunetd.GameScreen.groundTileDimensions;

public class SonicTower extends Tower{
    public static int price = 5;

    int damage;
    public SonicTower(Vector2 coords, int damage) {
        super(coords);
        super.model = new Scene(GameScreen.sceneAssetHashMap.get("towerRound_crystals.glb").scene);
        model.modelInstance.transform.setToTranslation(coords.x, groundTileDimensions.y, coords.y);
        this.damage = damage;
        super.price = price;
    }

    @Override
    public Enemy findEnemyInRange(Enemy e) {
        return super.findEnemyInRange(e);
    }

    @Override
    public void rotateTower(float degrees) {
        this.getTowerModel().modelInstance.transform.rotate(0f,1f,0f,10);
    }

    @Override
    public void hitEnemy(Enemy e) {
        e.setEnemySpeed(Math.round(e.maxSpeed*(1- damage /10f)));
    }
}
