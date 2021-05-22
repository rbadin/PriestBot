/*
 * Copyright 2014 Lucas Braga
 * This file is part of PriestBot.
 * 
 * PriestBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PriestBot is distributed in the hope that it will be useful
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PriestBot.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ancientpriest.priestbot;

import it.sauronsoftware.cron4j.Scheduler;

public class ScheduledCommand {
    String key;
    String pattern;
    Scheduler s;
    long lastMessageCount;
    int messageDifference;
    boolean active;

    public ScheduledCommand(String _channel, String _key, String _pattern, int _messageDifference, boolean _active) {
        System.out.println("KEY: " + _key);
        key = _key;
        pattern = _pattern; //In seconds
        lastMessageCount = 0;
        messageDifference = _messageDifference;
        active = _active;

        s = new Scheduler();
        System.out.println("Scheduling " + key + " on " + pattern);
        s.schedule(pattern, new ScheduledCommandTask(_channel, key, messageDifference));
        if (active)
            s.start();
    }

    public void setStatus(boolean status) {
        if (status == active)
            return;
        else if (status == true) {
            s.start();
            active = true;
        } else if (status == false) {
            System.out.println("Stopping timer");
            s.stop();
            active = false;
        }
    }

    private class ScheduledCommandTask implements Runnable {
        private String key;
        private String channel;
        private int messageDifference;

        public ScheduledCommandTask(String _channel, String _key, int _messageDifference) {
            key = _key;
            channel = _channel;
            messageDifference = _messageDifference;
        }

        public void run() {
            Channel channelInfo = BotManager.getInstance().getChannel(channel);
            if (channelInfo.messageCount - ScheduledCommand.this.lastMessageCount >= messageDifference) {
                String command = channelInfo.getCommand(key);

                if (command != null)
                    ReceiverBot.getInstance().send(channel, command);

                if (key.equalsIgnoreCase("commercial"))
                    channelInfo.runCommercial();
            } else {
                //System.out.println("DEBUG: No messages received since last send - " + key);
            }

            lastMessageCount = channelInfo.messageCount;
        }
    }
}
