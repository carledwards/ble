package com.bengalbot.android.command;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by jmendez on 12/20/14.
 */
public class Delay implements Command {
    Map<Properties, Object> properties;

    @Override
    public void create(Map<Properties, Object> properties) {
        this.properties = properties;
    }

    @Override
    public byte[] parse() {

        int duration = 2000; //default value

        if (properties.containsKey(Properties.MILLISECONDS)) {
            duration = (Integer)properties.get(Properties.MILLISECONDS);
        }
        return ByteBuffer.allocate(5).put((byte)DELAY_COMMAND_CODE).putInt((byte)duration).array();
    }

    @Override
    public void send() {

    }


    @Override
    public String getName() {
        return "Delay";
    }
}
