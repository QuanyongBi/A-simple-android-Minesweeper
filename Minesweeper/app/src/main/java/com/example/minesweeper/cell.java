package com.example.minesweeper;

import android.widget.TextView;

public class cell {
    private boolean isBomb;
    private int bombCount;
    private boolean isFlagged;

    public cell(boolean isBomb){
        this.isBomb = isBomb;
        this.bombCount = 0;
        isFlagged=false;
    }

    public void add_bomb_count(){bombCount++;}
    public void set_bomb(){isBomb = true; bombCount=0;}
    public boolean is_bomb(){return isBomb;}
    public int getBombCount(){return bombCount;}
    public boolean is_flagged(){return isFlagged;}
    public void setFlagged(){isFlagged = !isFlagged;}

}
