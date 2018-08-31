import java.io.PrintWriter;
import java.util.*;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;
import java.io.File;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Color;

class View extends JPanel implements MouseListener {
	Viz viz;
	Random rand;
	Stack<GameState> states;
	byte[] state;
	Graphics graphics;
	int size;

	View(Viz v) throws IOException
	{
		viz = v;
		rand = new Random(0);
		state = new byte[22];
		size = 48;
	}

	View(Viz v, Stack<GameState> statestack) throws IOException {
		viz = v;
		rand = new Random(0);
		state = new byte[22];
		size=48;
		states=statestack;
	}

	public void mousePressed(MouseEvent e)
	{
		if(states==null){
			state[rand.nextInt(22)] += (rand.nextInt(2) == 0 ? -1 : 1);
		}
		else{
			state=states.pop().toByteArray();
		}

		for(int i = 0; i < 11; i++)
			System.out.print("(" + state[2 * i] + "," +
					state[2 * i + 1] + ") ");
		System.out.println();
		viz.repaint();
	}

	public void mouseReleased(MouseEvent e) {    }
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }
	public void mouseClicked(MouseEvent e) {    }

	// Draw a block
	public void b(int x, int y)
	{
		graphics.fillRect(size * x, size * y, size, size);
	}

	// Draw a 3-block piece
	public void shape(int id, int red, int green, int blue,
					  int x1, int y1, int x2, int y2, int x3, int y3)
	{
		graphics.setColor(new Color(red, green, blue));
		b(state[2 * id] + x1, state[2 * id + 1] + y1);
		b(state[2 * id] + x2, state[2 * id + 1] + y2);
		b(state[2 * id] + x3, state[2 * id + 1] + y3);
	}

	// Draw a 4-block piece
	public void shape(int id, int red, int green, int blue,
					  int x1, int y1, int x2, int y2,
					  int x3, int y3, int x4, int y4)
	{
		shape(id, red, green, blue, x1, y1, x2, y2, x3, y3);
		b(state[2 * id] + x4, state[2 * id + 1] + y4);
	}

	public void paintComponent(Graphics g)
	{
		// Draw the black squares
		graphics = g;
		g.setColor(new Color(0, 0, 0));
		for(int i = 0; i < 10; i++) { b(i, 0); b(i, 9); }
		for(int i = 1; i < 9; i++) { b(0, i); b(9, i); }
		b(1, 1); b(1, 2); b(2, 1);
		b(7, 1); b(8, 1); b(8, 2);
		b(1, 7); b(1, 8); b(2, 8);
		b(8, 7); b(7, 8); b(8, 8);
		b(3, 4); b(4, 4); b(4, 3);

		// Draw the pieces
		shape(0, 255, 0, 0, 1, 3, 2, 3, 1, 4, 2, 4);
		shape(1, 0, 255, 0, 1, 5, 1, 6, 2, 6);
		shape(2, 128, 128, 255, 2, 5, 3, 5, 3, 6);
		shape(3, 255, 128, 128, 3, 7, 3, 8, 4, 8);
		shape(4, 255, 255, 128, 4, 7, 5, 7, 5, 8);
		shape(5, 128, 128, 0, 6, 7, 7, 7, 6, 8);
		shape(6, 0, 128, 128, 5, 4, 5, 5, 5, 6, 4, 5);
		shape(7, 0, 128, 0, 6, 4, 6, 5, 6, 6, 7, 5);
		shape(8, 0, 255, 255, 8, 5, 8, 6, 7, 6);
		shape(9, 0, 0, 255, 6, 2, 6, 3, 5, 3);
		shape(10, 255, 128, 0, 5, 1, 6, 1, 5, 2);
	}
}

class Viz extends JFrame
{
	public Viz() throws Exception
	{
		View view = new View(this);
		view.addMouseListener(view);
		this.setTitle("Puzzle");
		this.setSize(482, 505);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public Viz(Stack<GameState> stack) throws Exception
	{
		View view = new View(this, stack);
		view.addMouseListener(view);
		this.setTitle("Puzzle");
		this.setSize(482, 505);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public static void main(String[] args) throws Exception
	{
		new Viz();
	}
}


class GameState
{
	//offsetx and offsety: the offset from (0,0) for the top-left corner of the piece
	static byte[] ox = new byte[]{1,1,2,3,4,6,4,6,7,5,5};
	static byte[] oy = new byte[]{3,5,5,7,7,7,4,4,5,2,1};

	GameState prev;
	byte[] state;

	GameState(GameState _prev)
	{
		prev = _prev;
		state = new byte[22];
		if(_prev!=null){
			for(int i=0;i<22;i++){
				state[i]=_prev.state[i];
			}
		}
	}

	public String toString(){
		return "("+state[0]+","+state[1]+") ("+state[2]+","+state[3]+") ("+state[4]+","+state[5]+") ("+state[6]+","+state[7]+") ("+state[8]+","+state[9]+") ("+state[10]+","+state[11]+") ("+
				state[12]+","+state[13]+") ("+state[14]+","+state[15]+") ("+state[16]+","+state[17]+") ("+state[18]+","+state[19]+") ("+state[20]+","+state[21]+")";
	}

	public byte[] toByteArray(){
		return state;
	}

	public boolean isValid(){
		//grid[x][y]
		boolean[][] grid = new boolean[10][10];
		for(int i=0;i<10;i++){
			grid[i][0]=true;
			grid[i][9]=true;
			grid[0][i]=true;
			grid[9][i]=true;
		}
		grid[1][1]=true;
		grid[1][2]=true;
		grid[2][1]=true;
		grid[7][1]=true;
		grid[8][1]=true;
		grid[8][2]=true;
		grid[1][7]=true;
		grid[1][8]=true;
		grid[2][8]=true;
		grid[8][7]=true;
		grid[7][8]=true;
		grid[8][8]=true;
		grid[3][4]=true;
		grid[4][3]=true;
		grid[4][4]=true;
		//i - piece number
		for(int i=0;i<11;i++){
			//j - x pos
			for(int j=0;j<2;j++){
				//k - y pos
				for(int k=0;k<3;k++){
					//top-left missing
					if(j==0&&k==0&&(i==6||i==8||i==9)){
						continue;
					}
					//top-right missing
					if(j==1&&k==0&&(i==1||i==3||i==7)){
						continue;
					}
					//middle-left missing
					if(j==0&&k==1&&(i==2||i==4)){
						continue;
					}
					//middle-right missing
					if(j==1&&k==1&&(i==5||i==10)){
						continue;
					}
					//bottom-left missing
					if(j==0&&k==2&&i!=7){
						continue;
					}
					//bottom-right missing
					if(j==1&&k==2&&i!=6){
						continue;
					}


					if(grid[state[i*2]+j+ox[i]][state[i*2+1]+k+oy[i]]){
						return false;
					}
					else{
						grid[state[i*2]+j+ox[i]][state[i*2+1]+k+oy[i]]=true;
					}
				}
			}
		}
		return true;
	}

}

class StateComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		for(int i = 0; i < 22; i++)
		{
			if(a.state[i] < b.state[i])
				return -1;
			else if(a.state[i] > b.state[i])
				return 1;
		}
		return 0;
	}
}

public class Program
{
	public static void finished(GameState endState){
		GameState cur = endState;
		Stack<GameState> stack = new Stack<>();
		while(cur!=null){
			stack.push(cur);
			cur=cur.prev;
		}

		PrintWriter out = null;

		try {
			out = new PrintWriter(new File("results.txt"));
		}catch(Exception e){
			e.printStackTrace();
		}


		while(!stack.empty()){
			cur=stack.pop();
			out.println(cur);
		}

		out.flush();

		/*
		try{
			new Viz(stack);
		}catch(Exception e){
			e.printStackTrace();
		}
		*/
	}

	public static void main(String args[])
	{
		StateComparator comp = new StateComparator();
		TreeSet<GameState> seenit = new TreeSet<GameState>(comp);
		LinkedList<GameState> todo = new LinkedList<GameState>();

		GameState start = new GameState(null);

		todo.push(start);

		while(!todo.isEmpty()){
			GameState n = todo.pop();
			if(n.state[1]==-2){
				finished(n);
				break;
			}
			for(int i=0;i<22;i++){
				for(int j=-1;j<2;j+=2){
					GameState c = new GameState(n);
					c.state[i]+=j;
					if(!c.isValid()){
						continue;
					}
					if(!seenit.contains(c)){
						seenit.add(c);
						todo.add(c);
					}
				}
			}
		}

		try{
			//Viz.main(null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}