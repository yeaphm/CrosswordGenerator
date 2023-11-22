package assignment2.students.EfimPuzhalov;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class EfimPuzhalov {
    private static final int POPULATION_SIZE = 100; // 100
    private static final double CROSSOVER_RATE = 0.9; // 0.9
    private static final double MUTATION_RATE = 1;
    private static final int RESTART_GENERATION = 100000;
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
        int iteration = 1;
        System.out.println("Iteration: " + iteration);

        while (true) {
            if (generation >= RESTART_GENERATION) {
                population = initializePopulation();
                generation = 0;
                iteration++;
                System.out.println("Iteration: " + iteration);
            }

            // Getting the best layout recalculating fitness
            CrosswordLayout bestLayout = getBestLayout(population);

            double avgFitness = getAverageFitness(population);

            if (bestLayout.getCurrentFitness() <= 0) {
                System.out.println("Generation: " + generation + " | Average fitness: " + avgFitness + " | Best fitness: " + bestLayout.calculateFitness());
                bestLayout.printCrossword();
                System.out.println("Solution found!");
                break;
            }

            population = produceNewGeneration(population);
            generation++;
        }
    }

    private static List<CrosswordLayout> produceNewGeneration(List<CrosswordLayout> population) {
        List<CrosswordLayout> newGeneration = new ArrayList<>();
        newGeneration.add(new CrosswordLayout(words));

        for (int i = 1; i < POPULATION_SIZE; i++) {
            CrosswordLayout parent1 = selectParent(population);
            CrosswordLayout parent2 = selectParent(population);
            CrosswordLayout child = crossover(parent1, parent2);
            mutate(child);
            newGeneration.add(child);
        }

        return newGeneration;
    }

    private static List<CrosswordLayout> produceNewGeneration2(List<CrosswordLayout> population) {
        population.sort(Comparator.comparingInt(CrosswordLayout::getCurrentFitness));

        List<CrosswordLayout> children = new ArrayList<>();
        List<CrosswordLayout> parents = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            parents.add(population.get(i));
        }

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if (i != j) {
                    CrosswordLayout child = crossover(parents.get(i), parents.get(j));
                    mutate(child);
                    children.add(child);
                }
            }
        }

        children.sort(Comparator.comparingInt(CrosswordLayout::calculateFitness));

        List<CrosswordLayout> newGeneration = new ArrayList<>();

        int currentParent = 0;
        int currentChild = 0;
        for (int i = 0; i < POPULATION_SIZE; i++) {
            if (currentParent < 25 && parents.get(currentParent).getCurrentFitness() < children.get(currentChild).getCurrentFitness()) {
                newGeneration.add(parents.get(currentParent));
                currentParent++;
            } else {
                newGeneration.add(children.get(currentChild));
                currentChild++;
            }
        }

        return newGeneration;
    }

    private static List<CrosswordLayout> initializePopulation() {
        List<CrosswordLayout> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new CrosswordLayout(words));
        }
        return population;
    }

    private static double getAverageFitness(List<CrosswordLayout> population) {
        int totalFitness = 0;

        for (CrosswordLayout crosswordLayout : population) {
            totalFitness += crosswordLayout.getCurrentFitness();
        }

        return (double)totalFitness / population.size();
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

    private static CrosswordLayout selectParent(List<CrosswordLayout> population) {
        int tournamentSize = 10;
        CrosswordLayout bestLayout = population.get(random.nextInt(population.size()));
        for (int i = 1; i < tournamentSize; i++) {
            CrosswordLayout candidate = population.get(random.nextInt(population.size()));
            if (candidate.getCurrentFitness() < bestLayout.getCurrentFitness()) {
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

    private static CrosswordLayout getWorstLayout(List<CrosswordLayout> population) {
        CrosswordLayout worstLayout = population.get(0);
        for (int i = 1; i < population.size(); i++) {
            CrosswordLayout candidate = population.get(i);
            if (candidate.getCurrentFitness() > worstLayout.getCurrentFitness()) {
                worstLayout = candidate;
            }
        }
        return worstLayout;
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
    private int currentFitness;

    public CrosswordLayout(List<String> inputWords) {
        this.words = new ArrayList<>();
        for (String word : inputWords) {
            int row = random.nextInt(GRID_SIZE - 1);
            int col = random.nextInt(GRID_SIZE - 1);
            int orientation = random.nextInt(2); // 0 for horizontal, 1 for vertical
            words.add(new CrosswordWord(word, row, col, orientation));
        }
    }

    public int getCurrentFitness() {
        return currentFitness;
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

        this.currentFitness = fitness;

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

    private CrosswordWord getWordByCoordinates(int row, int col, int orientation) {
        for (CrosswordWord word: words) {
            if (word.row == row && word.col == col && word.orientation == orientation) {
                return word;
            }
        }

        return null;
    }

    private boolean isCrossingWordAbsent(CrosswordWord currentWord, boolean isFirstChar) {
        int row;
        int col;
        if (currentWord.orientation == 0) {
            row = currentWord.row - 1;
            if (isFirstChar) {
                col = currentWord.col;
            } else {
                col = currentWord.col + currentWord.word.length() - 1;
            }

            while (charInBounds(row, col) && grid[row][col] != '-') {
                CrosswordWord crossingWord = getWordByCoordinates(row, col, 1);

                if (crossingWord != null && (crossingWord.row + crossingWord.word.length() - 1) >= currentWord.row) {
                    return false;
                }

                row -= 1;
            }

        } else {
            if (isFirstChar) {
                row = currentWord.row;
            } else {
                row = currentWord.row + currentWord.word.length() - 1;
            }

            col = currentWord.col - 1;
            while (charInBounds(row, col) && grid[row][col] != '-') {
                CrosswordWord crossingWord = getWordByCoordinates(row, col, 0);

                if (crossingWord != null && (crossingWord.col + crossingWord.word.length() - 1) >= currentWord.col) {
                    return false;
                }

                col -= 1;
            }

        }

        return true;
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

                            // Check for crossing word
                            if (charInBounds(row - 1, col) && grid[row - 1][col] != '-') {
                                if (isCrossingWordAbsent(word, true)) {
                                    penalty += 10;
                                }
                            }
                        }
                        else if (charIdx == charArray.length - 1) { // Last char check
                            if (charInBounds(row, col + 1) && grid[row][col + 1] != '-') {
                                penalty += 10; // penalty for adjacent word from the right
                            }

                            // Check for crossing word
                            if (charInBounds(row - 1, col) && grid[row - 1][col] != '-') {
                                if (isCrossingWordAbsent(word, false)) {
                                    penalty += 10;
                                }
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

                            // Check for crossing word
                            if (charInBounds(row, col - 1) && grid[row][col - 1] != '-') {
                                if (isCrossingWordAbsent(word, true)) {
                                    penalty += 10;
                                }
                            }
                        }
                        else if (charIdx == charArray.length - 1) { // Last char check
                            if (charInBounds(row + 1, col) && grid[row + 1][col] != '-') {
                                penalty += 10; // penalty for adjacent word from the down
                            }

                            // Check for crossing word
                            if (charInBounds(row, col - 1) && grid[row][col - 1] != '-') {
                                if (isCrossingWordAbsent(word, false)) {
                                    penalty += 10;
                                }
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

    // TODO check the best option
    public CrosswordLayout crossover(CrosswordLayout partner) {
        CrosswordLayout child = new CrosswordLayout(new ArrayList<>());

        for (int i = 0; i < words.size(); i++) {
            CrosswordWord parent1Word = this.words.get(i);
            CrosswordWord parent2Word = partner.words.get(i);

            boolean wordParen = random.nextBoolean();

            int row = wordParen ? parent1Word.row : parent2Word.row;
            int col = wordParen ? parent1Word.col : parent2Word.col;
            int orientation = wordParen ? parent1Word.orientation : parent2Word.orientation;

            child.words.add(new CrosswordWord(parent1Word.word, row, col, orientation));
        }

        return child;
    }

    public void mutate() {
        int wordIndex = random.nextInt(words.size());
        CrosswordWord word = words.get(wordIndex);

        // Randomly change word's position or orientation
        word.row = random.nextInt(GRID_SIZE - 1);
        word.col = random.nextInt(GRID_SIZE - 1);
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