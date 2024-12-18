package battleship;

import java.util.*;

public class Main {
    private static final int SIZE = 10;
    private static final String[] SHIP_NAMES = {"Aircraft Carrier", "Battleship", "Submarine", "Cruiser", "Destroyer"};
    private static final int[] SHIP_LENGTHS = {5, 4, 3, 3, 2};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Player player1 = new Player("Player 1", SIZE);
        Player player2 = new Player("Player 2", SIZE);

        System.out.println(player1.getName() + ", place your ships on the game field");
        char[][] emptyCellsP1 = GameUtils.initializeField();
        GameUtils.InitialPrintField(emptyCellsP1);

        player1.placeShips(SHIP_NAMES, SHIP_LENGTHS, scanner);

        passControl(scanner);

        System.out.println(player2.getName() + ", place your ships on the game field");
        char[][] emptyCellsP2 = GameUtils.initializeField();
        GameUtils.InitialPrintField(emptyCellsP2);

        player2.placeShips(SHIP_NAMES, SHIP_LENGTHS, scanner);

        passControl(scanner);

        System.out.println("The game starts!");
        while (true) {
            if (takeTurn(player1, player2, scanner)) break;
            if (takeTurn(player2, player1, scanner)) break;
        }
        scanner.close();
    }

    private static boolean takeTurn(Player current, Player opponent, Scanner scanner) {
        System.out.println(current.getName() + ", it's your turn:");
        current.printBoards(opponent);

        while (true) {
            System.out.print("> ");
            String shot = scanner.nextLine().trim();
            try {
                int[] coords = GameUtils.convertCoordinate(shot, SIZE);
                boolean hit = opponent.getBoard().takeShot(coords[0], coords[1]);
                if (hit) {
                    System.out.println("You hit a ship!");
                    if (opponent.allShipsSunk()) {
                        System.out.println("You sank the last ship. You won. Congratulations!");
                        return true;
                    } else if (!opponent.allShipsSunk()) {
                        System.out.println("You sank a ship!");
                    }
                } else {
                    System.out.println("You missed!");
                }
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage() + " Try again:");
            }
        }

        passControl(scanner);
        return false;
    }

    private static void passControl(Scanner scanner) {
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();
        System.out.print("\033[H\033[2J"); // Clears the screen
        System.out.flush();
    }
}

class Board {
    private char[][] field;
    private List<Ship> ships = new ArrayList<>();

    public Board(int size) {
        field = new char[size][size];
        for (char[] row : field) Arrays.fill(row, '~');
    }

    public void print() {
        GameUtils.printField(field);
    }

    public void printFogged() {
        GameUtils.printFoggedField(field);
    }

    public boolean takeShot(int row, int col) {
        if (field[row][col] == 'O') {
            field[row][col] = 'X';
            for (Ship ship : ships) {
                if (ship.hit(row, col)) {
                    return true;
                }
            }
        } else if (field[row][col] == '~') {
            field[row][col] = 'M';
        }
        return false;
    }

    public void addShip(Ship ship) {
        ships.add(ship);
    }

    public boolean allShipsSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    public char[][] getField() {
        return field;
    }
}

class Ship {
    private Set<String> positions = new HashSet<>();
    private Set<String> hits = new HashSet<>();

    public Ship(List<int[]> coordinates) {
        for (int[] coord : coordinates) {
            positions.add(coord[0] + "," + coord[1]);
        }
    }

    public boolean hit(int row, int col) {
        String position = row + "," + col;
        if (positions.contains(position)) {
            hits.add(position);
            return true;
        }
        return false;
    }

    public boolean isSunk() {
        return positions.equals(hits);
    }
}

class ShipPlacer {
    public static boolean placeShip(Board board, String start, String end, int length) {
        int[] startCoords = GameUtils.parseCoordinates(start);
        int[] endCoords = GameUtils.parseCoordinates(end);

        // Pass the board's internal char[][] instead of the Board object
        if (!GameUtils.isValidPlacement(startCoords, endCoords, board.getField(), length)) {
            return false;
        }

        // Get a list of int[] coordinates
        List<int[]> rawShipCoords = GameUtils.getCoordinatesBetween(startCoords, endCoords);
        Ship ship = new Ship(rawShipCoords);
        board.addShip(ship);

        for(int[] coords : rawShipCoords) {
            board.getField()[coords[0]][coords[1]] = 'O';
        }
        return true;
    }
}

class Player {
    private String name;
    private Board board;
    private Board foggedBoard;

    public Player(String name, int size) {
        this.name = name;
        this.board = new Board(size);
        this.foggedBoard = new Board(size);
    }

    public String getName() {
        return name;
    }

    public Board getBoard() {
        return board;
    }

    public void printBoards(Player opponent) {
        opponent.getBoard().printFogged();
        System.out.println("---------------------");
        board.print();
    }

    public void placeShips(String[] shipNames, int[] shipLengths, Scanner scanner) {

        for (int i = 0; i < shipNames.length; i++) {
            while (true) {
                System.out.printf("Enter the coordinates of the %s (%d cells):%n", shipNames[i], shipLengths[i]);
                String input = scanner.nextLine().trim();
                //String[] coords = input.split("\\s+");

                String[] coordinates = input.split("\\s+");
                if (coordinates.length != 2) {
                    System.out.println("Invalid, you must enter two coordinates!");
                    continue;
                }

                String start = coordinates[0];
                String end = coordinates[1];

                char[][] boardArray = board.getField();

                String error = GameUtils.validateCoordinates(start, end, boardArray, shipLengths[i], shipNames[i]);
                if (error != null) {
                    System.out.println(error);
                    continue;
                }

                if (ShipPlacer.placeShip(board, coordinates[0], coordinates[1], shipLengths[i])) {
                    board.print();
                    break;
                }



//                if (coordinates.length == 2 && ShipPlacer.placeShip(board, coordinates[0], coordinates[1], shipLengths[i])) {
//                    board.print();
//                    break;
//                } else {
//                    System.out.println("Error! Wrong length of the " + Arrays.toString(shipNames) + "! Try again:");
//                }
            }
        }
    }

    public boolean allShipsSunk() {
        return board.allShipsSunk();
    }
}

class GameUtils {

    public static final int SIZE = 10;
    public static final char TILDA = '~';
    public static final char SHIP = 'O';
    public static final char HIT = 'X';
    public static final char MISS = 'M';

    /**
     * Initializes a game board with all cells set to '~'.
     */
    public static char[][] initializeField() {
        char[][] field = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(field[i], TILDA);
        }
        return field;
    }

    public static void InitialPrintField(char[][] field) {
        System.out.println();
        System.out.print("  "); // Space before column numbers
        for (int i = 1; i <= 10; i++) {
            System.out.print(i + (i < 10 ? " " : "")); //Align single-digit number
        }
        System.out.println();

        for (int i = 0; i < SIZE; i++) {
            System.out.print((char) ('A' + i) + " "); //Row label
            for (int j = 0; j < SIZE; j++) {
                System.out.print(field[i][j] + " ");
            }
            System.out.println(); //Move to the next row
        }
        System.out.println();
    }

    /**
     * Prints a fully visible game board showing all ships and hits/misses.
     */
    public static void printField(char[][] field) {
        printFormattedField(field, false);
    }

    /**
     * Prints a fogged game board showing only hits and misses but hiding ships.
     */
    public static void printFoggedField(char[][] field) {
        printFormattedField(field, true);
    }

    /**
     * Retrieves all coordinates between the start and end points (inclusive).
     */
    public static List<int[]> getCoordinatesBetween(int[] startCoords, int[] endCoords) {
        List<int[]> coordinates = new ArrayList<>();
        int startRow = startCoords[0], startCol = startCoords[1];
        int endRow = endCoords[0], endCol = endCoords[1];

        if (startRow == endRow) {
            // Horizontal placement
            for (int col = Math.min(startCol, endCol); col <= Math.max(startCol, endCol); col++) {
                coordinates.add(new int[]{startRow, col});
            }
        } else if (startCol == endCol) {
            // Vertical placement
            for (int row = Math.min(startRow, endRow); row <= Math.max(startRow, endRow); row++) {
                coordinates.add(new int[]{row, startCol});
            }
        }

        return coordinates;
    }

    private static String convertToCoordinate(int row, int col) {
        return (char) ('A' + row) + String.valueOf(col + 1);
    }


    /**
     * Validates the placement of a ship on the board.
     */
    public static boolean isValidPlacement(int[] startCoords, int[] endCoords, char[][] board, int length) {
        int startRow = startCoords[0], startCol = startCoords[1];
        int endRow = endCoords[0], endCol = endCoords[1];

        // Check alignment
        if (startRow != endRow && startCol != endCol) {
            return false; // Not aligned
        }

        // Check length
        int calculatedLength = (startRow == endRow) ? Math.abs(endCol - startCol) + 1 : Math.abs(endRow - startRow) + 1;
        if (calculatedLength != length) {
            return false; // Incorrect length
        }

        // Check for overlaps and proximity
        List<int[]> coordinates = getCoordinatesBetween(startCoords, endCoords);
        for (int[] coord : coordinates) {
            int row = coord[0], col = coord[1];
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col + 1; j++) {
                    if (i >= 0 && i < SIZE && j >= 0 && j < SIZE && board[i][j] == SHIP) {
                        return false; // Overlap or too close
                    }
                }
            }
        }

        return true;
    }

    /**
     * Parses a coordinate string (e.g., "A1") into row and column indices.
     */
    public static int[] parseCoordinates(String coordinates) {
        int row = coordinates.charAt(0) - 'A';
        int col = Integer.parseInt(coordinates.substring(1)) - 1;
        return new int[]{row, col};
    }

    /**
     * Formats and prints a game board.
     *
     * @param field  The game board to print.
     * @param fogged If true, hide ship cells; if false, show all cells.
     */
    private static void printFormattedField(char[][] field, boolean fogged) {
        System.out.print("  ");
        for (int i = 1; i <= SIZE; i++) {
            System.out.print(i + (i < 10 ? " " : ""));
        }
        System.out.println();

        for (int i = 0; i < SIZE; i++) {
            System.out.print((char) ('A' + i) + " ");
            for (int j = 0; j < SIZE; j++) {
                char cell = field[i][j];
                if (fogged && cell == SHIP) {
                    System.out.print(TILDA + " ");
                } else {
                    System.out.print(cell + " ");
                }
            }
            System.out.println();
        }
    }

    /**
     * Validates if the given coordinate is within the game board boundaries.
     */
    public static boolean isValidCoordinate(int[] coordinates, int size) {
        int row = coordinates[0];
        int col = coordinates[1];
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    /**
     * Converts a coordinate string (e.g., "A1") to an integer array (e.g., {0, 0}).
     * Validates the coordinates against the board size.
     *
     * @param coordinate The coordinate string (e.g., "A1").
     * @param boardSize The size of the board (e.g., 10 for a 10x10 board).
     * @return An integer array with row and column indices.
     * @throws IllegalArgumentException if the coordinate is invalid.
     */
    public static int[] convertCoordinate(String coordinate, int boardSize) {
        if (coordinate == null || coordinate.length() < 2) {
            throw new IllegalArgumentException("Invalid coordinate: " + coordinate);
        }

        coordinate = coordinate.toUpperCase();
        int row = coordinate.charAt(0) - 'A'; // Convert 'A' to 0, 'B' to 1, etc.
        int col;
        try {
            col = Integer.parseInt(coordinate.substring(1)) - 1; // Convert "1" to 0, "2" to 1, etc.
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate format: " + coordinate);
        }

        // Validate that the coordinates are within bounds
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + coordinate);
        }

        return new int[]{row, col};
    }

    public static String validateCoordinates(String start, String end, char[][] board, int shipLength, String shipName) {
        if (!start.matches("[A-J](10|[1-9])") || !end.matches("[A-J](10|[1-9])")) {
            return "Error, invalid coordinates!";
        }

        int[] startIndex = parseCoordinates(start);
        int[] endIndex = parseCoordinates(end);

        int startRow = startIndex[0];
        int startCol = startIndex[1];
        int endRow = endIndex[0];
        int endCol = endIndex[1];

        // Check bounds
        if (startRow < 0 || startRow >= SIZE || endRow < 0 || endRow >= SIZE ||
                startCol < 0 || startCol >= SIZE || endCol < 0 || endCol >= SIZE) {
            return "Error! Coordinates are out of bounds!";
        }

        // Check length
        if (startRow != endRow && startCol != endCol) {
            return "Error! Wrong ship location! Try again:";
        }
        int calculatedLength = (startRow == endRow) ? Math.abs(endCol - startCol) + 1 : Math.abs(endRow - startRow) + 1;
        if (calculatedLength != shipLength) {
            return "Error! Wrong length of the " + shipName + "! Try again:";
        }

        // Check overlap or proximity
        for (int i = Math.min(startRow, endRow) - 1; i <= Math.max(startRow, endRow) + 1; i++) {
            for (int j = Math.min(startCol, endCol) - 1; j <= Math.max(startCol, endCol) + 1; j++) {
                if (i >= 0 && i < SIZE && j >= 0 && j < SIZE && board[i][j] == SHIP) {
                    return "Error! You placed it too close to another one. Try again:";
                }
            }
        }

        return null; // Valid placement
    }

}

