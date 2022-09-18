package com.example.minesweeper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class loadingPageActivity extends AppCompatActivity {
    private final int COLUMN_COUNT = 8;
    private final int ROW_COUNT = 10;
    private int BOMB_COUNT = 4;
    private int FLAG_COUNT = 4;
    private int REVEALED_CELL_COUNT = 0;

    private int clock = 0;
    private boolean isRunning = false;
    private boolean is_ending = false;
    private boolean is_winning = false;
    private boolean flag_move = false;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    private cell[][] grid_cells = new cell[ROW_COUNT][COLUMN_COUNT];
    private TextView[][] tv_cells = new TextView[ROW_COUNT][COLUMN_COUNT];
    Map<Pair<Integer,Integer>,Integer> check_visited = new HashMap<Pair<Integer,Integer>,Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Timer stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_page);

        //TODO: timer bug probably all because not updating bundle
//        if(savedInstanceState != null){
//            clock = savedInstanceState.getInt("clock");
//            isRunning = savedInstanceState.getBoolean("isRunning");
//        }


        // Here starts the construction of whole grid.
        // Randomly mark four cells objects as bomb
        // Whenever a bomb is placed, add one bomb_count on all 8 neighbours
        GridLayout grid = (GridLayout) findViewById(R.id.bomb_grid);
        LayoutInflater li = LayoutInflater.from(this);
        TextView remain_flag = (TextView) findViewById(R.id.remain_flags);
        remain_flag.setText(String.valueOf(FLAG_COUNT));
        //Use a 2D array to store the cell objects
        for (int i = 0; i<ROW_COUNT; i++) {
            for (int j=0; j<COLUMN_COUNT; j++) {
                // first place all the grid as blank cells
                // then randomly pick bomb_num cells as bomb and initialize their neighbours.
                grid_cells[i][j] = new cell(false);
            }
        }
        bomb_initialize(grid_cells);

        // set click listener for the flag button
        Button flag = (Button) findViewById(R.id.flag_button);
        flag.setOnClickListener(this::onClickFlag);
        flag.setBackgroundColor(Color.WHITE);
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

        // if last click gets a result, this click will lead to result page
        if(is_ending){
            end_game(is_winning);
            return;
        }
        // only start timer when a user clicks the first cell
        if(!isRunning && !is_ending) runTimer();

        // map the cell with the textview
        TextView tv = (TextView) view;
        Pair<Integer,Integer> n = findIndexOfCellTextView(tv);
        int i = n.first;
        int j = n.second;
        cell cur_cell = grid_cells[i][j];

        // when the user is trying in flagging move
        if(flag_move){
            if(cur_cell.is_flagged()){
                // when the target cell is flagged already, remove the flag
                cur_cell.setFlagged();
                tv.setText("");
                FLAG_COUNT++;
            }else{
                // otherwise, put a flag on that cell that's not revealed before
                if(!check_visited.containsKey(new Pair<>(i,j)) && FLAG_COUNT !=0){
                    cur_cell.setFlagged();
                    tv.setText("\uD83D\uDEA9");
                    FLAG_COUNT--;
                }
            }
            TextView remain_flag = (TextView) findViewById(R.id.remain_flags);
            remain_flag.setText(String.valueOf(FLAG_COUNT));
            // check if this last flag move results in victory (a sneaky bug QAQ)
            if(FLAG_COUNT ==0 && check_visited.size()==COLUMN_COUNT*ROW_COUNT-BOMB_COUNT){
                is_ending = true;
                isRunning = false;
                is_winning = true;
                set_ending_warning();
            }
            return;
        }

        // these are normal cases for user clicking on the cell
        if(cur_cell.is_bomb()){
            // when user clicks on a bomb, show the bomb and go to result page
            if(!cur_cell.is_flagged()){
                tv.setBackgroundColor(Color.RED);
                tv.setText(R.string.mine);
                isRunning = false;
                is_ending = true;
                set_ending_warning();
            }
            //else if it's flagged, do nothing :3
        }else if(cur_cell.getBombCount()==0){
            // When the user clicks on empty cell
            // Use DFS to pop all empty neighbours and their empty neighbors
            reveal_all_blank(i,j,grid_cells,tv_cells,check_visited);
        }else{
            // scenario when just showing a number
            tv.setBackgroundColor(Color.GRAY);
            tv.setTextColor(Color.WHITE);
            check_visited.put(new Pair<>(i,j),0);
        }
        // when the clicking move is over, check if user wins
        if(FLAG_COUNT ==0 && check_visited.size()==COLUMN_COUNT*ROW_COUNT-BOMB_COUNT){
            is_ending = true;
            isRunning = false;
            is_winning = true;
            set_ending_warning();
        }

    }

    private void set_ending_warning(){
        TextView tv = (TextView) findViewById(R.id.ending_warning);
        tv.setText(R.string.ending_text);
        tv.setTextColor(Color.RED);
    }

    private void onClickFlag(View view){
        TextView mode_button = (TextView) findViewById(R.id.flag_button);
        if(flag_move){
            mode_button.setText(R.string.pick);
        }else{
            mode_button.setText(R.string.flag);
        }
        flag_move = !flag_move;
    }

    private void end_game(boolean isWinning){
        isRunning=false;

        // TODO: add a play again button
        if(!isWinning){
            // Situation when the user click on a bomb
            String msg = "You hit a bomb, Game Over";
            Intent intent = new Intent(this, resultPageActivity.class);
            intent.putExtra("msg", msg);
            startActivity(intent);
        }else{
            String msg = "";
            msg += "you've found all bombs\n";
            msg += "using " + clock +" seconds\n";
            msg += "Good Job";
            Intent intent = new Intent(this, resultPageActivity.class);
            intent.putExtra("msg", msg);
            startActivity(intent);
        }
    }

    private void reveal_all_blank(int i, int j, cell[][] grid_cells, TextView[][] tv_cells,
                                  Map<Pair<Integer,Integer>,Integer> check_visited) {
        Pair<Integer,Integer> n = new Pair<>(i,j);
        if(check_visited.containsKey(n) || i<0 || i>=ROW_COUNT || j<0 || j>=COLUMN_COUNT) return;
        TextView tempTv = tv_cells[i][j];

        if(grid_cells[i][j].getBombCount()!=0) {
            tempTv.setBackgroundColor(Color.GRAY);
            tempTv.setTextColor(Color.WHITE);
            check_visited.put(n,0);
            return;
        }
        tempTv.setBackgroundColor(Color.GRAY);
        check_visited.put(n,0);
        if(i>0) reveal_all_blank(i-1,j,grid_cells,tv_cells,check_visited);
        if(j>0) reveal_all_blank(i,j-1,grid_cells,tv_cells,check_visited);
        if(i<ROW_COUNT-1) reveal_all_blank(i+1,j,grid_cells,tv_cells,check_visited);
        if(j<COLUMN_COUNT-1) reveal_all_blank(i,j+1,grid_cells,tv_cells,check_visited);

        // TODO: verify if we need to reveal corner neibour for blank cell
        if(i>0 && j>0) reveal_all_blank(i-1,j-1,grid_cells,tv_cells,check_visited);
        if(i>0 && j<COLUMN_COUNT-1) reveal_all_blank(i-1,j+1,grid_cells,tv_cells,check_visited);
        if(i<ROW_COUNT-1 && j>0) reveal_all_blank(i+1,j-1,grid_cells,tv_cells,check_visited);
        if(i<ROW_COUNT-1 && j<COLUMN_COUNT-1) reveal_all_blank(i+1,j+1,grid_cells,tv_cells,check_visited);
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
                if(!isRunning) isRunning = true;
                int sec = clock;
                String time = sec+"s";
                tv.setText(time);

                if(isRunning&&!is_ending) clock++;
                handler.postDelayed(this,1000);
            }
        });
    }

}
