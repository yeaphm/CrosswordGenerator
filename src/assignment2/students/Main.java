package assignment2.students;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Main {
    private static final int POPULATION_SIZE = 100;
    private static final double CROSSOVER_RATE = 0.9;
    private static final double MUTATION_RATE = 0.5;
    private static final Random random = new Random();

    private static final List<String> words = new ArrayList<>();
    private static final String INPUTS_DIR_NAME = "inputs";

    public static void main(String[] args) {
        start();
    }

    private static void readAllWordsInFile(File file) {
        System.out.println("Processing file: " + file.getName());

        // Open the file using FileReader and BufferedReader
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            // Read all lines from the file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() > 1) {
                    words.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static File[] getAllFiles() {
        String projectPath = Paths.get("").toAbsolutePath().toString();
        String absoluteDirPath = Paths.get(projectPath, INPUTS_DIR_NAME).toString();
        File inputDirectory = new File(absoluteDirPath);

        return inputDirectory.listFiles();
    }

    private static void start() {
        File[] files = getAllFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    readAllWordsInFile(file);
                    buildCrossword();
                    words.clear();
                }
            }
        } else {
            System.out.println("No files found in the input directory.");
        }
    }

    private static void buildCrossword() {
        List<CrosswordLayout> population = initializePopulation();

        int generation = 0;
        while (true) {
            CrosswordLayout bestLayout = getBestLayout(population);

            System.out.println("Generation " + generation + " - Fitness: " + bestLayout.calculateFitness());

            if (bestLayout.calculateFitness() <= 0) {
                bestLayout.printCrossword();
                System.out.println("Solution found!");
                break;
            }

            List<CrosswordLayout> newGeneration = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                CrosswordLayout parent1 = selectParent(population);
                CrosswordLayout parent2 = selectParent(population);
                CrosswordLayout child = crossover(parent1, parent2);
                mutate(child);
                newGeneration.add(child);
            }

            population = newGeneration;
            generation++;
        }
    }

    private static List<CrosswordLayout> initializePopulation() {
        List<CrosswordLayout> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new CrosswordLayout(words));
        }
        return population;
    }

    private static CrosswordLayout getBestLayout(List<CrosswordLayout> population) {
        CrosswordLayout bestLayout = population.get(0);
        for (CrosswordLayout layout : population) {
            if (layout.calculateFitness() < bestLayout.calculateFitness()) {
                bestLayout = layout;
            }
        }
        return bestLayout;
    }

    // TODO reconsider (may be more optimal choice)
    private static CrosswordLayout selectParent(List<CrosswordLayout> population) {
        int tournamentSize = 5;
        CrosswordLayout bestLayout = population.get(random.nextInt(population.size()));
        for (int i = 1; i < tournamentSize; i++) {
            CrosswordLayout candidate = population.get(random.nextInt(population.size()));
            if (candidate.calculateFitness() < bestLayout.calculateFitness()) {
                bestLayout = candidate;
            }
        }
        return bestLayout;
    }

    private static CrosswordLayout crossover(CrosswordLayout parent1, CrosswordLayout parent2) {
        if (random.nextDouble() < CROSSOVER_RATE) {
            return parent1.crossover(parent2);
        } else {
            return parent1.copy();
        }
    }

    private static void mutate(CrosswordLayout layout) {
        if (random.nextDouble() < MUTATION_RATE) {
            layout.mutate();
        }
    }
}

class CrosswordWord {
    String word;
    int row;
    int col;
    int orientation;

    public CrosswordWord(String word, int row, int col, int orientation) {
        this.word = word;
        this.row = row;
        this.col = col;
        this.orientation = orientation;
    }
}

class CrosswordLayout {
    private static final int GRID_SIZE = 20;
    private static final char[][] grid = new char[GRID_SIZE][GRID_SIZE];
    private static final Random random = new Random();
    private final List<CrosswordWord> words;

    // TODO double check initial generation
    public CrosswordLayout(List<String> inputWords) {
        this.words = new ArrayList<>();
        for (String word : inputWords) {
            int row = random.nextInt(GRID_SIZE - 1);
            int col = random.nextInt(GRID_SIZE - 1);
            int orientation = random.nextInt(2); // 0 for horizontal, 1 for vertical
            words.add(new CrosswordWord(word, row, col, orientation));
        }
    }

    private void resetGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = '-';
            }
        }
    }

    public int calculateFitness() {
        int fitness = 0;

        for (CrosswordWord word : words) {
            fitness += outOfBoundsCheck(word);
        }

        fitness += overlapCheck();
        fitness += connectivityCheck();
        fitness += neighbouringWordsCheck();

        return fitness;
    }

    private boolean charInBounds(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

    private int outOfBoundsCheck(CrosswordWord word) {
        int penalty = 0;
        char[] charArray = word.word.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            int row = word.row + (word.orientation == 0 ? 0 : i);
            int col = word.col + (word.orientation == 1 ? 0 : i);

            if (!charInBounds(row, col)) {
                // Penalty for out of bounds position of the word
                penalty += 10;
            }
        }
        return penalty;
    }

    private int neighbouringWordsCheck() {
        int penalty = 0;

        for (CrosswordWord word : words) {
            char[] charArray = word.word.toCharArray();
            int adjCharCounterSideUp = 0;
            int adjCharCounterSideDown = 0;
            int adjCharCounterSideLeft = 0;
            int adjCharCounterSideRight = 0;

            for (int charIdx = 0; charIdx < charArray.length; charIdx++) {
                int row;
                int col;
                if (word.orientation == 0) {
                    row = word.row;
                    col = word.col + charIdx;

                    if (charInBounds(row, col)) {
                        if (charIdx == 0) { // First char check
                            if (charInBounds(row, col - 1) && grid[row][col - 1] != '-') {
                                penalty += 10; // penalty for adjacent word from the left
                            }
                        }
                        else if (charIdx == charArray.length - 1) { // Last char check
                            if (charInBounds(row, col + 1) && grid[row][col + 1] != '-') {
                                penalty += 10; // penalty for adjacent word from the right
                            }
                        }

                        // Parallel check
                        if (charInBounds(row - 1, col) && grid[row - 1][col] != '-') {
                            adjCharCounterSideUp++;
                            if (adjCharCounterSideUp > 1) {
                                penalty += 10; // penalty for the up adjacent word
                            }
                        } else if (charInBounds(row - 1, col) && grid[row - 1][col] == '-') {
                            adjCharCounterSideUp = 0;
                        }
                        if (charInBounds(row + 1, col) && grid[row + 1][col] != '-') {
                            adjCharCounterSideDown++;
                            if (adjCharCounterSideDown > 1) {
                                penalty += 10; // penalty for the down adjacent word
                            }
                        } else if (charInBounds(row + 1, col) && grid[row + 1][col] == '-') {
                            adjCharCounterSideDown = 0;
                        }
                    }
                } else {
                    row = word.row + charIdx;
                    col = word.col;

                    if (charInBounds(row, col)) {
                        if (charIdx == 0) { // First char check
                            if (charInBounds(row - 1, col) && grid[row - 1][col] != '-') {
                                penalty += 10; // penalty for adjacent word from the up
                            }
                        }
                        else if (charIdx == charArray.length - 1) { // Last char check
                            if (charInBounds(row + 1, col) && grid[row + 1][col] != '-') {
                                penalty += 10; // penalty for adjacent word from the down
                            }
                        }

                        // Parallel check
                        if (charInBounds(row, col - 1) && grid[row][col - 1] != '-') {
                            adjCharCounterSideLeft++;
                            if (adjCharCounterSideLeft > 1) {
                                penalty += 10; // penalty for the left adjacent word
                            }
                        } else if (charInBounds(row, col - 1) && grid[row][col - 1] == '-') {
                            adjCharCounterSideLeft = 0;
                        }
                        if (charInBounds(row, col + 1) && grid[row][col + 1] != '-') {
                            adjCharCounterSideRight++;
                            if (adjCharCounterSideRight > 1) {
                                penalty += 10; // penalty for the down adjacent word
                            }
                        } else if (charInBounds(row, col + 1) && grid[row][col + 1] == '-') {
                            adjCharCounterSideRight = 0;
                        }
                    }
                }
            }
        }

        return penalty;
    }

    private int overlapCheck() {
        int penalty = 0;
        resetGrid();

        for (CrosswordWord word : words) {
            char[] charArray = word.word.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                int row = word.row + (word.orientation == 0 ? 0 : i);
                int col = word.col + (word.orientation == 1 ? 0 : i);

                if (charInBounds(row, col)) {
                    if (grid[row][col] != '-' && grid[row][col] != charArray[i]) {
                        penalty += 10; // Penalty for overlapping different characters
                    }
                    grid[row][col] = charArray[i];
                }
            }
        }

        return penalty;
    }

    private int connectivityCheck() {
        boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
        int connectedComponents = 0;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!visited[i][j] && grid[i][j] != '-') {
                    dfs(i, j, visited);
                    connectedComponents++;
                }
            }
        }

        return connectedComponents > 1 ? connectedComponents * 10 : 0; // Penalty for disconnected components
    }

    private void dfs(int row, int col, boolean[][] visited) {
        if (!charInBounds(row, col) || visited[row][col] || grid[row][col] == '-') {
            return;
        }

        visited[row][col] = true;

        // Move in all four directions
        dfs(row - 1, col, visited);
        dfs(row + 1, col, visited);
        dfs(row, col - 1, visited);
        dfs(row, col + 1, visited);
    }

    // TODO reconsider
    public CrosswordLayout crossover(CrosswordLayout partner) {
        CrosswordLayout child = new CrosswordLayout(new ArrayList<>());

        for (int i = 0; i < words.size(); i++) {
            CrosswordWord parent1Word = this.words.get(i);
            CrosswordWord parent2Word = partner.words.get(i);

            int row = random.nextBoolean() ? parent1Word.row : parent2Word.row;
            int col = random.nextBoolean() ? parent1Word.col : parent2Word.col;
            int orientation = random.nextBoolean() ? parent1Word.orientation : parent2Word.orientation;

            child.words.add(new CrosswordWord(parent1Word.word, row, col, orientation));
        }

        return child;
    }

    public void mutate() {
        int wordIndex = random.nextInt(words.size());
        CrosswordWord word = words.get(wordIndex);

        // Randomly change word's position or orientation
        word.row = random.nextInt(GRID_SIZE);
        word.col = random.nextInt(GRID_SIZE);
        word.orientation = random.nextInt(2);
    }

    public CrosswordLayout copy() {
        CrosswordLayout copy = new CrosswordLayout(new ArrayList<>());
        for (CrosswordWord word : words) {
            copy.words.add(new CrosswordWord(word.word, word.row, word.col, word.orientation));
        }
        return copy;
    }

    public void printCrossword() {
        char[][] grid = new char[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = '-';
            }
        }

        for (CrosswordWord word : words) {
            char[] charArray = word.word.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                int row = word.row + (word.orientation == 0 ? 0 : i);
                int col = word.col + (word.orientation == 1 ? 0 : i);

                if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
                    grid[row][col] = charArray[i];
                } else {
                    // Handle out-of-bounds positions (this can happen after mutation)
                    System.out.println("Warning: Word '" + word.word + "' is out of bounds in the crossword grid.");
                }
            }
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }
}