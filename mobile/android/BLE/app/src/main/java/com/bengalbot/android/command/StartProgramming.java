package com.bengalbot.android.command;

import java.util.Map;

/**
 */
public class StartProgramming implements Command {


    Map<Properties, Object> properties;

    @Override
    public void create(Map<Properties, Object> properties) {
        this.properties = properties;
    }


    @Override
    public byte[] parse() {
        return new byte[] {(byte)START_PROGRAMMING_COMMAND_CODE};
    }

    @Override
    public void send() {

    }


    @Override
    public String getName() {
        return "StartProgramming";
    }
}
