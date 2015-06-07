package com.nakedape.scrabnart;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;


public class DrawActivity extends ActionBarActivity {
    private String LOG_TAG = "DrawActivity";
    private static int[] tile_pic_ids = {com.nakedape.scrabnart.R.drawable.ic_a_tile, com.nakedape.scrabnart.R.drawable.ic_b_tile, com.nakedape.scrabnart.R.drawable.ic_c_tile, com.nakedape.scrabnart.R.drawable.ic_d_tile, com.nakedape.scrabnart.R.drawable.ic_e_tile,
            com.nakedape.scrabnart.R.drawable.ic_f_tile, com.nakedape.scrabnart.R.drawable.ic_g_tile, com.nakedape.scrabnart.R.drawable.ic_h_tile, com.nakedape.scrabnart.R.drawable.ic_i_tile, com.nakedape.scrabnart.R.drawable.ic_j_tile, com.nakedape.scrabnart.R.drawable.ic_k_tile,
            com.nakedape.scrabnart.R.drawable.ic_l_tile, com.nakedape.scrabnart.R.drawable.ic_m_tile, com.nakedape.scrabnart.R.drawable.ic_n_tile, com.nakedape.scrabnart.R.drawable.ic_o_tile, com.nakedape.scrabnart.R.drawable.ic_p_tile, com.nakedape.scrabnart.R.drawable.ic_q_tile,
            com.nakedape.scrabnart.R.drawable.ic_r_tile, com.nakedape.scrabnart.R.drawable.ic_s_tile, com.nakedape.scrabnart.R.drawable.ic_t_tile, com.nakedape.scrabnart.R.drawable.ic_u_tile, com.nakedape.scrabnart.R.drawable.ic_v_tile, com.nakedape.scrabnart.R.drawable.ic_w_tile,
            com.nakedape.scrabnart.R.drawable.ic_x_tile, com.nakedape.scrabnart.R.drawable.ic_y_tile, com.nakedape.scrabnart.R.drawable.ic_z_tile};
    private static int[] tile_view_ids = {com.nakedape.scrabnart.R.id.tile1, com.nakedape.scrabnart.R.id.tile2, com.nakedape.scrabnart.R.id.tile3, com.nakedape.scrabnart.R.id.tile4, com.nakedape.scrabnart.R.id.tile5, com.nakedape.scrabnart.R.id.tile6, com.nakedape.scrabnart.R.id.tile7,
            com.nakedape.scrabnart.R.id.tile8, com.nakedape.scrabnart.R.id.tile9};
    private static int[] letter_view_ids = {com.nakedape.scrabnart.R.id.letter1, com.nakedape.scrabnart.R.id.letter2, com.nakedape.scrabnart.R.id.letter3, com.nakedape.scrabnart.R.id.letter4, com.nakedape.scrabnart.R.id.letter5, com.nakedape.scrabnart.R.id.letter6, com.nakedape.scrabnart.R.id.letter7, com.nakedape.scrabnart.R.id.letter8};
    private static int EMPTY = -1;

    private Context context;
    private int paletteSelection = com.nakedape.scrabnart.R.id.black;
    private DrawingView drawing;
    PopupWindow popup;
    private String match_id;
    private int[] tiles;
    private int[] letters = {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY};

    boolean cancelDrag = false;
    TileDragEventListener tileDragEventListener = new TileDragEventListener();
    LetterDragEventListener letterDragEventListener = new LetterDragEventListener();
    BackgroundDragEventListener backgroundDragEventListener = new BackgroundDragEventListener();
    View.OnTouchListener tileTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    OnTileMotionMove(v);
                    break;
                case MotionEvent.ACTION_UP:
                    OnTileMotionUp(v);
                    break;
            }
            return false;
        }
    };
    View.OnTouchListener letterTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    OnLetterMotionMove(v);
                    break;
                case MotionEvent.ACTION_UP:
                    OnLetterMotionUp(v);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.nakedape.scrabnart.R.layout.activity_draw);
        context = this;
        drawing = (DrawingView)findViewById(com.nakedape.scrabnart.R.id.drawing);
        // Drag listener to catch misplaced drops
        View view = findViewById(R.id.drawing_viewgroup);
        view.setOnDragListener(backgroundDragEventListener);
        tiles = getIntent().getIntArrayExtra(MainActivity.TILES);
        FillPlayerTiles();
        FillLetterTiles();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.nakedape.scrabnart.R.menu.menu_draw_scrabble, menu);
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

    public void TakeTurn(View v){
        Intent data = new Intent();
        data.putExtra(MainActivity.MATCH_ID, match_id);
        data.putExtra(MainActivity.WORD, letters);
        data.putExtra(MainActivity.TILES, tiles);
        data.putExtra(MainActivity.DRAWING, drawing.getDrawing());
        setResult(MainActivity.RESULT_NEW_WORD, data);
        finish();
    }
    public void OpenPalette(View v){
        // Inflate the popup_layout.xml
        final LinearLayout viewGroup = (LinearLayout) findViewById(com.nakedape.scrabnart.R.id.palette_popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(com.nakedape.scrabnart.R.layout.palette_popup, viewGroup);
        // Create the PopupWindow
        popup = new PopupWindow(context);
        popup.setContentView(layout);
        //Show previous selection
        View selectedColor = layout.findViewById(paletteSelection);
        selectedColor.setSelected(true);
        // Set popup dimensions
        int paletteWidth, paletteHeight;
        paletteWidth = findViewById(com.nakedape.scrabnart.R.id.drawing).getWidth() / 3;
        popup.setWidth(paletteWidth);
        paletteHeight = findViewById(com.nakedape.scrabnart.R.id.drawing).getHeight();
        popup.setHeight(paletteHeight);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new ColorDrawable(
                android.graphics.Color.TRANSPARENT));
        // Displaying the popup at the specified location
        popup.showAsDropDown(drawing, 0, -paletteHeight);
        //popup.showAtLocation(layout, Gravity.START, 0, 0);
    }
    public void ColorSelect(View v){
        popup.dismiss();
        paletteSelection = v.getId();
        switch (paletteSelection){
            case com.nakedape.scrabnart.R.id.black:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.black));
                break;
            case com.nakedape.scrabnart.R.id.blue:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.blue));
                break;
            case com.nakedape.scrabnart.R.id.brown:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.brown));
                break;
            case com.nakedape.scrabnart.R.id.green:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.green));
                break;
            case com.nakedape.scrabnart.R.id.light_blue:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.light_blue));
                break;
            case com.nakedape.scrabnart.R.id.light_brown:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.light_brown));
                break;
            case com.nakedape.scrabnart.R.id.light_green:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.light_green));
                break;
            case com.nakedape.scrabnart.R.id.orange:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.orange));
                break;
            case com.nakedape.scrabnart.R.id.pink:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.pink));
                break;
            case com.nakedape.scrabnart.R.id.red:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.red));
                break;
            case com.nakedape.scrabnart.R.id.violet:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.violet));
                break;
            case com.nakedape.scrabnart.R.id.white:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.white));
                break;
            case com.nakedape.scrabnart.R.id.yellow:
                drawing.setColor(getResources().getColor(com.nakedape.scrabnart.R.color.yellow));
                break;
        }
    }
    private void FillPlayerTiles(){
        ImageButton tile;
        for (int i = 0; i < tiles.length; i++){
            tile = (ImageButton)findViewById(tile_view_ids[i]);
            tile.setOnTouchListener(tileTouchListener);
            tile.setOnDragListener(tileDragEventListener);
            if (tiles[i] != EMPTY) {
                tile.setBackground(getResources().getDrawable(tile_pic_ids[tiles[i]]));
                tile.setTag(i);
            }
            else {
                tile.setBackground(getResources().getDrawable(com.nakedape.scrabnart.R.drawable.empty_tile));
                tile.setTag(i);
            }
        }
    }
    private void FillLetterTiles(){
        ImageButton letter;
        for (int i = 0; i < letters.length; i++){
            letter = (ImageButton)findViewById(letter_view_ids[i]);
            letter.setOnTouchListener(letterTouchListener);
            letter.setOnDragListener(letterDragEventListener);
            if (letters[i] != EMPTY) {
                letter.setBackground(getResources().getDrawable(tile_pic_ids[letters[i]]));
                letter.setTag(i);
            }
            else {
                letter.setBackground(getResources().getDrawable(com.nakedape.scrabnart.R.drawable.empty_tile));
                letter.setTag(i);
            }
        }
    }

    public void OnTileMotionMove(View v){
        cancelDrag = false;
        if (tiles[(int)v.getTag()] != EMPTY) {
            // Create a new ClipData.
            ClipData.Item itemId = new ClipData.Item(String.valueOf(v.getId()));
            ClipData.Item itemChar = new ClipData.Item((String.valueOf(v.getTag())));
            ClipData.Item itemLabel = new ClipData.Item("tile");

            // Create a new ClipData using the tag as a label, the plain text MIME type, and
            // the already-created item. This will create a new ClipDescription object within the
            // ClipData, and set its MIME type entry to "text/plain"
            String[] mime_type = {ClipDescription.MIMETYPE_TEXT_PLAIN};

            ClipData dragData = new ClipData("view_id", mime_type, itemId);
            dragData.addItem(itemChar);
            dragData.addItem(itemLabel);

            // Instantiates the drag shadow builder.
            View.DragShadowBuilder myShadow = new TileDragShadowBuilder(v);

            v.setBackground(getResources().getDrawable(R.drawable.empty_tile));
            // Starts the drag

            v.startDrag(dragData,  // the data to be dragged
                    myShadow,  // the drag shadow builder
                    null,      // no need to use local data
                    0          // flags (not currently used, set to 0)
            );
        }
    }
    public void OnTileMotionUp(View v){
        Log.d(LOG_TAG, "cancelDrag = true");
        int tileIndex = (int)v.getTag();
        if (tiles[tileIndex] != EMPTY) {
            cancelDrag = true;
            boolean keepSearching = true;
            for (int i = 0; i < letters.length && keepSearching; i++) {
                if (letters[i] == EMPTY) {
                    v.setBackground(getResources().getDrawable(R.drawable.empty_tile));
                    letters[i] = tiles[tileIndex];
                    tiles[tileIndex] = EMPTY;
                    View letterView = findViewById(letter_view_ids[i]);
                    letterView.setBackground(getResources().getDrawable(tile_pic_ids[letters[i]]));
                    keepSearching = false;
                }
            }
        }

    }
    public void OnLetterMotionMove(View v){
        cancelDrag = false;
        if (letters[(int)v.getTag()] != EMPTY) {
            // Create a new ClipData.
            ClipData.Item itemId = new ClipData.Item(String.valueOf(v.getId()));
            ClipData.Item itemIndex = new ClipData.Item((String.valueOf(v.getTag())));
            ClipData.Item itemLabel = new ClipData.Item("letter");

            // Create a new ClipData using the tag as a label, the plain text MIME type, and
            // the already-created item. This will create a new ClipDescription object within the
            // ClipData, and set its MIME type entry to "text/plain"
            String[] mime_type = {ClipDescription.MIMETYPE_TEXT_PLAIN};

            ClipData dragData = new ClipData("view_id", mime_type, itemId);
            dragData.addItem(itemIndex);
            dragData.addItem(itemLabel);

            // Instantiates the drag shadow builder.
            View.DragShadowBuilder myShadow = new TileDragShadowBuilder(v);
            //Remove old view
            v.setBackground(getResources().getDrawable(R.drawable.empty_tile));

            // Starts the drag
            v.startDrag(dragData,  // the data to be dragged
                    myShadow,  // the drag shadow builder
                    null,      // no need to use local data
                    0          // flags (not currently used, set to 0)
            );
        }
    }
    public void OnLetterMotionUp(View v){
        int letterIndex = (int)v.getTag();
        if (letters[letterIndex] != EMPTY) {
            cancelDrag = true;
            boolean keepSearching = true;
            for (int i = 0; i < tiles.length && keepSearching; i++) {
                if (tiles[i] == EMPTY) {
                    v.setBackground(getResources().getDrawable(R.drawable.empty_tile));
                    tiles[i] = letters[letterIndex];
                    letters[letterIndex] = EMPTY;
                    View letterView = findViewById(tile_view_ids[i]);
                    letterView.setBackground(getResources().getDrawable(tile_pic_ids[tiles[i]]));
                    keepSearching = false;
                }
            }
        }

    }

    protected class TileDragEventListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {
            String dragData;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    if (cancelDrag){
                        return false;
                    }
                    // Gets the item containing the dragged data
                    // Get view id
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    dragData = item.getText().toString();
                    int initId = Integer.parseInt(dragData);
                    // Get letter value
                    item = event.getClipData().getItemAt(1);
                    dragData = item.getText().toString();
                    int initIndex = Integer.parseInt(dragData);
                    // Get label for letter or tile
                    item = event.getClipData().getItemAt(2);
                    dragData = item.getText().toString();
                    String label = dragData;

                    // Remove tile from previous location
                    int initChar = 0;
                    switch (label){
                        case "tile":
                            initChar = tiles[initIndex];
                            tiles[initIndex] = EMPTY;
                            break;
                        case "letter":
                            initChar = letters[initIndex];
                            letters[initIndex] = EMPTY;
                            break;
                    }

                    // Add tile to new location
                    int displIndex = (int)v.getTag();
                    int displChar = tiles[displIndex];
                    tiles[displIndex] = initChar;
                    v.setBackground(getResources().getDrawable(tile_pic_ids[initChar]));
                    Log.d(LOG_TAG, String.valueOf(initChar));

                    if (displChar != EMPTY) {
                        switch (label){
                            case "tile":
                                tiles[initIndex] = displChar;
                                break;
                            case "letter":
                                letters[initIndex] = displChar;
                                break;
                        }
                        View initView = findViewById(initId);
                        initView.setBackground(getResources().getDrawable(tile_pic_ids[displChar]));
                        Log.d(LOG_TAG, String.valueOf(displChar));
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    return true;
            }

        }
    }
    protected class LetterDragEventListener implements View.OnDragListener{
        public boolean onDrag(View v, DragEvent event) {
            String dragData;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    if (cancelDrag){
                        return false;
                    }
                    // Gets the item containing the dragged data
                    // Get view id
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    dragData = item.getText().toString();
                    int initId = Integer.parseInt(dragData);
                    // Get letter value
                    item = event.getClipData().getItemAt(1);
                    dragData = item.getText().toString();
                    int initIndex = Integer.parseInt(dragData);
                    // Get label for letter or tile
                    item = event.getClipData().getItemAt(2);
                    dragData = item.getText().toString();
                    String label = dragData;

                    // Remove tile from previous location
                    int initChar = 0;
                    switch (label){
                        case "tile":
                            initChar = tiles[initIndex];
                            tiles[initIndex] = EMPTY;
                            break;
                        case "letter":
                            initChar = letters[initIndex];
                            letters[initIndex] = EMPTY;
                            break;
                    }

                    // Add tile to new location
                    int displIndex = (int)v.getTag();
                    int displChar = letters[displIndex];
                    letters[displIndex] = initChar;
                    v.setBackground(getResources().getDrawable(tile_pic_ids[initChar]));
                    Log.d(LOG_TAG, String.valueOf(initChar));

                    if (displChar != EMPTY) {
                        switch (label){
                            case "tile":
                                tiles[initIndex] = displChar;
                                break;
                            case "letter":
                                letters[initIndex] = displChar;
                                break;
                        }
                        View initView = findViewById(initId);
                        initView.setBackground(getResources().getDrawable(tile_pic_ids[displChar]));
                        Log.d(LOG_TAG, String.valueOf(displChar));
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    return true;
            }
        }
    }
    protected class BackgroundDragEventListener implements View.OnDragListener{
        public boolean onDrag(View v, DragEvent event){
            String dragData;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP: // Restore tile to initial position
                    if (cancelDrag){
                        return false;
                    }
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    dragData = item.getText().toString();
                    int initId = Integer.parseInt(dragData);
                    // Get letter value
                    item = event.getClipData().getItemAt(1);
                    dragData = item.getText().toString();
                    int index = Integer.parseInt(dragData);
                    item = event.getClipData().getItemAt(2);
                    String label = item.getText().toString();
                    View view = findViewById(initId);
                    switch (label) {
                        case "tile":
                            view.setBackground(getResources().getDrawable(tile_pic_ids[tiles[index]]));
                            break;
                        case "letter":
                            view.setBackground(getResources().getDrawable(tile_pic_ids[letters[index]]));
                            break;
                    }
                    Log.d(LOG_TAG, "Background drop");
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    return true;
            }
        }

    }
}
