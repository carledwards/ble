package com.bengalbot.android.command;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * Created by jmendez on 12/20/14.
 */
public class Animation implements Command {

    Map<Properties, Object> properties;

    @Override
    public void create(Map<Properties, Object> properties) {
        this.properties = properties;
    }

    @Override
    public byte[] parse() {

        int animationValue = 0;
        int totalOfCycles = 0;
        int cycleTimeInMillis = 2000; //default value

        if (properties.containsKey(Properties.ANIMATION_TYPE)) {
            animationValue = (Integer)properties.get(Properties.ANIMATION_TYPE);
        }

        if (properties.containsKey(Properties.TOTAL_CYCLES)) {
            totalOfCycles = (Integer)properties.get(Properties.TOTAL_CYCLES);
        }

        if (properties.containsKey(Properties.MILLISECONDS)) {
            cycleTimeInMillis = (Integer)properties.get(Properties.MILLISECONDS);
        }

        byte[] bytes = ByteBuffer.allocate(5).order(ByteOrder.BIG_ENDIAN).put(ANIMATION_PROGRAMMING_COMMAND_CODE).put((byte) animationValue)
                .putShort((short) cycleTimeInMillis)
                .put((byte) totalOfCycles).array();

       return CommandUtils.prepareForFrames(bytes);
    }

    @Override
    public void send() {

    }

    @Override
    public String getName() {
        return "Animation";
    }


}
