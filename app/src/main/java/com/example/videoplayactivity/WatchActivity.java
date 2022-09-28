package com.example.videoplayactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

public class WatchActivity extends AppCompatActivity {

    ExoPlayer exoPlayer;
    ImageView bt_fullscreen;
    boolean isFullScreen=false;
    boolean isLock = false;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        handler = new Handler(Looper.getMainLooper());

        PlayerView playerView = findViewById(R.id.player);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        bt_fullscreen = findViewById(R.id.bt_fullscreen);
        ImageView bt_lockscreen = findViewById(R.id.exo_lock);
        //toggle button with change icon fullscreen or exit fullscreen
        //the screen can rotate base on you angle direction sensor
        bt_fullscreen.setOnClickListener(view ->
        {
            if (!isFullScreen)
            {
                bt_fullscreen.setImageDrawable(
                        ContextCompat
                                .getDrawable(getApplicationContext(), R.drawable.ic_baseline_fullscreen_exit_24));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else
            {
                bt_fullscreen.setImageDrawable(ContextCompat
                        .getDrawable(getApplicationContext(), R.drawable.ic_baseline_fullscreen_24));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            isFullScreen = !isFullScreen;
        });
        bt_lockscreen.setOnClickListener(view ->
        {
            //change icon base on toggle lock screen or unlock screen
            if (!isLock)
            {
                bt_lockscreen.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_lock_24));
            } else
            {
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
        exoPlayer.addListener(new Player.Listener()
        {
            @Override
            public void onPlaybackStateChanged(int playbackState)
            {
                //when data video fetch stream from internet
                if (playbackState == Player.STATE_BUFFERING)
                {
                    progressBar.setVisibility(View.VISIBLE);

                } else if (playbackState == Player.STATE_READY) {
                    //then if streamed is loaded we hide the progress bar
                    progressBar.setVisibility(View.GONE);
                }

                if(!exoPlayer.getPlayWhenReady())
                {
                    handler.removeCallbacks(updateProgressAction);
                }
                else
                {
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
    private Runnable updateProgressAction = () -> onProgress();

    //at 4 second
    long ad = 4000;
    boolean check = false;
    private void onProgress()
    {
        ExoPlayer player= exoPlayer;
        long position = player == null? 0 : player.getCurrentPosition();
        handler.removeCallbacks(updateProgressAction);
        int playbackState = player ==null? Player.STATE_IDLE : player.getPlaybackState();
        if(playbackState != Player.STATE_IDLE && playbackState!= Player.STATE_ENDED)
        {
            long delayMs ;
            if(player.getPlayWhenReady() && playbackState == Player.STATE_READY)
            {
                delayMs  = 1000 - position % 1000;
                if(delayMs < 200)
                {
                    delayMs+=1000;
                }
            }
            else{
                delayMs = 1000;
            }

            //check to display ad
            if((ad-3000 <= position && position<=ad) &&!check)
            {
                check =true;
                initAd();
            }
            handler.postDelayed(updateProgressAction,delayMs);
        }
    }
    RewardedInterstitialAd rewardedInterstitialAd=null;
    private void initAd()
    {
        if(rewardedInterstitialAd!=null) return ;
        MobileAds.initialize(this);
        RewardedInterstitialAd.load(this, "ca-app-pub-3940256099942544/5354046379",
                new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback(){
                    @Override
                    public void onAdLoaded(@NonNull RewardedInterstitialAd p0)
                    {
                        rewardedInterstitialAd = p0;
                        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback()
                        {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError)
                            {
                                Log.d("WatchActivity_AD", adError.getMessage());
                            }

                            @Override
                            public void onAdShowedFullScreenContent()
                            {
                                handler.removeCallbacks(updateProgressAction);
                            }

                            @Override
                            public void onAdDismissedFullScreenContent()
                            {
                                //resume play
                                exoPlayer.setPlayWhenReady(true);
                                rewardedInterstitialAd = null;
                                check = false;
                            }
                        });
                        LinearLayout sec_ad_countdown = findViewById(R.id.sect_ad_countdown);
                        TextView tx_ad_countdown = findViewById(R.id.tx_ad_countdown);
                        sec_ad_countdown.setVisibility(View.VISIBLE);
                        new CountDownTimer(4000,1000)
                        {
                            @Override
                            public void onTick(long l)
                            {
                                tx_ad_countdown.setText("Ad in "+l/1000);
                            }

                            @Override
                            public void onFinish()
                            {
                                sec_ad_countdown.setVisibility(View.GONE);
                                rewardedInterstitialAd.show(WatchActivity.this, rewardItem ->
                                {

                                });
                            }
                        }.start();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError)
                    {
                        rewardedInterstitialAd = null;
                    }
                });
    }

    void lockScreen(boolean lock)
    {
        //just hide the control for lock screen and vise versa
        LinearLayout sec_mid = findViewById(R.id.sec_controlvid1);
        LinearLayout sec_bottom = findViewById(R.id.sec_controlvid2);
        if(lock)
        {
            sec_mid.setVisibility(View.INVISIBLE);
            sec_bottom.setVisibility(View.INVISIBLE);
        }
        else
        {
            sec_mid.setVisibility(View.VISIBLE);
            sec_bottom.setVisibility(View.VISIBLE);
        }
    }

    //when is in lock screen we not accept for backpress button
    @Override
    public void onBackPressed()
    {
        //on lock screen back press button not work
        if(isLock) return;

        //if user is in landscape mode we turn to portriat mode first then we can exit the app.
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            bt_fullscreen.performClick();
        }
        else super.onBackPressed();
    }

    // pause or release the player prevent memory leak
    @Override
    protected void onStop()
    {
        super.onStop();
        exoPlayer.stop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        exoPlayer.release();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        exoPlayer.pause();
    }

}
