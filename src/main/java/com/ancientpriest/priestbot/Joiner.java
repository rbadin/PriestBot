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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Joiner implements Runnable {

    private List<String> channels;

    public Joiner(Map<String, Channel> channels) {
        this.channels = new LinkedList<String>(channels.keySet());
    }

    public void run() {
        int count = 0;
        for (String channel : channels) {
            BotManager.getInstance().log("BM: Joining channel " + channel);
            BotManager.getInstance().receiverBot.joinChannel(channel);

            count++;

            if (count > 50) {
                count = 0;
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        BotManager.getInstance().receiverBot.startJoinCheck();
    }
}
