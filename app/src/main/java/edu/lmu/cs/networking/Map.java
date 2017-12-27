package edu.lmu.cs.networking;


import java.util.ArrayList;

public class Map {
    private final int SIZE = 25;
    private int locationX = (int) (Math.random() * SIZE);
    private int locationO = (int) (Math.random() * SIZE);

    public int getSIZE() {
        return SIZE;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationO() {
        return locationO;
    }

    private ArrayList<Integer> createWay() {
        ArrayList<Integer> way = new ArrayList<Integer>();
        int randomWay = (int) (Math.random() * 2);
        int differenceCol;
        int differenceRow;
        int sqrt = (int) (Math.sqrt(SIZE));
        int numberRowX = locationX / sqrt;
        int numberRowO = locationO / sqrt;
        int placeInLineX = locationX - locationX / sqrt * sqrt;
        int placeInLineO = locationO - locationO / sqrt * sqrt;
        if (numberRowX > numberRowO && placeInLineX > placeInLineO) {
            differenceCol = placeInLineX - placeInLineO;
            differenceRow = numberRowX - numberRowO;
            way = createWay(randomWay, differenceCol, differenceRow, locationX, sqrt, 1);
        } else if (numberRowX > numberRowO && placeInLineX < placeInLineO) {
            differenceCol = placeInLineO - placeInLineX;
            differenceRow = numberRowX - numberRowO;
            way = createWay(randomWay, differenceCol, differenceRow, locationX, sqrt, 2);
        } else if (numberRowX < numberRowO && placeInLineX > placeInLineO) {
            differenceCol = placeInLineX - placeInLineO;
            differenceRow = numberRowO - numberRowX;
            way = createWay(randomWay, differenceCol, differenceRow, locationO, sqrt, 2);
        } else if (numberRowX < numberRowO && placeInLineX < placeInLineO) {
            differenceCol = placeInLineO - placeInLineX;
            differenceRow = numberRowO - numberRowX;
            way = createWay(randomWay, differenceCol, differenceRow, locationO, sqrt, 1);
        } else if (numberRowX == numberRowO) {
            if (locationX > locationO) {
                differenceCol = placeInLineX - placeInLineO;
                differenceRow = 0;
                way = createWay(randomWay, differenceCol, differenceRow, locationX, sqrt, 3);
            } else {
                differenceCol = placeInLineO - placeInLineX;
                differenceRow = 0;
                way = createWay(randomWay, differenceCol, differenceRow, locationO, sqrt, 3);
            }
        } else if (placeInLineX == placeInLineO) {
            if (locationX > locationO) {
                differenceCol = 0;
                differenceRow = numberRowX - numberRowO;
                way = createWay(randomWay, differenceCol, differenceRow, locationX, sqrt, 4);
            } else {
                differenceCol = 0;
                differenceRow = numberRowO - numberRowX;
                way = createWay(randomWay, differenceCol, differenceRow, locationO, sqrt, 4);
            }
        }

        return way;
    }

    private ArrayList<Integer> createWay(int randomWay, int differenceCol, int differenceRow, int place, int sqrt, int typeCreate) {
        ArrayList<Integer> arrayWay = new ArrayList<Integer>();
        int index;
        switch (typeCreate) {
            case 1:
                if (randomWay == 0) {
                    for (index = 0; index < differenceCol; index++) {
                        arrayWay.add(place);
                        place -= 1;
                    }
                    for (index = 0; index < differenceRow + 1; index++) {
                        arrayWay.add(place);
                        place -= sqrt;
                    }
                } else {

                    for (index = 0; index < differenceRow; index++) {
                        arrayWay.add(place);
                        place -= sqrt;
                    }
                    for (index = 0; index < differenceCol + 1; index++) {
                        arrayWay.add(place);
                        place -= 1;
                    }
                }
                break;
            case 2:
                if (randomWay == 0) {
                    for (index = 0; index < differenceCol; index++) {
                        arrayWay.add(place);
                        place += 1;
                    }
                    for (index = 0; index < differenceRow + 1; index++) {
                        arrayWay.add(place);
                        place -= sqrt;
                    }
                } else {

                    for (index = 0; index < differenceRow; index++) {
                        arrayWay.add(place);
                        place -= sqrt;
                    }
                    for (index = 0; index < differenceCol + 1; index++) {
                        arrayWay.add(place);
                        place += 1;
                    }
                }
                break;
            case 3:
                for (index = 0; index < differenceCol; index++) {
                    arrayWay.add(place);
                    place -= 1;
                }
            case 4:
                for (index = 0; index < differenceRow + 1; index++) {
                    arrayWay.add(place);
                    place -= sqrt;
                }
                break;
        }
        return arrayWay;
    }

    public Object[] generate_map(Game game) {
        int sqrt = (int) Math.sqrt(SIZE);
        ArrayList<Integer> way = createWay();
        Object[] board = new Object[SIZE];
        int quantityGranite = sqrt;
        int quantityBrick = sqrt + sqrt;
        int index;
        int graniteCount = 0;
        while (graniteCount < quantityGranite) {
            int place = (int) (Math.random() * (SIZE));
            for (index = 0; index < way.size(); index++) {
                if (!way.contains(place)) {
                    board[place] = game.granite;
                    graniteCount++;
                }
            }
        }
        int brickCount = 0;
        while (brickCount < quantityBrick){
            int place = (int) (Math.random() * (SIZE));
            if (place != locationX && place != locationO){
                board[place] = game.brick;
                brickCount++;
            }
        }
        return board;
    }

    public void printMap(Object[] map){
        for (int i = 0; i < Math.sqrt(SIZE); i++) {
                    System.out.print(i + ") {");
                    for (int j = 0; j < Math.sqrt(SIZE); j++) {
                        System.out.print(map[i] + ", ");
                    }
                    System.out.println("}");
                }
    }
}