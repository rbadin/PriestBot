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

import java.util.*;


public class Gamble {
	private ArrayList<String> gamblers;
	private boolean enabled = false;
	private int reward;
	private int initialp;

	private Timer gaTimer;

	public Gamble(int prize) {
		reward = prize;
		initialp = prize;
		gamblers = new ArrayList<String>();
	}

	public Timer getTimer() {
        return gaTimer;
    }

    public void setReward(int prize) {
        reward = prize;
        initialp = prize;
    }

    public void setTimer(Timer t) {
        gaTimer = t;
    }

    public int getInitial() {
    	return initialp;
    }

    public void enterCreator(String name) {
        if (enabled) {
            if (!checkIfEntered(name)) {
                gamblers.add(name.toLowerCase());
                System.out.println("DEBUG: Entry accepted for creator " + name.toLowerCase() + ". Total reward now: " + reward);
           } else {
                System.out.println("DEBUG: Entry rejected. " + name.toLowerCase() + " is already entered.");
            }
        } else {
            System.out.println("DEBUG: Entry rejected. Gamble not running.");
        }
    }

	public void enter(String name) {
        if (enabled) {
            if (!checkIfEntered(name)) {
                gamblers.add(name.toLowerCase());
                reward = reward + initialp;
                System.out.println("DEBUG: Entry accepted for " + name.toLowerCase() + ". Total reward now: " + reward);
            } else {
                System.out.println("DEBUG: Entry rejected. " + name.toLowerCase() + " is already entered.");
            }
        } else {
            System.out.println("DEBUG: Entry rejected. Gamble not running.");
        }
    }

    public void setEnabled(boolean option) {
    	enabled = option;
    }

    public boolean getStatus() {
    	return enabled;
    }

    public void resetPrize() {
        reward = 0;
    }

    public String pickWinner() {
        if (gamblers.size() < 1)
            return "No users entered";

        Random generator = new Random();
        int randomIndex = generator.nextInt(gamblers.size());

        return gamblers.get(randomIndex);
    }

    public void reset() {
        gamblers.clear();
    }

    public int count() {
        return gamblers.size();
    }

    public int getPrize() {
    	return reward;
    }

    private boolean checkIfEntered(String name) {
        for (String entry : gamblers) {
            if (entry.equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }


}