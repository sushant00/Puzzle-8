/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;


public class PuzzleBoard {

    private static int NUM_TILES;
    int steps;
    PuzzleBoard previousBoard;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles;

    PuzzleBoard(Bitmap bitmap, int parentWidth, int numTiles) {
        this.NUM_TILES = numTiles;
        int x_cord = 0, y_cord = 0;
        bitmap = Bitmap.createScaledBitmap(bitmap,parentWidth, parentWidth,false);
        int BitWidth = bitmap.getWidth()/NUM_TILES;
        int BitHeight = bitmap.getHeight()/NUM_TILES;
        ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>(NUM_TILES*NUM_TILES);
        for(int i=0; i<NUM_TILES; i++){
            x_cord = 0;
            for(int j=0; j<NUM_TILES; j++) {
                chunkedImages.add(Bitmap.createBitmap(bitmap, x_cord, y_cord, BitWidth, BitHeight));
                x_cord += BitWidth;
            }
            y_cord+=BitHeight;
        }
        tiles = new ArrayList<PuzzleTile>(NUM_TILES*NUM_TILES);
        for(int i=1; i<NUM_TILES*NUM_TILES; i++) {
            tiles.add(new PuzzleTile(chunkedImages.get(i-1),i-1));
        }
        tiles.add(null);
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        steps = otherBoard.steps+1;
        previousBoard = otherBoard;
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> neighbour = new ArrayList<>();
        int i;
        for(i = 0;i<tiles.size();i++ ){
            if(tiles.get(i)== null){
                break;
            }
        }
        int nullX = i%NUM_TILES;
        int nullY = i/NUM_TILES;
        for(int[] j : NEIGHBOUR_COORDS){
            int neighbourX = nullX + j[0];
            int neighbourY = nullY + j[1];
            if(neighbourX >= 0 && neighbourX < NUM_TILES && neighbourY >= 0 && neighbourY < NUM_TILES){
                PuzzleBoard validNeighbour = new PuzzleBoard(this);
                Collections.swap(validNeighbour.tiles,i,XYtoIndex(neighbourX,neighbourY));
                neighbour.add(validNeighbour);
            }
        }
        return neighbour;
    }

    public int priority() {
        int distance = 0;
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                distance+= Math.abs((tile.getNumber() % NUM_TILES) - (i % NUM_TILES)) + Math.abs((tile.getNumber()/ NUM_TILES) - (i / NUM_TILES));
            }
        }
        return distance+steps;
    }
}
