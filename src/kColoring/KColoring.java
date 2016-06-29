package kColoring;

import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Xavier on 16/6/7.
 */
public class KColoring<V> {

    private SimpleGraph<V, DefaultEdge> graph;
    private HashMap<V, Integer> vertexIndexMap = new HashMap<>();
    private HashMap<Integer, V> indexVertexMap = new HashMap<>();
    private boolean[][] simpleGraph;
    private int K;
    private int[] vColors;
    private int[][] conflictTable;

    private int f;
    private int bestV;
    private int bestC;
    private int bestD;

    private int iter;
    private int[][] tabuTable;
    private int sameCount;

    private int everBestF;
    private int[] bakVColors;
    private int[][] bakConflictTable;
    private int failCount = 0;
    private boolean reachLocalOpt = false;

    /***********************parameters*********************************/
    private Random random = new Random();
//    private int TABU_BASE = 15;
//    private int TABU_MAX = 20;
    private int MAX_FAIL_COUNT = 100000;
    /***********************parameters*********************************/


//    public void setTabuT(int base, int max){
//        TABU_BASE = base;
//        TABU_MAX = max;
//    }

    private void rollBack(){
        System.arraycopy(bakVColors, 0, vColors, 0, vColors.length);

        for(int i=0; i<conflictTable.length; ++i){
            System.arraycopy(bakConflictTable[i], 0, conflictTable[i], 0, conflictTable[i].length);
        }
    }

    private void backUp(){
        System.arraycopy(vColors, 0, bakVColors, 0, bakVColors.length);

        for(int i=0; i<conflictTable.length; ++i){
            System.arraycopy(conflictTable[i], 0, bakConflictTable[i], 0, bakConflictTable[i].length);
        }

    }

    public KColoring(SimpleGraph<V, DefaultEdge> g){
        graph = g;

        int index=0;
        for(V v : g.vertexSet()){
            vertexIndexMap.put(v, index);
            indexVertexMap.put(index, v);
            ++index;
        }

        simpleGraph = new boolean[g.vertexSet().size()][g.vertexSet().size()];
        for(DefaultEdge e : graph.edgeSet()){
            int v = vertexIndexMap.get(graph.getEdgeSource(e));
            int u = vertexIndexMap.get(graph.getEdgeTarget(e));
            simpleGraph[v][u] = true;
            simpleGraph[u][v] = true;
        }
    }

    public void init(){
        vColors = new int[graph.vertexSet().size()];
        bakVColors = new int[graph.vertexSet().size()];
        for(int i=0; i<vColors.length; ++i){
            vColors[i] = random.nextInt(K);
        }
        calcConflictTable();
        f = calcF();
        everBestF = f;
        tabuTable = new int[graph.vertexSet().size()][K];
    }

    private int calcF(){
        int conflicts = 0;
        for(int v=0; v<graph.vertexSet().size()-1; ++v){
            for(int u=v+1; u<graph.vertexSet().size(); ++u){
                if(simpleGraph[v][u] && vColors[v] == vColors[u]){
                    ++conflicts;
                }
            }
        }
        return conflicts;
    }

    private void calcConflictTable(){
        conflictTable = new int[graph.vertexSet().size()][K];
        bakConflictTable = new int[graph.vertexSet().size()][K];
        for(int v=0; v<graph.vertexSet().size(); ++v){
            for(int u=0; u<graph.vertexSet().size(); ++u){
                if(simpleGraph[v][u]){
                    ++conflictTable[v][vColors[u]];
                }
            }
        }
    }

    private void findMove(){
        bestV = -1;
        bestC = -1;
        bestD = Integer.MAX_VALUE;

        for(int v=0; v < conflictTable.length; ++v){
            int currConflicts = conflictTable[v][vColors[v]];
            if(currConflicts == 0)continue;
            for(int c=0; c < conflictTable[v].length; ++c){
                if(c==vColors[v])continue;
                int delta = conflictTable[v][c] - currConflicts;
                if(tabuTable[v][c] <= iter) {
                    if (delta < bestD) {
                        bestV = v;
                        bestC = c;
                        bestD = delta;
                        sameCount = 1;
                    }else if(delta == bestD && random.nextInt(++sameCount)==0){
                        bestV = v;
                        bestC = c;
                    }
                }else{
                    if(delta < bestD && f + delta < everBestF){
                        bestV = v;
                        bestC = c;
                        bestD = delta;
                        sameCount = 1;
                    }
                }
            }
        }
    }

    private void makeMove(){
        int oriC = vColors[bestV];

        vColors[bestV] = bestC;
        f += bestD;

        if(bestD >= 0){
            reachLocalOpt = true;
        }

        if(f < everBestF){
            everBestF = f;
            backUp();
        }else if(reachLocalOpt){
            ++failCount;
        }

        for(int u=0; u<conflictTable.length; u++){
            if(!simpleGraph[bestV][u])continue;
            --conflictTable[u][oriC];
            ++conflictTable[u][bestC];
        }
        ++iter;
        //tabuTable[bestV][oriC] = iter + TABU_BASE + random.nextInt(TABU_MAX-TABU_BASE);
        tabuTable[bestV][oriC] = iter + f + random.nextInt(10);
    }

    private void perturbation(int strength){

        //System.out.println("Perturbating...");
        rollBack();
        f = everBestF;
        for(int i=0; i<strength;++i) {
            int v = random.nextInt(vColors.length);
            int c = random.nextInt(K);
            //int d = conflictTable[v][c] - conflictTable[v][vColors[c]];
            vColors[v] = c;
        }
        f = calcF();
        calcConflictTable();
        failCount = 0;
        reachLocalOpt = false;
    }

    private void localSearch(int maxIter){
        iter = 0;
        while(true){
            findMove();
            //System.out.println(iter);
            makeMove();

            if(iter % 200000 == 0){
                System.out.println("Iter: " + iter + ". Current f: " + f + ". Best f: " + everBestF);
//                if(f != calcF()){
//                    throw new Error("err...");
//                }
            }
            if(f==0)break;
            if(iter >= maxIter)break;

            if(failCount >= MAX_FAIL_COUNT){
                perturbation(vColors.length/3);
            }
        }
    }

    public boolean solve(int k, int maxIterations){
        K = k;
        init();
        localSearch(maxIterations);
        return f==0;
    }

    public ArrayList<ArrayList<V>> getConflictVertexes(){
        ArrayList<ArrayList<V>> conflictGroup = new ArrayList<>();

        for(int i=0; i<K; ++i){
            conflictGroup.add(new ArrayList<>());
        }

        for(int v=0; v < conflictTable.length; ++v){
            if(conflictTable[v][vColors[v]] > 0){
                conflictGroup.get(vColors[v]).add(indexVertexMap.get(v));
            }
        }

        return conflictGroup;
    }

}
