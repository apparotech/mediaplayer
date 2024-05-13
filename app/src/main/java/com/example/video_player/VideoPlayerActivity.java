package com.example.video_player;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.example.video_player.VideoFilesActivity.videoFilesAdapter;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.AudioMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.video_player.Adapter.PlaybackIconsAdapter;
import com.example.video_player.Adapter.VideoFilesAdapter;
import com.example.video_player.models.BrightnessDialog;
import com.example.video_player.models.IconModel;
import com.example.video_player.models.MediaFiles;
import com.example.video_player.models.OnSwipeTouchListener;
import com.example.video_player.models.PlaylistDialog;
import com.example.video_player.models.Utility;
import com.example.video_player.models.VolumeDialog;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener  {

    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String videoTitle;
    TextView title;
    PlaybackParameters parameters;
    private ControlsMode controlsMode;
    RelativeLayout root;
    ImageView videoBack, lock, unlock, scaling, videoList, videoMore;
    ImageView nextButton, previousButton;
    public enum ControlsMode {
        FULL_LOCK,
        SCREEN
    }
    ConcatenatingMediaSource concatenatingMediaSource;
    //horizontal recyclerview variables--------------
    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    RecyclerView recyclerViewIcons;
    boolean expand = false;
    DialogProperties dialogProperties;
    FilePickerDialog filePickerDialog;
    Uri uriSubtitle;
    PlaybackIconsAdapter playbackIconsAdapter;
    View nightMode;
    boolean mute = false;
    boolean dark = false;
    float speed;
    PictureInPictureParams.Builder pictureInPicture;
    //horizontal recyclerview variables------------
    //----------------swipe and zoom variables------------------
    private int device_height, device_width, brightness, media_volume;
    boolean start = false;
    boolean left, right;
    private float baseX, baseY;
    boolean swipe_move = false;
    private long diffX, diffY;
    public static final int MINIMUM_DISTANCE = 100;
    boolean success = false;
    private ContentResolver contentResolver;
    private Window window;
    LinearLayout vol_progress_container, vol_text_container, brt_progress_container, brt_text_container;
    ProgressBar  vol_progress,brt_progress;
    ImageView vol_icon, brt_icon;
    TextView vol_text, brt_text, total_duration;
    AudioManager audioManager;
    ScaleGestureDetector scaleGestureDetector;
    private float scale_factor = 1.0f;
    RelativeLayout zoomLayout;
    RelativeLayout zoomContainer;
    TextView zoom_perc;
    //-------------swipe and zoom variables-------------------------
    boolean double_tap = false;
    boolean singleTap = false;
    RelativeLayout double_tap_playpause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
       //    getSupportActionBar().hide();
        hideBottomBar();
        playerView = findViewById(R.id.exoplayer_view);
        position = getIntent().getIntExtra("position",1);
        videoTitle = getIntent().getStringExtra("video_title");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        screenOrientation();
        initViews();
        playVideo();
        fit();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels;
        device_height = displayMetrics.heightPixels;

        playerView.setOnTouchListener(new OnSwipeTouchListener(this) {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        playerView.showController();
                        start = true;
                        if (motionEvent.getX() < (device_width /2)) {
                            left = true;
                            right = false;
                        } else if (motionEvent.getX() > (device_width /2 )) {
                            left = false;
                            right = true;
                        }
                        baseX = motionEvent.getX();
                        baseY = motionEvent.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        swipe_move = true;
                        diffX = (long) Math.ceil(motionEvent.getX() - baseX);
                        diffY = (long) Math.ceil(motionEvent.getY() - baseY);
                        double brightnessSpeed = 0.01;
                        if (Math.abs(diffY) > MINIMUM_DISTANCE) {
                            start = true;
                            if (Math.abs(diffY)> Math.abs(diffX)) {
                                boolean value;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    value = android.provider.Settings.System.canWrite(getApplicationContext());
                                    if (value) {
                                        if (left) {
                                            contentResolver = getContentResolver();
                                            window = getWindow();
                                           try {
                                               android.provider.Settings.System.putInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                                                       android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                               brightness = android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS);
                                           } catch (
                                                   android.provider.Settings.SettingNotFoundException e) {
                                               e.printStackTrace();
                                           }
                                           int new_brightness = (int)  (brightness - (diffY * brightnessSpeed));
                                           if (new_brightness > 250) {
                                               new_brightness = 250;
                                           } else if (new_brightness <1) {
                                               new_brightness = 1;
                                           }
                                            double brt_percentage =  Math.ceil((((double) new_brightness / (double) 250) * (double) 100));
                                            brt_progress_container.setVisibility(View.VISIBLE);
                                            brt_text_container.setVisibility(View.VISIBLE);
                                            brt_progress.setProgress((int) brt_percentage);

                                            if (brt_percentage < 30) {
                                                brt_icon.setImageResource(R.drawable.ic_brightness_low);
                                            } else if (brt_percentage > 30 && brt_percentage < 80) {
                                                brt_icon.setImageResource(R.drawable.ic_brightness_moderate);
                                            } else if (brt_percentage > 80) {
                                                brt_icon.setImageResource(R.drawable.ic_brightness);
                                            }
                                            brt_text.setText(" " + (int) brt_percentage + "%");
                                            android.provider.Settings.System.putInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS,
                                                    (new_brightness));
                                            WindowManager.LayoutParams layoutParams = window.getAttributes();
                                            layoutParams.screenBrightness = brightness / (float) 255;
                                            window.setAttributes(layoutParams);

                                        } else if (right) {
                                            vol_text_container.setVisibility(View.VISIBLE);
                                            media_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                            double cal = (double) diffY *((double) maxVol / ((double) (device_height * 2) - brightnessSpeed));
                                            int newMediaVolume = media_volume - (int) cal;
                                            if (newMediaVolume > maxVol) {
                                                newMediaVolume = maxVol;
                                            } else if (newMediaVolume < 1) {
                                                newMediaVolume = 0;
                                            }
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                                    newMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                            double volPer = Math.ceil((((double) newMediaVolume / (double) maxVol) * (double) 100));
                                            vol_text.setText(" " + (int) volPer + "%");
                                            if (volPer < 1) {
                                                vol_icon.setImageResource(R.drawable.ic_volume_off);
                                                vol_text.setVisibility(View.VISIBLE);
                                                vol_text.setText("Off");
                                            } else if (volPer >= 1) {
                                                vol_icon.setImageResource(R.drawable.ic_volume);
                                                vol_text.setVisibility(View.VISIBLE);
                                            }
                                            vol_progress_container.setVisibility(View.VISIBLE);
                                            vol_progress.setProgress((int) volPer);

                                        }
                                        success = true;
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Allow write settings for swipe controls", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        startActivityForResult(intent, 111);
                                    }
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        swipe_move = false;
                        start = false;
                        vol_progress_container.setVisibility(View.GONE);
                        brt_progress_container.setVisibility(View.GONE);
                        vol_text_container.setVisibility(View.GONE);
                        brt_text_container.setVisibility(View.GONE);
                        break;
                }
                scaleGestureDetector.onTouchEvent(motionEvent);
                return super.onTouch(view, motionEvent);
            }

            public void onDoubleTouch() {
                super.onDoubleTouch();
                if (double_tap) {
                    player.setPlayWhenReady(true);
                    double_tap_playpause.setVisibility(View.GONE);
                    double_tap = false;
                } else {
                    player.setPlayWhenReady(false);
                    double_tap_playpause.setVisibility(View.VISIBLE);
                    double_tap = true;
                }
            }
            public void onSingleTouch(){
                super.onSingleTouch();
                if (singleTap) {
                    playerView.showController();
                    singleTap = false;
                } else {
                    playerView.hideController();
                    singleTap = true;
                }
                if (double_tap_playpause.getVisibility() == View.VISIBLE) {
                    double_tap_playpause.setVisibility(View.GONE);
                }
            }
        });
        horizontalIconList();
    }
     private void horizontalIconList(){
         iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
         iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
         //iconModelArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
        // iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
         iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));

        playbackIconsAdapter = new PlaybackIconsAdapter(iconModelArrayList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.HORIZONTAL,true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(playbackIconsAdapter);
        playbackIconsAdapter.notifyDataSetChanged();
        playbackIconsAdapter.setOnItemClickListener(new PlaybackIconsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
               if (position == 0){
                   if (expand){
                       iconModelArrayList.clear();
                       iconModelArrayList.add(new IconModel(R.drawable.ic_right,""));
                       iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
                     // iconModelArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
                      // iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                       iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
                       playbackIconsAdapter.notifyDataSetChanged();
                       expand = false;
                   } else {
                       if (iconModelArrayList.size() == 3){
                           iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                           iconModelArrayList.add(new IconModel(R.drawable.ic_volume, "Volume"));
                           iconModelArrayList.add(new IconModel(R.drawable.ic_brightness, "Brightness"));
                           iconModelArrayList.add(new IconModel(R.drawable.ic_speed, "Speed"));
                          // iconModelArrayList.add(new IconModel(R.drawable.ic_subtitle, "Subtitle"));
                       }
                       iconModelArrayList.set(position, new IconModel(R.drawable.ic_left,""));
                       playbackIconsAdapter.notifyDataSetChanged();
                       expand = true;
                   }
               }
               if (position == 1){
                   if (dark){
                       nightMode.setVisibility(View.GONE);
                       iconModelArrayList.set(position, new IconModel(R.drawable.ic_night_mode, "Night"));
                       playbackIconsAdapter.notifyDataSetChanged();
                       dark = false;
                   } else {
                       nightMode.setVisibility(View.VISIBLE);
                       iconModelArrayList.set(position, new IconModel(R.drawable.ic_night_mode, "Day"));
                       playbackIconsAdapter.notifyDataSetChanged();
                       dark = true;
                   }
               }

               if (position == 2){
                   if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                       playbackIconsAdapter.notifyDataSetChanged();
                   } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                       playbackIconsAdapter.notifyDataSetChanged();
                       
                   }
               }
               if (position ==3 ){
                   if (mute){
                       player.setVolume(100);
                       iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume_off, "Mute"));
                       playbackIconsAdapter.notifyDataSetChanged();
                       mute = false;
                   } else {
                       player.setVolume(0);
                       iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume, "unMute"));
                       playbackIconsAdapter.notifyDataSetChanged();
                       mute = true;
                   }
               }
               if (position == 4){
                   //volume
                   VolumeDialog volumeDialog = new VolumeDialog();
                   volumeDialog.show(getSupportFragmentManager(), "dialog");
                   playbackIconsAdapter.notifyDataSetChanged();
               }
               if (position == 5){
                   BrightnessDialog brightnessDialog = new BrightnessDialog();
                   brightnessDialog.show(getSupportFragmentManager(), "dialog");
                   playbackIconsAdapter.notifyDataSetChanged();
               }

               if(position == 6){
                   AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                   alertDialog.setTitle("Select PLayback Speed").setPositiveButton("OK", null);
                   String[] items = {"0.5x", "1x Normal Speed", "1.25x", "1.5x", "2x"};
                   int checkedItem = -1;
                   alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           switch (which) {
                               case 0:
                                   speed = 0.5f;
                                   parameters = new PlaybackParameters(speed);
                                   player.setPlaybackParameters(parameters);
                                   break;
                               case 1:
                                   speed = 1f;
                                   parameters = new PlaybackParameters(speed);
                                   player.setPlaybackParameters(parameters);
                                   break;
                               case 2:
                                   speed = 1.25f;
                                   parameters = new PlaybackParameters(speed);
                                   player.setPlaybackParameters(parameters);
                                   break;
                               case 3:
                                   speed = 1.5f;
                                   parameters = new PlaybackParameters(speed);
                                   player.setPlaybackParameters(parameters);
                                   break;
                               case 4:
                                   speed = 2f;
                                   parameters = new PlaybackParameters(speed);
                                   player.setPlaybackParameters(parameters);
                                   break;
                               default:
                                   break;
                           }
                       }
                   });
                   AlertDialog alert = alertDialog.create();
                   alert.show();
               }
               if (position ==7){

                   System.out.println("video player");

               }

            }
        });

     }




    private void initViews() {
        scaling = findViewById(R.id.scaling);
        videoMore = findViewById(R.id.video_more);
        title = findViewById(R.id.video_title);
        nextButton = findViewById(R.id.Exo_next);
        previousButton = findViewById(R.id.exo_prev);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        root = findViewById(R.id.root_layout);
        videoBack = findViewById(R.id.video_back);
        videoList = findViewById(R.id.video_list);
        brt_progress = findViewById(R.id.brt_progress);
        vol_progress = findViewById(R.id.vol_progress);
        vol_progress_container = findViewById(R.id.vol_progress_container);
        brt_progress_container = findViewById(R.id.brt_progress_container);
        vol_icon = findViewById(R.id.vol_icon);
        brt_icon = findViewById(R.id.brt_icon);
        vol_text = findViewById(R.id.vol_text);
        brt_text = findViewById(R.id.brt_text);
        vol_text_container = findViewById(R.id.vol_text_container);
        brt_text_container = findViewById(R.id.brt_text_container);
        double_tap_playpause = findViewById(R.id.double_tap_play_pause);
        zoomLayout = findViewById(R.id.zoom_layout);
        zoomContainer = findViewById(R.id.zoom_container);
        zoom_perc = findViewById(R.id.zoom_percentage);
        recyclerViewIcons = findViewById(R.id.recyclerview_icon);
        nightMode = findViewById(R.id.night_mode);
        scaleGestureDetector = new ScaleGestureDetector(this, new  ScaleDetector());
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        scaling.setOnClickListener(firstListener);
       title.setText(videoTitle);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        videoList.setOnClickListener(this);
       videoMore.setOnClickListener(this);
    }

    private  void playVideo(){
        String path = mVideoFiles.get(position).getPath();
        Uri uri  = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        String userAgent = createUserAgent();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem);
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(mediaSource);

        // player.seekTo(position, C.TIME_UNSET);
        long duration = player.getDuration();
        if (duration != C.TIME_UNSET && position >= 0 && position < duration) {
            player.seekTo(position);
        } else {
            Log.e(TAG, "Invalid seek position: " + position);
        }
        player.play();
        playError();
    }

    private void playVideoSubtitle(Uri subtitle){
        long oldPosition = player.getContentPosition();
        player.stop();

       String path = mVideoFiles.get(position).getPath();
       Uri uri  = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this,"app")
        );
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i< mVideoFiles.size(); i++){
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            Format textFormat =Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,Format.NO_VALUE, "app");
            MediaSource subtileSource = new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true)
                    .createMediaSource(Uri.parse(String.valueOf(subtitle)), textFormat, C.TIME_UNSET);
            MergingMediaSource mergingMediaSource = new MergingMediaSource(mediaSource, subtileSource);
            concatenatingMediaSource.addMediaSource(mergingMediaSource);

        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, oldPosition);
        playError();
    }



    private String createUserAgent(){
        String appName = "VideoPlayerApp";
        String appVersion = "1.0";
        String deviceInfo = "Andriod";

        return appName + "/" + appVersion + " (" + deviceInfo + ")";
}



 private void screenOrientation(){
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = mVideoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this,uri);
            bitmap = retriever.getFrameAtTime();
            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();
            if (videoWidth >  videoHeight) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        } catch (Exception e) {
            Log.e("MediaMetaDataRetriever", "screenOrientation: ");
        }
 }

  private  void playError() {
        player.addListener(new Player.EventListener() {
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
      player.setPlayWhenReady(true);
  }


    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onPause(){
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        if (isInPictureInPictureMode()){
            player.setPlayWhenReady(true);
        }else {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    protected void onResume() {

        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
  private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  public void hideBottomBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decodeView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decodeView.setSystemUiVisibility(uiOptions);
            
        }
  }

  public void onClick (View v) {
      final int id = v.getId();
      final int  VIDEO_BACK_ID = R.id.video_back;
      final int VIDEO_LIST_ID = R.id.video_list;
      final int LOCK_ID = R.id.lock;
      final int UNLOCK_ID = R.id.unlock;
      final int EXO_NEXT_ID = R.id.Exo_next;
      final int EXO_PREV_ID = R.id.exo_prev;
      final int VIDEO_MORE_ID = R.id.video_more;

      Map<Integer,Runnable> actions = new HashMap<>();
      actions.put(VIDEO_BACK_ID, this::handleVideoBack);
      actions.put(VIDEO_LIST_ID,this::showPlaylistDialog );
      actions.put(LOCK_ID, () -> setControlsMode(ControlsMode.SCREEN) );
      actions.put(UNLOCK_ID, () -> setControlsMode(ControlsMode.FULL_LOCK));
      actions.put(EXO_NEXT_ID, this::playNextVideo);
      actions.put(EXO_PREV_ID, this::playPreviousVideo);
      actions.put(VIDEO_MORE_ID, () -> showPopupMenu(v));

      if (actions.containsKey(id)) {
          actions.get(id).run();
      } else {
          System.out.println("Invalid ID: " + id);
      }
  }
  private void handleVideoBack(){
        if (player != null) {
            player.release();
        }
        finish();
  }
  private void showPlaylistDialog(){
      PlaylistDialog playlistDialog = new PlaylistDialog(mVideoFiles, videoFilesAdapter);
      playlistDialog.show(getSupportFragmentManager(), playlistDialog.getTag());
  }

  private  void playNextVideo () {
        try {
            player.stop();
            position++;
            playVideo();
            title.setText(mVideoFiles.get(position).getDisplayName());
        } catch (Exception e) {
            Toast.makeText(this, "no Next Video", Toast.LENGTH_SHORT).show();
            finish();
        }
  }

  private void playPreviousVideo(){
        try{
            player.stop();
            position--;
            playVideo();
            title.setText(mVideoFiles.get(position).getDisplayName());
        } catch (Exception e) {
            Toast.makeText(this, "no Previous Video", Toast.LENGTH_SHORT).show();
            finish();
        }
  }

  private  void setControlsMode(ControlsMode mode) {
        controlsMode = mode;
        if (mode == ControlsMode.SCREEN) {
            root.setVisibility(View.VISIBLE);
            lock.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();
        } else if (mode == ControlsMode.FULL_LOCK) {
            root.setVisibility(View.INVISIBLE);
            lock.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
        }
  }
  private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(VideoPlayerActivity.this,view);
        popupMenu.inflate(R.menu.player_menu);
        popupMenu.setOnMenuItemClickListener(item-> handlePopupMenuItem(item));
        popupMenu.show();
  }
  private boolean handlePopupMenuItem(MenuItem item) {
        int id = item.getItemId();
        if (id== R.id.next) {
            nextButton.performClick();
        } else if (id== R.id.send) {
            Uri uri = Uri.parse(mVideoFiles.get(position).getPath());
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("video/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "Share Video via"));
            
        } else if (id==R.id.properties) {
            double milliSeconds = Double.parseDouble(mVideoFiles.get(position).getDuration());
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(VideoPlayerActivity.this);
            alertDialog.setTitle("Properties");

            String one = "File: " + mVideoFiles.get(position).getDisplayName();

            String path = mVideoFiles.get(position).getPath();
            int indexOfPath = path.lastIndexOf("/");
            String two = "Path: " + path.substring(0, indexOfPath);

            String three = "Size: " + android.text.format.Formatter
                    .formatFileSize(VideoPlayerActivity.this, Long.parseLong(mVideoFiles.get(position).getSize()));

            String four = "Length: " + Utility.timeConversion((long) milliSeconds);

            String namewithFormat = mVideoFiles.get(position).getDisplayName();
            int index = namewithFormat.lastIndexOf(".");
            String format = namewithFormat.substring(index + 1);
            String five = "Format: " + format;

            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(mVideoFiles.get(position).getPath());
            String height = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String six = "Resolution: " + width + "x" + height;

            alertDialog.setMessage(one + "\n\n" + two + "\n\n" + three + "\n\n" + four +
                    "\n\n" + five + "\n\n" + six);
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        } else if (id== R.id.delete) {
            android.app.AlertDialog.Builder alertDialogDelete = new android.app.AlertDialog.Builder(VideoPlayerActivity.this);
            alertDialogDelete.setTitle("Delete");
            alertDialogDelete.setMessage("Do you want to delete this video");
            alertDialogDelete.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri contentUri = ContentUris
                            .withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    Long.parseLong(mVideoFiles.get(position).getId()));
                    File file = new File(mVideoFiles.get(position).getPath());
                    boolean delete = file.delete();
                    if (delete) {
                        getContentResolver().delete(contentUri, null, null);
                        mVideoFiles.remove(position);
                        nextButton.performClick();
                        Toast.makeText(VideoPlayerActivity.this, "Video Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VideoPlayerActivity.this, "can't deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            alertDialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogDelete.show();
        } else {
            System.out.println("Invalid Id");
        }

      return true;
  }

  View.OnClickListener firstListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
          player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
          scaling.setImageResource(R.drawable.fullscreen);

          Toast.makeText(VideoPlayerActivity.this,"Full Screen", Toast.LENGTH_SHORT).show();
          scaling.setOnClickListener(secondListener);
      }
  };
    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);

            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };
    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);

            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };

    public void fit(){
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
        scaling.setOnClickListener(secondListener);
    }



  private class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        public boolean onScale(ScaleGestureDetector detector) {
            scale_factor *= detector.getScaleFactor();
            scale_factor = Math.max(0.5f, Math.min(scale_factor, 6.0f));

            zoomLayout.setScaleX(scale_factor);
            zoomLayout.setScaleY(scale_factor);
            int percentage = (int) (scale_factor * 100);
            zoom_perc.setText(" " + percentage + "%");
            zoomContainer.setVisibility(View.VISIBLE);
            brt_text_container.setVisibility(View.GONE);
            vol_text_container.setVisibility(View.GONE);
            brt_progress_container.setVisibility(View.GONE);
            vol_progress_container.setVisibility(View.GONE);
            recyclerViewIcons = findViewById(R.id.recyclerview_icon);
            return true;
        }
        public void  onScaleEnd(ScaleGestureDetector detector) {
            zoomContainer.setVisibility(View.GONE);
            super.onScaleEnd(detector);
        }
  }

}