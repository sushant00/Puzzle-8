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

    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        int x_cord = 0, y_cord = 0;
        bitmap = Bitmap.createScaledBitmap(bitmap,parentWidth, parentWidth,false);
        Log.i("puzzle board","cords"+parentWidth+"as"+bitmap.getWidth());
        int BitWidth = bitmap.getWidth()/NUM_TILES;
        Log.i("puzzle board","wid"+BitWidth);
        int BitHeight = bitmap.getHeight()/NUM_TILES;
        Log.i("puzzle board","ht"+BitHeight);
        ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>(NUM_TILES*NUM_TILES);
        Log.i("puzzle board","chunk");
        for(int i=0; i<NUM_TILES; i++){
            x_cord = 0;
            for(int j=0; j<NUM_TILES; j++) {
                Log.i("puzzle board", "addto" + x_cord +"y"+ y_cord+"as" + BitWidth);
                chunkedImages.add(Bitmap.createBitmap(bitmap, x_cord, y_cord, BitWidth, BitHeight));
                x_cord += BitWidth;
            }
            y_cord+=BitHeight;
        }
        tiles = new ArrayList<PuzzleTile>(NUM_TILES*NUM_TILES);
        Log.i("puzzle board","tiling");
        for(int i=1; i<NUM_TILES*NUM_TILES; i++) {
            Log.i("puzzle board","tiling"+i);
            tiles.add(new PuzzleTile(chunkedImages.get(i-1),i));
            Log.i("puzzle board","tile added");
        }
        Log.i("puzzle board","nulltile");
        Bitmap nullBit = Bitmap.createBitmap(BitWidth,BitHeight, Bitmap.Config.ARGB_8888);
        tiles.add(new PuzzleTile(nullBit,NUM_TILES*NUM_TILES));
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
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

        Log.i("puzzleboard","draw");
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
            if(tiles.get(i).getNumber() == 9){
                break;
            }
        }
        Log.i("puzzle board","neighbour coords");
        for(int[] j : NEIGHBOUR_COORDS){
            int index = XYtoIndex(j[0],j[1]);
            if(index <9 && index >= 0){
                Log.i("puzzle board","neighbour add start");
                PuzzleBoard addit = new PuzzleBoard(this);
                Log.i("puzzle board","neighbour add swap");
                Collections.swap(addit.tiles,i,index);
                Log.i("puzzle board","neighbour add end");
                neighbour.add(addit);
            }
        }
        Log.i("puzzle board","neighbour returning");
        return neighbour;
    }

    public int priority() {
        return 0;
    }

}
