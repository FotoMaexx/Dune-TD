package io.swapastack.dunetd.ObjectUtil.Enemies;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.GameScreen;
import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.LinkedList;

public class Infantry extends Enemy{
    public static int reward = 1;

    public Infantry(int healthpoints, int speed, Vector2 coords, LinkedList<Vector2> path) {
        super(healthpoints, speed, coords, path);
        super.model = new Scene(GameScreen.sceneAssetHashMap.get("cute_cyborg/scene.gltf").scene);
        model.modelInstance.transform.scale(0.02f, 0.04f, 0.03f);
        super.reward = reward;
        super.animationController = new AnimationController(model.modelInstance);
        super.animationController.setAnimation("RUN", -1);
    }

}
