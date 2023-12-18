import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrosswordLayout {
    private static final int PENALTY = 10; // penalty amount for the rules violation
    private static final int GRID_SIZE = 20; // size of the layout
    private static final char[][] grid = new char[GRID_SIZE][GRID_SIZE]; // the layout itself
    private static final Random random = new Random();
    public final List<CrosswordWord> words; // list of the current words in a crossword
    private int currentFitness; // cache storage for the fitness value

    /**
     * Crossword layout constructor
     * @param inputWords list of the current words
     */
    public CrosswordLayout(List<String> inputWords) {
        this.words = new ArrayList<>();

        // placing each word to the random position on the grid
        for (String word : inputWords) {
            int row;
            int col;
            int orientation = random.nextInt(2); // 0 for horizontal, 1 for vertical

            // Restriction to avoid out of bound words
            if (orientation == 0) {
                col = random.nextInt(GRID_SIZE - word.length() + 1);
                row = random.nextInt(GRID_SIZE);
            } else {
                col = random.nextInt(GRID_SIZE);
                row = random.nextInt(GRID_SIZE - word.length() + 1);
            }
            words.add(new CrosswordWord(word, row, col, orientation));
        }

        // Indicate that fitness value was not calculated yet
        currentFitness = -1;
    }

    /**
     * Gets the current fitness of the layout
     * @return current fitness
     */
    public int getCurrentFitness() {
        // If the value is not in the cache, recalculate fitness
        if (this.currentFitness < 0) {
            this.currentFitness = this.calculateFitness();
        }

        return this.currentFitness;
    }

    /**
     * Clears the grid from the words
     */
    private void resetGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = '-';
            }
        }
    }

    /**
     * Calculates fitness of the current crossword
     * @return fitness of the current crossword
     */
    private int calculateFitness() {
        int fitness = 0;

        fitness += overlapCheck(); // penalty for the overlapping words
        fitness += connectivityCheck(); // penalty for each disconnected part
        fitness += neighbouringWordsCheck(); // penalty for wrongly adjacent words

        return fitness;
    }

    /**
     * Checks if the current coordinate in the bounds of grid
     * @param row rows coordinate
     * @param col columns coordinate
     * @return true if the char in bounds, false otherwise
     */
    private boolean charInBounds(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

    /**
     * Searches for the word by given parameters
     * @param row rows coordinate
     * @param col columns coordinate
     * @param orientation orientation of the word
     * @return word corresponding to the parameters, null if not found
     */
    private CrosswordWord getWordByCoordinates(int row, int col, int orientation) {
        for (CrosswordWord word: words) {
            if (word.row == row && word.col == col && word.orientation == orientation) {
                return word;
            }
        }

        return null;
    }

    /**
     * Checks for existence of the crossing word.
     * Goes to the start of the neighbouring words and checks if the unifying word exists.
     *
     * @param currentWord word, having a neighbour
     * @param isFirstChar flag indicating the position of the word search
     * @return true if the crossing word exists, false otherwise
     */
    private boolean isCrossingWordAbsent(CrosswordWord currentWord, boolean isFirstChar) {
        int row;
        int col;
        // If the current word is horizontal, the vertical crossing word is needed
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

        } else { // if the current word is vertical, the horizontal crossing word is needed
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

    /**
     * Neighbouring words validation
     * @return penalty value for the invalid words neighborhood
     */
    private int neighbouringWordsCheck() {
        int penalty = 0;

        for (CrosswordWord word : words) {
            char[] charArray = word.word.toCharArray();

            // Counters of adjacent chars by all the directions
            int adjCharCounterSideUp = 0;
            int adjCharCounterSideDown = 0;
            int adjCharCounterSideLeft = 0;
            int adjCharCounterSideRight = 0;

            for (int charIdx = 0; charIdx < charArray.length; charIdx++) {
                int row;
                int col;

                // If the word is horizontal
                if (word.orientation == 0) {
                    row = word.row; // row is constant
                    col = word.col + charIdx; // traverse through the columns

                    if (charInBounds(row, col)) {
                        // First char check
                        if (charIdx == 0) {
                            // Check for the adjacent word from the left side
                            if (charInBounds(row, col - 1) && grid[row][col - 1] != '-') {
                                penalty += PENALTY; // penalty for adjacent word from the left
                            }

                            // Check for correctness neighbour from the top, if exists
                            if (charInBounds(row - 1, col) && grid[row - 1][col] != '-') {
                                if (isCrossingWordAbsent(word, true)) {
                                    penalty += PENALTY; // penalty for the forming new word
                                }
                            }
                        }
                        // Last char check
                        else if (charIdx == charArray.length - 1) {
                            // Check for the adjacent word from the right side
                            if (charInBounds(row, col + 1) && grid[row][col + 1] != '-') {
                                penalty += PENALTY; // penalty for adjacent word from the right
                            }

                            // Check for correctness neighbour from the top, if exists
                            if (charInBounds(row - 1, col) && grid[row - 1][col] != '-') {
                                if (isCrossingWordAbsent(word, false)) {
                                    penalty += PENALTY; // penalty for the forming new word
                                }
                            }
                        }

                        // Parallel words check
                        if (charInBounds(row - 1, col) && grid[row - 1][col] != '-') {
                            adjCharCounterSideUp++; // increment counter of adjacent chars from the upside
                            if (adjCharCounterSideUp > 1) {
                                penalty += PENALTY; // penalty for the upside adjacent word
                            }
                        } else if (charInBounds(row - 1, col) && grid[row - 1][col] == '-') {
                            adjCharCounterSideUp = 0; // resetting counter if no adjacent chars
                        }
                        if (charInBounds(row + 1, col) && grid[row + 1][col] != '-') {
                            adjCharCounterSideDown++; // increment counter of adjacent chars from the downside
                            if (adjCharCounterSideDown > 1) {
                                penalty += PENALTY; // penalty for the downside adjacent word
                            }
                        } else if (charInBounds(row + 1, col) && grid[row + 1][col] == '-') {
                            adjCharCounterSideDown = 0; // resetting counter if no adjacent chars
                        }
                    }
                } else { // if the word is vertical
                    row = word.row + charIdx; // traverse through the rows
                    col = word.col; // column is constant

                    if (charInBounds(row, col)) {
                        // First char check
                        if (charIdx == 0) {
                            // Check for the adjacent word from the upside
                            if (charInBounds(row - 1, col) && grid[row - 1][col] != '-') {
                                penalty += PENALTY; // penalty for adjacent word from the upside
                            }

                            // Check for correctness neighbour from the left, if exists
                            if (charInBounds(row, col - 1) && grid[row][col - 1] != '-') {
                                if (isCrossingWordAbsent(word, true)) {
                                    penalty += PENALTY; // penalty for the forming new word
                                }
                            }
                        }
                        // Last char check
                        else if (charIdx == charArray.length - 1) {
                            // Check for the adjacent word from the downside
                            if (charInBounds(row + 1, col) && grid[row + 1][col] != '-') {
                                penalty += PENALTY; // penalty for adjacent word from the downside
                            }

                            // Check for correctness neighbour from the left, if exists
                            if (charInBounds(row, col - 1) && grid[row][col - 1] != '-') {
                                if (isCrossingWordAbsent(word, false)) {
                                    penalty += PENALTY; // penalty for the forming new word
                                }
                            }
                        }

                        // Parallel check
                        if (charInBounds(row, col - 1) && grid[row][col - 1] != '-') {
                            adjCharCounterSideLeft++; // increment counter of adjacent chars from the left side
                            if (adjCharCounterSideLeft > 1) {
                                penalty += PENALTY; // penalty for the left side adjacent word
                            }
                        } else if (charInBounds(row, col - 1) && grid[row][col - 1] == '-') {
                            adjCharCounterSideLeft = 0; // resetting counter if no adjacent chars
                        }
                        if (charInBounds(row, col + 1) && grid[row][col + 1] != '-') {
                            adjCharCounterSideRight++; // increment counter of adjacent chars from the right side
                            if (adjCharCounterSideRight > 1) {
                                penalty += PENALTY; // penalty for the right adjacent word
                            }
                        } else if (charInBounds(row, col + 1) && grid[row][col + 1] == '-') {
                            adjCharCounterSideRight = 0; // resetting counter if no adjacent chars
                        }
                    }
                }
            }
        }

        return penalty;
    }

    /**
     * Checks for the overlapping words
     * @return penalty value for the overlapping words
     */
    private int overlapCheck() {
        int penalty = 0;
        resetGrid();

        // Traverse though the word list and find the wrong overlaps
        for (CrosswordWord word : words) {
            char[] charArray = word.word.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                int row = word.row + (word.orientation == 0 ? 0 : i);
                int col = word.col + (word.orientation == 1 ? 0 : i);

                if (charInBounds(row, col)) {
                    // Compare the value on the grid with the current word character
                    if (grid[row][col] != '-' && grid[row][col] != charArray[i]) {
                        penalty += PENALTY; // Penalty for overlapping different characters
                    }
                    grid[row][col] = charArray[i]; // appending the grid with a new char
                }
            }
        }

        return penalty;
    }

    /**
     * Checks for the connectivity of the words
     * @return penalty value for the separated parts of the crossword
     */
    private int connectivityCheck() {
        boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
        int connectedComponents = 0;

        // DFS algorithm usage to determine the number of separate components
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!visited[i][j] && grid[i][j] != '-') {
                    dfs(i, j, visited);
                    connectedComponents++;
                }
            }
        }

        // If crossword consists of more than one separated components,
        // give penalty per each component.
        return connectedComponents > 1 ? connectedComponents * PENALTY : 0;
    }

    /**
     * DFS algorithm helping to determining the crossword connectivity
     * @param row rows coordinate
     * @param col columns coordinate
     * @param visited array of visited cells
     */
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

    /**
     * Crossover of the two parents
     * @param partner partner layout
     * @return offspring
     */
    public CrosswordLayout crossover(CrosswordLayout partner) {
        // Offspring initialization
        CrosswordLayout child = new CrosswordLayout(new ArrayList<>());

        // Traverse through the word list
        for (int i = 0; i < words.size(); i++) {
            CrosswordWord parent1Word = this.words.get(i);
            CrosswordWord parent2Word = partner.words.get(i);

            // Randomly pick a parent of the current gene (word)
            boolean wordParen = random.nextBoolean();

            // Copy parameters to the child word list
            int row = wordParen ? parent1Word.row : parent2Word.row;
            int col = wordParen ? parent1Word.col : parent2Word.col;
            int orientation = wordParen ? parent1Word.orientation : parent2Word.orientation;
            child.words.add(new CrosswordWord(parent1Word.word, row, col, orientation));
        }

        return child;
    }

    /**
     * Mutating the current individual
     */
    public void mutate() {
        // Select random gene (word)
        int wordIndex = random.nextInt(words.size());
        CrosswordWord word = words.get(wordIndex);

        // Randomly change word's position or orientation
        word.orientation = random.nextInt(2);
        if (word.orientation == 0) {
            word.col = random.nextInt(GRID_SIZE - word.word.length() + 1);
            word.row = random.nextInt(GRID_SIZE);
        } else {
            word.col = random.nextInt(GRID_SIZE);
            word.row = random.nextInt(GRID_SIZE - word.word.length() + 1);
        }

        // Update fitness, indicating the need for recalculation
        this.currentFitness = -1;
    }

    /**
     * Creates a deep copy of the crossword layout
     * @return copy of the current crossword
     */
    public CrosswordLayout copy() {
        CrosswordLayout copy = new CrosswordLayout(new ArrayList<>());
        for (CrosswordWord word : words) {
            copy.words.add(new CrosswordWord(word.word, word.row, word.col, word.orientation));
        }
        return copy;
    }

    /**
     * Prints crossword layout to the console
     */
    public void printCrossword() {
        // Grid initialization
        char[][] grid = new char[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = '-';
            }
        }

        // Words placing
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
