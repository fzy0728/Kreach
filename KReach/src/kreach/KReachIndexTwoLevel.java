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
import java.util.Queue;
import temporary.Quintuple;
import temporary.Triple;

/**
 *
 * @author Helmond
 */
public class KReachIndexTwoLevel extends KReachIndex{
    
    private Quintuple<WeightedGraph,WeightedGraph,Graph,HashSet<Integer>,HashSet<Integer>> index;
    private final int b;

    public KReachIndexTwoLevel(Graph g, int k, int b) {
        super(g, k, 5);
        this.b = b;
    }

    @Override
    protected void construct2(Graph g) {
        HashSet<Integer> S = new HashSet<>();
        Graph D1 = new Graph();
        HashMap<DirectedEdge, Integer> w1 = new HashMap<>();
        int i = 0;
        DegreeStructure ds = new DegreeStructure(g);
        int mv = ds.popMax();
        while (i < b && mv != -1) {
            System.out.println("mv:"+mv);
            khopbfs(g, mv, k, S, D1, w1);
            khopbfs2(g, mv, k, S, D1, w1);
            S.add(mv);
            mv = ds.popMax();
            i++;
        }
        System.out.println("D1 done, building Gprime");
        Graph Gprime = new Graph();
        for (int v : g.vertices()) {
            if (!S.contains(v)) {
                Gprime.addVertex(v);
            }
        }
        for (DirectedEdge e : g.edges()) {
            if (S.contains(e.u) || S.contains(e.v)) {
                continue;
            }
            Gprime.addEdge(e.u, e.v);
        }
        Graph D2 = new Graph();
        HashSet<Integer> SPrime = new HashSet<>();
        HashMap<DirectedEdge, Integer> w2 = new HashMap<>();
        DegreeStructure dsPrime = new DegreeStructure(Gprime);
        int mvPrime = dsPrime.popMax();
        Runtime runtime = Runtime.getRuntime();
        int memreq = 3000;
        boolean enoughmem = (runtime.maxMemory()-runtime.totalMemory())/(1024*1024)>memreq;
        
        while (enoughmem && mvPrime !=-1)
        {
            khopbfs(Gprime, mvPrime, k, SPrime, D2, w2);
            khopbfs2(Gprime, mvPrime, k, SPrime, D2, w2);
            SPrime.add(mvPrime);
            mvPrime = ds.popMax();
            if(Math.random()>0.01)System.out.println((runtime.maxMemory()-runtime.totalMemory())/(1024*1024));
            enoughmem = (runtime.maxMemory()-runtime.totalMemory())/(1024*1024)>memreq;
        }
        System.out.println("D2 step 1 done, fine tuning now");
        HashSet<Integer> VD1capSprime = new HashSet<>(SPrime);
        VD1capSprime.retainAll(D1.vertices());
        System.out.println("Size of intersection; " + VD1capSprime.size());
        for (int u : VD1capSprime) {
            for (int v : VD1capSprime) {
                if (u == v) {
                    continue;
                }
                DirectedEdge uv = new DirectedEdge(u, v);
                if (D2.edges().contains(uv)) {
                    int weight2 = w2.get(uv);
                    int minw = weight2;
                    for (int x : S) {
                        for (int y : S) {

                            DirectedEdge ux = new DirectedEdge(u, x);
                            if (!w1.containsKey(ux)) {
                                continue;
                            }
                            int xw1 = w1.get(ux);
                            DirectedEdge xy = new DirectedEdge(x, y);
                            if (!w1.containsKey(xy) && x != y) {
                                continue;
                            }
                            int xw2 = (x == y ? 0 : w1.get(xy));
                            DirectedEdge yv = new DirectedEdge(y, v);
                            if (!w1.containsKey(yv)) {
                                continue;
                            }
                            int xw3 = w1.get(yv);
                            int thisweight = xw1 + xw2 + xw3;
                            minw = Math.min(minw, thisweight);
                        }
                    }
                    w2.put(uv, minw);
                } else {
                    int d = 999999999;
                    for (int x : S) {
                        for (int y : S) {
                            DirectedEdge ux = new DirectedEdge(u, x);
                            if (!w1.containsKey(ux)) {
                                continue;
                            }
                            int xw1 = w1.get(ux);
                            DirectedEdge xy = new DirectedEdge(x, y);
                            if (!w1.containsKey(xy) && x != y) {
                                continue;
                            }
                            int xw2 = (x == y ? 0 : w1.get(xy));
                            DirectedEdge yv = new DirectedEdge(y, v);
                            if (!w1.containsKey(yv)) {
                                continue;
                            }
                            int xw3 = w1.get(yv);
                            int thisweight = xw1 + xw2 + xw3;
                            d = Math.min(d, thisweight);
                        }
                    }
                    if (d <= k) {
                        Gprime.addEdge(u, v);
                        w2.put(uv, d);
                    }
                }
            }
        }
        System.out.println("Tuning done, staring constrution of D3");
        Graph D3 = new Graph();
        for (int v : Gprime.vertices()) {
            if (!SPrime.contains(v)) {
                D3.addVertex(v);
            }
        }
        for (DirectedEdge e : Gprime.edges()) {
            if (SPrime.contains(e.u) || SPrime.contains(e.v)) {
                continue;
            }
            D3.addEdge(e.u, e.v);
        }
        this.index = new Quintuple<>(new WeightedGraph(D1, w1), new WeightedGraph(D2, w2), D3, S, SPrime);
    }

    @Override
    protected boolean query2(int s, int t) {
        HashSet<Integer> S = index.k4;
        HashSet<Integer> Sprime = index.k5;
        WeightedGraph D1 = index.k1;
        WeightedGraph D2 = index.k2;
        Graph D3 = index.k3;
        boolean Ss = S.contains(s);
        boolean St = S.contains(t);
        if (Ss && St) {
            Case(1);
            return D1.getWeight(s, t)!=null;

        } else if (Ss || St) {
            Case(2);
            boolean t1 = D1.getWeight(s, t)!=null;
            if (t1) {
                return true;
            }
            for (int v : S) {
                Integer w1 = D1.getWeight(s, v);
                Integer w2 = D1.getWeight(v, t);
                if (w1 != null && w2 != null && w1 + w2 <= k) {
                    return true;
                }

            }
            return false;
        }
        boolean Sps = Sprime.contains(s);
        boolean Spt = Sprime.contains(t);
        
        if (Sps && Spt) {
            Case(3);
            if (D1.getWeight(s, t)!=null) {
                return true;
            }
            for (int u : S) {
                for (int v : S) {
                    Integer w1 = D1.getWeight(s, u);
                    Integer w2 = D1.getWeight(u, v);
                    Integer w3 = D1.getWeight(v, t);
                    if (w1 != null && w2 != null && w3 != null && w1 + w2 + w3 <= k) {
                        return true;
                    }
                }
            }
            return false;
            
        } else if (Sps || Spt) {
            Case(4);
            if (D1.getWeight(s, t)!=null) {
                return true;
            }
            for (int v : Sprime) {

                Integer w1 = D2.getWeight(s, v);
                Integer w2 = D2.getWeight(v, t);
                if (w1 != null && w2 != null && w1 + w2 <= k) {
                    return true;
                }
            }
            for (int u : S) {
                for (int v : S) {
                    Integer w1 = D1.getWeight(s, u);
                    Integer w2 = D1.getWeight(u, v);
                    Integer w3 = D1.getWeight(v, t);
                    if (w1 != null && w2 != null && w3 != null && w1 + w2 + w3 <= k) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            Case(5);
            for (int u : S) {
                for (int v : S) {
                    Integer w1 = D1.getWeight(s, u);
                    Integer w2 = D1.getWeight(u, v);
                    Integer w3 = D1.getWeight(v, t);
                    if (w1 != null && w2 != null && w3 != null && w1 + w2 + w3 <= k) {
                        return true;
                    }
                }
            }
            for (int u : Sprime) {
                for (int v : Sprime) {
                    Integer w1 = D2.getWeight(s, u);
                    Integer w2 = D2.getWeight(u, v);
                    Integer w3 = D2.getWeight(v, t);
                    if (w1 != null && w2 != null && w3 != null && w1 + w2 + w3 <= k) {
                        return true;
                    }
                }
            }
                return paralelBFS(D3,k,s,t);
        }
    }
    
        public static boolean algorithm5(Quintuple<WeightedGraph, WeightedGraph, Graph, HashSet<Integer>, HashSet<Integer>> scaleIndex, int k, int s, int t) {
        HashSet<Integer> S = scaleIndex.k4;
        HashSet<Integer> Sprime = scaleIndex.k5;
        WeightedGraph D1 = scaleIndex.k1;
        WeightedGraph D2 = scaleIndex.k2;
        Graph D3 = scaleIndex.k3;
        boolean Ss = S.contains(s);
        boolean St = S.contains(t);
        if (Ss && St) {
            return D1.getWeight(s, t)!=null && D1.getWeight(s, t)<=k;

        } else if (Ss || St) {
            boolean t1 = D1.getWeight(s, t)!=null && D1.getWeight(s, t)<=k;
            if (t1) {
                return true;
            }
            for (int v : S) {
                Integer w1 = D1.getWeight(s, v);
                Integer w2 = D1.getWeight(v, t);
                if (w1 != null && w2 != null && w1 + w2 <= k) {
                    return true;
                }

            }
            return false;
        }
        boolean Sps = Sprime.contains(s);
        boolean Spt = Sprime.contains(t);
        
        if (Sps && Spt) {
            if (D1.getWeight(s, t)!=null&& D2.getWeight(s, t)<=k) {
                return true;
            }
            for (int u : S) {
                for (int v : S) {
                    Integer w1 = D1.getWeight(s, u);
                    Integer w2 = D1.getWeight(u, v);
                    Integer w3 = D1.getWeight(v, t);
                    if (w1 != null && w2 != null && w3 != null && w1 + w2 + w3 <= k) {
                        return true;
                    }
                }
            }
            return false;
            
        } else if (Sps || Spt) {
            if (D1.getWeight(s, t)!=null&& D2.getWeight(s, t)<=k) {
                return true;
            }
            for (int v : Sprime) {

                Integer w1 = D2.getWeight(s, v);
                Integer w2 = D2.getWeight(v, t);
                if (w1 != null && w2 != null && w1 + w2 <= k) {
                    return true;
                }
            }
            for (int u : S) {
                for (int v : S) {
                    Integer w1 = D1.getWeight(s, u);
                    Integer w2 = D1.getWeight(u, v);
                    Integer w3 = D1.getWeight(v, t);
                    if (w1 != null && w2 != null && w3 != null && w1 + w2 + w3 <= k) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            for (int u : S) {
                for (int v : S) {
                    Integer w1 = D1.getWeight(s, u);
                    Integer w2 = D1.getWeight(u, v);
                    Integer w3 = D1.getWeight(v, t);
                    if (w1 != null && w2 != null && w3 != null && w1 + w2 + w3 <= k) {
                        return true;
                    }
                }
            }
            for (int u : Sprime) {
                for (int v : Sprime) {
                    Integer w1 = D2.getWeight(s, u);
                    Integer w2 = D2.getWeight(u, v);
                    Integer w3 = D2.getWeight(v, t);
                    if (w1 != null && w2 != null && w3 != null && w1 + w2 + w3 <= k) {
                        return true;
                    }
                }
            }
                return paralelBFS(D3,k,s,t);
        }
    }

    @Override
    protected Object getIndex() {
        return new Triple<>(index.k1.k1,index.k2.k1,index.k3);
    }
    
    
    private static void khopbfs(Graph g, int source, int k, HashSet<Integer> S, Graph d1, HashMap<DirectedEdge, Integer> w1) {
        Queue<Integer> Q = new LinkedList<>();
        HashMap<Integer, Integer> dist = new HashMap<>();
        HashMap<Integer, Integer> parents = new HashMap<>();
        Q.add(source);
        parents.put(source, source);
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
                    parents.put(v, u);
                }
            }
            if (S.contains(u)) {
                d1.addEdge(source, u);
                w1.put(new DirectedEdge(source, u), d);
            } else {
                Integer parent = parents.get(u);
                boolean noneinS = true;
                while (parent != source) {
                    if (S.contains(parent)) {
                        noneinS = false;
                        break;
                    }
                    parent = parents.get(parent);
                }
                if (noneinS) {
                    d1.addVertex(u);
                    d1.addEdge(source, u);
                    w1.put(new DirectedEdge(source, u), d);
                }
            }
        }

    }

    private static void khopbfs2(Graph g, int source, int k, HashSet<Integer> S, Graph d1, HashMap<DirectedEdge, Integer> w1) {
        Queue<Integer> Q = new LinkedList<>();
        HashMap<Integer, Integer> dist = new HashMap<>();
        HashMap<Integer, Integer> parents = new HashMap<>();
        Q.add(source);
        parents.put(source, source);
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
                    parents.put(v, u);
                }
            }
            if (S.contains(u)) {
                d1.addEdge(u, source);
                w1.put(new DirectedEdge(u, source), d);
            } else {
                int parent = parents.get(u);
                boolean noneinS = true;
                while (parent != source) {
                    if (S.contains(parent)) {
                        noneinS = false;
                        break;
                    }
                    parent = parents.get(parent);
                }
                if (noneinS) {
                    d1.addVertex(u);
                    d1.addEdge(u, source);
                    w1.put(new DirectedEdge(u, source), d);
                }
            }
        }

    }

    private static boolean paralelBFS(Graph D3, int k, int s, int t) {
        Graph g = D3;
        Graph g2 = Graph.inverseLayer(g);
        Queue<Integer> Q1 = new LinkedList<>();
        Queue<Integer> Q2 = new LinkedList<>();
        Q1.add(s);
        Q2.add(t);
        HashMap<Integer,Integer> dist1 = new HashMap<>(), dist2 = new HashMap<>();
        dist1.put(s, 0);
        dist2.put(t, 0);
        int halfk = (int)Math.ceil(k/2d);
        while(!Q1.isEmpty())
        {
            int u = Q1.poll();
            int d = dist1.get(u);
            if(d>=halfk)break;
            for(int v:g.out(u))
            {
                if(!dist1.containsKey(v)){
                dist1.put(v, d+1);
                Q1.add(v);}
            }
        }
        while(!Q2.isEmpty())
        {
            int u = Q2.poll();
            int d = dist2.get(u);
            if(d>=halfk)break;
            for(int v:g2.out(u))
            {if(dist2.containsKey(v))continue;
                dist2.put(v, d+1);
                Q2.add(v);
                if(dist1.keySet().contains(v))
                {
                    int d1 = dist1.get(v);
                    if(d1+d+1<=k)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
}