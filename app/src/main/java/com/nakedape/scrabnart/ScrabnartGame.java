package com.nakedape.scrabnart;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Nathan on 6/1/2015.
 */
public class ScrabnartGame implements Serializable{
    private final static char[] tiles = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private final static int[] tile_counts = {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 2, 3, 1};
    public final static int[] point_values = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};
    private final static int EMPTY = -1;


    transient private Random random;
    private GameData data;
    public boolean isSaved = false;

    public ScrabnartGame(){
        random = new Random();
        data = new GameData();
    }
    public ScrabnartGame(String match_id, String player1_id, String player2_id){
        data = new GameData(match_id, player1_id, player2_id);
        random = new Random();
        data.setNextPlayer(player1_id);
        DrawTiles(player1_id);
        DrawTiles(player2_id);
        Log.d("Scrabnart", player1_id + ": " + getTilesAsString(getPlayerTilesInt(player1_id)));
        Log.d("Scrabnart", player2_id + ": " + getTilesAsString(getPlayerTilesInt(player2_id)));
    }
    public ScrabnartGame(byte[] gameData){
        random = new Random();
        ByteArrayInputStream b = new ByteArrayInputStream(gameData);
        try {
            ObjectInputStream o = new ObjectInputStream(b);
            data = (GameData) o.readObject();
        } catch (IOException e) {Log.d("IOException", e.getCause().getStackTrace()[0].getMethodName());}
        catch (ClassNotFoundException e) {Log.d("ClassNotFoundException", e.getCause().getMessage());}
    }

    public String getMatch_id() {return data.getMatch_id();}
    public String getPlayer1_id(){
        return data.getPlayer1_id();
    }
    public String getPlayer2_id(){
        return data.getPlayer2_id();
    }
    public String getNextPlayer(){return data.getNextPlayer();}

    public void TakeDrawingTurn(int[] word, int[] tilesRemaining, ArrayList<DrawEvent> drawing){
        data.setCurrentWord(word);
        data.setCurrentDrawing(drawing);
        if (getNextPlayer().equals(getPlayer1_id())){
            data.setPlayer1_tiles(tilesRemaining);
            data.setNextPlayer(getPlayer2_id());
        }
        else{
            data.setPlayer2_tiles(tilesRemaining);
            data.setNextPlayer(getPlayer1_id());
        }
        isSaved = false;
        Log.d("Scrabnart", data.getPlayer1_id() + ": " + getTilesAsString(getPlayerTilesInt(data.getPlayer1_id())));
        Log.d("Scrabnart", data.getPlayer2_id() + ": " + getTilesAsString(getPlayerTilesInt(data.getPlayer2_id())));
    }
    public void DrawTiles(String player_id){
        String player1_id = data.getPlayer1_id();

        if (player_id.equals(player1_id)){
            int[] player1_tiles = data.getPlayer1_tiles();
            for (int i = 0; i < player1_tiles.length; i++){
                if (player1_tiles[i] == EMPTY)
                    player1_tiles[i] = DrawTile();
            }
        }
        else {
            int[] player2_tiles = data.getPlayer2_tiles();
            for (int i = 0; i < player2_tiles.length; i++){
                if (player2_tiles[i] == EMPTY)
                    player2_tiles[i] = DrawTile();
            }
        }
    }
    private int DrawTile(){
        int tile;
        int index;
        do {
            index = random.nextInt(100);
            tile = data.getTile_bag()[index];
        } while (tile == EMPTY);
        data.getTile_bag()[index] = EMPTY;
        return tile;
    }

    public void TakeGuessTurn(int points){
        addPoints(data.getNextPlayer(), points);
        DrawTiles(data.getNextPlayer());
        Log.d("Scrabnart", data.getPlayer1_id() + ": " + getTilesAsString(getPlayerTilesInt(data.getPlayer1_id())));
        Log.d("Scrabnart", data.getPlayer2_id() + ": " + getTilesAsString(getPlayerTilesInt(data.getPlayer2_id())));
    }

    public int[] getPlayerTilesInt(String player_id){
        if (player_id.equals(data.getPlayer1_id()))
            return data.getPlayer1_tiles();
        else
            return data.getPlayer2_tiles();
    }
    public int[] getScrambledWordTiles(){
        int[] tileSet = new int[9];
        System.arraycopy(data.getCurrentWord(), 0, tileSet, 0, data.getCurrentWord().length);
        for (int i = 0; i < tileSet.length; i++){
            if (tileSet[i] == EMPTY){
                tileSet[i] = random.nextInt(26);
            }
        }

        return ShuffleArray(tileSet);
    }
    private int[] ShuffleArray(int[] array)    {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            if (index != i)
            {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
        return array;
    }
    public String getTilesAsString(int[] tiles){
        String word ="";
        for (int t : tiles){
            if (t != -1)
                word += ScrabnartGame.tiles[t];
        }
        return word;
    }
    public int[] getCurrentWord(){
        return data.getCurrentWord();
    }
    public String getCurrentWordAsString(){
        return getTilesAsString(data.getCurrentWord());
    }

    public void addPoints(String player_id, int points){
        if (player_id.equals(getPlayer1_id())){
            data.setPlayer1_points(points + data.getPlayer1_points());
        }
        else{
            data.setPlayer2_points(points + data.getPlayer2_points());
        }
    }
    public int getPoints(String player_id){
        if (player_id.equals(getPlayer1_id()))
            return data.getPlayer1_points();
        else
            return data.getPlayer2_points();
    }

    public ArrayList<DrawEvent> getCurrentDrawing(){
        return data.getCurrentDrawing();
    }

    public byte[] getGameData(){
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(data);
        }catch (IOException e) {
            Log.d("Serialization Error", e.getMessage());
            for (int i = 0; i < e.getStackTrace().length; i++) {
                Log.d("Serialization Error", e.getStackTrace()[i].getClassName());
                Log.d("Serialization Error", e.getStackTrace()[i].getMethodName());
            }
        }
        return b.toByteArray();
    }

    public class GameData implements Serializable{
        private String match_id;
        public String getMatch_id(){return match_id;}

        private String player1_id, player2_id, nextPlayer;
        public String getPlayer1_id(){return player1_id;}
        public String getPlayer2_id(){return player2_id;}
        public void setNextPlayer(String nextPlayer){this.nextPlayer = nextPlayer;}
        public String getNextPlayer(){return nextPlayer;}

        private int[] player1_tiles = {-1, -1, -1, -1, -1, -1, -1, -1, -1};
        public void setPlayer1_tiles(int[] tiles){player1_tiles = tiles;}
        public int[] getPlayer1_tiles(){return player1_tiles;}

        private int[] player2_tiles = {-1, -1, -1, -1, -1, -1, -1, -1, -1};
        public void setPlayer2_tiles(int[] tiles){player2_tiles = tiles;}
        public int[] getPlayer2_tiles(){return player2_tiles;}

        private int[] tile_bag;
        public void setTile_bag(int[] tiles){tile_bag = tiles;}
        public int[] getTile_bag(){return tile_bag;}

        private int[] currentWord = {-1, -1, -1, -1, -1, -1, -1, -1};
        public void setCurrentWord(int[] tiles){currentWord = tiles;}
        public int[] getCurrentWord(){return currentWord;}

        private ArrayList<DrawEvent> currentDrawing;
        public void setCurrentDrawing(ArrayList<DrawEvent> drawing){currentDrawing = drawing;}
        public ArrayList<DrawEvent> getCurrentDrawing(){return currentDrawing;}

        private int player1_points, player2_points;
        public void setPlayer1_points(int points){player1_points = points;}
        public int getPlayer1_points(){return player1_points;}
        public void setPlayer2_points(int points){player2_points = points;}
        public int getPlayer2_points(){return  player2_points;}

        public GameData(){}
        public GameData(String match_id, String player1_id, String player2_id){
            this.match_id = match_id;
            this.player1_id = player1_id;
            this.player2_id = player2_id;
            nextPlayer = player2_id;
            currentDrawing = new ArrayList<>();
            tile_bag = new int[100];
            int index = 0;
            for (int tile = 0; tile < tile_counts.length; tile++){
                for (int count = 0; count < tile_counts[tile]; count++){
                    tile_bag[index++] = tile;
                }
            }
        }

    }
}
