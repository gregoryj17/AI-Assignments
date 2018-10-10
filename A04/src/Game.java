import java.util.Scanner;

public class Game {
    int[] board;

    /*
    012
    345
    678
     */
    boolean isWon(){
        if(board[0]!=0&&board[0]==board[1]&&board[1]==board[2])return true; //012
        else if(board[0]!=0&&board[0]==board[3]&&board[3]==board[6])return true; //036
        else if(board[0]!=0&&board[0]==board[4]&&board[4]==board[8])return true; //048
        else if(board[3]!=0&&board[3]==board[4]&&board[4]==board[5])return true; //345
        else if(board[6]!=0&&board[6]==board[7]&&board[7]==board[8])return true; //678
        else if(board[1]!=0&&board[1]==board[4]&&board[4]==board[7])return true; //147
        else if(board[2]!=0&&board[2]==board[5]&&board[5]==board[8])return true; //258
        else return (board[2]!=0&&board[2]==board[4]&&board[4]==board[6]); //246
    }

    boolean isOver(){
        if(isWon())return true;
        else for(int i = 0; i < board.length; i++){
            if(board[i]==0)return false;
        }
        return true;
    }

    int winner(){
        if(!isWon())return -1;
        else if(board[0]!=0&&board[0]==board[1]&&board[1]==board[2])return board[0];
        else if(board[0]!=0&&board[0]==board[3]&&board[3]==board[6])return board[0];
        else if(board[0]!=0&&board[0]==board[4]&&board[4]==board[8])return board[0];
        else if(board[3]!=0&&board[3]==board[4]&&board[4]==board[5])return board[3];
        else if(board[6]!=0&&board[6]==board[7]&&board[7]==board[8])return board[6];
        else if(board[1]!=0&&board[1]==board[4]&&board[4]==board[7])return board[1];
        else return board[2];
    }

    boolean tryMove(int move, int player){
        if(move<0||move>8)return false;
        else if(board[move]!=0)return false;
        else{
            board[move]=player;
            return true;
        }
    }

    void playGame(GamePlayer a, GamePlayer b){
        board = new int[9];
        while(!isWon()){
            if(!tryMove(a.getMove(board), 1)){
                System.out.println("X has made an illegal move. O wins.");
                break;
            }
            if(isOver()){
                break;
            }
            if(!tryMove(b.getMove(board), 2)){
                System.out.println("O has made an illegal move. X wins.");
                break;
            }
        }
        if(isOver()){
            if(isWon()){
                if(winner()==1){
                    System.out.println("X wins.");
                }
                else{
                    System.out.println("O wins.");
                }
            }
            else{
                System.out.println("It's a draw.");
            }
        }
    }

    public static void main(String[] args){
        Game g = new Game();
        g.playGame(new HumanPlayer(), new AIPlayer());
    }

}

interface GamePlayer{
    int getMove(int[] board);
}

class HumanPlayer implements GamePlayer{
    public int getMove(int[] board){
        Scanner scan = new Scanner(System.in);
        System.out.println();

        for(int i=0;i<board.length;i++){
            if(i%3!=0){
                System.out.print(" |");
            }
            else if(i>0){
                System.out.println("\n---+---+---");
            }
            System.out.print(" ");
            switch(board[i]){
                case 1:
                    System.out.print("X");
                    break;
                case 2:
                    System.out.print("O");
                    break;
                default:
                    System.out.print(i+1);
                    break;
            }

        }

        System.out.print("\nYour move: ");

        return scan.nextInt()-1;
    }
}

class AIPlayer implements GamePlayer{
    public int getMove(int[] board){
        int bestIndex=-1;
        int bestScore=-2;
        for(int i=0; i<board.length; i++){
            if(board[i]==0){
                int score = minimax(board.clone(), i, 2);
                if(score>bestScore){
                    bestIndex = i;
                    bestScore = score;
                }
            }
        }
        return bestIndex;
    }

    int minimax(int[] board, int move, int player){
        board[move]=player;
    	if(isOver(board)){
        	if(!isWon(board))return 0;
        	else if(winner(board)==2)return 1;
        	else return -1;
		}
		if(player==1) {
			int value = Integer.MIN_VALUE;
			for(int i=0;i<board.length;i++) {
				if(board[i]==0){
					value=Integer.max(value,minimax(board.clone(),i,2));
				}
			}
			return value;
		}
        else {
			int value=Integer.MAX_VALUE;
			for(int i=0;i<board.length;i++) {
				if(board[i]==0){
				value = Integer.min(value, minimax(board.clone(), i, 1));
				}
			}
			return value;
		}
    }

    boolean isWon(int[] board){
        if(board[0]!=0&&board[0]==board[1]&&board[1]==board[2])return true;
        else if(board[0]!=0&&board[0]==board[3]&&board[3]==board[6])return true;
        else if(board[0]!=0&&board[0]==board[4]&&board[4]==board[8])return true;
        else if(board[3]!=0&&board[3]==board[4]&&board[4]==board[5])return true;
        else if(board[6]!=0&&board[6]==board[7]&&board[7]==board[8])return true;
        else if(board[1]!=0&&board[1]==board[4]&&board[4]==board[7])return true;
        else if(board[2]!=0&&board[2]==board[5]&&board[5]==board[8])return true;
        else return (board[2]!=0&&board[2]==board[4]&&board[4]==board[6]);
    }

    boolean isOver(int[] board){
        if(isWon(board))return true;
        else for(int i = 0; i < board.length; i++){
            if(board[i]==0)return false;
        }
        return true;
    }

    int winner(int[] board){
        if(!isWon(board))return -1;
        else if(board[0]!=0&&board[0]==board[1]&&board[1]==board[2])return board[0];
        else if(board[0]!=0&&board[0]==board[3]&&board[3]==board[6])return board[0];
        else if(board[0]!=0&&board[0]==board[4]&&board[4]==board[8])return board[0];
        else if(board[3]!=0&&board[3]==board[4]&&board[4]==board[5])return board[3];
        else if(board[6]!=0&&board[6]==board[7]&&board[7]==board[8])return board[6];
        else if(board[1]!=0&&board[1]==board[4]&&board[4]==board[7])return board[1];
        else return board[2];
    }
}