import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

/**
 * A class to generate and store a graph from a text file
 * 
 * @author Haoyu Tian (22262122) and Dhaval Vaghjiani (22258431)
 */
public class Graph {

	public int numberOfVertices;
	public HashMap<Integer, LinkedList<Integer>> adjList;

	public HashMap<Integer, Boolean> marked;
	public HashMap<Integer, Integer> id;
	public int count;

	/**
	 * Constructor for making a graph manually.
	 * 
	 * @param vertices:
	 *            the number of vertices in the graph
	 */
	public Graph(int vertices) {
		numberOfVertices = vertices;
		adjList = new HashMap<>();
	}

	/**
	 * Constructor for making a graph from a text file.
	 * 
	 * @param filepath:
	 *            path to file from which the graph will be generated
	 * @throws IOException:
	 *             if file not found.
	 */

	public Graph(String filepath) throws IOException {
		numberOfVertices = getUniqueVertices(filepath).size();
		adjList = new HashMap<>();

		int token1 = 0;
		int token2 = 0;

		Scanner inFile1 = new Scanner(new File(filepath));
		while (inFile1.hasNext()) {
			token1 = inFile1.nextInt();
			token2 = inFile1.nextInt();
			if (!edgeExist(token1, token2)) {
				addEdge(token1, token2);
			}
		}
		inFile1.close();

	}

	/**
	 * Returns the adjacency list of the graph
	 * 
	 * @return a HashMap representing the adjacency list
	 * @throws IOException
	 */
	public HashMap<Integer, LinkedList<Integer>> getAdjacencyList() throws IOException {
		return adjList;
	}

	/**
	 * Method that returns an ArrayList of the unique vertices in the given file.
	 * 
	 * @param filepath
	 *            : path to file from which the unique vertices will be found.
	 * @return an ArrayList of the unique vertices in the file.
	 * @throws IOException:
	 *             if file is not found.
	 */
	public ArrayList<Integer> getUniqueVertices(String filepath) throws IOException {

		int token1 = 0;
		Scanner inFile1 = new Scanner(new File(filepath));
		ArrayList<Integer> temps = new ArrayList<Integer>();
		Set<Integer> tempsSet = new HashSet<>();

		while (inFile1.hasNext()) {
			token1 = inFile1.nextInt();
			temps.add(token1);
		}
		inFile1.close();

		tempsSet.addAll(temps);
		temps.clear();
		temps.addAll(tempsSet);
		Collections.sort(temps);

		return temps;
	}

	/**
	 * Method that prints out the edges from the given file. Used for testing
	 * purposes.
	 * 
	 * @param filepath
	 * @throws IOException
	 */

	public void getEdges(String filepath) throws IOException {
		int token1 = 0;
		int token2 = 0;

		Scanner inFile1 = new Scanner(new File(filepath));
		while (inFile1.hasNext()) {
			token1 = inFile1.nextInt();
			token2 = inFile1.nextInt();
			String str = "edge between " + token1 + "and " + token2 + "\n";
			System.out.println(str);
		}
		inFile1.close();
	}

	/**
	 * Creates a HashMap that maps a label to each vertex in the file.
	 * 
	 * @param filepath
	 * @return a HashMap that maps between a label and vertex
	 * @throws IOException
	 */
	public HashMap<Integer, Integer> getMappingHashMap() throws IOException {
		HashMap<Integer, Integer> mappingHashMap = new HashMap<>();
		ArrayList<Integer> vertices = new ArrayList<>();
		vertices.addAll(adjList.keySet());
		for (int i = 0; i < adjList.keySet().size(); i++) {
			mappingHashMap.put(i, vertices.get(i));
		}
		return mappingHashMap;
	}

	/**
	 * Returns number of vertices in the graph.
	 * 
	 * @return number of vertices in the graph.
	 */
	public int getNumberOfVertices() {
		return numberOfVertices;
	}

	/**
	 * Adds an edge between two vertices in the graph.
	 * 
	 * @param vertex1:
	 *            first vertex
	 * @param vertex2:
	 *            second vertex
	 */

	public void addEdge(int vertex1, int vertex2) {
		if (!adjList.containsKey(vertex1)) {
			LinkedList<Integer> list = new LinkedList<>();
			list.add(vertex2);
			adjList.put(vertex1, list);
		} else {
			LinkedList<Integer> current = adjList.get(vertex1);
			current.add(vertex2);
			adjList.put(vertex1, current);
		}
		if (!adjList.containsKey(vertex2)) {
			LinkedList<Integer> list = new LinkedList<>();
			list.add(vertex1);
			adjList.put(vertex2, list);
		} else {
			LinkedList<Integer> current = adjList.get(vertex2);
			current.add(vertex1);
			adjList.put(vertex2, current);
		}
	}

	/**
	 * Splits a graph into its connected subgraphs.
	 * 
	 * @return an ArrayList containing the separate subgraphs.
	 * @throws IOException
	 */
	public ArrayList<Graph> separateGraphs() throws IOException {
		HashMap<Integer, Integer> map = getMappingHashMap();

		marked = new HashMap<>();
		id = new HashMap<>();
		for (int i : map.values()) {
			marked.put(i, false);
		}
		for (int v : map.values()) {
			if (!marked.get(v)) {
				dfs(this, v);
				count++;
			}
		}
		ArrayList<Graph> listOfGraphs = new ArrayList<Graph>();
		HashMap<Integer, Integer> nVerticesComponent = new HashMap<>();
		for (int i : id.values()) {
			if (!nVerticesComponent.keySet().contains(i)) {
				nVerticesComponent.put(i, 1);
			} else {
				nVerticesComponent.put(i, nVerticesComponent.get(i) + 1);
			}
		}
		for (int i : nVerticesComponent.keySet()) {
			ArrayList<Integer> verticesInComp = new ArrayList<Integer>();
			for (int k : id.keySet()) {
				if (id.get(k) == i) {
					verticesInComp.add(k);
				}
			}
			Graph comp = new Graph(nVerticesComponent.get(i));
			HashMap<Integer, LinkedList<Integer>> tempAdjList = new HashMap<Integer, LinkedList<Integer>>();
			for (int vert : verticesInComp) {
				tempAdjList.put(vert, adjList.get(vert));
			}
			comp.adjList = tempAdjList;
			listOfGraphs.add(comp);
		}
		return listOfGraphs;
	}

	/**
	 * Implements a Depth-First-Search on a graph with start vertex v.
	 * 
	 * @param g:
	 *            graph
	 * @param v:
	 *            start vertex
	 * @throws IOException
	 */
	public void dfs(Graph g, int v) throws IOException {
		HashMap<Integer, Integer> map = g.getMappingHashMap();
		marked.put(v, true);
		id.put(v, count);
		for (int n : map.values()) {
			if (g.adjList.get(v).contains(n)) {
				if (!marked.get(n)) {
					dfs(g, n);
				}
			}
		}
	}

	/**
	 * Checks if an edge between two vertices exist in a graph.
	 * 
	 * @param vertex1:
	 *            first vertex
	 * @param vertex2:
	 *            second vertex
	 * @return true if edge exists, else false
	 */
	public boolean edgeExist(int vertex1, int vertex2) {
		if (adjList.containsKey(vertex1)) {
			if (adjList.get(vertex1).contains(vertex2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns number of connected components in graph.
	 * 
	 * @return number of connected components in a graph.
	 */

	public int count() {
		return count;
	}
}
