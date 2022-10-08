package com.example.videoplayactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media.AudioManagerCompat;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import java.text.BreakIterator;

public class WatchActivity extends AppCompatActivity {

    ExoPlayer exoPlayer;
    PlayerView playerView;
    ImageView bt_fullscreen, bt_lockscreen;
    boolean isFullScreen = false;
    boolean isLock = false;
    Handler handler;
    TextView title;
    ImageView nightMode;
    boolean dark = false;
    AudioManager audioManager;
    ImageView nextButton;
    ImageView previousButton;
    ImageView rePlay;
    ImageView menuBar;
    ImageView backButton;
    int brightness;
    ImageView volume;
    RelativeLayout root;
    SeekBar seekBar;
    //swipe and zoom variable
    private int device_height, device_width, media_vol;
    boolean start = false;
    boolean left, right;
    private float baseX, baseY;
    boolean swipe_move = false;
    private long diffX, diffY;
    public static final int MINIMUM_DISTANCE = 100;
    boolean success = false;
    ProgressBar vol_progress, brt_progress;
    LinearLayout vol_progress_container, vol_text_container, brt_progress_container, brt_text_container;
    ImageView vol_icons, brt_icon;
    ProgressBar progressBar;
    private ContentResolver contentResolver;
    private Window window;
    TextView vol_text, brt_text;
    boolean singleTap = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        handler = new Handler(Looper.getMainLooper());


        initViews();
        playVideo();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels;
        device_height = displayMetrics.heightPixels;

        playerView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        playerView.showController();
                        start = true;
                        if (motionEvent.getX() < (device_width / 2)) {
                            left = true;
                            right = false;
                        } else if (motionEvent.getX() > (device_width / 2)) {
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
                            if (Math.abs(diffY) > Math.abs(diffX)) {
                                boolean value;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    value = Settings.System.canWrite(getApplicationContext());
                                    if (value) {
                                        if (left) {
                                            //Toast.makeText(getApplicationContext(), "left swipe", Toast.LENGTH_SHORT).show();
                                            contentResolver = getContentResolver();
                                            window = getWindow();

                                            try{

                                                android.provider.Settings.System.putInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                                                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                                brightness = android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS);
                                            } catch (android.provider.Settings.SettingNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                            int new_brightness = (int) (brightness - ((diffY * brightnessSpeed)));
                                            if (new_brightness > 250){
                                                new_brightness = 250;
                                            }else if (new_brightness < 1) {
                                                new_brightness = 1;
                                            }

                                            double brt_percentage = Math.ceil((((double) new_brightness / (double) 250) * (double) 100));
                                            brt_progress_container.setVisibility(View.VISIBLE);
                                            brt_text_container.setVisibility(View.VISIBLE);
                                            brt_progress.setProgress((int) brt_percentage);

                                            if(brt_percentage < 30) {
                                                brt_icon.setImageResource(R.drawable.ic_baseline_brightness_low_24);

                                            }else if (brt_percentage > 30 && brt_percentage <80 ){
                                                brt_icon.setImageResource(R.drawable.ic_baseline_brightness_medium_24);

                                            }else if (brt_percentage > 80) {
                                                brt_icon.setImageResource(R.drawable.ic_baseline_brightness_5_24);
                                            }
                                            brt_text.setText(" " +(int) brt_percentage + "%");
                                            android.provider.Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, (new_brightness));
                                            WindowManager.LayoutParams layoutParams = window.getAttributes();
                                            layoutParams.screenBrightness = brightness/(float) 255;
                                            window.setAttributes(layoutParams);


                                        } else if (right) {
                                            //Toast.makeText(getApplicationContext(), "right swipe", Toast.LENGTH_SHORT).show();

                                            vol_text_container.setVisibility(View.VISIBLE);
                                            media_vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                            double cal = (double) diffY * ((double) maxVol / ((double) (device_height*2) - brightnessSpeed));
                                            int newMediaVolume = media_vol- (int) cal;
                                            if(newMediaVolume >maxVol) {
                                                newMediaVolume = maxVol;

                                            }else if (newMediaVolume <1 ) {
                                                newMediaVolume = 0;
                                            }
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                            double volPer = Math.ceil((((double) newMediaVolume / (double) maxVol) * (double) 100));
                                            vol_text.setText(" " +(int) volPer+ "/");
                                            if (volPer < 1) {
                                                vol_icons.setImageResource(R.drawable.ic_baseline_volume_off_24);
                                                vol_text.setVisibility(View.VISIBLE);
                                                vol_text.setText("Off");

                                            }else if ( volPer>=1 ) {
                                                vol_icons.setImageResource(R.drawable.ic_baseline_volume);
                                                vol_text.setVisibility(View.VISIBLE);
                                            }
                                            vol_progress_container.setVisibility(View.VISIBLE);
                                            vol_progress.setProgress((int) volPer);

                                        }
                                        success = true;

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Allow write settings for swipe", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
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


                return super.onTouch(view, motionEvent);
            }

            @Override
            public void onDoubleTouch() {
                super.onDoubleTouch();
            }

            @Override
            public void onSingleTouch() {

                super.onSingleTouch();
                if(singleTap){
                    playerView.showController();
                    singleTap = false;

                }else {
                    playerView.hideController();
                    singleTap = true;
                }
            }
        });

        bt_fullscreen.setOnClickListener(view ->
        {
            if (!isFullScreen) {
                bt_fullscreen.setImageDrawable(
                        ContextCompat
                                .getDrawable(getApplicationContext(), R.drawable.ic_baseline_fullscreen_exit_24));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                bt_fullscreen.setImageDrawable(ContextCompat
                        .getDrawable(getApplicationContext(), R.drawable.ic_baseline_fullscreen_24));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            isFullScreen = !isFullScreen;

        });
        bt_lockscreen.setOnClickListener(view ->
        {
            //change icon base on toggle lock screen or unlock screen
            if (!isLock) {
                bt_lockscreen.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_lock_24));
            } else {
                bt_lockscreen.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_lock_open_24));
            }
            isLock = !isLock;
            //method for toggle will do next
            lockScreen(isLock);

        });
        //instance the player with skip back duration 5 second or forward 5 second
        //5000 millisecond = 5 second
        exoPlayer = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(5000)
                .build();
        playerView.setPlayer(exoPlayer);
        //screen alway active
        playerView.setKeepScreenOn(true);
        //track state
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                //when data video fetch stream from internet
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);

                } else if (playbackState == Player.STATE_READY) {
                    //then if streamed is loaded we hide the progress bar
                    progressBar.setVisibility(View.GONE);
                }

                if (!exoPlayer.getPlayWhenReady()) {
                    handler.removeCallbacks(updateProgressAction);
                } else {
                    onProgress();
                }
            }
        });
        //pass the video link and play
        Uri videoUrl = Uri.parse("https://www.rmp-streaming.com/media/big-buck-bunny-360p.mp4");
        MediaItem media = MediaItem.fromUri(videoUrl);
        exoPlayer.setMediaItem(media);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private void playVideo() {
    }

    private void initViews() {


        bt_fullscreen = findViewById(R.id.scaling);
        bt_lockscreen = findViewById(R.id.lock);
        ImageView bt_lockscreen = findViewById(R.id.lock);
        title = findViewById(androidx.core.R.id.title);
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        rePlay = findViewById(R.id.exo_replay);
        seekBar = findViewById(R.id.seekBarId);
        root = findViewById(R.id.root_layout);
        vol_text_container = findViewById(R.id.vol_text_container);
        brt_text_container = findViewById(R.id.vol_text_container);
        vol_progress_container = findViewById(R.id.vol_progress_container);
        brt_progress_container = findViewById(R.id.brt_progress_container);
        vol_icons = findViewById(R.id.vol_icon);
        brt_icon = findViewById(R.id.brt_icon);
        vol_text = findViewById(R.id.vol_text);
        brt_text = findViewById(R.id.brt_text);
        playerView = findViewById(R.id.player);
        vol_progress = findViewById(R.id.vol_progress);
        brt_progress = findViewById(R.id.brt_progress);
        progressBar =  findViewById(R.id.progressbarId);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


    }

    private Runnable updateProgressAction = () -> onProgress();

    //at 4 second
    long ad = 4000;
    boolean check = false;

    private void onProgress() {
        ExoPlayer player = exoPlayer;
        long position = player == null ? 0 : player.getCurrentPosition();
        handler.removeCallbacks(updateProgressAction);
        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                delayMs = 1000 - position % 1000;
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }

            //check to display ad
            if ((ad - 3000 <= position && position <= ad) && !check) {
                check = true;
                initAd();
            }
            handler.postDelayed(updateProgressAction, delayMs);
        }
    }

    RewardedInterstitialAd rewardedInterstitialAd = null;

    private void initAd() {
        if (rewardedInterstitialAd != null) return;
        MobileAds.initialize(this);
        RewardedInterstitialAd.load(this, "ca-app-pub-3940256099942544/5354046379",
                new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedInterstitialAd p0) {
                        rewardedInterstitialAd = p0;
                        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                Log.d("WatchActivity_AD", adError.getMessage());
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                handler.removeCallbacks(updateProgressAction);
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                //resume play
                                exoPlayer.setPlayWhenReady(true);
                                rewardedInterstitialAd = null;
                                check = false;
                            }
                        });
                        LinearLayout sec_ad_countdown = findViewById(R.id.sect_ad_countdown);
                        TextView tx_ad_countdown = findViewById(R.id.tx_ad_countdown);
                        sec_ad_countdown.setVisibility(View.VISIBLE);
                        new CountDownTimer(4000, 1000) {
                            @Override
                            public void onTick(long l) {
                                tx_ad_countdown.setText("Ad in " + l / 1000);
                            }

                            @Override
                            public void onFinish() {
                                sec_ad_countdown.setVisibility(View.GONE);
                                rewardedInterstitialAd.show(WatchActivity.this, rewardItem ->
                                {

                                });
                            }
                        }.start();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedInterstitialAd = null;
                    }
                });
    }

    void lockScreen(boolean lock) {
        //just hide the control for lock screen and vise versa
        LinearLayout sec_mid = findViewById(R.id.toolbar);
        LinearLayout sec_bottom = findViewById(R.id.bottom_icons);
        if (lock) {
            sec_mid.setVisibility(View.INVISIBLE);
            sec_bottom.setVisibility(View.INVISIBLE);
        } else {
            sec_mid.setVisibility(View.VISIBLE);
            sec_bottom.setVisibility(View.VISIBLE);
        }
    }

    //when is in lock screen we not accept for backpress button
    @Override
    public void onBackPressed() {
        //on lock screen back press button not work
        if (isLock) return;

        //if user is in landscape mode we turn to portriat mode first then we can exit the app.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bt_fullscreen.performClick();
        } else super.onBackPressed();
    }

    // pause or release the player prevent memory leak
    @Override
    protected void onStop() {
        super.onStop();
        exoPlayer.release();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.pause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111){
           boolean value;
           if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.M ){
               value = Settings.System.canWrite(getApplicationContext());
               if(value){
                   success = true;
               }else {
                   Toast.makeText(getApplicationContext(), "Not Granted", Toast.LENGTH_SHORT).show();
               }
        }

        }
    }
}





