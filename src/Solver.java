/**
 * Solver.java
 * Kevin Lou
 * 4/19/22
 * Solves a 3x3 maze problem.
 * Original code from: https://github.com/technobium/q-learning-java
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Solver {

    private final double alpha = 0.1; // Learning rate
    private final double gamma = 0.9; // Eagerness - 0 looks in the near future, 1 looks in the distant future

    private final int mazeWidth = 3;
    private final int mazeHeight = 3;
    private final int statesCount = mazeHeight * mazeWidth;

    private final int reward = 100;
    private final int penalty = -10;

    private char[][] maze;  // Maze read from file
    private int[][] R;       // Reward lookup
    private double[][] Q;    // Q learning

    public static void solve() {
        Solver s = new Solver();

        s.init();
        s.calculateQ();
        s.printQ();
    }

    public void init() {
        File file = new File("resources/maze.txt");

        R = new int[statesCount][statesCount];
        Q = new double[statesCount][statesCount];
        maze = new char[mazeHeight][mazeWidth];


        try (FileInputStream fis = new FileInputStream(file)) {

            int i = 0;
            int j = 0;

            int content;

            // Read the maze from the input file
            while ((content = fis.read()) != -1) {
                char c = (char) content;
                if (c != '0' && c != 'F' && c != 'X') {
                    continue;
                }
                maze[i][j] = c;
                j++;
                if (j == mazeWidth) {
                    j = 0;
                    i++;
                }
            }

            // We will navigate through the reward matrix R using k index
            for (int k = 0; k < statesCount; k++) {

                // We will navigate with i and j through the maze, so we need
                // to translate k into i and j
                i = k / mazeWidth;
                j = k - i * mazeWidth;

                // Fill in the reward matrix with -1
                for (int s = 0; s < statesCount; s++) {
                    R[k][s] = -1;
                }

                // If not in final state or a wall try moving in all directions in the maze
                if (maze[i][j] != 'F') {

                    // Try to move left in the maze
                    int goLeft = j - 1;
                    if (goLeft >= 0) {
                        int target = i * mazeWidth + goLeft;
                        if (maze[i][goLeft] == '0') {
                            R[k][target] = 0;
                        } else if (maze[i][goLeft] == 'F') {
                            R[k][target] = reward;
                        } else {
                            R[k][target] = penalty;
                        }
                    }

                    // Try to move right in the maze
                    int goRight = j + 1;
                    if (goRight < mazeWidth) {
                        int target = i * mazeWidth + goRight;
                        if (maze[i][goRight] == '0') {
                            R[k][target] = 0;
                        } else if (maze[i][goRight] == 'F') {
                            R[k][target] = reward;
                        } else {
                            R[k][target] = penalty;
                        }
                    }

                    // Try to move up in the maze
                    int goUp = i - 1;
                    if (goUp >= 0) {
                        int target = goUp * mazeWidth + j;
                        if (maze[goUp][j] == '0') {
                            R[k][target] = 0;
                        } else if (maze[goUp][j] == 'F') {
                            R[k][target] = reward;
                        } else {
                            R[k][target] = penalty;
                        }
                    }

                    // Try to move down in the maze
                    int goDown = i + 1;
                    if (goDown < mazeHeight) {
                        int target = goDown * mazeWidth + j;
                        if (maze[goDown][j] == '0') {
                            R[k][target] = 0;
                        } else if (maze[goDown][j] == 'F') {
                            R[k][target] = reward;
                        } else {
                            R[k][target] = penalty;
                        }
                    }
                }
            }
            initializeQ();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Set Q values to R values
    void initializeQ() {
        for (int i = 0; i < statesCount; i++){
            for(int j = 0; j < statesCount; j++){
                Q[i][j] = (double)R[i][j];
            }
        }
    }

    void calculateQ() {
        Random rand = new Random();

        for (int i = 0; i < 1000; i++) { // Train cycles
            // Select random initial state
            int crtState = rand.nextInt(statesCount);

            while (!isFinalState(crtState)) {
                int[] actionsFromCurrentState = possibleActionsFromState(crtState);

                // Pick a random action from the ones possible
                int index = rand.nextInt(actionsFromCurrentState.length);
                int nextState = actionsFromCurrentState[index];

                // Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
                double q = Q[crtState][nextState];
                double maxQ = maxQ(nextState);
                int r = R[crtState][nextState];

                double value = q + alpha * (r + gamma * maxQ - q);
                Q[crtState][nextState] = value;

                crtState = nextState;
            }
        }
    }

    boolean isFinalState(int state) {
        int i = state / mazeWidth;
        int j = state - i * mazeWidth;

        return maze[i][j] == 'F';
    }

    int[] possibleActionsFromState(int state) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < statesCount; i++) {
            if (R[state][i] != -1) {
                result.add(i);
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }

    double maxQ(int nextState) {
        int[] actionsFromState = possibleActionsFromState(nextState);
        //the learning rate and eagerness will keep the W value above the lowest reward
        double maxValue = -10;
        for (int nextAction : actionsFromState) {
            double value = Q[nextState][nextAction];

            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }

    void printPolicy() {
        System.out.println("\nPrint policy");
        for (int i = 0; i < statesCount; i++) {
            System.out.println("From state " + i + " goto state " + getPolicyFromState(i));
        }
    }

    int getPolicyFromState(int state) {
        int[] actionsFromState = possibleActionsFromState(state);

        double maxValue = Double.MIN_VALUE;
        int policyGotoState = state;

        // Pick to move to the state that has the maximum Q value
        for (int nextState : actionsFromState) {
            double value = Q[state][nextState];

            if (value > maxValue) {
                maxValue = value;
                policyGotoState = nextState;
            }
        }
        return policyGotoState;
    }

    void printQ() {
        StringBuilder sb = new StringBuilder();
        sb.append("Q matrix \n");
        for (int i = 0; i < Q.length; i++) {
            sb.append("From state ").append(i).append(":  ");
            for (int j = 0; j < Q[i].length; j++) {
                sb.append(String.format("%6.2f ", (Q[i][j])));
            }
            sb.append("\n");
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter("resources/output.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.println(sb);
        pw.close();
    }
}