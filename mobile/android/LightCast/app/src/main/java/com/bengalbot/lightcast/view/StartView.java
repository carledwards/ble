package com.bengalbot.lightcast.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bengalbot.lightcast.R;
import com.bengalbot.lightcast.command.Command;
import com.bengalbot.lightcast.command.Properties;
import com.bengalbot.lightcast.command.StartProgramming;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class StartView extends CardView  implements View.OnClickListener, CommandView {

    private PresetViewActions mPresetViewctionsListener;
    private SeekBar mSeekBar;
    private TextView mStartTextView;
    private Map<Properties, Object> properties;

    public StartView(Context context, PresetViewActions presetViewctionsListener) {
        super(context);
        this.mPresetViewctionsListener = presetViewctionsListener;
        initView();

    }

    public StartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public StartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.start_view, null);

        v.findViewById(R.id.button_close).setOnClickListener(this);
        mStartTextView = (TextView)v.findViewById(R.id.start_label);
        properties = new HashMap<>();


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

        Command command = new StartProgramming();
        return command;
    }
}
