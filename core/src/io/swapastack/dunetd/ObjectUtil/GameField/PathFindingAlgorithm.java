package io.swapastack.dunetd.ObjectUtil.GameField;

import io.swapastack.dunetd.ObjectUtil.Enemies.Enemy;

import java.util.*;

public class PathFindingAlgorithm {
    /** Dijkstra shortest path algorithm.
     * @return A list with all the tiles an enemy has to visit to get from start to end.
     * @see Enemy **/
    public static LinkedList<Node> getPath(Graph graph, Node source, Node end){
        HashSet<Node> nodes = new HashSet<>();
        HashMap<Node, Integer> dist = new HashMap<>();
        HashMap<Node, Node> prev = new HashMap<>();
        for (Node n : graph.getNodes()){
            dist.put(n, Integer.MAX_VALUE);
            prev.put(n, null);
        }

        nodes = graph.getNodes();
        dist.replace(source,0);

        while(!nodes.isEmpty()){ //n
            Node u = findMin(dist, nodes);
            nodes.remove(u); //n

            if(u == null ||u.equals(end))
                break;

            for(Node v : u.getNeighbors()){
                if (!nodes.contains(v)) //n
                    continue;

                int alt = dist.get(u) + 1;
                if (alt < dist.get(v)){
                    dist.replace(v,alt);
                    prev.replace(v,u);
                }
            }
        }

        LinkedList<Node> path = new LinkedList<>();
        Node u = end;
        if (prev.get(u) != null || u.equals(source)){
            while (u != null){
                path.addFirst(u);
                u = prev.get(u);
            }
        }
        return path;
    }

    public static Node findMin(HashMap<Node,Integer> map, HashSet<Node> nodes){
        int currentMin = Integer.MAX_VALUE;
        Node returnNode = null;

        for (Map.Entry<Node, Integer> entry : map.entrySet()){
            if (entry.getValue() < currentMin && nodes.contains(entry.getKey())){
                currentMin = entry.getValue();
                returnNode = entry.getKey();
            }
        }
        return returnNode;
    }
}
