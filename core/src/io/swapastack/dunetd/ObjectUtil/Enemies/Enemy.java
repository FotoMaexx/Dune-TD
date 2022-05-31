package io.swapastack.dunetd.ObjectUtil.Enemies;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.GameScreen;
import io.swapastack.dunetd.ObjectUtil.GameField.PathFindingAlgorithm;
import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.LinkedList;

/**
 * Abstract class for all the enemies.
 **/
public abstract class Enemy {
    int healthpoints;
    float speed;
    public final float maxSpeed;
    public LinkedList<Vector2> destination;
    Vector2 coords;
    Scene model;
    int rotation;
    int reward;
    AnimationController animationController;

    /**
     * Constructor:
     *
     * @param healthpoints The total live points for the enemy.
     * @param speed        The speed for the enemy.
     * @param coords       The current position of the enemy (X- and Z-coordinate).
     * @param path         The path calculated by {@link PathFindingAlgorithm Dijkstras Shortest Path Algorithm}.
     **/
    public Enemy(int healthpoints, float speed, Vector2 coords, LinkedList<Vector2> path) {
        this.healthpoints = healthpoints;
        this.speed = speed;
        this.maxSpeed = speed;
        this.coords = coords;
        this.destination = path;
        this.rotation = 0;
    }

    public int getHealthpoints() {
        return healthpoints;
    }

    public void changeHealthpoints(int value) {
        this.healthpoints = this.healthpoints + value;
    }

    public float getEnemySpeed() {
        return speed;
    }

    public void setEnemySpeed(float speed) {
        this.speed = speed;
    }

    public Scene getEnemyModel() {
        return this.model;
    }

    public void setEnemyModel(Scene model) {
        this.model = model;
    }

    public void setModelToPosition() {
        this.model.modelInstance.transform.trn(this.coords.x, GameScreen.groundTileDimensions.y, this.coords.y);
    }

    public void moveEnemy(float deltaX, float deltaZ) {
        this.model.modelInstance.transform.trn(deltaX, 0f, deltaZ);
    }

    public void rotateEnemy(float degrees) {
        if (this.rotation == degrees)
            return;
        this.model.modelInstance.transform.rotate(0f, 1f, 0f, degrees - this.rotation);
        this.rotation += (degrees - this.rotation) % 360;
    }

    public Vector2 getEnemyCoords() {
        return coords;
    }

    public void setEnemyCoords(Vector2 coords) {
        this.coords = coords;
    }

    public void setEnemyCoords(float x, float y) {
        this.coords = new Vector2(x, y);
    }

    public int getReward() {
        return this.reward;
    }

    public AnimationController getAnimationController() {
        return this.animationController;
    }

}
