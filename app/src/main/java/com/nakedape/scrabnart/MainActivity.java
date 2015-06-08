package com.nakedape.scrabnart;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.LoadMatchesResponse;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String LOG_TAG = "MainActivity";

    public final static int RESULT_NEW_WORD = 4001;
    public final static int GUESS_RESULT = 4002;

    public final static String TILES = "com.nakedape.scrabnart.tiles";
    public final static String WORD = "com.nakedape.scrabnart.word";
    public final static String DRAWING = "com.nakedape.scrabnart.drawing";
    public final static String MATCH_ID = "com.nakedape.scrabnart.match_id";
    public final static String POINTS = "com.nakedape.scrabnart.points";
    public final static String GAME_DATA ="com.nakedape.scrabnart.game_data";

    private GoogleApiClient mGoogleApiClient;
    private Context context = this;
    private ScrabnartGame scrabnartGame;
    private ArrayList<String> leaveMatchList;
    private ArrayList<String> declineInvitationList;

    // Activity method overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.nakedape.scrabnart.R.layout.activity_main);
        ListView listView = (ListView)findViewById(R.id.invitation_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MatchListElement match = (MatchListElement) parent.getItemAtPosition(position);
                if (match != null) {
                    switch (match.getType()) {
                        case MatchListElement.INVITATION:
                            Invitation invitation = match.getInvitation();
                            Games.TurnBasedMultiplayer.acceptInvitation(mGoogleApiClient, invitation.getInvitationId()).setResultCallback(new MatchInitiatedCallback());
                            break;
                        case MatchListElement.MY_TURN_MATCH:
                            StartGuessTurn(match.getMatch());
                            break;
                    }
                }
            }
        });
        leaveMatchList = new ArrayList<>();
        declineInvitationList = new ArrayList<>();
        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                        // add other APIs and scopes here as needed
                .build();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.nakedape.scrabnart.R.menu.menu_main, menu);
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
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (myMatchesResponse != null) myMatchesResponse.release();
        if (theirMatchesResponse != null) theirMatchesResponse.release();
        mGoogleApiClient.disconnect();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    // Bring up an error dialog to alert the user that sign-in
                    // failed. The R.string.signin_failure should reference an error
                    // string in your strings.xml file that tells the user they
                    // could not be signed in, such as "Unable to sign in."
                    BaseGameUtils.showActivityResultError(this,
                            requestCode, resultCode, R.string.sign_in_failed);
                }
                break;
            case RC_SELECT_PLAYERS:
                if (resultCode != Activity.RESULT_OK) {
                    // user canceled
                    return;
                }
                SelectPlayers(intent);
                break;
            case RESULT_NEW_WORD:
                if (resultCode != Activity.RESULT_CANCELED)
                    FinishDrawTurn(intent);
                break;
            case GUESS_RESULT:
                if (resultCode != Activity.RESULT_CANCELED){
                    FinishGuessTurn(intent);
                }
                break;

        }
    }

    // Google API client variables, overrides, and related methods
    private final static int RC_SIGN_IN = 9001;
    private final static int RC_SELECT_PLAYERS = 9002;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    private LoadMatchesResponse myMatchesResponse;
    private LoadMatchesResponseAdapter myTurnItemAdapter;
    private ResultCallback<TurnBasedMultiplayer.LoadMatchesResult> myTurnMatchesResultCallback = new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {
        @Override
        public void onResult(TurnBasedMultiplayer.LoadMatchesResult loadMatchesResult) {
            myMatchesResponse = loadMatchesResult.getMatches();
            myTurnItemAdapter = new LoadMatchesResponseAdapter(context, R.layout.invitiation_row);
            myTurnItemAdapter.addAll(myMatchesResponse);
            ListView inviteListView = (ListView) findViewById(R.id.invitation_listview);
            View spinner = findViewById(R.id.progress1);
            spinner.setVisibility(View.GONE);
            inviteListView.setVisibility(View.VISIBLE);
            inviteListView.setAdapter(myTurnItemAdapter);
            Log.d("My Turn Matches", String.valueOf(myMatchesResponse.getMyTurnMatches().getCount()));
            Log.d("Invitations", String.valueOf(myMatchesResponse.getInvitations().getCount()));
        }
    };
    private LoadMatchesResponse theirMatchesResponse;
    private LoadMatchesResponseAdapter theirTurnItemAdapter;
    private ResultCallback<TurnBasedMultiplayer.LoadMatchesResult> theirTurnMatchesResultCallback = new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {
        @Override
        public void onResult(TurnBasedMultiplayer.LoadMatchesResult loadMatchesResult) {
            theirMatchesResponse = loadMatchesResult.getMatches();
            theirTurnItemAdapter = new LoadMatchesResponseAdapter(context, R.layout.invitiation_row);
            View spinner = findViewById(R.id.progress2);
            spinner.setVisibility(View.GONE);
            ListView theirTurnListView = (ListView)findViewById(R.id.their_turn_listview);
            theirTurnListView.setVisibility(View.VISIBLE);
            theirTurnItemAdapter.addAll(theirMatchesResponse);
            theirTurnListView.setAdapter(theirTurnItemAdapter);
        }
    };
    @Override
    public void onConnected(Bundle connectionHint) {
        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        findViewById(R.id.newGameButton).setEnabled(true);
        if (scrabnartGame != null && !scrabnartGame.isSaved) {
            Log.d(LOG_TAG, "turn submitted via onConnnected");
            Log.d(LOG_TAG, "Player 1: " + scrabnartGame.getPlayer1_id() + " Player 2: " + scrabnartGame.getPlayer2_id());
            Log.d(LOG_TAG, "Next player: " + scrabnartGame.getNextPlayer());
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, scrabnartGame.getMatch_id(), scrabnartGame.getGameData(), scrabnartGame.getNextPlayer());
            scrabnartGame.isSaved = true;
        }
        for (String s : leaveMatchList){
            Games.TurnBasedMultiplayer.leaveMatch(mGoogleApiClient, s);
        }
        leaveMatchList.clear();
        for (String s : declineInvitationList){
            Games.TurnBasedMultiplayer.declineInvitation(mGoogleApiClient, s);
        }
        declineInvitationList.clear();
        Games.TurnBasedMultiplayer
                .loadMatchesByStatus(mGoogleApiClient, new int[]{TurnBasedMatch.MATCH_TURN_STATUS_INVITED, TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN})
                .setResultCallback(myTurnMatchesResultCallback);
        Games.TurnBasedMultiplayer
                .loadMatchesByStatus(mGoogleApiClient, new int[] {TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN})
                .setResultCallback(theirTurnMatchesResultCallback);
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getResources().getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        // Put code here to display the sign-in button
    }
    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }
    private void SelectPlayers(Intent data){

        // Get the invitee list.
        final ArrayList<String> invitees =
                data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        // Get auto-match criteria.
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(
                Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(
                Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
        } else {
            autoMatchCriteria = null;
        }

        TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                .addInvitedPlayers(invitees)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        // Create and start the match.
        Games.TurnBasedMultiplayer
                .createMatch(mGoogleApiClient, tbmc)
                .setResultCallback(new MatchInitiatedCallback());
    }
    public class MatchInitiatedCallback implements ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> {

        @Override
        public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
            // Check if the status code is not success.
            Status status = result.getStatus();
            if (status.isSuccess()) {
                Log.d("Game start", status.getStatusMessage());


                TurnBasedMatch match = result.getMatch();
                if (match == null) return;

                // If this player is not the first player in this match, continue.
                if (match.getData() != null) {
                    StartGuessTurn(match);
                    return;
                }

                // Otherwise, this is the first player. Initialize the game state.
                ArrayList<String> playerIds = match.getParticipantIds();
                String player1_id = match.getCreatorId();
                String player2_id = playerIds.get(1);
                for (String s : playerIds){
                    if (s != player1_id)
                        player2_id = s;
                }
                scrabnartGame = new ScrabnartGame(match.getMatchId(), player1_id, player2_id);
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(), scrabnartGame.getGameData(), player1_id);
                }
                scrabnartGame.isSaved = true;
                Log.d(LOG_TAG, "Player 1: " + scrabnartGame.getPlayer1_id() + " Player 2: " + scrabnartGame.getPlayer2_id());
                Log.d(LOG_TAG, "Next player: " + scrabnartGame.getNextPlayer());
                // Let the player take the first turn
                Intent myIntent = new Intent();
                myIntent.setClassName("com.nakedape.scrabnart", "com.nakedape.scrabnart.DrawActivity");
                myIntent.putExtra(MATCH_ID, match.getMatchId());
                myIntent.putExtra(TILES, scrabnartGame.getPlayerTilesInt(scrabnartGame.getPlayer1_id()));
                startActivityForResult(myIntent, RESULT_NEW_WORD);


            }
        }
    }
    public void GoogleSignInClick(View view){
        if (view.getId() == R.id.sign_in_button) {
            // start the asynchronous sign in flow
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
        else if (view.getId() == R.id.sign_out_button) {
            // sign out.
            mSignInClicked = false;
            Games.signOut(mGoogleApiClient);

            // show sign-in button, hide the sign-out button
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }
    private void UploadGame(String match_id, String notification){
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d(LOG_TAG, "Player 1: " + scrabnartGame.getPlayer1_id() + " Player 2: " + scrabnartGame.getPlayer2_id());
            Log.d(LOG_TAG, "Next player: " + scrabnartGame.getNextPlayer());
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match_id, scrabnartGame.getGameData(), scrabnartGame.getNextPlayer());
            if (notification != null)
                Toast.makeText(context, notification, Toast.LENGTH_SHORT).show();
            scrabnartGame.isSaved = true;
        }
        else {
            scrabnartGame.isSaved = false;
        }
    }

    // Game play methods and variables
    public void NewGame(View v){
        Intent intent =
                Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 1, false);
        startActivityForResult(intent, RC_SELECT_PLAYERS);

    }
    private void StartDrawTurn(String match_id){
        Intent myIntent = new Intent();
        myIntent.setClassName("com.nakedape.scrabnart", "com.nakedape.scrabnart.DrawActivity");
        myIntent.putExtra(MATCH_ID, match_id);
        myIntent.putExtra(TILES, scrabnartGame.getPlayerTilesInt(scrabnartGame.getNextPlayer()));
        startActivityForResult(myIntent, RESULT_NEW_WORD);

    }
    private void FinishDrawTurn(Intent data){
        String match_id = data.getStringExtra(MATCH_ID);
        int[] tiles = data.getIntArrayExtra(TILES);
        int[] word = data.getIntArrayExtra(WORD);
        ArrayList<DrawEvent> drawing = data.getParcelableArrayListExtra(DRAWING);
        scrabnartGame.TakeDrawingTurn(word, tiles, drawing);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d(LOG_TAG, "Player 1: " + scrabnartGame.getPlayer1_id() + " Player 2: " + scrabnartGame.getPlayer2_id());
            Log.d(LOG_TAG, "Next player: " + scrabnartGame.getNextPlayer());
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match_id, scrabnartGame.getGameData(), scrabnartGame.getNextPlayer());
            Toast.makeText(context, "Played " + scrabnartGame.getTilesAsString(data.getIntArrayExtra(TILES)).toUpperCase(), Toast.LENGTH_SHORT).show();
            scrabnartGame.isSaved = true;
        }
    }
    private void StartGuessTurn(TurnBasedMatch match){
        scrabnartGame = new ScrabnartGame(match.getData());
        scrabnartGame.isSaved = true;
        Log.d(LOG_TAG, "Next player: " + scrabnartGame.getNextPlayer());
        Intent myIntent = new Intent();
        myIntent.setClassName("com.nakedape.scrabnart", "com.nakedape.scrabnart.GuessActivity");
        myIntent.putExtra(MATCH_ID, match.getMatchId());
        myIntent.putExtra(GAME_DATA, match.getData());
        startActivityForResult(myIntent, GUESS_RESULT);
    }
    private void FinishGuessTurn(Intent data){
        String match_id = data.getStringExtra(MATCH_ID);
        scrabnartGame = new ScrabnartGame(data.getByteArrayExtra(GAME_DATA));
        UploadGame(match_id, null);
        StartDrawTurn(match_id);
    }
    private void LeaveMatch(String match_id){
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            Games.TurnBasedMultiplayer.leaveMatch(mGoogleApiClient, match_id);
        }
        else {
            leaveMatchList.add(match_id);
        }
    }
    private void DeclineInvitation(String match_id){
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            Games.TurnBasedMultiplayer.declineInvitation(mGoogleApiClient, match_id);
        }
        else {
            declineInvitationList.add(match_id);
        }
    }


    private int myTurnItemPosition = 0;
    private PopupMenu.OnMenuItemClickListener myTurnItemMenuListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Log.d(LOG_TAG, "onMenuItemClick " + String.valueOf(myTurnItemPosition));
            TurnBasedMatch match = null;
            Invitation invitation = null;
            if (myTurnItemAdapter.getItem(myTurnItemPosition).getType() == MatchListElement.MY_TURN_MATCH) {
                match = myTurnItemAdapter.getItem(myTurnItemPosition).getMatch();
            }
            else {
                invitation = myTurnItemAdapter.getItem(myTurnItemPosition).getInvitation();
            }
            switch (item.getItemId()){
                case R.id.action_accept_invitation:
                    return true;
                case R.id.action_decline_invitation:
                    myTurnItemAdapter.removeItem(myTurnItemPosition);
                    DeclineInvitation(invitation.getInvitationId());
                    return true;
                case R.id.action_take_my_turn:
                    myTurnItemAdapter.removeItem(myTurnItemPosition);
                    StartGuessTurn(match);
                    return true;
                case R.id.action_leave_match:
                    myTurnItemAdapter.removeItem(myTurnItemPosition);
                    LeaveMatch(match.getMatchId());
                    return true;
                default:
                    return false;
            }
        }
    };
    private int theirTurnItemPosition = 0;
    private PopupMenu.OnMenuItemClickListener theirTurnItemMenuListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Log.d(LOG_TAG, "onMenuItemClick " + String.valueOf(myTurnItemPosition));
            TurnBasedMatch match = theirTurnItemAdapter.getItem(theirTurnItemPosition).getMatch();
            switch (item.getItemId()){
                case R.id.action_leave_match:
                    theirTurnItemAdapter.removeItem(myTurnItemPosition);
                    LeaveMatch(match.getMatchId());
                    return true;
                default:
                    return false;
            }
        }
    };
    private class LoadMatchesResponseAdapter extends BaseAdapter {


        private ArrayList<MatchListElement> mData;
        private LayoutInflater mInflater;
        private Context context;
        private int resource_id;

        public LoadMatchesResponseAdapter(Context context, int resource_id) {
            this.context = context;
            this.resource_id = resource_id;
            mData = new ArrayList<>(5);
        }

        public void addAll(LoadMatchesResponse matchesResponse){
            if (matchesResponse.getInvitations() != null && matchesResponse.getInvitations().getCount() > 0) {
                for (Invitation i : matchesResponse.getInvitations()) {
                    mData.add(new MatchListElement(i));
                }
            }
            if (matchesResponse.getMyTurnMatches() != null && matchesResponse.getMyTurnMatches().getCount() > 0) {
                for (TurnBasedMatch m : matchesResponse.getMyTurnMatches()) {
                    mData.add(new MatchListElement(m, MatchListElement.MY_TURN_MATCH));
                }
            }
            if (matchesResponse.getTheirTurnMatches() != null && matchesResponse.getTheirTurnMatches().getCount() > 0) {
                for (TurnBasedMatch m : matchesResponse.getTheirTurnMatches()){
                    mData.add(new MatchListElement(m, MatchListElement.THEIR_TURN_MATCH));
                }
            }
        }
        @Override
        public int getCount() {
            return mData.size();
        }
        @Override
        public MatchListElement getItem(int position) {
            return mData.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        public void removeItem(int position) {
            mData.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                MatchListElement data = mData.get(position);
                switch (data.matchType){
                    case MatchListElement.INVITATION:
                        convertView = getInvitationView(position);
                        break;
                    case MatchListElement.MY_TURN_MATCH:
                        convertView = getMyMatchView(position);
                        break;
                    case MatchListElement.THEIR_TURN_MATCH:
                        convertView = getTheirMatchView(position);
                        break;
                }
            }
            AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(context,
                    R.animator.animator_listitem_add);
            set.setStartDelay(position * 200);
            set.setTarget(convertView);
            set.start();
            return convertView;
        }
        private View getInvitationView(final int position){
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = mInflater.inflate(R.layout.invitiation_row, null);
            ImageView picture = (ImageView)convertView.findViewById(R.id.imageView1);
            String imageUrl;
            TextView textView = (TextView) convertView.findViewById(R.id.textView1);
            Button button;
            final MatchListElement data = mData.get(position);
            Invitation invitation = data.getInvitation();
            ImageManager.create(context).loadImage(picture, invitation.getInviter().getIconImageUri());
            textView.setText(data.getInvitation().getInviter().getDisplayName() + getString(R.string.match_invitation));
            convertView.findViewById(R.id.match_row_layout).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            return false;
                        case MotionEvent.ACTION_UP:
                            Log.d(LOG_TAG, "Row action up");
                            TurnBasedMatch match = mData.get(position).getMatch();
                            mData.remove(position);
                            notifyDataSetChanged();
                            StartGuessTurn(match);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            ImageButton menuButton = (ImageButton)convertView.findViewById(R.id.menu_button);
            final PopupMenu menu = new PopupMenu(context, menuButton);
            menu.inflate(R.menu.menu_invitation_item);
            menu.setOnMenuItemClickListener(myTurnItemMenuListener);
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myTurnItemPosition = position;
                    Log.d(LOG_TAG, "onClick " + String.valueOf(position));
                    menu.show();
                }
            });
            return convertView;
        }
        private View getMyMatchView(final int position) {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = mInflater.inflate(R.layout.invitiation_row, null);
            ImageView picture = (ImageView) convertView.findViewById(R.id.imageView1);
            String imageUrl;
            TextView textView = (TextView) convertView.findViewById(R.id.textView1);
            final MatchListElement data = mData.get(position);
            TurnBasedMatch myTurnMatch = data.getMatch();
            ImageManager.create(context).loadImage(picture, myTurnMatch.getParticipant(myTurnMatch.getLastUpdaterId()).getIconImageUri());
            textView.setText(myTurnMatch.getParticipant(myTurnMatch.getLastUpdaterId()).getDisplayName() + getString(R.string.my_turn_msg));
            convertView.findViewById(R.id.match_row_layout).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            return false;
                        case MotionEvent.ACTION_UP:
                            Log.d(LOG_TAG, "Row action up");
                            TurnBasedMatch match = mData.get(position).getMatch();
                            mData.remove(position);
                            notifyDataSetChanged();
                            StartGuessTurn(match);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            ImageButton menuButton = (ImageButton)convertView.findViewById(R.id.menu_button);
            final PopupMenu menu = new PopupMenu(context, menuButton);
            menu.inflate(R.menu.menu_myturn_item);
            menu.setOnMenuItemClickListener(myTurnItemMenuListener);
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myTurnItemPosition = position;
                    menu.show();;
                }
            });
            return convertView;
        }
        private View getTheirMatchView(final int position) {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = mInflater.inflate(R.layout.invitiation_row, null);
            ImageView picture = (ImageView) convertView.findViewById(R.id.imageView1);
            String imageUrl;
            TextView textView = (TextView) convertView.findViewById(R.id.textView1);
            Button button;
            final MatchListElement data = mData.get(position);
            TurnBasedMatch theirTurnMatch = data.getMatch();
            ImageManager.create(context).loadImage(picture, theirTurnMatch.getParticipant(theirTurnMatch.getLastUpdaterId()).getIconImageUri());
            String msg = getString(R.string.their_turn_msg) + theirTurnMatch.getParticipant(theirTurnMatch.getPendingParticipantId()).getDisplayName();
            textView.setText(msg);
            ImageButton menuButton = (ImageButton)convertView.findViewById(R.id.menu_button);
            final PopupMenu menu = new PopupMenu(context, menuButton);
            menu.inflate(R.menu.menu_their_turn_item);
            menu.setOnMenuItemClickListener(theirTurnItemMenuListener);
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    theirTurnItemPosition = position;
                    menu.show();
                }
            });
            return convertView;
        }
    }
    private class MatchListElement {
        public static final int INVITATION = 0;
        public static final int MY_TURN_MATCH = 1;
        public static final int THEIR_TURN_MATCH = 2;
        private Invitation invitation;
        private TurnBasedMatch match;
        private int matchType;

        public MatchListElement(){}
        public MatchListElement(Invitation invitation){
            this.invitation = invitation;
            matchType = INVITATION;
        }
        public MatchListElement(TurnBasedMatch match, int myOrTheirTurn){
            this.match = match;
            matchType = myOrTheirTurn;
        }
        public int getType(){return matchType;}
        public Invitation getInvitation(){return invitation;}
        public TurnBasedMatch getMatch(){return match;}
    }
}
