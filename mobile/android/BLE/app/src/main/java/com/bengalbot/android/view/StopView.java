package com.bengalbot.android.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bengalbot.android.R;
import com.bengalbot.android.command.Command;
import com.bengalbot.android.command.EndProgramming;
import com.bengalbot.android.command.Properties;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class StopView extends CardView  implements View.OnClickListener, CommandView {

    private PresetViewActions mPresetViewctionsListener;
    private TextView mStartTextView;
    private Map<Properties, Object> properties;

    public StopView(Context context, PresetViewActions presetViewctionsListener) {
        super(context);
        this.mPresetViewctionsListener = presetViewctionsListener;
        initView();

    }

    public StopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public StopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.stop_view, null);

        v.findViewById(R.id.button_close).setOnClickListener(this);
        mStartTextView = (TextView)v.findViewById(R.id.stop_label);

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

        Command command = new EndProgramming();
        command.create(properties);
        return command;
    }
}