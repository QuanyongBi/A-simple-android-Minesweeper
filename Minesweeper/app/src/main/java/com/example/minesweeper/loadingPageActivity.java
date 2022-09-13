package com.example.minesweeper;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class loadingPageActivity extends AppCompatActivity {
    private final int COLUMN_COUNT = 8;
    private final int ROW_COUNT = 10;
    private int BOMB_COUNT = 4;

    private int clock = 0;
    private boolean isRunning = false;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    private cell[][] grid_cells = new cell[ROW_COUNT][COLUMN_COUNT];
    private TextView[][] tv_cells = new TextView[ROW_COUNT][COLUMN_COUNT];
    @Override

    public void onCreate(Bundle savedInstanceState) {
        // Timer stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_page);

        if(savedInstanceState != null){
            clock = savedInstanceState.getInt("clock");
            isRunning = savedInstanceState.getBoolean("isRunning");
        }
        runTimer();
        isRunning=false;
        // Here starts the construction of whole grid.
        // Randomly mark four cells objects as bomb
        // Whenever a bomb is placed, add one bomb_count on all 8 neighbours
        GridLayout grid = (GridLayout) findViewById(R.id.bomb_grid);
        LayoutInflater li = LayoutInflater.from(this);
        //Use a 2D array to store the cell objects
        for (int i = 0; i<ROW_COUNT; i++) {
            for (int j=0; j<COLUMN_COUNT; j++) {
                // first place all the grid as blank cells
                // then randomly pick bomb_num cells as bomb and initialize their neighbours.
                grid_cells[i][j] = new cell(false);
            }
        }
        bomb_initialize(grid_cells);

        // Inflate a list of TextView for manifest
        for (int i = 0; i<=9; i++) {
            for (int j=0; j<=7; j++) {
                TextView tv = (TextView) li.inflate(R.layout.cell_layout, grid, false);
                tv.setBackgroundColor(Color.GREEN);
                tv.setTextColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);
                cell curCell = grid_cells[i][j];
                if (curCell.is_bomb()){
                    tv.setBackgroundColor(Color.GREEN);
                }else if(curCell.getBombCount() != 0){
                    tv.setText(String.valueOf(grid_cells[i][j].getBombCount()));
                }else{
                    tv.setText("");
                }
                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);
                grid.addView(tv, lp);
                tv_cells[i][j] = tv;
            }
        }
    }

    private void bomb_initialize(cell[][] grid_cells) {
        int[][] check_bomb = new int[ROW_COUNT][COLUMN_COUNT];
        int bomb_c = BOMB_COUNT;
        while(bomb_c>0){
            int r = (int) (Math.random()*ROW_COUNT);
            int c = (int) (Math.random()*COLUMN_COUNT);
            if(grid_cells[r][c].is_bomb()) continue;
            grid_cells[r][c].set_bomb();
            set_neighbors(grid_cells,r,c);
            bomb_c--;
        }
    }

    private void set_neighbors(cell[][] grid_cells, int r, int c) {
        if(r>0) grid_cells[r-1][c].add_bomb_count();
        if(c>0) grid_cells[r][c-1].add_bomb_count();
        if(r<ROW_COUNT-1) grid_cells[r+1][c].add_bomb_count();
        if(c<COLUMN_COUNT-1) grid_cells[r][c+1].add_bomb_count();

        if(r>0 && c>0) grid_cells[r-1][c-1].add_bomb_count();
        if(r>0 && c<COLUMN_COUNT-1) grid_cells[r-1][c+1].add_bomb_count();
        if(r<ROW_COUNT-1 && c>0) grid_cells[r+1][c-1].add_bomb_count();
        if(r<ROW_COUNT-1 && c<COLUMN_COUNT-1) grid_cells[r+1][c+1].add_bomb_count();

    }

    private Pair<Integer,Integer> findIndexOfCellTextView(TextView tv) {
        for (int i=0; i<ROW_COUNT; i++) {
            for(int j=0; j<COLUMN_COUNT; j++) {
                if (tv_cells[i][j] == tv)
                    return new Pair<Integer,Integer>(i,j);
            }
        }
        return new Pair<Integer,Integer>(-1,-1);
    }

    public void onClickTV(View view){
        // this is when the user actually click on that grid
        TextView tv = (TextView) view;
        Pair<Integer,Integer> n = findIndexOfCellTextView(tv);
        int i = n.first;
        int j = n.second;
        cell cur_cell = grid_cells[i][j];
        if(cur_cell.is_bomb()){
            tv.setBackgroundColor(Color.RED);
            tv.setText("\uD83D\uDCA3");
            end_game(false);
        }else if(cur_cell.getBombCount()==0){
            // Use DFS to pop all neighbours and their neighbors
            Map<Pair<Integer,Integer>,Integer> check_visited = new HashMap<Pair<Integer,Integer>,Integer>();
            List<Pair<Integer,Integer>> to_pop = new ArrayList<Pair<Integer,Integer>>();
            reveal_all_blank(i,j,grid_cells,tv_cells,check_visited);
            isRunning=true;
        }else{
            // scenario when just showing a number
            tv.setBackgroundColor(Color.GRAY);
            tv.setTextColor(Color.WHITE);
            isRunning=true;
        }

    }

    private void end_game(boolean isWinning){
        isRunning=false;
    }

    private void reveal_all_blank(int i, int j, cell[][] grid_cells, TextView[][] tv_cells,
                                  Map<Pair<Integer,Integer>,Integer> check_visited) {
        Pair<Integer,Integer> n = new Pair<>(i,j);
        if(check_visited.containsKey(n) || i<0 || i>=ROW_COUNT || j<0 || j>=COLUMN_COUNT) return;
        TextView tempTv = tv_cells[i][j];
        if(grid_cells[i][j].getBombCount()!=0) {
            tempTv.setBackgroundColor(Color.GRAY);
            tempTv.setTextColor(Color.WHITE);
            return;
        }
        tempTv.setBackgroundColor(Color.GRAY);
        check_visited.put(n,0);
        if(i>0) reveal_all_blank(i-1,j,grid_cells,tv_cells,check_visited);
        if(j>0) reveal_all_blank(i,j-1,grid_cells,tv_cells,check_visited);
        if(i<ROW_COUNT-1) reveal_all_blank(i+1,j,grid_cells,tv_cells,check_visited);
        if(j<COLUMN_COUNT-1) reveal_all_blank(i,j+1,grid_cells,tv_cells,check_visited);
    }

    public void onSaveInstanceStates(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("clock",clock);
        savedInstanceState.putBoolean("isRunning",isRunning);
    }

    private void runTimer(){
        TextView tv = (TextView) findViewById(R.id.time_view);
        Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {

                int sec = clock % 60;
                String time = sec+"s";
                tv.setText(time);

                if(isRunning) clock++;
                handler.postDelayed(this,1000);
            }
        });
    }


}
