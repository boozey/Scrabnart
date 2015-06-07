package com.nakedape.scrabnart;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;


public class GuessActivity extends ActionBarActivity {

    private static int[] tile_pic_ids = {com.nakedape.scrabnart.R.drawable.ic_a_tile, com.nakedape.scrabnart.R.drawable.ic_b_tile, com.nakedape.scrabnart.R.drawable.ic_c_tile, com.nakedape.scrabnart.R.drawable.ic_d_tile, com.nakedape.scrabnart.R.drawable.ic_e_tile,
            com.nakedape.scrabnart.R.drawable.ic_f_tile, com.nakedape.scrabnart.R.drawable.ic_g_tile, com.nakedape.scrabnart.R.drawable.ic_h_tile, com.nakedape.scrabnart.R.drawable.ic_i_tile, com.nakedape.scrabnart.R.drawable.ic_j_tile, com.nakedape.scrabnart.R.drawable.ic_k_tile,
            com.nakedape.scrabnart.R.drawable.ic_l_tile, com.nakedape.scrabnart.R.drawable.ic_m_tile, com.nakedape.scrabnart.R.drawable.ic_n_tile, com.nakedape.scrabnart.R.drawable.ic_o_tile, com.nakedape.scrabnart.R.drawable.ic_p_tile, com.nakedape.scrabnart.R.drawable.ic_q_tile,
            com.nakedape.scrabnart.R.drawable.ic_r_tile, com.nakedape.scrabnart.R.drawable.ic_s_tile, com.nakedape.scrabnart.R.drawable.ic_t_tile, com.nakedape.scrabnart.R.drawable.ic_u_tile, com.nakedape.scrabnart.R.drawable.ic_v_tile, com.nakedape.scrabnart.R.drawable.ic_w_tile,
            com.nakedape.scrabnart.R.drawable.ic_x_tile, com.nakedape.scrabnart.R.drawable.ic_y_tile, com.nakedape.scrabnart.R.drawable.ic_z_tile};
    private static int[] tile_view_ids = {com.nakedape.scrabnart.R.id.tile1, com.nakedape.scrabnart.R.id.tile2, com.nakedape.scrabnart.R.id.tile3, com.nakedape.scrabnart.R.id.tile4, com.nakedape.scrabnart.R.id.tile5, com.nakedape.scrabnart.R.id.tile6, com.nakedape.scrabnart.R.id.tile7,
            com.nakedape.scrabnart.R.id.tile8, com.nakedape.scrabnart.R.id.tile9};
    private static int[] letter_view_ids = {com.nakedape.scrabnart.R.id.letter1, com.nakedape.scrabnart.R.id.letter2, com.nakedape.scrabnart.R.id.letter3, com.nakedape.scrabnart.R.id.letter4, com.nakedape.scrabnart.R.id.letter5, com.nakedape.scrabnart.R.id.letter6, com.nakedape.scrabnart.R.id.letter7, com.nakedape.scrabnart.R.id.letter8};
    private static final int EMPTY = -1;
    private DrawingPlayer drawingPlayer;
    private int match_id;
    private int[] word;
    private int[] tiles;
    private int[] letters = {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY};
    private int points = 0;
    private ArrayList<DrawEvent> drawing;
    private Dialog winPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.nakedape.scrabnart.R.layout.activity_guess);
        drawingPlayer = (DrawingPlayer)findViewById(com.nakedape.scrabnart.R.id.drawing_player);
        Intent data = getIntent();
        match_id = data.getIntExtra(MainActivity.MATCH_ID, 0);
        word = data.getIntArrayExtra(MainActivity.WORD);
        tiles = data.getIntArrayExtra(MainActivity.TILES);
        drawing = data.getParcelableArrayListExtra(MainActivity.DRAWING);
        drawingPlayer.SetData(drawing);
        FillGuessTiles();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.nakedape.scrabnart.R.menu.menu_guess, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.nakedape.scrabnart.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void PlayDrawing(View v) {
        Log.d("Drawing", "Playing animation");
        drawingPlayer.Play();
    }
    public void ClickTile(View v){
        int tileIndex = (int)v.getTag();
        if (tiles[tileIndex] != EMPTY) {
            boolean keepSearching = true;
            for (int i = 0; i < letters.length && keepSearching; i++) {
                if (letters[i] == EMPTY) {
                    v.setBackground(getResources().getDrawable(R.drawable.empty_tile));
                    letters[i] = tiles[tileIndex];
                    tiles[tileIndex] = EMPTY;
                    View letterView = findViewById(letter_view_ids[i]);
                    letterView.setBackground(getResources().getDrawable(tile_pic_ids[letters[i]]));
                    letterView.setTag(i);
                    keepSearching = false;
                }
            }
            CheckForWinner();
        }

    }
    public void ClickLetter(View v){
        int letterIndex = (int)v.getTag();
        if (letters[letterIndex] != EMPTY){
            boolean keepSearching = true;
            for (int i = 0; i < tiles.length && keepSearching; i++){
                if (tiles[i] == EMPTY){
                    v.setBackground(getResources().getDrawable(R.drawable.empty_tile));
                    tiles[i] = letters[letterIndex];
                    letters[letterIndex] = EMPTY;
                    View tileView = findViewById(tile_view_ids[i]);
                    tileView.setBackground(getResources().getDrawable(tile_pic_ids[tiles[i]]));
                    tileView.setTag(i);
                    keepSearching = false;
                }
            }
        }
    }
    private void CheckForWinner(){
        boolean winner = true;
        for (int i = 0; i < word.length && winner; i++){
            winner = word[i] == letters[i];
        }
        if (winner) {
            for (int l : word){
                if (l != EMPTY){
                    points += ScrabnartGame.point_values[l];
                }
            }
            displayWinnerPopup();
        }
    }
    private void displayWinnerPopup(){
        // Inflate the popup_layout.xml
        RelativeLayout viewGroup = (RelativeLayout) findViewById(com.nakedape.scrabnart.R.id.winner_popup);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(com.nakedape.scrabnart.R.layout.winner_popup, viewGroup);
        LinearLayout tileRow = (LinearLayout)layout.findViewById(R.id.letter_layout);
        for (int l : word){
            if (l != EMPTY){
                ImageView tileView = new ImageView(this);
                tileView.setBackground(getResources().getDrawable(tile_pic_ids[l]));
                tileRow.addView(tileView);
            }
        }
        winPopup = new Dialog(this);
        winPopup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        winPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        winPopup.setContentView(layout);
        winPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                PopupClick(null);
            }
        });
        winPopup.show();
    }
    public void PopupClick(View v){
        if (winPopup.isShowing()) winPopup.dismiss();
        Intent data = new Intent();
        data.putExtra(MainActivity.MATCH_ID, match_id);
        data.putExtra(MainActivity.POINTS, points);
        setResult(MainActivity.GUESS_RESULT, data);
        finish();

    }
    private void FillGuessTiles(){
        ImageView tileImage;
        for (int i = 0; i < tiles.length; i++){
            tileImage = (ImageView)findViewById(tile_view_ids[i]);
            tileImage.setBackground(getResources().getDrawable(tile_pic_ids[tiles[i]]));
            tileImage.setTag(i);
        }
    }
}
