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

import java.util.Timer;
import java.util.TimerTask;

public class AutoHost {
	private Timer ahtimer;
    private boolean active;
    private String lastHosted;

    public AutoHost() {
    	active = false;
    }

    public Timer getTimer() {
        return ahtimer;
    }

    public void setLastHosted(String lh) {
    	lastHosted = lh;
    }

    public String getLastHosted() {
    	return lastHosted;
    }

     public void setTimer(Timer t) {
        ahtimer = t;
    }

    public void setStatus(boolean status) {
		active = status;
    }

    public boolean getStatus() {
    	return active;
    }

}
