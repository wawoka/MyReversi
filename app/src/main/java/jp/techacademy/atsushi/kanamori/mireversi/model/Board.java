package jp.techacademy.atsushi.kanamori.mireversi.model;

import java.util.ArrayList;
import java.util.List;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;
import jp.techacademy.atsushi.kanamori.mireversi.exceptions.*;
import jp.techacademy.atsushi.kanamori.mireversi.model.Cell.E_STATUS;

public class Board {

    private static final String TAG = "Board";
    public static final int COLS = 8;
    public static final int ROWS = 8;

    private RectF rect = new RectF();

    private Cell cells[][] = new Cell[ROWS][COLS];
    private Cell.E_STATUS turn;

    public Board(){
        this.rect.left = 0f;
        this.rect.top = 0f;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cells[i][j] = new Cell(this, j, i);
            }
        }

        //初期の配置をセット
        cells[ROWS/2 -1][COLS/2 -1].setStatus(Cell.E_STATUS.White);
        cells[ROWS/2 -1][COLS/2].setStatus(Cell.E_STATUS.Black);
        cells[ROWS/2][COLS/2 -1].setStatus(Cell.E_STATUS.Black);
        cells[ROWS/2][COLS/2].setStatus(Cell.E_STATUS.White);

        turn = Cell.E_STATUS.Black;
        ArrayList<Cell> changedCells = new ArrayList<Cell>();
        setAllReversibleCells(changedCells);
    }

    public void setRectF(RectF rect) {
        this.rect = rect;
    }
    public RectF getRectF(){
        return this.rect;
    }

    public void setSize(int w, int h){
        int sz = w < h ? w: h;						//正方形になる様に小さいほうに合わせる。

        this.rect.right = this.rect.left + (int)(sz / Board.COLS) * Board.COLS;		//列数で割り切れない場合は余りを捨てる。
        this.rect.bottom = this.rect.top + (int)(sz / Board.ROWS) * Board.ROWS;		//行数で割り切れない場合は余りを捨てる。

        float cellW = this.getCellWidth();
        float cellH = this.getCellHeidht();

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cells[i][j].setLeft(j * cellW);
                cells[i][j].setTop(i * cellH);
                cells[i][j].setWidth(cellW);
                cells[i][j].setHeight(cellH);
            }
        }
    }

    public Cell[][] getCells(){
        return cells;
    }
    public void setCells(Cell[][] cells){
        this.cells = cells;
    }

    public float getCellWidth(){
        return this.rect.width() / (float)COLS;
    }

    public float getCellHeidht(){
        return this.rect.height() / (float)ROWS;
    }

    public ArrayList<Cell> changeCell(int r, int c, Cell.E_STATUS newStatus) throws Exception{
        Cell cell = cells[r][c];

        ArrayList<Cell> list = cell.getReversibleCells();
        if (list.size() == 0){
            throw new InvalidMoveException();
        }

        ArrayList<Cell> changedCells = new ArrayList<Cell>();

        for (Cell cell2 : list) {
            cell2.setStatus(newStatus);
            changedCells.add(cell2);
        }

        cell.setStatus(newStatus);
        changedCells.add(cell);

        return changedCells;
    }

    public Cell.E_STATUS getTurn(){
        return this.turn;
    }

    public boolean isFinished(){
        return (this.turn == E_STATUS.None);
    }

    public void setFinished(){
        this.turn = E_STATUS.None;
    }

    public int changeTurn(List<Cell> changedCells){
        if (this.turn == E_STATUS.Black){
            this.turn = E_STATUS.White;
        } else {
            this.turn = E_STATUS.Black;
        }

        return setAllReversibleCells(changedCells);
    }

    private int setAllReversibleCells(List<Cell> changedCells){
        int n = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Cell cell = cells[i][j];

                //再計算の前に前回マークされていた部分を変更リストに追加。
                if (cell.getReversibleCells().size() > 0){
                    changedCells.add(cell);
                }

                //裏返されるセルのリストを再計算。
                cell.setReversibleCells(this.turn);

                //再計算後に今回マークされた部分を変更リストに追加。
                if (cell.getReversibleCells().size() > 0){
                    n++;
                    changedCells.add(cell);
                }
            }
        }
        return n;
    }

    public int countCells(Cell.E_STATUS status){
        int n = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (cells[i][j].getStatus() == status){
                    n++;
                }
            }
        }
        return n;
    }

    public int countBlankCells(){
        return countCells(E_STATUS.None);
    }

    public Cell.E_STATUS getWinner(){
        E_STATUS winner = E_STATUS.None;
        int cntB = countCells(E_STATUS.Black);
        int cntW = countCells(E_STATUS.White);
        if (cntB > cntW){
            winner = E_STATUS.Black;
        } else if (cntB < cntW){
            winner = E_STATUS.White;
        }
        return winner;
    }

    public String getTurnDisplay(){
        return Cell.statusToDisplay(this.turn);
    }


    /**
     * 状態を文字列にシリアライズする。
     */
    public String getStateString(){
        StringBuilder str = new StringBuilder();

        str.append(Cell.statusToString(this.turn) + ":");

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                str.append(cells[i][j].getStatusString());
            }
        }

        String s = str.toString();
        Log.d(TAG, "getStateString:" + s);
        return s;
    }



    /**
     * 文字列から状態を復元する。
     * @param s
     */
    public void loadFromStateString(String s){
        if (TextUtils.isEmpty(s)) return;

        this.turn = Cell.stringToStatus(s.substring(0, 1));

        String s2;
        int start = 2;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                s2 = s.substring(start, start+1);
                cells[i][j].setStatusString(s2);
                start++;
            }
        }

        ArrayList<Cell> changedCells = new ArrayList<Cell>();
        setAllReversibleCells(changedCells);
    }

}