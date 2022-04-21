import java.io.IOException;
import java.io.PrintWriter;

public class Generator {

    public static void main(String[] args) throws IOException {
        char[][] maze = new char[3][3];
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                double rand = Math.random();
                if(rand < 0.6) maze[i][j] = '0';
                else maze[i][j] = 'X';
            }
        }

        int randX = (int) (3*Math.random());
        int randY = (int) (3*Math.random());
        maze[randX][randY] = 'F';

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++) sb.append(maze[i][j]);
            sb.append("\n");
        }

        PrintWriter pw = new PrintWriter("resources/maze.txt");
        pw.println(sb);
        pw.close();

        //solve maze
        Solver.solve();
    }

}
