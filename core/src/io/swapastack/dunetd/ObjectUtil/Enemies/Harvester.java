package io.swapastack.dunetd.ObjectUtil.Enemies;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.GameScreen;
import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.LinkedList;

public class Harvester extends Enemy{
    public static int reward = 3;

    public Harvester(int healthpoints, int speed, Vector2 coords, LinkedList<Vector2> path) {
        super(healthpoints, speed, coords, path);
        super.model = new Scene(GameScreen.sceneAssetHashMap.get("spaceship_orion/scene.gltf").scene);
        model.modelInstance.transform.scale(0.2f, 0.2f, 0.2f);
        super.reward = reward;
        super.animationController = new AnimationController(model.modelInstance);
        super.animationController.setAnimation("Action", -1);
    }

}
