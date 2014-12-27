package com.bengalbot.lightcast.command;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by jmendez on 12/20/14.
 */
public class Brightness implements Command {

    Map<Properties, Object> properties;

    @Override
    public void create(Map<Properties, Object> properties) {
        this.properties = properties;
    }


    @Override
    public byte[] parse() {
        int percentage = 100; //default Value;

        if (properties.containsKey(Properties.PERCENTAGE)) {
            percentage = (Integer)properties.get(Properties.PERCENTAGE);
        }

        byte[] bytes = ByteBuffer.allocate(3)
                .put(BRIGHTNESS_PROGRAMMING_COMMAND_CODE)
                .putShort((short) percentage).array();

        return CommandUtils.prepareForFrames(bytes);
    }

    @Override
    public void send() {

    }

    @Override
    public String getName() {
        return "Brightness";
    }
}
