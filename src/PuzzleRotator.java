/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

/**
 *
 * @author Tom
 */
public class PuzzleRotator {
    public static int[][] rotatePuzzle(String filename) {
        int[][] puzzle = null;
        int[][] copyOfPuzzle = null;
        try (Scanner scanner = new Scanner(new File(Objects.requireNonNull(Main.class.getResource(filename)).getFile()))) {
            int size = scanner.nextInt();
            puzzle = new int[size][size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    puzzle[i][j] = scanner.nextInt();
                }
            }
            
            File file = new File((Main.class.getResource(filename)).getFile());
            String path = file.getName().substring(0, file.getName().length() - 4).concat("-rotated.txt");
            FileWriter writer = new FileWriter(new File(file.getParent() + "/" + path));
            writer.write(size + "\n");
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    writer.write(puzzle[size - j - 1][i] + "");
                    if (j != size - 1)
                        writer.write(" ");
                }
                if (i != size - 1)
                    writer.write("\n");
            }
            writer.close();
            copyOfPuzzle = new int[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    copyOfPuzzle[i][j] = puzzle[size - j - 1][i];
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File not found.");
        } catch (IOException ex) {
            System.err.println("File cannot be written.");
        }
        return copyOfPuzzle;
    }
}
