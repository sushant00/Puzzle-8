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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    public PuzzleBoardView(Context context) {
        super(context);
        Log.i("here","as"+context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap,int numTiles) {
        int width = getWidth();
        puzzleBoard = new PuzzleBoard(imageBitmap, width, numTiles);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        if (animation == null && puzzleBoard != null) {
            for(int i = 0;i<NUM_SHUFFLE_STEPS;i++){
                puzzleBoard = puzzleBoard.neighbours().get(random.nextInt(puzzleBoard.neighbours().size()));
            }
            //puzzleBoard.reset();
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        MinHeap pq = new MinHeap();
        puzzleBoard.steps = 0;puzzleBoard.previousBoard = null;
        pq.add(puzzleBoard);
        PuzzleBoard tmp = puzzleBoard,tmpPrevious;
        while(!pq.isEmpty()){
            tmpPrevious = tmp;
            tmp = pq.remove();
            if(tmp.resolved()){
                ArrayList<PuzzleBoard> solutionPath = new ArrayList<PuzzleBoard>();
                solutionPath.add(tmp);
                while(tmp.previousBoard!=null) {
                    tmp = tmp.previousBoard;
                    solutionPath.add(tmp);
                }
                Collections.reverse(solutionPath);
                animation = new ArrayList<PuzzleBoard>(solutionPath);
                invalidate();
                return;
            }
            else{
                for(PuzzleBoard n: tmp.neighbours()){
                    if (n.equals(tmpPrevious))
                        continue;
                    else
                        pq.add(n);
                }
            }
        }
    }



    class MinHeap{
        public int size;
        public ArrayList<PuzzleBoard> heap;

        public MinHeap(){
            this.size = 0;
            heap = new ArrayList<PuzzleBoard>();
            heap.add(null);
        }

        public void add(PuzzleBoard x){
            size++;
            if (heap.size()<=size)
                heap.add(x);
            int newPos = percolateUp(size, x);
            heap.set(newPos, x);
        }

        private int percolateUp(int hole, PuzzleBoard x){
            while(hole > 1 && x.priority() < heap.get(hole/2).priority()){
                heap.set(hole, heap.get(hole/2));
                hole /= 2;
            }
            return hole;
        }

        public PuzzleBoard remove(){
            PuzzleBoard Val = heap.get(1);
            size--;
            int newPos = percolateDown(1,heap.get(size+1));
            heap.set(newPos, heap.get(size+1));
            return Val;
        }

        private int percolateDown(int hole, PuzzleBoard val){
            int left,right,target;
            while(2*hole <= this.size){
                left = hole*2;
                right = left + 1;
                if(right <= size && heap.get(right).priority() < heap.get(left).priority()) target = right;
                else target = left;

                if(heap.get(target).priority() < val.priority()){
                    heap.set(hole, heap.get(target));
                    hole = target;
                }
                else break;
            }
            return hole;
        }

        public boolean isEmpty(){
            return size == 0;
        }
    }
}

