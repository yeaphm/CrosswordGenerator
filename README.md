
# Crossword Generator

Implemented for the Intro to AI course at Innopolis University.

**Description**

The project is aimed to construct a connected crosswords from the provided word list using genetic algoritm.


## Authors

- [@yeaphm](https://www.github.com/yeaphm)


## Installation

- Clone the project from [GitHub](https://github.com/yeaphm/CrosswordGenerator)
- Use JDK-17 to launch the project
    
## Usage/Examples

* The input is implemented via "input" folder, where you can put your .txt files with list of words for the crossword. (Some of the inputs are already provided as an example.) The inputs are represented by M *.txt files ending with a new line character.

* The main output implemented in console, building a crossword with some statistics provided.

* The outputs directory created automatically. The outputs are represented by *.txt files ending with a new line character. The output contains M lines, corresponding to each input word. Each line contain 3 integers: 
    * Crossword’s row number X of the word’s first symbol (𝑋 ∈ [0; 19])
    * Crossword’s column number Y of the word’s first symbol (𝑌 ∈ [0; 19])
    * Horizontal (0) or Vertical (1) location
    The numeration starts from the top left corner.




## Acknowledgements

 - [Teaching a stool to walk](https://habr.com/ru/articles/340772/)
 - [Machine Intelligence - Lecture 18 (Evolutionary Algorithms)](https://www.youtube.com/watch?v=3-NiZPbkr7A)


## License

The Crossword Generator is released under the [MIT](https://choosealicense.com/licenses/mit/). Feel free to use, modify, and distribute the project as per the terms of the license.
