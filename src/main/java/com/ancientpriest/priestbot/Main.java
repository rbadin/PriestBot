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

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Main {

    public static void main(String[] args) throws NickAlreadyInUseException, IOException, IrcException {
        String propertiesFile = "global.properties";
        if (args.length > 0) {
            propertiesFile = args[0];
        }

        BotManager bm = new BotManager(propertiesFile);
    }
    public static String getTimeTilNow(Date date) {
        long difference = (long) (System.currentTimeMillis() / 1000) - (date.getTime() / 1000);
        String returnString = "";
        if (difference >= 31536000) {
            int years = (int) (difference / 31536000);
            returnString += years + "y ";
            difference -= years * 31536000;
        }
        if (difference >= 86400) {
            int days = (int) (difference / 86400);
            returnString += days + "d ";
            difference -= days * 86400;
        }
        if (difference >= 3600) {
            int hours = (int) (difference / 3600);
            returnString += hours + "h ";
            difference -= hours * 3600;
        }
        if (difference >= 60) {
            int minutes = (int) (difference / 60);
            returnString += minutes + "m ";
            difference -= minutes * 60;
        }

        int seconds = (int) difference;
        returnString += seconds + "s";


        System.out.println(returnString);
        return returnString;
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }

    public static List<String> splitEqually(String text, int size) {
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos) + replacement + string.substring(pos + toReplace.length(), string.length());
        } else {
            return string;
        }
    }

}
