package edu.uic.ai.puzzle;

import java.util.List;

import java.util.Stack;

public class TreeSearch {

	private int[][] goal_state = new int[4][4]; // Target goal configuration
	private int nodeCount = 0; // Number of Nodes Expanded
	private String problem = null;
	private Heuristic heuristic = null;

	public TreeSearch(String initialProblem) {
		this.problem = initialProblem;
		initGoalState();
	}

	public TreeSearch() {
		initGoalState();
	}

	public void initGoalState() {
		int tileNumber = 1;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (i == 3 && j == 3) {
					goal_state[i][j] = 0;
				} else
					goal_state[i][j] = tileNumber;
				tileNumber++;
			}
		}
	}

	/**
	 * Recursive a star Search
	 * 
	 * @param node
	 * @param limit
	 * @return
	 */

	public Object recursive_IDA(Stack<Node> path, int g, int bound) {

		//Get the current node from the path.
		Node node = path.peek();
		node.setG(g);
		node.setH(getHeuristic(node));
		node.setF(g + node.getH());

		//Validate bound
		if (node.getF() > bound) {
			return node.getF();
		}

		// if passed the test return Solution as Node
		if (goal_test(node.getStateArray())) {
			return node;
		}

		int min = Integer.MAX_VALUE; // Infinity

		nodeCount++;

		// Getting the list of actions available per Node
		List<Action> actions = node.getActions();

		for (Action action : actions) {

			Node child = childNode(node, action);
			//child.setPathLenght(getPath(child));
			if (!path.contains(child)) {

				path.add(child);

				int newCost = node.getG() + 1;// child.getPathLenght();
				Object result = recursive_IDA(path, newCost, bound);

				if (result instanceof Node) {// Found
					return result;
				}

				if (result instanceof Integer) {
					int result_f_cost = ((Integer) result).intValue();
					if (result_f_cost < min) {
						min = result_f_cost;
					}
				}
				path.pop();
			}
		}
		return Integer.valueOf(min);

	}

	/**
	 * Iterative Deepening A star Search
	 * 
	 * @param problem
	 * @return
	 */
	public Object ida_star(String problem) {

		Object result = null;

		// create node with problem. Making a node based on initial state
		Node initialNode = new Node(); 
		initialNode.setState(problem);
		initialNode.parseState(); // Parse from string to 2-D Array.

		//Heuristic calculation of the initial node
		int bound = getHeuristic(initialNode);
		
		//search path with initial node added
		Stack<Node> path = new Stack<Node>();
		path.add(initialNode);

		do {
			result = recursive_IDA(path, 0, bound);

			if (result instanceof Node) {	// if found node, return with all data
				return result;
			}
			if (result instanceof Integer) {
				if (((Integer) result) == Integer.MAX_VALUE) {
					return result;
				}
			}
			bound = (Integer) result;
		} while (bound != Integer.MAX_VALUE);

		return null;
	}

	/**
	 * Validate Heuristic Type
	 * 
	 * @param node containing the Heuristic and the State(array)
	 * @return Number of Steps
	 */
	public int getHeuristic(Node node) {
		int number_steps = 0;

		switch (this.getHeuristic()) {
		case MISPLACED_TILES:
			number_steps = getMisplacedTilesHeuristic(node.getStateArray());
			break;

		default: // MANHATTAN_DISTANCE
			number_steps = getManhattanHeuristic(node.getStateArray());
			break;
		}
		return number_steps;
	}

	/**
	 * Calculate Number based on Misplaced Tiles
	 * 
	 * @param nodeState
	 * @return number of steps
	 */
	public int getMisplacedTilesHeuristic(int[][] nodeState) {

		int number_misplaced_tiles = 0;
		// Iterate over the matrix to compare if each tiles has correct number
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (nodeState[i][j] != 0 && nodeState[i][j] != goal_state[i][j]) {
					number_misplaced_tiles++;
				}
			}
		}
		return number_misplaced_tiles;
	}

	/**
	 * Calculate the number based on Manhattan Heuristic
	 * 
	 * @param node state
	 * @return number of steps
	 */
	public int getManhattanHeuristic(int[][] nodeState) {

		int current_value, i_index_goal, j_index_goal, moves_i_axes, moves_j_axes, tile_moves, total_moves = 0;

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {

				// Getting value of current position(i,j) and its goal position
				current_value = nodeState[i][j];

				if (current_value == 0) { // if 0 goal position must be 3,3
					i_index_goal = j_index_goal = 3;

				} else { // otherwise calculate position in a 4x4 matrix
					i_index_goal = (current_value / 4);
					j_index_goal = (current_value % 4) - 1;
				}

				// Calculating moves for individual current tile
				moves_i_axes = Math.abs((i - i_index_goal));
				moves_j_axes = Math.abs((j - j_index_goal));
				tile_moves = moves_i_axes + moves_j_axes;

				// Adding up this moves to the total of moves
				total_moves += tile_moves;
			}
		}
		return total_moves;
	}

	/**
	 * Return the length from the given node to the root
	 * 
	 * @param node
	 * @return length of node
	 */
	public static int getPath(Node node) {
		int path_lenght = 0;
		while (node.getParent() != null) {
			node = node.getParent();
			path_lenght++;
		}
		return path_lenght;
	}

	////////////////////////// Utility methods ///////////////////

	/**
	 * Test the goal by comparing the given state(array) with the target
	 * 
	 * @param nodeState
	 * @return
	 */
	public boolean goal_test(int[][] nodeState) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (nodeState[i][j] != goal_state[i][j]) { // if only one is different
					return false; // goal test failed
				}
			}
		}
		return true; // by default
	}

	/**
	 * Create a new Child
	 * 
	 * @param parent
	 * @param action
	 * @return
	 */
	public Node childNode(Node parent, Action action) {

		// Initialize new Child Node with parameters
		Node newChild = new Node();
		newChild.setMoves(parent.getMoves());
		newChild.setStateArray(parent.getStateArray());
		newChild.setParent(parent);

		// Getting position of 0 in the matrix: e.g. 2,3
		String xyPosition = Node.getZeroPosition(newChild.getStateArray());
		int xpos = Integer.parseInt(xyPosition.charAt(0) + "");
		int ypos = Integer.parseInt(xyPosition.charAt(2) + "");
		int oldXPosition = xpos;
		int newXPosition = xpos;
		int oldYPosition = ypos;
		int newYPosition = ypos;

		// Calculate the new position
		switch (action) {
		case UP:
			newXPosition--;
			newChild.setMoves(newChild.getMoves() + "U");
			break;
		case DOWN:
			newXPosition++;
			newChild.setMoves(newChild.getMoves() + "D");
			break;
		case LEFT:
			newYPosition--;
			newChild.setMoves(newChild.getMoves() + "L");
			break;
		case RIGHT:
			newYPosition++;
			newChild.setMoves(newChild.getMoves() + "R");
			break;
		default:
			break;
		}

		// make copy of the state to process it
		int[][] tempNew = new int[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				tempNew[i][j] = newChild.getStateArray()[i][j];
			}
		}

		// Value to be exchanged with 0
		int valueToExchange = newChild.getStateArray()[newXPosition][newYPosition];

		// Updated new Values for the new matrix
		tempNew[newXPosition][newYPosition] = 0;
		tempNew[oldXPosition][oldYPosition] = valueToExchange;

		// Assign new matrix to the created Child node
		newChild.setStateArray(tempNew);
		return newChild;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

	public Heuristic getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(Heuristic heuristic) {
		this.heuristic = heuristic;
	}

	public String getProblem() {
		return problem;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

}
