package com.example.videoplayactivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class VolumeDialog extends AppCompatDialogFragment {

    private ImageView cross;
    private SeekBar seekBar;
    TextView vol_no;
    AudioManager audioManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.vol_dialog_item,null);
        builder.setView(view);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        cross = view.findViewById(R.id.vol_close);
        vol_no = view.findViewById(R.id.vol_number);
        seekBar = view.findViewById(R.id.seekBarId);

        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
                int mediavolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxvol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                double volPer = Math.ceil((((double) mediavolume / (double) maxvol) * (double) 100));
                vol_no.setText("" +volPer);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

            }
        });

        return builder.create();
    }
}
