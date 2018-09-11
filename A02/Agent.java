import java.util.*;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {

    PriorityQueue<GameState> border=new PriorityQueue<>();
    Stack<GameState> moves=new Stack<>();
    GameState next=new GameState(100,100);
    boolean heur=false;
    int goalX=100;
    int goalY=100;

    void drawPlan(Graphics g, Model m) {
        g.setColor(Color.red);
        g.drawLine((int) m.getX(), (int) m.getY(), (int) m.getDestinationX(), (int) m.getDestinationY());
        if(!moves.empty()){
            Stack<GameState> todraw = (Stack<GameState>)moves.clone();
            GameState drawing = new GameState((int)m.getX(),(int)m.getY());
            GameState drawing2 = todraw.pop();
            while(drawing2!=null){
                g.drawLine(drawing.x,drawing.y,drawing2.x,drawing2.y);
                drawing=drawing2;
                if(!todraw.empty())drawing2=todraw.pop();
                else drawing2=null;
            }
        }
        g.setColor(Color.yellow);
        if(!border.isEmpty()){
            for(GameState s : border){
                g.fillOval(s.x,s.y,10,10);
            }
        }
    }

    void update(Model m) {
        Controller c = m.getController();

        m.setDestination((int)m.getX()/10*10,(int)m.getY()/10*10);

        GameState goalState = new GameState();
        goalState.x = goalX;
        goalState.y = goalY;

        QueueComparator queuecomp = new QueueComparator();
        HeurQueueComparator hqueuecomp = new HeurQueueComparator();
        PriorityQueue<GameState> frontier;
        if(heur) frontier = new PriorityQueue(hqueuecomp);
        else frontier = new PriorityQueue<>(queuecomp);
        SetComparator setcomp = new SetComparator();
        TreeSet<GameState> beenthere = new TreeSet(setcomp);
        GameState startState = new GameState();
        startState.cost = 0.0;
        startState.parent = null;
        startState.x=(int)m.getX();
        startState.y=(int)m.getY();
        beenthere.add(startState);
        frontier.add(startState);

        //System.out.println(heur);

        while (frontier.size() > 0) {
            GameState s = frontier.poll(); // get lowest-cost state
            if (s.x==goalState.x&&s.y==goalState.y) {
                goalState = s;
                break;
            }
            for(int i=0; i<8; i++){
                GameState child = transition(s, i); // compute the next state
                if(child.x<1||child.y<1||child.x>1199||child.y>599){
                    continue;
                }
                int acost = action_cost(s, i, m); // compute the cost of the action
                if(heur){
                    child.dist=Math.sqrt(Math.pow((child.x-goalX),2)+Math.pow((child.y-goalY),2));
                }
                if (beenthere.contains(child)){
                    GameState oldchild = beenthere.floor(child);
                    if (s.cost + acost < oldchild.cost) {
                        oldchild.cost = s.cost + acost;
                        oldchild.parent = s;
                    }
                }
                else{
                    child.cost = s.cost + acost;
                    child.parent = s;
                    frontier.add(child);
                    beenthere.add(child);
                }
            }
        }

        GameState n = goalState;
        Stack<GameState> path = new Stack<>();
        while(n.parent!=null){
            path.push(n);
            n=n.parent;
        }
        if(!path.empty())next = path.pop();
        m.setDestination(next.x,next.y);

        if(!path.empty())moves=path;
        if(!frontier.isEmpty())border=frontier;

        //System.out.println(m.getX()+" "+m.getY());

        while (true) {
            MouseEvent e = c.nextMouseEvent();
            if (e == null)
                break;

            if(e.getButton()==1)heur=false;
            if(e.getButton()==3)heur=true;

            goalX = Math.max(e.getX()/10*10,10);
            goalY = Math.max(e.getY()/10*10,10);


            //m.setDestination(goalState.x,goalState.y);
            //m.setDestination(e.getX(), e.getY());
        }
    }

    /*  0 1 2
        7 _ 3
        6 5 4  */
    GameState transition(GameState s, int i){
        if(i<0||i>7)return null;
        GameState n = new GameState(s);
        n.parent=s;
        if(i<3)n.y-=10;
        if(i>3&&i<7)n.y+=10;
        if(i<1||i>5)n.x-=10;
        if(i>1&&i<5)n.x+=10;
        return n;
    }

    int action_cost(GameState s, int i, Model m){
        if(i%2==0){
            return (int)(Math.sqrt(200)/m.getTravelSpeed(s.x,s.y));
        }
        else{
            return (int)(10.0/m.getTravelSpeed(s.x,s.y));
        }
    }

    public static void main(String[] args) throws Exception {
        Controller.playGame();
    }
}

class HeurQueueComparator implements Comparator<GameState>{
    public int compare(GameState a, GameState b){
        double acost = a.cost+(0.5*a.dist);
        double bcost = b.cost+(0.5*b.dist);
        if(acost<bcost) return -1;
        if(bcost<acost) return 1;
        return 0;
    }
}

class QueueComparator implements Comparator<GameState>{
    public int compare(GameState a, GameState b){
        if(a.cost<b.cost)return -1;
        if(b.cost<a.cost)return 1;
        return 0;
    }
}

class SetComparator implements Comparator<GameState>{
    public int compare(GameState a, GameState b){
        if(a.x<b.x)return -1;
        if(b.x<a.x)return 1;
        if(a.y<b.y)return -1;
        if(b.y<a.y)return 1;
        return 0;
    }
}

class GameState{
    public double cost;
    public GameState parent;
    public int x;
    public int y;
    public double dist;

    GameState(){
        cost=0;
        parent=null;
        x=0;
        y=0;
    }

    GameState(double cost, GameState par){
        this.cost = cost;
        parent = par;
        x=0;
        y=0;
    }

    GameState(int xx, int yy){
        x=xx;
        y=yy;
    }

    GameState(GameState s){
        cost=s.cost;
        parent=s.parent;
        x=s.x;
        y=s.y;
    }

}