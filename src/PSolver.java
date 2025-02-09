import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tom
 */
public class PSolver implements Solver {
    private int[] puzzle;
    private int size;
    private int sizeOfBox;
    private final Set<Integer> allNumbers = new HashSet<>();
    private final List<Set<Integer>> possibleNums = new LinkedList<>();
    private final List<Set<Integer>> numsCanFill = new LinkedList<>();
    private final List<Point> positionChanged = new LinkedList<>();
    private enum WriteBoxResult {
        UNSOLVABLE, CHANGED, CHECKED
    };

    @Override
    public int[][] solve() {
        if (!hasPuzzle())
            return null;
        if (writeConfidentNums(0, 0, false))
            if (writeNumbers(0, 0)) {
                positionChanged.clear();
                return getPuzzle();
            }
        System.out.println("The Sudoku puzzle cannot be solved");
        return null;
    }

    @Override
    public void setPuzzle(int[][] puzzle) {
        if (puzzle == null) {
            System.out.println("Null Sudoku puzzle has been input");
            return;
        }
        int sqrtOfSize = (int)Math.sqrt(puzzle.length);
        if (sqrtOfSize != Math.sqrt(puzzle.length)) {
            System.out.println("Invalid Sudoku puzzle size has been input");
            return;
        }
        final int expectExistence = 3;
        for (int row = 0; row < puzzle.length; row++) {
            for (int column = 0; column < puzzle[0].length; column++) {
                if (puzzle[row][column] != 0) {
                    int colBox = column / sqrtOfSize;
                    int rowBox = row / sqrtOfSize;
                    int actualExistence = 0;
                    for (int i = 0; i < puzzle.length; i++) {
                        if (puzzle[row][column] == puzzle[row][i])
                            actualExistence += 1;
                        if (puzzle[row][column] == puzzle[i][column])
                            actualExistence += 1;
                        if (puzzle[row][column] == puzzle[rowBox * sqrtOfSize + i / sqrtOfSize][colBox * sqrtOfSize + i % sqrtOfSize])
                            actualExistence += 1;
                    }
                    if (actualExistence > expectExistence) {
                        System.out.println("Invalid Sukoku puzzle numbers has been input");
                        return;
                    }
                }
            }
        }
        size = puzzle.length;
        sizeOfBox = sqrtOfSize;
        this.puzzle = new int[size * size];
        allNumbers.clear();
        numsCanFill.clear();
        possibleNums.clear();
        for (int i = 0; i < size; i++) {
            allNumbers.add(i + 1);
            numsCanFill.add(new HashSet<>());
            for (int j = 0; j < size; j++) {
                possibleNums.add(new HashSet<>());
                this.puzzle[i * size + j] = puzzle[i][j];
            }
        }
    }

    @Override
    public int[][] getPuzzle() {
        if (!hasPuzzle())
            return null;
        int newPuzzle[][] = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                newPuzzle[i][j] = puzzle[i * size + j];
        return newPuzzle;
    }
    
    private boolean hasPuzzle() {
        if (puzzle == null) {
            System.out.println("No puzzle is in the solver");
            return false;
        }
        return true;
    }
    
    private boolean writeConfidentNums(int row, int column, boolean afterGuess) {
        if (row == size)
            return true;
        WriteBoxResult result = WriteBoxResult.CHECKED;
        for (int i = 0; i < 3; i++) {
            switch (i) {
                case 0:
                    if (row % sizeOfBox + column % sizeOfBox == 0)
                        result = writeNumsInBox(row, column, afterGuess);
                    break;
                case 1:
                    if (column == 0)
                        result = writeNumsInRow(row, afterGuess);
                    break;
                default:
                    if (row == 0)
                        result = writeNumsInCol(column, afterGuess);
                    break;
            }
            if (result == WriteBoxResult.UNSOLVABLE)
                return false;
            if (result == WriteBoxResult.CHANGED)
                return writeConfidentNums(0, 0, afterGuess);
        }
        return writeNextConf(row, column, afterGuess);
    }
    
    private WriteBoxResult writeNumsInBox(int row, int column, boolean afterGuess) {
        boolean changed = false;
        for (int i = 0; i < size; i++)
            numsCanFill.get(i).clear();
        for (int i = 0; i < size; i++) {
            int posOfRow = row + i / sizeOfBox;
            int posOfCol = column + i % sizeOfBox;
            if (puzzle[posOfRow * size + posOfCol] != 0) {
                numsCanFill.get(puzzle[posOfRow * size + posOfCol] - 1).add(i);
                continue;
            }
            possibleNums.get(posOfRow * size + posOfCol).addAll(allNumbers);
            retainPossibleNums(possibleNums.get(posOfRow * size + posOfCol), posOfRow, posOfCol);
            if (possibleNums.get(posOfRow * size + posOfCol).isEmpty())
                return WriteBoxResult.UNSOLVABLE;
            if (possibleNums.get(posOfRow * size + posOfCol).size() == 1) {
                if (afterGuess) {
                    Point currentPoint = new Point(posOfRow, posOfCol);
                    positionChanged.add(currentPoint);
                }
                puzzle[posOfRow * size + posOfCol] = (int)possibleNums.get(posOfRow * size + posOfCol).iterator().next();
                changed = true;
            }
            for (int j = 0; j < size; j++)
                if (possibleNums.get(posOfRow * size + posOfCol).contains(j + 1))
                    numsCanFill.get(j).add(i);
        }
        for (int i = 0; i < size; i++) {
            if (numsCanFill.get(i).isEmpty())
                return WriteBoxResult.UNSOLVABLE;
            if (numsCanFill.get(i).size() == 1) {
                int posInBox = (int)numsCanFill.get(i).iterator().next();
                int posOfRow = row + posInBox / sizeOfBox;
                int posOfCol = column + posInBox % sizeOfBox;
                int posOfPuzzle = posOfRow * size + posOfCol;
                if (puzzle[posOfPuzzle] == 0) {
                    if (afterGuess) {
                        Point currentPoint = new Point(posOfRow, posOfCol);
                        positionChanged.add(currentPoint);
                    }
                   puzzle[posOfPuzzle] = i + 1;
                   changed = true;
                }
            }
        }
        if (changed)
            return WriteBoxResult.CHANGED;
        return WriteBoxResult.CHECKED;
    }
    
    private WriteBoxResult writeNumsInRow(int row, boolean afterGuess) {
        boolean changed = false;
        for (int i = 0; i < size; i++)
            numsCanFill.get(i).clear();
        for (int i = 0; i < size; i++) {
            int posOfRow = row;
            int posOfCol = i;
            if (puzzle[posOfRow * size + posOfCol] != 0) {
                numsCanFill.get(puzzle[posOfRow * size + posOfCol] - 1).add(i);
                continue;
            }
            possibleNums.get(posOfRow * size + posOfCol).addAll(allNumbers);
            retainPossibleNums(possibleNums.get(posOfRow * size + posOfCol), posOfRow, posOfCol);
            if (possibleNums.get(posOfRow * size + posOfCol).isEmpty())
                return WriteBoxResult.UNSOLVABLE;
            if (possibleNums.get(posOfRow * size + posOfCol).size() == 1) {
                if (afterGuess) {
                    Point currentPoint = new Point(posOfRow, posOfCol);
                    positionChanged.add(currentPoint);
                }
                puzzle[posOfRow * size + posOfCol] = (int)possibleNums.get(posOfRow * size + posOfCol).iterator().next();
                changed = true;
            }
            for (int j = 0; j < size; j++)
                if (possibleNums.get(posOfRow * size + posOfCol).contains(j + 1))
                    numsCanFill.get(j).add(i);
        }
        for (int i = 0; i < size; i++) {
            if (numsCanFill.get(i).isEmpty())
                return WriteBoxResult.UNSOLVABLE;
            if (numsCanFill.get(i).size() == 1) {
                int posInRow = (int)numsCanFill.get(i).iterator().next();
                int posOfRow = row;
                int posOfCol = posInRow;
                int posOfPuzzle = row * size + posInRow;
                if (puzzle[posOfPuzzle] == 0) {
                    if (afterGuess) {
                        Point currentPoint = new Point(posOfRow, posOfCol);
                        positionChanged.add(currentPoint);
                    }
                   puzzle[posOfPuzzle] = i + 1;
                   changed = true;
                }
            }
        }
        if (changed)
            return WriteBoxResult.CHANGED;
        return WriteBoxResult.CHECKED;
    }
    
    private WriteBoxResult writeNumsInCol(int column, boolean afterGuess) {
        boolean changed = false;
        for (int i = 0; i < size; i++)
            numsCanFill.get(i).clear();
        for (int i = 0; i < size; i++) {
            int posOfRow = i;
            int posOfCol = column;
            if (puzzle[posOfRow * size + posOfCol] != 0) {
                numsCanFill.get(puzzle[posOfRow * size + posOfCol] - 1).add(i);
                continue;
            }
            possibleNums.get(posOfRow * size + posOfCol).addAll(allNumbers);
            retainPossibleNums(possibleNums.get(posOfRow * size + posOfCol), posOfRow, posOfCol);
            if (possibleNums.get(posOfRow * size + posOfCol).isEmpty())
                return WriteBoxResult.UNSOLVABLE;
            if (possibleNums.get(posOfRow * size + posOfCol).size() == 1) {
                if (afterGuess) {
                    Point currentPoint = new Point(posOfRow, posOfCol);
                    positionChanged.add(currentPoint);
                }
                puzzle[posOfRow * size + posOfCol] = (int)possibleNums.get(posOfRow * size + posOfCol).iterator().next();
                changed = true;
            }
            for (int j = 0; j < size; j++)
                if (possibleNums.get(posOfRow * size + posOfCol).contains(j + 1))
                    numsCanFill.get(j).add(i);
        }
        for (int i = 0; i < size; i++) {
            if (numsCanFill.get(i).isEmpty())
                return WriteBoxResult.UNSOLVABLE;
            if (numsCanFill.get(i).size() == 1) {
                int posInCol = (int)numsCanFill.get(i).iterator().next();
                int posOfRow = posInCol;
                int posOfCol = column;
                int posOfPuzzle = posInCol * size + column;
                if (puzzle[posOfPuzzle] == 0) {
                    if (afterGuess) {
                        Point currentPoint = new Point(posOfRow, posOfCol);
                        positionChanged.add(currentPoint);
                    }
                   puzzle[posOfPuzzle] = i + 1;
                   changed = true;
                }
            }
        }
        if (changed)
            return WriteBoxResult.CHANGED;
        return WriteBoxResult.CHECKED;
    }
    
    private boolean writeNextConf(int row, int column, boolean afterGuess) {
        if (column < size - 1)
            return writeConfidentNums(row, column + 1, afterGuess);
        else return writeConfidentNums(row + 1, 0, afterGuess);
    }
    
    private boolean writeNumbers(int row, int column) {
        if (row == size)
            return true;
        if (puzzle[row * size + column] != 0)
            return writeNext(row, column);
        possibleNums.get(row * size + column).addAll(allNumbers);
        retainPossibleNums(possibleNums.get(row * size + column), row, column);
        Iterator<Integer> it = possibleNums.get(row * size + column).iterator();
        Point currentPoint = new Point(row, column);
        positionChanged.add(currentPoint);
        while (it.hasNext()) {
            puzzle[row * size + column] = it.next();
            if (writeConfidentNums(0, 0, true))
                if (writeNext(row, column))
                    return true;
            while (positionChanged.lastIndexOf(currentPoint) + 1 != positionChanged.size()) {
                Point pointToBeRemoved = positionChanged.get(positionChanged.lastIndexOf(currentPoint) + 1);
                int x = pointToBeRemoved.x;
                int y = pointToBeRemoved.y;
                positionChanged.remove(pointToBeRemoved);
                puzzle[x * size + y] = 0;
            }
        }
        Point pointToBeRemoved = positionChanged.get(positionChanged.lastIndexOf(currentPoint));
        int x = pointToBeRemoved.x;
        int y = pointToBeRemoved.y;
        positionChanged.remove(pointToBeRemoved);
        puzzle[x * size + y] = 0;
        return false;
    }
    
    private boolean writeNext(int row, int column) {
        if (column < size - 1)
            return writeNumbers(row, column + 1);
        else return writeNumbers(row + 1, 0);
    }
    
    private void retainPossibleNums(Set<Integer> nums, int row, int column) {
        int colBox = column / sizeOfBox;
        int rowBox = row / sizeOfBox;
        for (int i = 0; i < size; i++) {
            nums.remove(puzzle[row * size + i]);
            nums.remove(puzzle[i * size + column]);
            nums.remove(puzzle[(rowBox * sizeOfBox + i / sizeOfBox) * size + colBox * sizeOfBox + i % sizeOfBox]);
        }
    }
    
}
