package com.sternhalma.server.games.basicsternhalma;

import com.sternhalma.common.games.basicsternhalma.Board;
import com.sternhalma.server.connection.Player;
import com.sternhalma.server.games.Game;

import java.awt.*;
import java.util.HashMap;

public class BasicSternhalma implements Game {

    private final HashMap<Player, Integer> players;
    private final Board board;
    private int turn;

    public BasicSternhalma() {
        players = new HashMap<>();
        board = new Board();
        turn = 1;
    }

    @Override
    public boolean joinPlayer(Player player) {
        if (!players.containsKey(player)) {
            int id = board.addPlayer();
            players.put(player, id);
            players.keySet().forEach(p -> {
                p.sendMessage("BOARD_UPDATE:" + players.get(p) + ":" + turn);
                p.sendObject(board);
            });
            return true;
        }
        return false;
    }

    private boolean isPlayersTurn(Player player) {
        int playerID = players.get(player);
        int controlNumber = turn % board.getNumberOfPlayers();
        return playerID == (controlNumber == 0 ? board.getNumberOfPlayers() : controlNumber);
    }

    @Override
    public boolean performAction(Player player, String action) {
        //MOVE:4,0:4,2
        String[] tokens = action.split(":", 3);
        String command = tokens[0];
        if (isPlayersTurn(player)) {
            switch (command) {
                case "MOVE" -> {
                    String fromStr = tokens[1];
                    String toStr = tokens[2];
                    int oldX = Integer.parseInt(fromStr.split(",")[0]);
                    int oldY = Integer.parseInt(fromStr.split(",")[1]);
                    int newX = Integer.parseInt(toStr.split(",")[0]);
                    int newY = Integer.parseInt(toStr.split(",")[1]);
                    Point oldPoint = new Point(oldX, oldY);
                    Point newPoint = new Point(newX, newY);
                    if (
                            board.isValidMove(oldPoint, newPoint) || board.isValidJump(oldPoint, newPoint)
                    ) {
                        if (
                                !board.isLeavingOpponentBase(oldPoint, newPoint)
                        ) {
                            board.movePiece(oldPoint, newPoint);
                            players.keySet().forEach(p -> {
                                p.sendMessage("BOARD_UPDATE:" + players.get(p) + ":" + turn);
                                p.sendObject(board);
                            });
                        }
                    }
                }
                case "ENDTURN" -> {
                    turn++;
                    int playerID = players.get(player);
                    if (!board.getWinners().contains(playerID) && board.checkForWin(playerID)) {
                        board.addWinner(playerID);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getGameName() {
        return "BasicSternhalma";
    }
}
