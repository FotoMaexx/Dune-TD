package io.swapastack.dunetd.ObjectUtil.Enemies;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.GameScreen;
import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.LinkedList;

public class BossEnemy extends Enemy{
    public static int reward = 7;

    public BossEnemy(int healthpoints, int speed, Vector2 coords, LinkedList<Vector2> path) {
        super(healthpoints, speed, coords, path);
        super.model = new Scene(GameScreen.sceneAssetHashMap.get("faceted_character/scene.gltf").scene);
        model.modelInstance.transform.scale(0.005f, 0.005f, 0.005f);
        model.modelInstance.transform.rotate(0f,1f,0f,180f);
        super.reward = reward;
        super.animationController = new AnimationController(model.modelInstance);
        super.animationController.setAnimation("Armature|Run", -1);
    }

}
