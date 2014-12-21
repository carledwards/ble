package com.bengalbot.android.command;

import java.util.Map;

/**
 * Created by jmendez on 12/20/14.
 */
public class EndProgramming implements com.bengalbot.android.command.Command {
    Map<Properties, Object> properties;

    @Override
    public void create(Map<Properties, Object> properties) {
        this.properties = properties;
    }

    @Override
    public byte[] parse() {
        return new byte[] {(byte)END_PROGRAMMING_COMMAND_CODE};
    }

    @Override
    public void send() {

    }


    @Override
    public String getName() {
        return "EndProgramming";
    }
}