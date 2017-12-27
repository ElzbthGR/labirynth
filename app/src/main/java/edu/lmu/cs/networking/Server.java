package edu.lmu.cs.networking;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;



public class Server {

    public static volatile List<Game> gamesArchive = new ArrayList<Game>() {
    };

    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(8102);
        System.out.println("Server is Running...");
        try {
            while (true) {

                Map board = new Map();
                Game game = new Game();
                Object[] map = board.generate_map(game);
                gamesArchive.add(game);
                Game.Player playerX = game.new Player(listener.accept(), '1');
                Game.Player playerO = game.new Player(listener.accept(), '2');
                playerX.setOpponent(playerO);
                playerO.setOpponent(playerX);
                game.currentPlayer = playerX;
                game.setBoard(map, board.getLocationX(), board.getLocationO());
                playerX.start();
                playerO.start();
                board.printMap(map);
            }
        } finally {
            listener.close();
        }
    }
}

