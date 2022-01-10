package com.sternhalma.client.games.basicsternhalma;

import com.sternhalma.client.connection.Client;
import com.sternhalma.client.games.Game;
import com.sternhalma.client.games.basicsternhalma.gui.BasicSternhalmaPanel;
import com.sternhalma.common.games.basicsternhalma.Board;

import javax.swing.*;
import java.awt.*;

public class BasicSternhalma implements Game {
    private BasicSternhalmaPanel panel;
    private Client client;

    @Override
    public void init(Client client) {
        this.client = client;
        panel = new BasicSternhalmaPanel(this);
        this.client.getClientFrame().setGamePanel(panel);
        Object object;
        while(true){
            object = client.readObject();
            if(object instanceof Board) {
                Board board = (Board) object;
                panel.setBoard(board);
                continue;
            }
            if(object instanceof String) {
                String message = (String) object;
                String tokens[] = message.split(":");
                switch(tokens[0]) {
                    case "BOARD_UPDATE" -> {
                        if(tokens.length > 3){
                            try {
                                int playerID = Integer.parseInt(tokens[1]);
                                int turn = Integer.parseInt(tokens[2]);
                                int playerNum = Integer.parseInt(tokens[3]);

                                int controlNumber = turn % playerNum;
                                int realTurn = (controlNumber == 0 ? playerNum : controlNumber);

                                panel.setPlayerID(playerID);
                                panel.setTurn(realTurn);
                                panel.repaint();
                            } catch (NumberFormatException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    case "WINNER" -> {
                        if(tokens.length > 1){
                            try {
                                int playerID = Integer.parseInt(tokens[1]);
                                JOptionPane.showMessageDialog(client.getClientFrame(),"Player " + playerID + "ended the game!", "Information", JOptionPane.INFORMATION_MESSAGE);
                            } catch (NumberFormatException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    case "GAME_ENDED" -> {
                        JOptionPane.showMessageDialog(client.getClientFrame(),"Game ended!", "Information", JOptionPane.INFORMATION_MESSAGE);
                        return; //disconnect
                    }
                }
            }
        }
    }

    public void movePiece(Point from, Point to){
        if(client.isConnected())
            client.sendMessage("performAction:"+ client.getGameID() +":MOVE:"+from.x+","+from.y+":"+to.x+","+to.y);
    }

    public void endTurn(){
        if(client.isConnected())
            client.sendMessage("performAction:"+ client.getGameID() +":ENDTURN");
    }
}
