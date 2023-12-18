import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    // Constants for genetic algorithm parameters
    private static final int POPULATION_SIZE = 100; // Size of the population
    private static final double CROSSOVER_RATE = 0.9; // Crossover rate
    private static final double TOURNAMENT_RATIO = 0.1; // Tournament ratio
    private static final double MUTATION_RATE = 1; // Mutation rate
    private static final int RESTART_GENERATION = 100000; // Restart generation threshold
    private static final Random random = new Random();
    private static final List<String> words = new ArrayList<>(); // List of the current words
    private static final String INPUTS_DIR_NAME = "inputs"; // Input directory name
    private static final String OUTPUTS_DIR_NAME = "outputs"; // Output directory name
    private static String outputsPath = ""; // Storage for the output path
    private static String outputFileName = ""; // Name of the current output file

    /**
     * Main method to start the program
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        start();
    }

    /**
     * Initializes necessary directories and processes input files
     * Launches the crossword building algorithm
     */
    private static void start() {
        // Construct the full path to the "outputs" directory
        outputsPath = getCurrentPath() + OUTPUTS_DIR_NAME;

        File outputsDirectory = new File(outputsPath);

        // Create "outputs" directory if it doesn't exist
        if (!outputsDirectory.exists()) {
            boolean created = outputsDirectory.mkdirs();
            if (!created) {
                System.out.println("Failed to create the 'outputs' directory.");
                return;
            }
        }

        // Getting all the files from the input directory
        File[] files = getAllFiles();

        // Processing each input file
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    outputFileName = getOutputFileName(file);

                    // If words are read successfully, launch the algorithm
                    if (readAllWordsInFile(file)) {
                        buildCrossword();
                    }

                    // Update the list of words when the file is processed
                    words.clear();
                }
            }
        } else {
            System.out.println("No files found in the input directory.");
        }
    }

    /**
     * Builds the crossword using a genetic algorithm
     */
    private static void buildCrossword() {
        // The initial population initialization
        List<CrosswordLayout> population = initializePopulation();

        int generation = 0;
        int iteration = 0;
        System.out.println("Iteration: " + iteration);

        // Record the start time
        long startTime = System.currentTimeMillis();

        // Staring the loop of finding the optimal solution
        while (true) {
            // If the number of generations exceeds the allowed threshold
            // the algorithm is being restarted, where iteration indicates
            // the number of restarts
            if (generation >= RESTART_GENERATION) {
                population = initializePopulation();
                generation = 0;
                iteration++;
                System.out.println("Iteration: " + iteration);
            }

            // Getting the best layout from the population
            CrosswordLayout bestLayout = getBestLayout(population);

            // Getting the words layout from the population
            CrosswordLayout worstLayout = getWorstLayout(population);

            // Average fitness calculation
            double avgFitness = getAverageFitness(population);

            // If the best layout reaches 0 fitness, the algorithm terminates
            // providing the valid crossword layout
            if (bestLayout.getCurrentFitness() <= 0) {
                // Collecting the time statistics per test
                long endTime = System.currentTimeMillis();
                long elapsedTimeInSeconds = (endTime - startTime) / 1000;
                String formattedTime = formatTime(elapsedTimeInSeconds);

                // Results output
                System.out.println("Generation: " + generation + " | Average fitness: " + avgFitness + " | Worst fitness: " + worstLayout.getCurrentFitness() + " | Best fitness: " + bestLayout.getCurrentFitness());
                System.out.println("Time elapsed: " + formattedTime);
                bestLayout.printCrossword();
                System.out.println("Solution found!\n");

                // Writing the layout to the output file
                writeToOutputFile(bestLayout);
                break;
            }

            // Producing new generation in case of absence the optimal solution
            population = produceNewGeneration(population);
            generation++;
        }
    }

    /**
     * Reads all words from a file and adds them to the 'words' list
     * @param file The input file
     */
    private static boolean readAllWordsInFile(File file) {
        System.out.println("Processing file: " + file.getName());

        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            // Read all words from the file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() > 1) {
                    words.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Gets the current working directory
     * @return The current working directory path
     */
    private static String getCurrentPath() {
        // Get the package name
        String packageName = Main.class.getPackageName();

        // Replace dots with file separator
        String packagePath = packageName.replace('.', File.separatorChar);

        // Get the current working directory
        String currentPath = System.getProperty("user.dir");

        // Construct the full path to the "inputs" directory
        return currentPath + File.separator + "src" + File.separator + packagePath + File.separator;
    }

    /**
     * Gets all files in the "inputs" directory
     * @return An array of File objects representing input files
     */
    private static File[] getAllFiles() {
        String inputsPath = getCurrentPath() + INPUTS_DIR_NAME;

        File inputDirectory = new File(inputsPath);

        if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
            System.out.println("Input directory does not exist or is not a directory.");
            return new File[0]; // Return an empty array to indicate no files found
        }

        return inputDirectory.listFiles();
    }

    /**
     * Generates the output file name based on the input file name
     * @param inputFile The input file
     * @return The corresponding output file name
     */
    private static String getOutputFileName(File inputFile) {
        String inputFileName = inputFile.getName();
        return inputFileName.replace("input", "output");
    }

    /**
     * Writes the result layout to the output file
     * @param resultLayout The crossword layout to write to the output file
     */
    private static void writeToOutputFile(CrosswordLayout resultLayout) {
        File outputFile = new File(outputsPath, outputFileName);

        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            for (CrosswordWord word : resultLayout.words) {
                // Format the word information and write it to a new line
                String line = String.format("%d %d %d %s", word.row, word.col, word.orientation, word.word);
                fileWriter.write(line + System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Failed to create the output file: " + e.getMessage());
        }
    }

    /**
     * Formats time in HH:mm:ss
     * @param seconds The time in seconds
     * @return Formatted time string
     */
    private static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long remainder = seconds % 3600;
        long minutes = remainder / 60;
        long secs = remainder % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Produces new generation based on the current population
     * @param population current population
     * @return new generation
     */
    private static List<CrosswordLayout> produceNewGeneration(List<CrosswordLayout> population) {
        List<CrosswordLayout> newGeneration = new ArrayList<>();

        // Adding random individual to introduce some diversity
        // to the population
        newGeneration.add(new CrosswordLayout(words));

        for (int i = 1; i < POPULATION_SIZE; i++) {
            // Parents selection
            CrosswordLayout parent1 = selectParent(population);
            CrosswordLayout parent2 = selectParent(population);

            // Crossover
            CrosswordLayout child = crossover(parent1, parent2);

            // Mutation
            mutate(child);

            // Appending new generation with the new individual
            newGeneration.add(child);
        }

        return newGeneration;
    }

    /**
     * Initializes population with the current list of words
     * @return initial population
     */
    private static List<CrosswordLayout> initializePopulation() {
        List<CrosswordLayout> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new CrosswordLayout(words));
        }
        return population;
    }

    /**
     * Calculates the average fitness for the particular population
     * @param population current population
     * @return average fitness of the current population
     */
    private static double getAverageFitness(List<CrosswordLayout> population) {
        int totalFitness = 0;

        for (CrosswordLayout crosswordLayout : population) {
            totalFitness += crosswordLayout.getCurrentFitness();
        }

        return (double)totalFitness / population.size();
    }

    /**
     * Finding the best layout with the minimal fitness value
     * @param population current population
     * @return best current crossword layout
     */
    private static CrosswordLayout getBestLayout(List<CrosswordLayout> population) {
        CrosswordLayout bestLayout = population.get(0);
        for (CrosswordLayout layout : population) {
            if (layout.getCurrentFitness() < bestLayout.getCurrentFitness()) {
                bestLayout = layout;
            }
        }
        return bestLayout;
    }

    /**
     * Finding the worst layout with the maximal fitness value
     * @param population current population
     * @return worst crossword layout
     */
    private static CrosswordLayout getWorstLayout(List<CrosswordLayout> population) {
        CrosswordLayout worstLayout = population.get(0);
        for (CrosswordLayout layout : population) {
            if (layout.getCurrentFitness() > worstLayout.getCurrentFitness()) {
                worstLayout = layout;
            }
        }
        return worstLayout;
    }

    /**
     * Selects parent based on the roulette tournament principle.
     * Random sample is chosen from the current population.
     * The best individual from the sample have a right to become a parent.
     * @param population current population
     * @return selected parent
     */
    private static CrosswordLayout selectParent(List<CrosswordLayout> population) {
        // Forming a sample tournament size based on tournament ratio
        int tournamentSize = (int)(POPULATION_SIZE * TOURNAMENT_RATIO);

        // Selecting random species to the tournament
        // Finding the optimal local layout
        CrosswordLayout bestLayout = population.get(random.nextInt(population.size()));
        for (int i = 1; i < tournamentSize; i++) {
            CrosswordLayout candidate = population.get(random.nextInt(population.size()));
            if (candidate.getCurrentFitness() < bestLayout.getCurrentFitness()) {
                bestLayout = candidate;
            }
        }
        return bestLayout;
    }

    /**
     * Crossover of two parents with a declared crossover rate
     * @param parent1 1st parent layout
     * @param parent2 2d parent layout
     * @return offspring or the copy of the parent
     */
    private static CrosswordLayout crossover(CrosswordLayout parent1, CrosswordLayout parent2) {
        if (random.nextDouble() < CROSSOVER_RATE) {
            return parent1.crossover(parent2);
        } else {
            return parent1.copy();
        }
    }

    /**
     * Mutation of the crossword with a declared mutation rate
     * @param layout mutating crossword layout
     */
    private static void mutate(CrosswordLayout layout) {
        if (random.nextDouble() < MUTATION_RATE) {
            layout.mutate();
        }
    }
}