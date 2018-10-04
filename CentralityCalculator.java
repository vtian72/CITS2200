import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * A class to calculate various centrality measures of a graph.
 * 
 * @author Haoyu Tian (22262122) and Dhaval Vaghjiani (22258431)
 */

public class CentralityCalculator {

	public static int vertices;

	/**
	 * Initialises the number of vertices in a graph.
	 * 
	 * @param g:
	 *            graph
	 */

	public CentralityCalculator(Graph g) {
		vertices = g.getNumberOfVertices();
	}

	/**
	 * Calculates the degree centrality of each vertex in the graph.
	 * 
	 * @param g:
	 *            graph
	 * @return a HashMap with key as vertex and value as its degree centrality.
	 * @throws IOException
	 */

	public static HashMap<Integer, Integer> degreeCentrality(Graph g) throws IOException {
		HashMap<Integer, Integer> degreeCentrality = new HashMap<>();
		for (int vertex : g.adjList.keySet()) {
			degreeCentrality.put(vertex, g.adjList.get(vertex).size());
		}
		return degreeCentrality;
	}

	/**
	 * Calculates the closeness centrality of each vertex in the graph.
	 * 
	 * @param g:
	 *            graph
	 * @return a HashMap with key as vertex and value as its closeness centrality.
	 * @throws IOException
	 */

	public static HashMap<Integer, Float> closenessCentrality(Graph g) throws IOException {
		HashMap<Integer, Float> result = new HashMap<>();
		HashMap<Integer, Integer> map = g.getMappingHashMap();

		for (int i = 0; i < g.getNumberOfVertices(); i++) {
			HashMap<Integer, Integer> distances = getDistances(g, map.get(i));
			float sum = 0;
			for (int temp : distances.values()) {
				sum = sum + temp;
			}
			float value = (float) 1.0 / sum;
			result.put(map.get(i), value);
		}
		return result;
	}

	/**
	 * Calculates the betweenness centrality of each vertex in the graph.
	 * 
	 * @param g:
	 *            graph
	 * @return a HashMap with key as vertex and value as its betweenness centrality.
	 * @throws IOException
	 */

	public static HashMap<Integer, Double> betweenessCentrality(Graph g) throws IOException {

		HashMap<Integer, Double> betCentrality = new HashMap<>();
		HashMap<Integer, LinkedList<Integer>> adjList = g.getAdjacencyList();
		HashMap<Integer, Integer> map = g.getMappingHashMap();
		for (int temp : adjList.keySet()) {
			betCentrality.put(temp, 0.0);
		}
		for (int i = 0; i < g.getNumberOfVertices(); i++) {
			Stack<Integer> s = new Stack<>();
			Queue<Integer> q = new LinkedList<>();
			HashMap<Integer, LinkedList<Integer>> l = new HashMap<>();

			HashMap<Integer, Double> numShortestPaths = new HashMap<>();

			for (int temp : adjList.keySet()) {
				numShortestPaths.put(temp, 0.0);
			}
			numShortestPaths.put(map.get(i), 1.0);
			HashMap<Integer, Integer> distance = new HashMap<>();
			for (int temp : adjList.keySet()) {
				distance.put(temp, Integer.MAX_VALUE);
			}
			q.add(map.get(i));
			distance.put(map.get(i), 0);
			for (int temp : adjList.keySet()) {
				l.put(temp, new LinkedList<>());
			}
			while (!q.isEmpty()) {
				int removed = q.remove();
				s.add(removed);
				for (int j = 0; j < g.getNumberOfVertices(); j++) {
					if (adjList.get(removed).contains(map.get(j))) {
						if (distance.get(map.get(j)) == Integer.MAX_VALUE) {
							q.add(map.get(j));
							numShortestPaths.put(map.get(j), numShortestPaths.get(removed));
							distance.put(map.get(j), distance.get(removed) + 1);
							LinkedList<Integer> parent = new LinkedList<>();
							parent.add(removed);
							l.put(map.get(j), parent);
						} else if (distance.get(map.get(j)) == distance.get(removed) + 1) {
							numShortestPaths.put(map.get(j),
									numShortestPaths.get(map.get(j)) + numShortestPaths.get(removed));
							@SuppressWarnings("unchecked")
							LinkedList<Integer> parent = (LinkedList<Integer>) l.get(map.get(j)).clone();
							parent.add(removed);
							l.put(map.get(j), parent);
						}
					}
				}
			}

			HashMap<Integer, Double> dependency = new HashMap<>();
			for (Integer node : adjList.keySet()) {
				dependency.put(node, 0.0);
			}
			double betweeness = 0;
			while (!s.isEmpty()) {
				int temp = s.pop();
				for (int j = 0; j < l.get(temp).size(); j++) {
					int val = l.get(temp).get(j);
					double answer = 0;
					answer = dependency.get(val)
							+ ((numShortestPaths.get(val)) / numShortestPaths.get(temp)) * (1 + dependency.get(temp));
					dependency.put(val, answer);
				}
				if (temp != map.get(i)) {
					betweeness = betCentrality.get(temp) + dependency.get(temp);
					betCentrality.put(temp, betweeness);
				}
			}
		}
		for (int a : betCentrality.keySet()) {
			double b = betCentrality.get(a);
			betCentrality.put(a, b / 2);
		}
		return betCentrality;
	}

	/**
	 * Calculates the katz centrality of each vertex in the graph.
	 * 
	 * @param g:
	 *            graph
	 * @param alpha:
	 *            attenuation factor
	 * @return a HashMap with key as vertex and value as its katz centrality.
	 * @throws IOException
	 */

	public static HashMap<Integer, Double> katzCentrality(Graph g, double alpha) throws IOException {
		HashMap<Integer, Double> katzCentrality = new HashMap<>();
		HashMap<Integer, Integer> map = g.getMappingHashMap();
		for (int i = 0; i < map.size(); i++) {
			double katz = 0;
			HashMap<Integer, Integer> distance = getDistances(g, map.get(i));
			for (int j = 0; j < map.size(); j++) {
				if (distance.get(map.get(j)) <= 0) {
					continue;
				}
				katz += Math.pow(alpha, distance.get(map.get(j)));
			}
			katzCentrality.put(map.get(i), katz);
		}
		return katzCentrality;
	}

	/**
	 * Helper method that returns a HashMap listing the shortest path distances
	 * between the startVertex and all other vertices.
	 * 
	 * @param g:
	 *            graph
	 * @param startVertex:
	 *            start vertex
	 * @return a HashMap listing the shortest path distances between the startVertex
	 *         and all other vertices.
	 * @throws IOException
	 */
	public static HashMap<Integer, Integer> getDistances(Graph g, int startVertex) throws IOException {

		HashMap<Integer, Integer> distance = new HashMap<>();
		HashMap<Integer, Integer> map = g.getMappingHashMap();
		for (int i = 0; i < g.getNumberOfVertices(); i++) {
			distance.put(map.get(i), -1);
		}
		distance.put(startVertex, 0);
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.offer(startVertex);

		while (queue.peek() != null) {
			int vertex = queue.remove();
			LinkedList<Integer> adjacentVertices = g.adjList.get(vertex);
			for (int j = 0; j < adjacentVertices.size(); j++) {
				if (adjacentVertices.get(j) == startVertex) {
					continue;
				}
				if (distance.get(adjacentVertices.get(j)) == -1) {
					queue.offer(adjacentVertices.get(j));
					distance.put(adjacentVertices.get(j), distance.get(vertex) + 1);
				}
			}
		}
		return distance;
	}

	/**
	 * Main method.
	 * 
	 * @param args[0]:
	 *            filepath
	 * @param args[1]:
	 *            name of centrality measure
	 * @param args[2]:
	 *            alpha value
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Graph g = new Graph(args[0]);
		ArrayList<Graph> sepGraphs = g.separateGraphs();
		String centralityType = args[1];
		float alphaValue = Float.valueOf(args[2]);
		System.out.println("\nTop 5 vertices in each connected component by " + centralityType + " centrality:");
		for (int i = 0; i < sepGraphs.size(); i++) {
			System.out.println("\nComponent " + (i + 1) + ": \n");

			if (centralityType.equals("katz")) {

				long start_time = System.nanoTime();
				HashMap<Integer, Double> results = katzCentrality(sepGraphs.get(i), alphaValue);
				long end_time = System.nanoTime();
				double difference = (end_time - start_time) / 1e6;
				PriorityQueue<Integer> pQueue = new PriorityQueue<>(sepGraphs.get(i).getNumberOfVertices(),
						new Comparator<Integer>() {
							public int compare(Integer o1, Integer o2) {
								if (results.get(o1) < results.get(o2))
									return +1;
								if (results.get(o1).equals(results.get(o2)))
									return 0;
								return -1;
							}
						});
				for (Integer j : results.keySet()) {
					pQueue.add(j);
				}
				for (int k = 0; k < 5; k++) {
					System.out.println(pQueue.poll());
				}
				System.out.println("\nThe calculation took " + difference + " millisecs");

			} else if (centralityType.equals("degree")) {

				long start_time = System.nanoTime();
				HashMap<Integer, Integer> results = degreeCentrality(sepGraphs.get(i));
				long end_time = System.nanoTime();
				double difference = (end_time - start_time) / 1e6;

				PriorityQueue<Integer> pQueue = new PriorityQueue<>(sepGraphs.get(i).getNumberOfVertices(),
						new Comparator<Integer>() {
							public int compare(Integer o1, Integer o2) {
								if (results.get(o1) < results.get(o2))
									return +1;
								if (results.get(o1).equals(results.get(o2)))
									return 0;
								return -1;
							}
						});
				for (Integer j : results.keySet()) {
					pQueue.add(j);
				}
				for (int k = 0; k < 5; k++) {
					System.out.println(pQueue.poll());
				}
				System.out.println("\nThe calculation took " + difference + " millisecs");

			}

			else if (centralityType.equals("closeness")) {

				long start_time = System.nanoTime();
				HashMap<Integer, Float> results = closenessCentrality(sepGraphs.get(i));
				long end_time = System.nanoTime();
				double difference = (end_time - start_time) / 1e6;
				PriorityQueue<Integer> pQueue = new PriorityQueue<>(sepGraphs.get(i).getNumberOfVertices(),
						new Comparator<Integer>() {
							public int compare(Integer o1, Integer o2) {
								if (results.get(o1) < results.get(o2))
									return +1;
								if (results.get(o1).equals(results.get(o2)))
									return 0;
								return -1;
							}
						});
				for (Integer j : results.keySet()) {
					pQueue.add(j);
				}
				for (int k = 0; k < 5; k++) {
					System.out.println(pQueue.poll());
				}
				System.out.println("\nThe calculation took " + difference + " millisecs");

			}

			else if (centralityType.equals("betweenness")) {
				long start_time = System.nanoTime();

				HashMap<Integer, Double> results = betweenessCentrality(sepGraphs.get(i));
				long end_time = System.nanoTime();
				double difference = (end_time - start_time) / 1e6;
				PriorityQueue<Integer> pQueue = new PriorityQueue<>(sepGraphs.get(i).getNumberOfVertices(),
						new Comparator<Integer>() {
							public int compare(Integer o1, Integer o2) {
								if (results.get(o1) < results.get(o2))
									return +1;
								if (results.get(o1).equals(results.get(o2)))
									return 0;
								return -1;
							}
						});
				for (Integer j : results.keySet()) {
					pQueue.add(j);
				}
				for (int k = 0; k < 5; k++) {
					System.out.println(pQueue.poll());
				}
				System.out.println("\nThe calculation took " + difference + " millisecs");

			} else {
				System.out.println("Invalid centrality type. Please try again.");
			}

		}

	}

}
