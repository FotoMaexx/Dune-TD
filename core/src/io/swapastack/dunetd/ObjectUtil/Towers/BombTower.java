package io.swapastack.dunetd.ObjectUtil.Towers;

import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.GameScreen;
import io.swapastack.dunetd.ObjectUtil.Enemies.Enemy;
import net.mgsx.gltf.scene3d.scene.Scene;

import static io.swapastack.dunetd.GameScreen.groundTileDimensions;

public class BombTower extends Tower{
    public static int price = 2;

    int damage;
    byte shootCooldown;
    final byte COOLDOWNTIMER = 10;
    public BombTower(Vector2 coords, int damage) {
        super(coords);
        super.model = new Scene(GameScreen.sceneAssetHashMap.get("weapon_blaster.glb").scene);
        model.modelInstance.transform.setToTranslation(coords.x, groundTileDimensions.y, coords.y);
        this.damage = damage;
        super.price = price;
    }

    @Override
    public Enemy findEnemyInRange(Enemy e) {
        return super.findEnemyInRange(e);
    }

    @Override
    public void hitEnemy(Enemy e) {
        shootCooldown++;
        if(shootCooldown %COOLDOWNTIMER != 0)
            return;
        shootCooldown = 0;
        e.changeHealthpoints(-this.damage);
    }
}
