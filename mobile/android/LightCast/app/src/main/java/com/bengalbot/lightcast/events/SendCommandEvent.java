package com.bengalbot.lightcast.events;

import com.bengalbot.lightcast.command.Command;

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
