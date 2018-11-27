import java.util.ArrayList;
import java.util.Random;

public class Main {

	//constants used in q-learning
	static double alpha = 0.1;
	static double eps = 0.05;
	static double gamma = 0.97;
	//random number generator
	static Random rand = new Random();
	//movement constants
	static final int UP = 0;
	static final int DOWN = 1;
	static final int LEFT = 2;
	static final int RIGHT = 3;
	static final int WALL = 4, START = 5, GOAL = 6;
	//score constants
	static final double WALL_SCORE = -1000.0;
	static final double GOAL_SCORE = 100.0;
	static final double DEFAULT_SCORE = -1.0;
	//tables
	static double[][] table;
	static int[][] policy;
	//start and goal location
	static final int GOALROW = 0, GOALCOL = 19, STARTROW = 9, STARTCOL = 0;

	//the actual learning process
	public static boolean qLearn(int r, int c) {
		int row = r;
		int col = c;

		int action = nextRandomAction(row, col);
		if (rand.nextDouble() >= eps){
			// Exploit (pick the best action)
			for (int candidate = 0; candidate < 4; candidate++)
				if (q(row, col, candidate) > q(row, col, action))
					action = candidate;
			if (q(row, col, action) == DEFAULT_SCORE)
				action = nextRandomAction(row, col);
		}

		// Do the action
		row = nextRow(row, action);
		col = nextCol(col, action);

		// Learn from that experience
		double qscore = ((1 - alpha) * table[r][c]) + (alpha * (table[row][col] + (gamma * maxVal(row, col))));
		table[r][c] = qscore;
		policy[r][c] = action;

		return policy[row][col]==GOAL;

	}

	public static double maxVal(int row, int col){
		double score = -1.0;

		if(row>0)score=Math.max(score,table[row-1][col]);
		if(row<table.length-1)score=Math.max(score,table[row+1][col]);
		if(col>0)score=Math.max(score,table[row][col-1]);
		if(col<table[row].length-1)score=Math.max(score,table[row][col+1]);

		return score;
	}

	public static int nextRow(int row, int action){
		if(action==UP)return row-1;
		else if(action==DOWN)return row+1;
		else return row;
	}

	public static int nextCol(int col, int action){
		if(action==LEFT)return col-1;
		else if(action==RIGHT)return col+1;
		else return col;
	}

	public static int nextRandomAction(int row, int col){
		ArrayList<Integer> list = new ArrayList<>();
		if (row > 0 && policy[row - 1][col] != WALL) list.add(UP);
		if (row < table.length - 1 && policy[row + 1][col] != WALL) list.add(DOWN);
		if (col > 0 && policy[row][col - 1] != WALL) list.add(LEFT);
		if (col < table[row].length - 1 && policy[row][col + 1] != WALL) list.add(RIGHT);
		return list.get((int) (Math.random() * list.size()));
	}


	public static double q(int row, int col, int action){
		if(action==UP&&row>0)return table[row-1][col];
		else if(action==DOWN&&row<table.length-1)return table[row+1][col];
		else if(action==LEFT&&col>0)return table[row][col-1];
		else if(action==RIGHT&&col<table[row].length-1)return table[row][col+1];
		else return -10000.0;
	}

	public static void printBoard(){
		for(int i = 0; i<table.length; i++){
			for(int j = 0; j<table[i].length; j++){
				if(i==STARTROW&&j==STARTCOL){
					System.out.print('S');
					continue;
				}
				int cur = policy[i][j];
				if(cur==UP)System.out.print('^');
				else if(cur==DOWN)System.out.print('v');
				else if(cur==LEFT)System.out.print('<');
				else if(cur==RIGHT)System.out.print('>');
				else if(cur==WALL)System.out.print('#');
				else if(cur==START)System.out.print('S');
				else if(cur==GOAL)System.out.print('G');
				else System.out.print(' ');
			}
			System.out.println();
		}
		System.out.println();
	}

	public static void main(String[] args){
		table = new double[10][20];
		policy = new int[10][20];

		for(int r = 0; r<table.length; r++){
			for(int c = 0; c<table[r].length; c++){
				table[r][c]=DEFAULT_SCORE;
				if(r==0)policy[r][c]=DOWN;
			}
		}

		table[GOALROW][GOALCOL]=GOAL_SCORE;
		policy[GOALROW][GOALCOL]=GOAL;
		policy[STARTROW][STARTCOL]=START;
		for(int i = 0; i < table.length; i++){
			if(i==5){
				i++;
				continue;
			}
			table[i][10]=WALL_SCORE;
			policy[i][10]=WALL;
		}
		for(int i = 0; i < 10001; i++) {
			int row = STARTROW;
			int col = STARTCOL;
			while(!qLearn(row,col)){
				int r = row;
				row = nextRow(row, policy[row][col]);
				col = nextCol(col, policy[r][col]);
			}
			if(i%1000==0){
				printBoard();
			}

			//System.out.println(i);
		}

	}

}

