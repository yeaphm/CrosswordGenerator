public class CrosswordWord {
    String word; // the word itself
    int row; // coordinate by rows
    int col; // coordinate by columns
    int orientation; // orientation value 0 - horizontal, 1 - vertical

    public CrosswordWord(String word, int row, int col, int orientation) {
        this.word = word;
        this.row = row;
        this.col = col;
        this.orientation = orientation;
    }
}
