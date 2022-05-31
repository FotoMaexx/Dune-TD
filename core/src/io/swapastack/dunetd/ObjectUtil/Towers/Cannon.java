package io.swapastack.dunetd.ObjectUtil.Towers;

import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.GameScreen;
import io.swapastack.dunetd.ObjectUtil.Enemies.Enemy;
import net.mgsx.gltf.scene3d.scene.Scene;

import static io.swapastack.dunetd.GameScreen.groundTileDimensions;

public class Cannon extends Tower{
    public static int price = 1;

    int damage;
    public Enemy focused;
    boolean hasFocus;
    public Cannon(Vector2 coords, int damage) {
        super(coords);
        super.model = new Scene(GameScreen.sceneAssetHashMap.get("weapon_cannon.glb").scene);
        model.modelInstance.transform.setToTranslation(coords.x, groundTileDimensions.y, coords.y);
        this.damage = damage;
        this.hasFocus = false;
        super.price = price;
    }

    @Override
    public Enemy findEnemyInRange(Enemy e) {
        if(this.hasFocus && !this.focused.equals(e)){
            return null;
        }
        this.focused = super.findEnemyInRange(e);
        this.hasFocus = this.focused != null;
        return this.focused;
    }

    @Override
    public void hitEnemy(Enemy e) {
        e.changeHealthpoints(-this.damage);
    }

    public boolean hasFocus() {
        return hasFocus;
    }

    public void setFocus(boolean hasFocus) {
        this.hasFocus = hasFocus;
    }
}
