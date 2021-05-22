package com.ancientpriest.priestbot;

import java.util.*;

/**
 * Created by bragluca on 5/20/16.
 */
public class Bet {
    private HashMap<String, Integer> bets;
    private HashMap<String, Integer> betsW;
    private HashMap<String, Integer> betsL;
    private boolean enabled = false;
    private int totalPrize;
    private int initialP;
    private HashMap<String, Integer> percentages;
    private Timer betTimer;

    public Bet(int prize) {
        betsW = new HashMap<String, Integer>();
        betsL = new HashMap<String, Integer>();
        bets = new HashMap<String, Integer>();
        percentages = new HashMap<String, Integer>();
        totalPrize = prize;
        initialP = prize;
    }

    public Timer getBetTimer() {
        return betTimer;
    }

    public void setBetTimer(Timer t) {
        betTimer = t;
    }
    
    public void setStatus(boolean setting) {
        enabled = setting;
    }
    
    public void reset() {
        bets.clear();
        betsW.clear();
        betsL.clear();
    }
    
    public boolean getStatus() {
        return enabled;
    }

    public int getInitialP() {
        return initialP;
    }

    private boolean checkIfEntered(String name) {
        name = name.toLowerCase();

        for (String entry : bets.keySet()) {
            if (entry.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }
    /*
    public void enterCreator(String name, int amount) {
        if (enabled) {
            if (!checkIfEntered(name)) {
                bets.put(name.toLowerCase(), amount);
                System.out.println("DEBUG: Accepted entry for: " + name + " with bet: " + amount);
            } else
                System.out.println("DEBUG: Entry rejected for: " + name + " as they have already been entered");
        } else
            System.out.println("DEBUG: Entry rejected. Gamble is not enabled");
    }
    */

    public void enterWin(String name, int amount) {
        if (enabled) {
            if (!checkIfEntered(name)) {
                bets.put(name.toLowerCase(), amount);
                betsW.put(name.toLowerCase(), amount);
                totalPrize = totalPrize + initialP;
                System.out.println("DEBUG: Entry accepted for: " + name + " with amount: " + amount);
            } else {
                System.out.println("DEBUG: Entry rejected for : " + name + ". Already entered!");
            }
        } else {
            System.out.println("DEBUG: Entry rejected since bet isn' enabled");
        }
    }
    
    public void enterLose(String name, int amount) {
        if (enabled) {
            if (!checkIfEntered(name)) {
                bets.put(name.toLowerCase(), amount);
                betsL.put(name.toLowerCase(), amount);
                totalPrize = totalPrize + initialP;
                System.out.println("DEBUG: Entry accepted for: " + name + " with amount: " + amount);
            } else {
                System.out.println("DEBUG: Entry rejected for : " + name + ". Already entered!");
            }
        } else {
            System.out.println("DEBUG: Entry rejected since bet isn' enabled");
        }
    }

    private int calculatePercentage(int x, int y) {
        int ph = (int) ((x * 100.0f) / y);
        return ph;
    }

    private void populatePercentages(Map <String, Integer> toCalc) {
        for (String key : toCalc.keySet()) {
            percentages.put(key, this.calculatePercentage(toCalc.get(key), totalPrize));
        }
    }
    
    public Map<String, Integer> getWinners(String result) {
        if (bets.size() < 1)
            return null;
        if (result.equalsIgnoreCase("win")) 
            this.populatePercentages(betsW);
        else if (result.equalsIgnoreCase("lose"))
            this.populatePercentages(betsL);
        
        return percentages;
    }
    
    public int getPrize() {
        return totalPrize;
    }

}
