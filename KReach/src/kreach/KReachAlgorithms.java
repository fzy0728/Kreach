/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kreach;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import temporary.Triple;
import temporary.Tuple;

/**
 *
 * @author Helmond
 */
public class KReachAlgorithms {

    public static Tuple<Graph, HashMap<DirectedEdge, Integer>> computeOriginalKReachGraph(Graph g, int k) {
        Set<Integer> S = VertexCoverAlgorithms.computeBasic2AproxVertexCover(g);
        Graph I = new Graph();
        System.out.println("Found vertex cover of size" + S.size());
        HashMap<DirectedEdge, Integer> wI = new HashMap<>();
        int i = 0;
        for (Integer u : S) {
            if (i++ % 100 == 0) {
                System.out.println(i);
            }
            HashMap<Integer, Integer> Sku = BFSu(g, u, k);
            for (Map.Entry<Integer, Integer> e : Sku.entrySet()) {
                int d = e.getValue();
                int v = e.getKey();
                I.addEdge(u, v);
                if (d <= k - 2) {
                    wI.put(new DirectedEdge(u, v), k - 2);
                } else if (d < k - 1) {
                    wI.put(new DirectedEdge(u, v), k - 1);
                } else {
                    wI.put(new DirectedEdge(u, v), k);
                }
            }
        }
        return new Tuple<>(I, wI);
    }

    public static boolean queryKReach1(Graph g, int s, int t, Tuple<Graph, HashMap<DirectedEdge, Integer>> kreach, int k) {
        Graph gI = kreach.k1;
        HashMap<DirectedEdge, Integer> wI = kreach.k2;
        HashSet<Integer> VI = gI.vertices();
        HashSet<DirectedEdge> EI = gI.edges();
        System.out.println("query...");
        // case 1: both s and t are in the vertex cover
        if (VI.contains(s) && VI.contains(t)) {
            System.out.println("case 1");
            return EI.contains(new DirectedEdge(s, t));
        }
        // case 2: only s is in the vertex cover
        if (VI.contains(s) && !VI.contains(t)) {
            System.out.println("case 2");
            for (int v : g.in(t)) {
                DirectedEdge e = new DirectedEdge(s, v);
                if (EI.contains(e) && wI.get(e) <= k - 1) {
                    return true;
                }
            }
            return false;
        }

        // case 3: only t is in the vertex cover
        if (!VI.contains(s) && VI.contains(t)) {
            System.out.println("case 3");
            for (int v : g.out(s)) {
                DirectedEdge e = new DirectedEdge(v, t);
                if (EI.contains(e) && wI.get(e) <= k - 1) {
                    return true;
                }
            }
            return false;
        } // case 4: both s and t are not in the vertex cover
        else//(!VI.contains(s) && !VI.contains(t))
        {
            System.out.println("case 4");
            for (int u : g.out(s)) {

                for (int v : g.in(t)) {
                    DirectedEdge e = new DirectedEdge(u, v);
                    if (EI.contains(e) && wI.get(e) <= k - 2) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static HashMap<Integer, Integer> BFSu(Graph g, int source, int k) {
        Queue<Integer> Q = new LinkedList<>();
        HashMap<Integer, Integer> dist = new HashMap<>();
        Q.add(source);
        dist.put(source, 0);
        while (!Q.isEmpty()) {

            int u = Q.poll();
            int d = dist.get(u);
            if (d == k) {
                break;
            }
            List<Integer> N = g.out(u);
            for (Integer v : N) {
                if (!(dist.containsKey(v))) {
                    dist.put(v, d + 1);
                    Q.add(v);
                }
            }
        }
        return dist;

    }

    public static Triple<
            Tuple<Graph, HashMap<DirectedEdge, Integer>>, Tuple<Graph, HashMap<DirectedEdge, Integer>>, Graph> algorithm3(Graph g, int k, int b) {
        HashSet<Integer> S = new HashSet<>();
        Graph D1 = new Graph();
        HashMap<DirectedEdge,Integer> w1 = new HashMap<>();
        int i = 0;
        DegreeStructure ds = new DegreeStructure(g);
        int mv = ds.popMax();
        while (i < b && mv != -1) {
            khopbfs(g,mv,k,S,D1,w1);
            khopbfs2(g,mv,k,S,D1,w1);
            S.add(mv);
            mv = ds.popMax();
            i++;
        }
        Graph Gprime = new Graph();
        for(int v:g.vertices())
        {
            if(!S.contains(v))
                Gprime.addVertex(v);
        }
        for(DirectedEdge e:g.edges())
        {
            if(S.contains(e.u) || S.contains(e.v))continue;
            Gprime.addEdge(e.u, e.v);
        }
        Graph D2 = new Graph();
        HashSet<Integer> SPrime = new HashSet<>();
        HashMap<DirectedEdge,Integer> w2 = new HashMap<>();
        DegreeStructure dsPrime = new DegreeStructure(Gprime);
        int mvPrime = dsPrime.popMax();
        boolean enoughmem = Math.random()>0.01;
        while(enoughmem)//TODO
        {
            khopbfs(Gprime,mvPrime,k,SPrime,D2,w2);
            khopbfs(Gprime,mvPrime,k,SPrime,D2,w2);
            SPrime.add(mvPrime);
            mvPrime = ds.popMax();
            enoughmem = Math.random()>0.01;
        }
        return null;//TODO
    }

    public static void khopbfs(Graph g, int source, int k, HashSet<Integer> S, Graph d1, HashMap<DirectedEdge,Integer> w1) {
        Queue<Integer> Q = new LinkedList<>();
        HashMap<Integer, Integer> dist = new HashMap<>();
        HashMap<Integer,Integer> parents = new HashMap<>();
        Q.add(source);
        dist.put(source, 0);
        while (!Q.isEmpty()) {

            int u = Q.poll();
            int d = dist.get(u);
            if (d == k) {
                break;
            }
            List<Integer> N = g.out(u);
            for (Integer v : N) {
                if (!(dist.containsKey(v))) {
                    dist.put(v, d + 1);
                    Q.add(v);
                    parents.put(v,u);
                }
            }
            if(S.contains(u))
            {
                d1.addEdge(source, u);
                w1.put(new DirectedEdge(source,u), d);
            }else
            {
                int parent = parents.get(u);
                boolean noneinS = true;
                while(parent!=source)
                {
                   if(S.contains(parent)){noneinS = false;break;}
                   parent = parents.get(parent);
                }
                if(noneinS)
                {
                    d1.addVertex(u);
                    d1.addEdge(source, u);
                    w1.put(new DirectedEdge(source,u),d);
                }
            }
        }

    }
    public static void khopbfs2(Graph g, int source, int k, HashSet<Integer> S, Graph d1, HashMap<DirectedEdge,Integer> w1) {
        Queue<Integer> Q = new LinkedList<>();
        HashMap<Integer, Integer> dist = new HashMap<>();
        HashMap<Integer,Integer> parents = new HashMap<>();
        Q.add(source);
        dist.put(source, 0);
        while (!Q.isEmpty()) {

            int u = Q.poll();
            int d = dist.get(u);
            if (d == k) {
                break;
            }
            List<Integer> N = g.in(u);
            for (Integer v : N) {
                if (!(dist.containsKey(v))) {
                    dist.put(v, d + 1);
                    Q.add(v);
                    parents.put(v,u);
                }
            }
            if(S.contains(u))
            {
                d1.addEdge(u,source);
                w1.put(new DirectedEdge(u,source), d);
            }else
            {
                int parent = parents.get(u);
                boolean noneinS = true;
                while(parent!=source)
                {
                   if(S.contains(parent)){noneinS = false;break;}
                   parent = parents.get(parent);
                }
                if(noneinS)
                {
                    d1.addVertex(u);
                    d1.addEdge(u,source);
                    w1.put(new DirectedEdge(u,source),d);
                }
            }
        }

    }
}