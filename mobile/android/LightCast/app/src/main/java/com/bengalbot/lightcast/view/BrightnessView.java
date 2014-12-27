package com.bengalbot.lightcast.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bengalbot.lightcast.R;
import com.bengalbot.lightcast.command.Brightness;
import com.bengalbot.lightcast.command.Command;
import com.bengalbot.lightcast.command.Properties;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class BrightnessView extends CardView  implements View.OnClickListener, CommandView {

    private PresetViewActions mPresetViewctionsListener;
    private SeekBar mSeekBar;
    private TextView mBrightnessTextView;

    private Map<Properties, Object> properties;

    public BrightnessView(Context context, PresetViewActions presetViewctionsListener) {
        super(context);
        this.mPresetViewctionsListener = presetViewctionsListener;
        initView();

    }

    public BrightnessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BrightnessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        properties = new HashMap<Properties, Object>();

        View v = inflater.inflate(R.layout.brightness_view, null);
        v.findViewById(R.id.button_close).setOnClickListener(this);
        mBrightnessTextView = (TextView)v.findViewById(R.id.brightness_label);

        mSeekBar = (SeekBar)v.findViewById(R.id.brightness_seek);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mBrightnessTextView.setText(getContext().getString(R.string.brightness_value, Integer.toString(i)) + "%");
                properties.put(Properties.PERCENTAGE, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBrightnessTextView.setText(getContext().getString(R.string.brightness_value, Integer.toString(mSeekBar.getProgress())) + "%");

        addView(v);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_close) {
            mPresetViewctionsListener.onCloseView(this);
        }
    }

    @Override
    public Command getCommand() {

        Command command = new Brightness();
        command.create(properties);
        return command;
    }
}
