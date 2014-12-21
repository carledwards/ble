package com.bengalbot.android.events;

import com.bengalbot.android.command.Command;

/**
 * Created by jmendez on 12/20/14.
 */
public class SendCommandEvent {

    public Command command;


    //Charset.forName("UTF-8")
    public SendCommandEvent(Command command) {
        this.command = command;
    }

}
