package io.swapastack.dunetd.ObjectUtil.Towers;

import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.ObjectUtil.Enemies.Enemy;
import net.mgsx.gltf.scene3d.scene.Scene;

public abstract class Tower {
    Vector2 coords;
    Scene model;
    int rotation;
    int price;

    public Tower(Vector2 coords){
        this.coords = coords;
        this.rotation = 0;
    }


    public Scene getTowerModel(){
        return this.model;
    }


    public void setTowerModel(Scene model) {
        this.model = model;
    }


    public void rotateTower(float degrees){
        if(this.rotation == degrees)
            return;
        this.model.modelInstance.transform.rotate(0f,1f,0f,degrees-this.rotation);
        this.rotation += (degrees-this.rotation) % 360;
    }

    public Vector2 getCoords() {
        return coords;
    }

    public int getPrice(){
        return this.price;
    }

    public Enemy findEnemyInRange(Enemy e){

        if(this.getCoords().x == Math.round(e.getEnemyCoords().x - 1) && this.getCoords().y == Math.round(e.getEnemyCoords().y - 1)){ //NW
            this.rotateTower(225);
        }
        else if(this.getCoords().x == Math.round(e.getEnemyCoords().x - 1) && this.getCoords().y == Math.round(e.getEnemyCoords().y)){ //W
            this.rotateTower(270);
        }
        else if(this.getCoords().x == Math.round(e.getEnemyCoords().x - 1) && this.getCoords().y == Math.round(e.getEnemyCoords().y + 1)){ //SW
            this.rotateTower(315);
        }
        else if(this.getCoords().x == Math.round(e.getEnemyCoords().x) && this.getCoords().y == Math.round(e.getEnemyCoords().y + 1)){ //S
            this.rotateTower(0);
        }
        else if(this.getCoords().x == Math.round(e.getEnemyCoords().x + 1) && this.getCoords().y == Math.round(e.getEnemyCoords().y + 1)){ //SO
            this.rotateTower(45);
        }
        else if(this.getCoords().x == Math.round(e.getEnemyCoords().x + 1) && this.getCoords().y == Math.round(e.getEnemyCoords().y)){ //O
            this.rotateTower(90);
        }
        else if(this.getCoords().x == Math.round(e.getEnemyCoords().x + 1) && this.getCoords().y == Math.round(e.getEnemyCoords().y - 1)){ //NO
            this.rotateTower(135);
        }
        else if(this.getCoords().x == Math.round(e.getEnemyCoords().x) && this.getCoords().y == Math.round(e.getEnemyCoords().y - 1)){ //N
            this.rotateTower(180);
        }
        else
            return null;
        return e;
    }

    public void hitEnemy(Enemy e){

    };

}
