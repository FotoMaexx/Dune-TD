package io.swapastack.dunetd.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import io.swapastack.dunetd.GameScreen;
import io.swapastack.dunetd.ObjectUtil.Enemies.BossEnemy;
import io.swapastack.dunetd.ObjectUtil.Enemies.Harvester;
import io.swapastack.dunetd.ObjectUtil.Enemies.Infantry;
import io.swapastack.dunetd.ObjectUtil.GameField.PathFindingAlgorithm;
import io.swapastack.dunetd.ObjectUtil.GameField.Graph;
import io.swapastack.dunetd.ObjectUtil.GameField.Node;
import io.swapastack.dunetd.ObjectUtil.Towers.BombTower;
import io.swapastack.dunetd.ObjectUtil.Towers.Cannon;
import io.swapastack.dunetd.ObjectUtil.Towers.SonicTower;
import io.swapastack.dunetd.UI.TowerPlacingUI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class UnitTest {
    @Test
    void testDijkstra() {
        int[][] GRID = {
                { 1, 1, 0, 0, 1 },
                {0, 1, 1, 0, 1 },
                { 1, 1, 0, 1, 1 },
                { 1, 1, 1, 0, 1 },
                { 1, 0, 1, 1, 1 },

        };
        LinkedList<Node> path = PathFindingAlgorithm.getPath(new Graph(GRID), new Node(new Vector2(1, 3)), new Node(new Vector2(3, 3)));
    }

    @Test
    void testSpiceCost() {
        Assertions.assertEquals(7, BossEnemy.reward);
    }

    @Test
    void testTowerPrice() {
        Assertions.assertEquals(2, BombTower.price);
        Assertions.assertEquals(1, Cannon.price);
        Assertions.assertEquals(5, SonicTower.price);
    }
}