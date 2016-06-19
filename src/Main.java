import kColoring.KColoring;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here

        String instancefile = "instances/DSJC500.5.col.txt";

        //Graph<Integer> g = new Graph<>();
        SimpleGraph<Integer, DefaultEdge> g = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        BufferedReader in;
        in = new BufferedReader(new FileReader(instancefile));
        String line = in.readLine();
        String[] r = line.split(" ");

        while(!r[0].equals( "p")){
            line = in.readLine();
            r = line.split(" ");
        }

        int nodeNum = Integer.parseInt(r[2]);
        int edgeNum = Integer.parseInt(r[3]);

        for(int i=1; i<=nodeNum; ++i){
            g.addVertex(i);
        }

        for(int i = 0; i < edgeNum; ++i){
            line = in.readLine();
            r = line.split(" ");

            Integer v1 = Integer.parseInt(r[1]);
            Integer v2 = Integer.parseInt(r[2]);
            g.addEdge(v1, v2);
            //System.out.println(v1 + "  " + v2);
        }


        KColoring<Integer> solver = new KColoring<>(g);

//        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
//
//        g.addVertex("a");
//        g.addVertex("b");
//        g.addVertex("c");
//        g.addVertex("d");
//        g.addVertex("e");
//        g.addEdge("a","b");
//        g.addEdge("a","e");
//        g.addEdge("d","b");
//        g.addEdge("c","b");
//        g.addEdge("d","c");
//        g.addEdge("e","d");
//
//        KColoring<String> solver = new KColoring<>(g);
        System.out.print(solver.solve(49));
    }
}
