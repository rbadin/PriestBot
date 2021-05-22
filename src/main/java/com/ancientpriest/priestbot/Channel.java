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


import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import redis.clients.jedis.Jedis;


public class Channel {
    public PropertiesFile config;
    public PointsFile point;

    private String channel;
    private String twitchname;
    private String rChannel;
    boolean staticChannel;
    private HashMap<String, String> commands = new HashMap<String, String>();
    private HashMap<String, Integer> hosts = new HashMap<String, Integer>();
    private ArrayList<String> hostsn = new ArrayList<String>();
    private ArrayList<String> hostsp = new ArrayList<String>();
    private ArrayList<String> answers = new ArrayList<String>();
    private HashMap<String, Integer> commandsRestrictions = new HashMap<String, Integer>();
    private HashMap<String, Integer> commandsCount = new HashMap<String, Integer>();
    private HashMap<String, Integer> points = new HashMap<String, Integer>();
    private HashMap<String, String> cProperties = new HashMap<>();
    HashMap<String, RepeatCommand> commandsRepeat = new HashMap<String, RepeatCommand>();
    HashMap<String, ScheduledCommand> commandsSchedule = new HashMap<String, ScheduledCommand>();
    List<Pattern> autoReplyTrigger = new ArrayList<Pattern>();
    List<String> autoReplyResponse = new ArrayList<String>();
    private boolean filterCaps;
    private int filterCapsPercent;
    private int filterCapsMinCharacters;
    private int filterCapsMinCapitals;
    private boolean filterLinks;
    private boolean filterOffensive;
    private boolean filterEmotes;
    private boolean filterSymbols;
    private boolean filterCaule;
    private int filterSymbolsPercent;
    private int filterSymbolsMin;
    private int filterEmotesMax;
    private int pointsAmount;
    private int pointsDelay;
    private boolean filterEmotesSingle;
    private boolean enableWR;
    private boolean enableQuoteGame;
    private boolean enableFollowage;
    private boolean enableSubWhisper;
    private boolean enableVideoNovo;
    private int filterMaxLength;
    private String topic;
    private String uptimeMessage;
    private String symbolsTO;
    private String linksTO;
    private String meTO;
    private String capsTO;
    private String maxTO;
    private String offensiveTO;
    private String emoteTO;
    private String emoteSingleTO;
    private String summoner;
    private String smurf1;
    private String smurf2;
    private String smurf3;
    private String smurf4;
    private String region;
    private String pointsName;
    private String subWhisperMessage;
    private String resubWhisperMessage;
    private String timeZone;
    private String youtubeChannelId;
    private int topicTime;
    private Set<String> regulars = new HashSet<String>();
    private Set<String> subscribers = new HashSet<String>();
    private Set<String> moderators = new HashSet<String>();
    Set<String> tagModerators = new HashSet<String>();
    private Set<String> owners = new HashSet<String>();
    private Set<String> admins = new HashSet<String>();
    private Set<String> quotebans = new HashSet<String>();
    private Set<String> permittedUsers = new HashSet<String>();
    private ArrayList<String> permittedDomains = new ArrayList<String>();
    public boolean useTopic = true;
    public boolean useFilters = true;
    private Poll currentPoll;
    private Giveaway currentGiveaway;
    private boolean enableThrow;
    private boolean enableRoulette;
    private boolean enable8ball;
    private boolean enableMusic;
    private boolean enableLeague;
    private boolean enablePoints;
    private boolean enableStats;
    private boolean enableHost;
    private boolean enableQuote;
    private boolean enableLove;
    private boolean quoteModsOnly;
    private boolean signKicks;
    private boolean announceJoinParts;
    private boolean enableUptime;
    private String lastfm;
    private String steamID;
    private String lastHosted = "";
    private int mode; //0: Admin/owner only; 1: Mod Only; 2: Everyone; -1 Special mode to admins to use for channel moderation
    private int bulletInt;
    Raffle raffle;
    //Jedis redis = new Jedis("newredis.mth1ur.ng.0001.usw2.cache.amazonaws.com");
    Jedis redis = new Jedis("priestbott.2ce3ey.ng.0001.usw2.cache.amazonaws.com");    
    //Jedis redis = new Jedis("127.0.0.1");
    private Gamble currentGamble;
    private Bet currentBet;
    private AutoHost currentAutoHost;
    private Points currentPoints;
    public boolean logChat;
    public long messageCount;
    public int commercialLength;
    String clickToTweetFormat;
    private boolean filterColors;
    private boolean filterMe;
    private String _commandsKey;
    private String _commandsValue;
    private Set<String> offensiveWords = new HashSet<String>();
    private List<Pattern> offensiveWordsRegex = new LinkedList<Pattern>();
    Map<String, EnumMap<FilterType, Integer>> warningCount;
    Map<String, Long> warningTime;
    private int timeoutDuration;
    private int rouletteTO;
    private boolean enableWarnings;
    Map<String, Long> commandCooldown;
    Map<String, Long> robCooldown;
    Set<WebSocket> wsSubscribers = new HashSet<WebSocket>();
    String prefix;
    String emoteSet;
    boolean subscriberRegulars;
    boolean vipRegulars;
    boolean sIgnoreEmotes;
    boolean sIgnoreSymbols;
    boolean sIgnoreLinks;
    boolean sIgnoreCaps;
    boolean sIgnoreCaule;
    boolean vIgnoreEmotes;
    boolean vIgnoreSymbols;
    boolean vIgnoreLinks;
    boolean vIgnoreCaps;
    boolean vIgnoreCaule;
    boolean skipNextCommercial = false;

    private Map<String, Object> defaults = new HashMap<String, Object>();
    private Map<String, Object> _prop = new HashMap<>();

    public Channel(String name) {
        channel = name;
        rChannel = channel.substring(1);
        config = new PropertiesFile(name + ".properties");
        Config.joinChannel(name);
        // point = new PointsFile(name + ".points");
        loadProperties(name);
        //_loadProperties(name);
        //_loadCommands(name);
        if (this.enablePoints)
            loadPoints(name);
        warningCount = new HashMap<String, EnumMap<FilterType, Integer>>();
        warningTime = new HashMap<String, Long>();
        commandCooldown = new HashMap<String, Long>();
        robCooldown = new HashMap<String, Long>();

        twitchname = channel.substring(1);
    }

    public Channel(String name, int mode) {
        this(name);
        setMode(mode);
    }

    public String getChannel() {
        return channel;
    }

    public String getTwitchName() {
        return twitchname;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix.charAt(0) + "";

        Config.setProperty(this.channel,"commandPrefix", this.prefix);
    }

    public String getEmoteSet() {
        return emoteSet;
    }

    public void setEmoteSet(String emoteSet) {
        this.emoteSet = emoteSet;

        Config.setProperty(this.channel,"emoteSet", emoteSet);
    }

    public boolean getSubscriberRegulars() {
        return subscriberRegulars;
    }
    
    public boolean getSEmotes() {
        return sIgnoreEmotes;
    }
    
    public boolean getSCaule() {
        return sIgnoreCaule;
    }
    
    public boolean getSSymbols() {
        return sIgnoreSymbols;
    }
    
    public boolean getSLinks() {
        return sIgnoreLinks;
    }
    
    public boolean getSCaps() {
        return sIgnoreCaps;
    }
    
    public boolean getVEmotes() {
        return vIgnoreEmotes;
    }
    
    public boolean getVSymbols() {
        return vIgnoreSymbols;
    }
    
    public boolean getVLinks() {
        return vIgnoreLinks;
    }
    
    public boolean getVCaps() {
        return vIgnoreCaps;
    }
    
    public boolean getVCaule() {
        return vIgnoreCaule;
    }
    
    public boolean getVipRegulars() {
        return vipRegulars;
    }

    public void setSubscriberRegulars(boolean subscriberRegulars) {
        subscribers.clear();

        this.subscriberRegulars = subscriberRegulars;
        Config.setProperty(this.channel,"subscriberRegulars", String.valueOf(subscriberRegulars));
    }
    
    public void setSCaps(boolean sCaps) {

        this.sIgnoreCaps = sCaps;
        Config.setProperty(this.channel,"sIgnoreCaps", String.valueOf(sIgnoreCaps));
    }
    
    public void setSEmotes(boolean sEmotes) {

        this.sIgnoreEmotes = sEmotes;
        Config.setProperty(this.channel,"sIgnoreEmotes", String.valueOf(sIgnoreEmotes));
    }
    
    public void setSCaule(boolean sCaule) {
        this.sIgnoreCaule = sCaule;
        Config.setProperty(this.channel, "sIgnoreCaule", String.valueOf(sIgnoreCaule));
    }
    
    public void setSSymbols(boolean sSymbols) {

        this.sIgnoreSymbols = sSymbols;
        Config.setProperty(this.channel,"sIgnoreSymbols", String.valueOf(sIgnoreSymbols));
    }
    
    public void setSLinks(boolean sLinks) {

        this.sIgnoreLinks = sLinks;
        Config.setProperty(this.channel,"sIgnoreLinks", String.valueOf(sIgnoreLinks));
    }
    
    public void setVipRegulars(boolean vipRegulars) {
        this.vipRegulars = vipRegulars;
        Config.setProperty(this.channel, "vipRegulars", String.valueOf(vipRegulars));
    }
    
    public void setVCaps(boolean vCaps) {

        this.vIgnoreCaps = vCaps;
        Config.setProperty(this.channel,"vIgnoreCaps", String.valueOf(vIgnoreCaps));
    }
    
    public void setVEmotes(boolean vEmotes) {

        this.vIgnoreEmotes = vEmotes;
        Config.setProperty(this.channel,"vIgnoreEmotes", String.valueOf(vIgnoreEmotes));
    }
    
    public void setVSymbols(boolean vSymbols) {

        this.vIgnoreSymbols = vSymbols;
        Config.setProperty(this.channel,"vIgnoreSymbols", String.valueOf(vIgnoreSymbols));
    }
    
    public void setVLinks(boolean vLinks) {

        this.vIgnoreLinks = vLinks;
        Config.setProperty(this.channel,"vIgnoreLinks", String.valueOf(vIgnoreLinks));
    }
    
    public void setVCaule(boolean vCaule) {
        this.vIgnoreCaule = vCaule;
        Config.setProperty(this.channel, "vIgnoreCaule", String.valueOf(vIgnoreCaule));
    }

    //##############################################################

    public String getCommand(String key) {
        key = key.toLowerCase();

        if (Config.isCommand(channel, key))
            return Config.getCommand(channel, key);
        else
            return null;
        /*
        if (commands.containsKey(key)) {
            return commands.get(key);
        } else {
            return null;
        }
        */
    }

    public void setCommand(String key, String command, String author) {
        key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        System.out.println("Key: " + key);
        //command = command.replaceAll(",,", "");

        if (key.length() < 1)
            return;

        Config.addCommand(channel, key, command, "0", author);

        /*
        if (commands.containsKey(key)) {
            commands.remove(key);
            commands.put(key, command);
        } else {
            commands.put(key, command);
        }

        if (!commandsCount.containsKey(key)) {
            commandsCount.put(key, 0);
        }
        */
        if (redis.hget("channel:" + rChannel, command) == null)
            redis.hset("channel:" + rChannel, command, "0");
            
        /*
        String commandsKey = "";
        String commandsValue = "";

        Iterator itr = commands.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry pairs = (Map.Entry) itr.next();
            commandsKey += pairs.getKey() + ",";
            commandsValue += pairs.getValue() + ",,";
        }

        Config.setProperty(this.channel,"commandsKey", commandsKey);
        Config.setProperty(this.channel,"commandsValue", commandsValue);
        */
    }

    public void removeCommand(String key) {

        if (Config.isCommand(channel, key)) {
            Config.removeCommand(channel, key);
            //commandsRestrictions.remove(key);

           // saveCommandRestrictions();
        }
        /*
        if (commands.containsKey(key)) {
            commands.remove(key);
            commandsRestrictions.remove(key);

            String commandsKey = "";
            String commandsValue = "";

            Iterator itr = commands.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry pairs = (Map.Entry) itr.next();
                commandsKey += pairs.getKey() + ",";
                commandsValue += pairs.getValue() + ",,";
            }

            Config.setProperty(this.channel,"commandsKey", commandsKey);
            Config.setProperty(this.channel,"commandsValue", commandsValue);

            saveCommandRestrictions();
        }
        */

    }
    
    public void resetCommandCount(String command) {
        command = command.toLowerCase().replaceAll("!", "");
        /*
        if (commandsCount.containsKey(command)) 
            commandsCount.put(command, 0);
        */
        
        if (redis.hget("channel:" + rChannel, command) != null)
            redis.hset("channel:" + rChannel, command, "0");
            
            
        
        
        //saveCommandCounts();
        
    }
    
    public void setCommandCount(String command, int count) {
        command = command.toLowerCase().replaceAll("!", "");
        
        if (redis.hget("channel:" + rChannel, command) != null)
            redis.hset("channel:" + rChannel, command, String.valueOf(count));
        /*
        if (commandsCount.containsKey(command))
            commandsCount.put(command, count);
        */
        
        //saveCommandCounts();
    }
    
    public String getCommandCount(String command) {
        command = command.toLowerCase();
        System.out.println("Checking count for command: " + command);
        
        if (redis.hget("channel:" + rChannel, command) == null)
            return "0";
        else
            return redis.hget("channel:" + rChannel, command);
        /*
        if (!commandsCount.containsKey(command))
            return "0";
        else
            return Integer.toString(commandsCount.get(command));
        */
    }
        
    public boolean incCommandsCount(String command) {
        command = command.toLowerCase();
     
        if (redis.hget("channel:" + rChannel, command) == null) {
            redis.hset("channel:" + rChannel, command, "1");
            return true;
        } 
            
        int count = Integer.valueOf(redis.hget("channel:" + rChannel, command));
        count += 1;
        String toAdd = String.valueOf(count);
        
        redis.hset("channel:" + rChannel, command, toAdd);
        
        //saveCommandCounts();
        
        return true;
    }
    
    public void saveCommandCounts() {
        String commandCountsString = "";
        Map<String, String> toInsert = new HashMap<String, String>();
        
        Iterator itr = commandsCount.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        
        while (itr.hasNext()) {
            Map.Entry pairs = (Map.Entry) itr.next();
            toInsert.put(pairs.getKey().toString(), pairs.getValue().toString());
            commandCountsString += pairs.getKey() + "|" + pairs.getValue() + ",";
        }
        redis.hmset("channel:" + channel.substring(1), toInsert);
        Config.setProperty(this.channel,"commandCounts", commandCountsString);
    }

    public boolean setCommandsRestriction(String command, int level) {
        command = command.toLowerCase();

        if (!Config.isCommand(channel, command))
            return false;

        Config.setCommandLevel(channel, command, String.valueOf(level));
        //commandsRestrictions.put(command, level);

        //saveCommandRestrictions();

        return true;
    }

    public boolean checkCommandRestriction(String command, int level) {
        command = command.toLowerCase();
        System.out.println("Checking command: " + command + " User level: " + level);
        if (Config.getCommandRestriction(channel, command) == 0)
            return true;

        if (level >= Config.getCommandRestriction(channel, command))
            return true;

        return false;
    }

    public void saveCommandRestrictions() {
        String commandRestrictionsString = "";

        Iterator itr = commandsRestrictions.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry pairs = (Map.Entry) itr.next();
            commandRestrictionsString += pairs.getKey() + "|" + pairs.getValue() + ",";
        }

        Config.setProperty(this.channel,"commandRestrictions", commandRestrictionsString);
    }

    public void setRepeatCommand(String key, int delay, int diff) {
        if (commandsRepeat.containsKey(key)) {
            commandsRepeat.get(key).timer.cancel();
            commandsRepeat.remove(key);
            RepeatCommand rc = new RepeatCommand(channel, key, delay, diff, true);
            commandsRepeat.put(key, rc);
        } else {
            RepeatCommand rc = new RepeatCommand(channel, key, delay, diff, true);
            commandsRepeat.put(key, rc);
        }

        writeRepeatCommand();
    }

    public void removeRepeatCommand(String key) {
        if (commandsRepeat.containsKey(key)) {
            commandsRepeat.get(key).timer.cancel();
            commandsRepeat.remove(key);

            writeRepeatCommand();
        }
    }

    public void setRepeatCommandStatus(String key, boolean status) {
        if (commandsRepeat.containsKey(key)) {
            commandsRepeat.get(key).setStatus(status);
            writeRepeatCommand();
        }
    }

    private void writeRepeatCommand() {
        String commandsRepeatKey = "";
        String commandsRepeatDelay = "";
        String commandsRepeatDiff = "";
        String commandsRepeatActive = "";

        Iterator itr = commandsRepeat.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry pairs = (Map.Entry) itr.next();
            commandsRepeatKey += pairs.getKey() + ",";
            commandsRepeatDelay += ((RepeatCommand) pairs.getValue()).delay + ",";
            commandsRepeatDiff += ((RepeatCommand) pairs.getValue()).messageDifference + ",";
            commandsRepeatActive += ((RepeatCommand) pairs.getValue()).active + ",";
        }

        Config.setProperty(this.channel,"commandsRepeatKey", commandsRepeatKey);
        Config.setProperty(this.channel,"commandsRepeatDelay", commandsRepeatDelay);
        Config.setProperty(this.channel,"commandsRepeatDiff", commandsRepeatDiff);
        Config.setProperty(this.channel,"commandsRepeatActive", commandsRepeatActive);
    }

    public void setScheduledCommand(String key, String pattern, int diff) {
        if (commandsSchedule.containsKey(key)) {
            commandsSchedule.get(key).s.stop();
            commandsSchedule.remove(key);
            ScheduledCommand rc = new ScheduledCommand(channel, key, pattern, diff, true);
            commandsSchedule.put(key, rc);
        } else {
            ScheduledCommand rc = new ScheduledCommand(channel, key, pattern, diff, true);
            commandsSchedule.put(key, rc);
        }

        writeScheduledCommand();


    }

    public void removeScheduledCommand(String key) {
        if (commandsSchedule.containsKey(key)) {
            commandsSchedule.get(key).s.stop();
            commandsSchedule.remove(key);

            writeScheduledCommand();
        }
    }

    public void setScheduledCommandStatus(String key, boolean status) {
        if (commandsSchedule.containsKey(key)) {
            commandsSchedule.get(key).setStatus(status);
            writeScheduledCommand();
        }
    }

    private void writeScheduledCommand() {
        String commandsScheduleKey = "";
        String commandsSchedulePattern = "";
        String commandsScheduleDiff = "";
        String commandsScheduleActive = "";

        Iterator itr = commandsSchedule.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry pairs = (Map.Entry) itr.next();
            commandsScheduleKey += pairs.getKey() + ",,";
            commandsSchedulePattern += ((ScheduledCommand) pairs.getValue()).pattern + ",,";
            commandsScheduleDiff += ((ScheduledCommand) pairs.getValue()).messageDifference + ",,";
            commandsScheduleActive += ((ScheduledCommand) pairs.getValue()).active + ",,";

        }

        Config.setProperty(this.channel,"commandsScheduleKey", commandsScheduleKey);
        Config.setProperty(this.channel,"commandsSchedulePattern", commandsSchedulePattern);
        Config.setProperty(this.channel,"commandsScheduleDiff", commandsScheduleDiff);
        Config.setProperty(this.channel,"commandsScheduleActive", commandsScheduleActive);
    }

    public String getCommandList() {

        List<String> cList;
        String commandKeys = "";
        cList = Config.getCommands(channel);

        for (String s : cList)
            commandKeys += s + ", ";



        /*
        Iterator itr = commands.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry pairs = (Map.Entry) itr.next();
            commandKeys += pairs.getKey() + ", ";
        }
        */
        return commandKeys;

    }

    public void addAutoReply(String trigger, String response) {
        trigger = trigger.replaceAll(",,", "");
        response.replaceAll(",,", "");

        if (!trigger.startsWith("REGEX:")) {
            String[] parts = trigger.replaceFirst("^\\*", "").replaceFirst("\\*$", "").split("\\*");

            //Only apply leading & trailing any if an one was requested
            boolean trailingAny = trigger.endsWith("*");
            if (trigger.startsWith("*"))
                trigger = ".*";
            else
                trigger = "";

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].length() < 1)
                    continue;

                trigger += Pattern.quote(parts[i]);
                if (i != parts.length - 1)
                    trigger += ".*";
            }

            if (trailingAny)
                trigger += ".*";

        } else {
            trigger = trigger.replaceAll("REGEX:", "");
        }

        System.out.println("Final: " + trigger);
        autoReplyTrigger.add(Pattern.compile(trigger, Pattern.CASE_INSENSITIVE));
        autoReplyResponse.add(response);

        saveAutoReply();
    }

    public boolean removeAutoReply(int pos) {
        pos = pos - 1;

        if (pos > autoReplyTrigger.size() - 1)
            return false;

        autoReplyTrigger.remove(pos);
        autoReplyResponse.remove(pos);

        saveAutoReply();

        return true;
    }

    private void saveAutoReply() {
        String triggerString = "";
        String responseString = "";

        for (int i = 0; i < autoReplyTrigger.size(); i++) {
            triggerString += autoReplyTrigger.get(i).toString() + ",,";
            responseString += autoReplyResponse.get(i).toString() + ",,";
        }

        Config.setProperty(this.channel,"autoReplyTriggers", triggerString);
        Config.setProperty(this.channel,"autoReplyResponse", responseString);
    }

    //#####################################################
/*
    public void loadPoints(String name) {
        try {
        BufferedReader br = new BufferedReader(new FileReader(name + ".points"));
        String line;
        while ((line = br.readLine()) != null) {
           String[] result = line.split("=");
           points.put(result[0], Integer.parseInt(result[1]));
        }
        br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
*/
    

    
    public void loadPoints(String name) {
        name = name.replaceAll("#", "");
        System.out.println("Trying to load points from the DB. Fingers crossed");
        String url = Constants.BASEURL + "points";
        String user = Constants.SQLUSER;
        String password = Constants.SQLPASSWORD;
        name = name.toLowerCase();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        
        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("SELECT * from " + name);
            rs = st.executeQuery();
            
            while (rs.next()) 
                points.put(rs.getString(2), rs.getInt(3));
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Channel.java:599 - Error loading points");
        } finally {
            try {
                if (st != null)
                    st.close();
                if (rs != null)
                    rs.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    public String getAnswer() {
        System.out.println("Attemping to load answers");
        String url = Constants.BASEURL + "answers";
        String user = Constants.SQLUSER;
        String answer = "";
        String password = Constants.SQLPASSWORD;
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        
        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("SELECT * FROM 8ball ORDER BY RAND() LIMIT 1");
            rs = st.executeQuery();

            while (rs.next()) {
                answer = rs.getString(2);
            }
            return answer;

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Channel.java: Error loading answers from db");
            return "Error";

        } finally {
            try {
                if (st != null)
                    st.close();
                if (rs != null)
                    rs.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
    }


    public static int randInt(int min, int max) {

    Random rand = new Random();

    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
}

    public int getPoints(String username) {
        username = username.toLowerCase();

        if (points.containsKey(username)) {
            return points.get(username);
        } else {
            return 0;
        }
    }
    
        public int getRankByUsername(String username) {
        username = username.toLowerCase();
        List<String> rankers = new LinkedList<String>();
        Map<String, Integer> pointssorted = MapUtil.sortByValue(points);
        
        for (Map.Entry entry : pointssorted.entrySet()) {
            rankers.add((String) entry.getKey());
        }
        
        return rankers.indexOf(username);
    }
    
    public String getUsernameByIndex(int position) {
        List<String> rankers = new LinkedList<String>();
        Map<String, Integer> pointssorted = MapUtil.sortByValue(points);
        
        for (Map.Entry entry : pointssorted.entrySet())
            rankers.add((String) entry.getKey());
        
        return rankers.get(position);
    }
    
    public String getUsernameAndPointsByIndex(int position) {
        List<String> rankers = new LinkedList<String>();
        Map<String, Integer> pointssorted = MapUtil.sortByValue(points);
        
        for (Map.Entry entry : pointssorted.entrySet())
            rankers.add((String) entry.getKey() + " with " + entry.getValue() + " DPs.");
        
        return rankers.get(position);
    }
    
    public int getPointsByIndex(int position) {
        List<Integer> rankers = new LinkedList<Integer>();
        Map<String, Integer> pointssorted = MapUtil.sortByValue(points);
        
        for (Map.Entry entry : pointssorted.entrySet())
            rankers.add((Integer) entry.getValue());
        
        return rankers.get(position);
    }
    
 
    
    public List<String> getTopPoints() {
        List<String> toppoints = new LinkedList<String>();
        
        Map<String, Integer> pointssorted = MapUtil.sortByValue(points);
        
        int j = 0;
        
        for (Map.Entry entry : pointssorted.entrySet()) {
            if (j > 9)
                break;
            
            toppoints.add((String) entry.getKey() + ": " + entry.getValue());
            j++;
        }
        
        return toppoints;      
        
    }
    
    public List<Integer> getTopPointsN() {
        List<Integer> toppoints = new LinkedList<Integer>();
        
        Map<String, Integer> pointssorted = MapUtil.sortByValue(points);
        int j = 0;
        
        for (Map.Entry entry : pointssorted.entrySet()) {
            if (j > 9)
                break;
            toppoints.add((Integer) entry.getValue());
            j++;
        }
        
        return toppoints;
    }

    public int addPoints(String username, int newPoints) {
        username = username.toLowerCase();
        int toReturn = Points.addPoints(channel.substring(1), username, newPoints);
        Points.logPoints(channel.substring(1), "Points added " + newPoints, "admin", username, new java.util.Date().toString());
        points.put(username, toReturn);
        return toReturn;

    }
    
    public void distributePoints(Set<String> names, int newPoints) {
        for (String name : names) {
            name = name.toLowerCase();
            int toReturn = Points.addPoints(channel.substring(1), name, newPoints);
            points.put(name, toReturn);
        }
    }

    

    public int removePoints(String username, int newPoints) {
        username = username.toLowerCase();
        int toReturn = Points.removePoints(channel.substring(1), username, newPoints);
        points.put(username, toReturn);
        return toReturn;
    }


    //#####################################################

    public String getTopic() {
        return topic;
    }

    public void setTopic(String s) {
        topic = s;
        Config.setProperty(this.channel,"topic", topic);
        topicTime = (int) (System.currentTimeMillis() / 1000);
        Config.setProperty(this.channel,"topicTime", String.valueOf(topicTime));
    }

    public void updateGame(String game) throws IOException {
        String channelID = String.valueOf(JSONUtil.krakenUserID(this.channel.substring(1)));
        System.out.println(BotManager.putRemoteData("https://api.twitch.tv/kraken/channels/" + channelID, "{\"channel\": {\"game\": \"" + JSONObject.escape(game) + "\"}}"));
    }

    public void updateStatus(String status) throws IOException {
        String channelID = String.valueOf(JSONUtil.krakenUserID(this.channel.substring(1)));
        System.out.println(BotManager.putRemoteData("https://api.twitch.tv/kraken/channels/" + channelID, "{\"channel\": {\"status\": \"" + JSONObject.escape(status) + "\"}}"));
    }

    public String getTopicTime() {
        int difference = (int) (System.currentTimeMillis() / 1000) - topicTime;
        String returnString = "";

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

        int seconds = (int) (difference / 60);
        returnString += seconds + "m";
        difference -= seconds * 60;


        return returnString;
    }

    //#####################################################

    public int getFilterSymbolsMin() {
        return filterSymbolsMin;
    }

    public int getFilterSymbolsPercent() {
        return filterSymbolsPercent;
    }

    public void setFilterSymbolsMin(int symbols) {
        filterSymbolsMin = symbols;
        Config.setProperty(this.channel,"filterSymbolsMin", String.valueOf(filterSymbolsMin));
    }

    public void setFilterSymbolsPercent(int symbols) {
        filterSymbolsPercent = symbols;
        Config.setProperty(this.channel,"filterSymbolsPercent", String.valueOf(filterSymbolsPercent));
    }

    public boolean getFilterCaps() {
        return filterCaps;
    }

    public int getfilterCapsPercent() {
        return filterCapsPercent;
    }

    public int getfilterCapsMinCharacters() {
        return filterCapsMinCharacters;
    }

    public int getfilterCapsMinCapitals() {
        return filterCapsMinCapitals;
    }

    public void setFilterCaps(boolean caps) {
        filterCaps = caps;
        Config.setProperty(this.channel,"filterCaps", String.valueOf(filterCaps));
    }

    public void setAutoHost(boolean setting) {
        this.enableHost = setting;
        Config.setProperty(this.channel,"enableHost", String.valueOf(this.enableHost));
    }

    public void setfilterCapsPercent(int caps) {
        filterCapsPercent = caps;
        Config.setProperty(this.channel,"filterCapsPercent", String.valueOf(filterCapsPercent));
    }

    public void setfilterCapsMinCharacters(int caps) {
        filterCapsMinCharacters = caps;
        Config.setProperty(this.channel,"filterCapsMinCharacters", String.valueOf(filterCapsMinCharacters));
    }

    public void setfilterCapsMinCapitals(int caps) {
        filterCapsMinCapitals = caps;
        Config.setProperty(this.channel,"filterCapsMinCapitals", String.valueOf(filterCapsMinCapitals));
    }

    public void setFilterLinks(boolean links) {
        filterLinks = links;
        Config.setProperty(this.channel,"filterLinks", String.valueOf(links));
    }

    public void setFilterCaule(boolean caule) {
        filterCaule = caule;
        Config.setProperty(this.channel, "filterCaule", String.valueOf(caule));
    }
    
    public boolean getFilterCaule() {
        return filterCaule;
    }
    
    public boolean getFilterLinks() {
        return filterLinks;
    }

    public void setFilterOffensive(boolean option) {
        filterOffensive = option;
        Config.setProperty(this.channel,"filterOffensive", String.valueOf(option));
    }

    public boolean getFilterOffensive() {
        return filterOffensive;
    }

    public void setFilterEmotes(boolean option) {
        filterEmotes = option;
        Config.setProperty(this.channel,"filterEmotes", String.valueOf(option));
    }

    public boolean getFilterEmotes() {
        return filterEmotes;
    }

    public void setFilterSymbols(boolean option) {
        filterSymbols = option;
        Config.setProperty(this.channel,"filterSymbols", String.valueOf(option));
    }

    public boolean getFilterSymbols() {
        return filterSymbols;
    }

    public int getFilterMax() {
        return filterMaxLength;
    }

    public void setFilterMax(int option) {
        filterMaxLength = option;
        Config.setProperty(this.channel,"filterMaxLength", String.valueOf(option));
    }

    public void setFilterEmotesMax(int option) {
        filterEmotesMax = option;
        Config.setProperty(this.channel,"filterEmotesMax", String.valueOf(option));
    }

    public int getFilterEmotesMax() {
        return filterEmotesMax;
    }

    public boolean getFilterEmotesSingle() {
        return filterEmotesSingle;
    }

    public void setFilterEmotesSingle(boolean filterEmotesSingle) {
        this.filterEmotesSingle = filterEmotesSingle;

        Config.setProperty(this.channel,"filterEmotesSingle", String.valueOf(filterEmotesSingle));
    }

    public void setAnnounceJoinParts(boolean bol) {
        announceJoinParts = bol;
        Config.setProperty(this.channel,"announceJoinParts", String.valueOf(bol));
    }

    public boolean getAnnounceJoinParts() {
        return announceJoinParts;
    }

    public void setFilterColor(boolean option) {
        filterColors = option;
        Config.setProperty(this.channel,"filterColors", String.valueOf(option));
    }

    public boolean getFilterColor() {
        return filterColors;
    }

    public void setFilterMe(boolean option) {
        filterMe = option;
        Config.setProperty(this.channel,"filterMe", String.valueOf(option));
    }

    public boolean getFilterMe() {
        return filterMe;
    }

    public void setEnableWarnings(boolean option) {
        enableWarnings = option;
        Config.setProperty(this.channel,"enableWarnings", String.valueOf(option));
    }

    public boolean getEnableWarnings() {
        return enableWarnings;
    }

    public void setTimeoutDuration(int option) {
        timeoutDuration = option;
        Config.setProperty(this.channel,"timeoutDuration", String.valueOf(option));
    }

    public void setRouletteTO(int option)  {
        rouletteTO = option;
        Config.setProperty(this.channel,"rouletteTO", String.valueOf(option));
    }

    public int getRouletteTO() {
        return rouletteTO;
    }

    public int getTimeoutDuration() {
        return timeoutDuration;
    }

    //###################################################

    public boolean isRegular(String name) {
        synchronized (regulars) {
            for (String s : regulars) {
                if (s.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addRegular(String name) {
        synchronized (regulars) {
            regulars.add(name.toLowerCase());
        }

        String regularsString = "";

        synchronized (regulars) {
            for (String s : regulars) {
                regularsString += s + ",";
            }
        }

        Config.setProperty(this.channel,"regulars", regularsString);
    }

    public void removeRegular(String name) {
        synchronized (regulars) {
            if (regulars.contains(name.toLowerCase()))
                regulars.remove(name.toLowerCase());
        }
        String regularsString = "";

        synchronized (regulars) {
            for (String s : regulars) {
                regularsString += s + ",";
            }
        }

        Config.setProperty(this.channel,"regulars", regularsString);
    }

    public Set<String> getRegulars() {
        return regulars;
    }

    public void permitUser(String name) {
        synchronized (permittedUsers) {
            if (permittedUsers.contains(name.toLowerCase()))
                return;
        }

        synchronized (permittedUsers) {
            permittedUsers.add(name.toLowerCase());
        }
    }

    public boolean linkPermissionCheck(String name) {

        if (this.isRegular(name)) {
            return true;
        }

        synchronized (permittedUsers) {
            if (permittedUsers.contains(name.toLowerCase())) {
                permittedUsers.remove(name.toLowerCase());
                return true;
            }
        }

        return false;
    }

    public boolean isSubscriber(String name) {
        if (subscribers.contains(name.toLowerCase()))
            return true;

//        if (emoteSet.length() > 0)
//            if (BotManager.getInstance().checkEmoteSetMapping(name, emoteSet))
//                return true;
        return false;
    }

    public void addSubscriber(String name) {
        subscribers.add(name.toLowerCase());
    }

    //###################################################

    public boolean isModerator(String name) {
        synchronized (tagModerators) {
            if (tagModerators.contains(name))
                return true;
        }
        synchronized (moderators) {
            if (moderators.contains(name.toLowerCase()))
                return true;
        }

        return false;
    }

    public void addModerator(String name) {
        synchronized (moderators) {
            moderators.add(name.toLowerCase());
        }

        String moderatorsString = "";

        synchronized (moderators) {
            for (String s : moderators) {
                moderatorsString += s + ",";
            }
        }

        Config.setProperty(this.channel,"moderators", moderatorsString);
    }

    public void removeModerator(String name) {
        synchronized (moderators) {
            if (moderators.contains(name.toLowerCase()))
                moderators.remove(name.toLowerCase());
        }

        String moderatorsString = "";

        synchronized (moderators) {
            for (String s : moderators) {
                moderatorsString += s + ",";
            }
        }

        Config.setProperty(this.channel,"moderators", moderatorsString);
    }

    public Set<String> getModerators() {
        return moderators;
    }

    //###################################################

    
    public boolean isQuoteBanned(String name) {
        synchronized (quotebans) {
            if (quotebans.contains(name.toLowerCase()))
                return true;
        }
        
        return false;
    }
    
    public void addQuoteBanned(String name) {
        synchronized (quotebans) {
            quotebans.add(name.toLowerCase());
        }
        
        String quoteBans = "";
        
        synchronized (quotebans) {
            for (String s : quotebans)
                quoteBans += s + ",";
                
        }

        Config.setProperty(this.channel,"quotebans", quoteBans);
    }
    
    public void removeQuoteBanned(String name) {
        synchronized (quotebans) {
            quotebans.remove(name.toLowerCase());
        }
        
        String quoteBans = "";
        
        synchronized (quotebans) {
            for (String s : quotebans)
                quoteBans += s + ",";
        }

        Config.setProperty(this.channel,"quotebans", quoteBans);
    }
    
    public Set<String> getQuoteBans() {
        return quotebans;
    }

    //###################################################
    
    public boolean isAdmin(String name) {
        synchronized (admins) {
            if (admins.contains(name.toLowerCase()))
                return true;
        }
        return false;
    }
    
    public void addAdmin(String name) {
        synchronized (admins) {
            admins.add(name.toLowerCase());
        }
        
        String adminsString = "";
        
        synchronized (admins) {
            for (String s : admins) {
                adminsString += s + ",";
            }
        }

        Config.setProperty(this.channel,"admins", adminsString);
    }
    
    public void removeAdmin(String name) {
        synchronized (admins) {
            if (admins.contains(name.toLowerCase()))
                admins.remove(name.toLowerCase());
        }

        String adminsString = "";

        synchronized (admins) {
            for (String s : admins) {
                adminsString += s + ",";
            }
        }

        Config.setProperty(this.channel,"admins", adminsString);
    }

    public Set<String> getAdmins() {
        return admins;
    }

    public boolean isOwner(String name) {
        synchronized (owners) {
            if (owners.contains(name.toLowerCase()))
                return true;
        }

        return false;
    }

    public void addOwner(String name) {
        synchronized (owners) {
            owners.add(name.toLowerCase());
        }

        String ownersString = "";

        synchronized (owners) {
            for (String s : owners) {
                ownersString += s + ",";
            }
        }

        Config.setProperty(this.channel,"owners", ownersString);
    }

    public void removeOwner(String name) {
        synchronized (owners) {
            if (owners.contains(name.toLowerCase()))
                owners.remove(name.toLowerCase());
        }

        String ownersString = "";

        synchronized (owners) {
            for (String s : owners) {
                ownersString += s + ",";
            }
        }

        Config.setProperty(this.channel,"owners", ownersString);
    }

    public Set<String> getOwners() {
        return owners;
    }

    //###################################################

    public void addPermittedDomain(String name) {
        synchronized (permittedDomains) {
            permittedDomains.add(name.toLowerCase());
        }

        String permittedDomainsString = "";

        synchronized (permittedDomains) {
            for (String s : permittedDomains) {
                permittedDomainsString += s + ",";
            }
        }

        Config.setProperty(this.channel,"permittedDomains", permittedDomainsString);
    }

    public void removePermittedDomain(String name) {
        synchronized (permittedDomains) {
            for (int i = 0; i < permittedDomains.size(); i++) {
                if (permittedDomains.get(i).equalsIgnoreCase(name)) {
                    permittedDomains.remove(i);
                }
            }
        }

        String permittedDomainsString = "";

        synchronized (permittedDomains) {
            for (String s : permittedDomains) {
                permittedDomainsString += s + ",";
            }
        }

        Config.setProperty(this.channel,"permittedDomains", permittedDomainsString);
    }

    public boolean isDomainPermitted(String domain) {
        for (String d : permittedDomains) {
            if (d.equalsIgnoreCase(domain)) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<String> getpermittedDomains() {
        return permittedDomains;
    }
    // #################################################

    public void addOffensive(String word) {
        synchronized (offensiveWords) {
            offensiveWords.add(word);
        }

        synchronized (offensiveWordsRegex) {
            if (word.startsWith("REGEX:")) {
                String line = word.substring(6);
                System.out.println("Adding: " + line);
                Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
                offensiveWordsRegex.add(tempP);
            } else {
                String line = ".*" + Pattern.quote(word) + ".*";
                System.out.println("Adding: " + line);
                Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
                offensiveWordsRegex.add(tempP);
            }

        }

        String offensiveWordsString = "";


        synchronized (offensiveWords) {
            for (String s : offensiveWords) {
                offensiveWordsString += s + ",,";
            }
        }

        Config.setProperty(this.channel,"offensiveWords", offensiveWordsString);
    }

    public void removeOffensive(String word) {
        synchronized (offensiveWords) {
            if (offensiveWords.contains(word))
                offensiveWords.remove(word);
        }

        String offensiveWordsString = "";
        synchronized (offensiveWords) {
            for (String s : offensiveWords) {
                offensiveWordsString += s + ",,";
            }
        }

        Config.setProperty(this.channel,"offensiveWords", offensiveWordsString);

        synchronized (offensiveWordsRegex) {
            offensiveWordsRegex.clear();

            for (String w : offensiveWords) {
                if (w.startsWith("REGEX:")) {
                    String line = w.substring(6);
                    System.out.println("ReAdding: " + line);
                    Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
                    offensiveWordsRegex.add(tempP);
                } else {
                    String line = ".*" + Pattern.quote(w) + ".*";
                    System.out.println("ReAdding: " + line);
                    Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
                    offensiveWordsRegex.add(tempP);
                }
            }
        }
    }

    public void clearBannedPhrases() {
        offensiveWords.clear();
        offensiveWordsRegex.clear();
        Config.setProperty(this.channel,"offensiveWords", "");
    }

    public boolean isBannedPhrase(String phrase) {
        return offensiveWords.contains(phrase);
    }

    public boolean isOffensive(String word) {
        for (Pattern reg : offensiveWordsRegex) {
            Matcher match = reg.matcher(word);
            if (match.find()) {
                BotManager.getInstance().log("BANPHRASE FILTER: Phrase (" + word + ") matched (" + reg.toString() + ")");
                Config.logTimeout(channel, "null", reg.toString(), word, "OFFENSIVE", -1);
                return true;
            }
        }

        int severity = Integer.parseInt(Config.getProperty(this.channel,"banPhraseSeverity"));
        if (BotManager.getInstance().banPhraseLists.containsKey(severity)) {
            for (Pattern reg : BotManager.getInstance().banPhraseLists.get(severity)) {
                Matcher match = reg.matcher(word);
                if (match.find()) {
                    BotManager.getInstance().log("BANPHRASE FILTER: Phrase (" + word + ") matched (" + reg.toString() + ")");
                    return true;
                }
            }
        }

        return false;
    }

    public Set<String> getOffensive() {
        return offensiveWords;
    }

    public String getSymbolsTO() {
        return symbolsTO;
    }

    public void setSymbolsTO(String message) {
        this.symbolsTO = message;
        Config.setProperty(this.channel,"symbolsTO", message);
    }

    public String getUptimeMessage() { return uptimeMessage; }

    public void setUptimeMessage(String message) {
        this.uptimeMessage = message;
        Config.setProperty(this.channel, "uptimeMessage", message);
    }

    public String getCapsTO() {
        return capsTO;
    }

    public void setCapsTO(String message) {
        this.capsTO = message;
        Config.setProperty(this.channel,"capsTO", message);
    }

    public String getLinksTO() {
        return linksTO;
    }

    public void setLinksTO(String message) {
        this.linksTO = message;
        Config.setProperty(this.channel,"linksTO", message);
    }

    public String getEmoteTO() {
        return emoteTO;
    }

    public void setEmoteTO(String message) {
        this.emoteTO = message;
        Config.setProperty(this.channel,"emoteTO", message);
    }

    public String getEmoteSingleTO() {
        return emoteSingleTO;
    }

    public void setEmoteSingleTO(String message) {
        this.emoteSingleTO = message;
        Config.setProperty(this.channel,"emotesingleTO", message);
    }

    public String getOffensiveTO() {
        return offensiveTO;
    }

    public void setOffensiveTO(String message) {
        this.offensiveTO = message;
        Config.setProperty(this.channel,"offensiveTO", message);
    }

    public String getMeTO() {
        return meTO;
    }

    public void setMeTO(String message) {
        this.meTO = message;
        Config.setProperty(this.channel,"meTO", message);
    }

    public String getMaxTO() {
        return maxTO;
    }

    public void setMaxTO(String message) {
        this.maxTO = message;
        Config.setProperty(this.channel,"maxTO", message);
    }

    // ##################################################

    public void setTopicFeature(boolean setting) {
        this.useTopic = setting;
        Config.setProperty(this.channel,"useTopic", String.valueOf(this.useTopic));

    }

    public void setFiltersFeature(boolean setting) {
        this.useFilters = setting;
        Config.setProperty(this.channel,"useFilters", String.valueOf(this.useFilters));
    }

    public Poll getPoll() {
        return currentPoll;
    }

    public void setPoll(Poll _poll) {
        currentPoll = _poll;
    }

    public Giveaway getGiveaway() {
        return currentGiveaway;
    }

    public void setGiveaway(Giveaway _gw) {
        currentGiveaway = _gw;
    }

    public Gamble getGamble() {
        return currentGamble;
    }

    public void setGamble(Gamble _ga) {
        currentGamble = _ga;
    }
    
    public Bet getBet() {
        return currentBet;
    }
    
    public void setBet(Bet _bet) {
        currentBet = _bet;
    }

    public boolean checkThrow() {
        return enableThrow;
    }
    
    public void setThrow(boolean setting) {
        this.enableThrow = setting;
        Config.setProperty(this.channel,"enableThrow", String.valueOf(this.enableThrow));
    }

    public boolean checkPoints() {
        return enablePoints;
    }
    
    public boolean checkStats() {
        return enableStats;
    }

    public void setPoints(boolean setting) {
        this.enablePoints = setting;
        Config.setProperty(this.channel,"enablePoints", String.valueOf(this.enablePoints));
    }
    
    public void setStats(boolean setting) {
        this.enableStats = setting;
        Config.setProperty(this.channel,"enableStats", String.valueOf(this.enableStats));
    }
    
    public boolean checkRoulette() {
        return enableRoulette;
    }
    
    public boolean check8ball() {
        return enable8ball;
    }
    
    public boolean getMusic() {
        return enableMusic;
    }

    public boolean checkLeague() {
        return enableLeague;
    }

    public boolean checkQuote() {
        return enableQuote;
    }
    
    public boolean checkLove() {
        return enableLove;
    }
    
    public boolean checkWR() {
        return enableWR;
    }
    
    public boolean checkQuoteGame() {
        return enableQuoteGame;
    }
    
    public boolean checkFollowage() {
        return enableFollowage;
    }
    
    public boolean checkSubWhisper() {
        return enableSubWhisper;
    }
    
    public boolean checkVideoNovoEnabled() {
        return enableVideoNovo;
    }
    
    public void setVideoNovo(boolean setting) {
        this.enableVideoNovo = setting;
        Config.setProperty(this.channel,"enableVideoNovo", String.valueOf(this.enableVideoNovo));
    } 

    public boolean checkQuoteMods() {
        return quoteModsOnly;
    }
    
    public String getSubWhisperMessage() {
        return subWhisperMessage;
    }

    public String getResubWhisperMessage() { return resubWhisperMessage; }

    public String getSummoner() {
        return summoner;
    }
    
    public String getSmurf1() {
        return smurf1;
    }
    
    public String getSmurf2() {
        return smurf2;
    }
    
    public String getSmurf3() {
        return smurf3;
    }
    
    public String getSmurf4() {
        return smurf4;
    }

    public String getRegion() {
        return region;
    }
    
    public String getPointsName() {
        return pointsName;
    }

    public int getPointsAmount() { return pointsAmount; }

    public int getPointsDelay() { return pointsDelay; }

    public void setPointsDelay(int delay) {
        this.pointsDelay = delay;
        Config.setProperty(this.channel,"pointsDelay", String.valueOf(delay));
    }

    public void setPointsAmount(int amount) {
        this.pointsAmount = amount;
        Config.setProperty(this.channel,"pointsAmount", String.valueOf(amount));
    }

    public void setSummoner(String summ) {
        this.summoner = summ;
        Config.setProperty(this.channel,"summoner", summ);
    }
    
    public void setSmurf1(String name) {
        this.smurf1 = name;
        Config.setProperty(this.channel,"smurf1", name);
    }
    
    public void setSmurf2(String name) {
        this.smurf2 = name;
        Config.setProperty(this.channel,"smurf2", name);
    }
    
    public void setSmurf3(String name) {
        this.smurf3 = name;
        Config.setProperty(this.channel,"smurf3", name);
    }
    
    public void setSmurf4(String name) {
        this.smurf4 = name;
        Config.setProperty(this.channel,"smurf4", name);
    }

    public void setRegion(String regg) {
        this.region = regg;
        Config.setProperty(this.channel,"region", regg);
    }
    
    public void setPointsName(String pn) {
        this.pointsName = pn;
        Config.setProperty(this.channel,"pointsName", pn);
    }

    public void setQuoteMods(boolean setting) {
        this.quoteModsOnly = setting;
        Config.setProperty(this.channel,"quoteModsOnly", String.valueOf(this.quoteModsOnly));
    }

    public void setQuote(boolean setting) {
        this.enableQuote = setting;
        Config.setProperty(this.channel,"enableQuote", String.valueOf(this.enableQuote));
    }
    
    public void setLove(boolean setting) {
        this.enableLove = setting;
        Config.setProperty(this.channel,"enableLove", String.valueOf(this.enableLove));
    }

    public void setLeague(boolean setting) {
        this.enableLeague = setting;
        Config.setProperty(this.channel,"enableLeague", String.valueOf(this.enableLeague));
    }
    
    public void setRoulette(boolean setting) {
        this.enableRoulette = setting;
        Config.setProperty(this.channel,"enableRoulette", String.valueOf(this.enableRoulette));
    }
    
    public void set8ball(boolean setting) {
        this.enable8ball = setting;
        Config.setProperty(this.channel,"enable8ball", String.valueOf(this.enable8ball));
    }
    
    public void setWR(boolean setting) {
        this.enableWR = setting;
        Config.setProperty(this.channel,"enableWR", String.valueOf(this.enableWR));
    }
    
    public void setQuoteGame(boolean setting) {
        this.enableQuoteGame = setting;
        Config.setProperty(this.channel,"enableQuoteGame", String.valueOf(this.enableQuoteGame));
    }
    
    public void setFollowage(boolean setting) {
        this.enableFollowage = setting;
        Config.setProperty(this.channel,"enableFollowage", String.valueOf(this.enableFollowage));
    }
    
    public void setEnableSubWhisper(boolean setting) {
        this.enableSubWhisper = setting;
        Config.setProperty(this.channel,"enableSubWhisper", String.valueOf(this.enableSubWhisper));
    }
    
    public void setSubWhisperMessage(String message) {
        this.subWhisperMessage = message;
        Config.setProperty(this.channel,"subWhisperMessage", this.subWhisperMessage);
    }

    public void setResubWhisperMessage(String message) {
        this.resubWhisperMessage = message;
        Config.setProperty(this.channel,"resubWhisperMessage", this.resubWhisperMessage);
    }
    
    public void setMusic(boolean setting) {
        this.enableMusic = setting;
        Config.setProperty(this.channel,"enableMusic", String.valueOf(this.enableMusic));
    }

    public boolean checkSignKicks() {
        return signKicks;
    }

    public boolean checkUptime() { return enableUptime; }

    public void setUptime(boolean setting) {
        this.enableUptime = setting;
        Config.setProperty(this.channel, "enableUptime", String.valueOf(this.enableUptime));
    }

    public void setSignKicks(boolean setting) {
        this.signKicks = setting;
        Config.setProperty(this.channel,"signKicks", String.valueOf(this.signKicks));
    }

    public void setLogging(boolean option) {
        logChat = option;
        Config.setProperty(this.channel,"logChat", String.valueOf(option));
    }

    public boolean getLogging() {
        return logChat;
    }

    public int getCommercialLength() {
        return commercialLength;
    }

    public void setCommercialLength(int commercialLength) {
        this.commercialLength = commercialLength;
        Config.setProperty(this.channel,"commercialLength", String.valueOf(commercialLength));
    }

    // ##################################################

    public boolean checkPermittedDomain(String message) {
        //Allow base domain w/o a path
        if (message.matches(".*(twitch\\.tv|twitchtv\\.com|justin\\.tv)")) {
            System.out.println("INFO: Permitted domain match on jtv/ttv base domain.");
            return true;
        }

        for (String d : permittedDomains) {
            //d = d.replaceAll("\\.", "\\\\.");

            String test = ".*(\\.|^|//)" + Pattern.quote(d) + "(/|$).*";
            if (message.matches(test)) {
                //System.out.println("DEBUG: Matched permitted domain: " + test);
                return true;
            }
        }
        return false;
    }

    // #################################################

    public String getLastfm() {
        return lastfm;
    }

    public void setLastfm(String string) {
        lastfm = string;
        Config.setProperty(this.channel,"lastfm", lastfm);
    }
    
    // #################################################
    
    public String getTimezone() {
        return timeZone;
    }
    
    public void setTimezone(String tz) {
        timeZone = tz;
        Config.setProperty(this.channel,"timeZone", timeZone);
    }

    // #################################################
    
    public String getYoutubeChannelID() {
        return youtubeChannelId;
    }
    
    public void setYoutubeChannelID(String id) {
        youtubeChannelId = id;
        Config.setProperty(this.channel,"youtubeChannelId", youtubeChannelId);
    }
    
    // #################################################


    public String getSteam() {
        return steamID;
    }

    public void setSteam(String string) {
        steamID = string;
        Config.setProperty(this.channel,"steamID", steamID);
    }

    // #################################################

    public AutoHost getAH() {
        return currentAutoHost;
    }
    
    public Points getPts() {
        return currentPoints;
    }

    public void setAH(AutoHost _ah) {
        currentAutoHost = _ah;
    }
    
    public void setPts(Points _p) {
        currentPoints = _p;
    }

    public boolean checkHost() {
        return enableHost;
    }

    public void setHost(String channelname, Integer priority) {
        channelname = channelname.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        System.out.println("Channel name added: " + channelname);

        if (channelname.length() < 1) 
            return;

        if (hosts.containsKey(channelname)) {
            hosts.remove(channelname);
            hosts.put(channelname, priority);
            System.out.println("Adding Host: " + channelname + " With Priority: " + priority);
        } else {
            hosts.put(channelname, priority);
            System.out.println("Adding Host: " + channelname + " With Priority: " + priority);
        }

        if (priority == 1) {
            hostsn.add(channelname);
            String hostsnormal = "";

            Iterator itr = hosts.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry pairs = (Map.Entry) itr.next();
                hostsnormal += pairs.getKey() + ",";
            }

            Config.setProperty(this.channel,"hostnormal", hostsnormal);
        } else if (priority == 2) {
            hostsp.add(channelname);
            String hostspriority = "";

            Iterator itr2 = hosts.entrySet().iterator();

            while (itr2.hasNext()) {
                Map.Entry pairs2 = (Map.Entry) itr2.next();
                hostspriority += pairs2.getKey()  + ",";
            }

            Config.setProperty(this.channel,"hostpriority", hostspriority);
        }


    }

    public void removeHost(String key) {
        if (hosts.containsKey(key)) {
            int priority = hosts.get(key);
            hosts.remove(key);

            if (priority == 1) {
                hostsn.remove(key);
                String hostsnormal = "";

                for (String s : hostsn) {
                    hostsnormal += s + ",";
                }

                if (hostsnormal.equalsIgnoreCase(","))
                    hostsnormal = "";

                Config.setProperty(this.channel,"hostnormal", hostsnormal);
            } else if (priority == 2) {
                hostsp.remove(key);
                String hostspriority = "";

                for (String ss : hostsp) {
                    hostspriority += ss + ",";
                }

                if (hostspriority.equalsIgnoreCase(","))
                    hostspriority = "";

                Config.setProperty(this.channel,"hostpriority", hostspriority);
            }

        }
    }

    public ArrayList<String> getHostsp() {
        return hostsp;
    }

    public ArrayList<String> getHostsn() {
        return hostsn;
    }

    public void setLastHosted(String lh) {
        lastHosted = lh;
    }

    public String getLastHosted() {
        return lastHosted;
    }


    public String getChannelToHost() {
        Collections.shuffle(hostsp);
        Collections.shuffle(hostsn);
        boolean found = false;
        String target = "";
        outer: while (!found) {
            for (String s : hostsp) {
                if (!JSONUtil.krakenIsLive(s))
                    continue;
                else if (s.equalsIgnoreCase(this.getLastHosted()))
                    continue;
                else {
                    target = s;
                    found = true;
                    System.out.println("Found the channel: " + s);
                    break outer;
                }    
            }
            for (String ss : hostsn) {
                if (!JSONUtil.krakenIsLive(ss))
                    continue;
                else if (ss.equalsIgnoreCase(this.getLastHosted()))
                    continue;
                else {
                    target = ss;
                    found = true;
                    System.out.println("Found the channel: " + ss);
                    break outer;
                }
            }
           found = true;  
        }
        return target;
    }

    // #################################################

    public String getClickToTweetFormat() {
        return clickToTweetFormat;
    }

    public void setClickToTweetFormat(String string) {
        clickToTweetFormat = string;
        Config.setProperty(this.channel,"clickToTweetFormat", clickToTweetFormat);
    }

    public int getWarningCount(String name, FilterType type) {
        if (warningCount.containsKey(name.toLowerCase()) && warningCount.get(name.toLowerCase()).containsKey(type))
            return warningCount.get(name.toLowerCase()).get(type);
        else
            return 0;
    }

    public void incWarningCount(String name, FilterType type) {
        clearWarnings();
        synchronized (warningCount) {
            if (warningCount.containsKey(name.toLowerCase())) {
                if (warningCount.get(name.toLowerCase()).containsKey(type)) {
                    warningCount.get(name.toLowerCase()).put(type, warningCount.get(name.toLowerCase()).get(type) + 1);
                    warningTime.put(name.toLowerCase(), getTime());
                } else {
                    warningCount.get(name.toLowerCase()).put(type, 1);
                    warningTime.put(name.toLowerCase(), getTime());
                }
            } else {
                warningCount.put(name.toLowerCase(), new EnumMap<FilterType, Integer>(FilterType.class));
                warningCount.get(name.toLowerCase()).put(type, 1);
                warningTime.put(name.toLowerCase(), getTime());
            }
        }
    }

    public void clearWarnings() {
        List<String> toRemove = new ArrayList<String>();
        synchronized (warningTime) {
            synchronized (warningCount) {
                long time = getTime();
                for (Map.Entry<String, Long> entry : warningTime.entrySet()) {
                    if ((time - entry.getValue()) > 3600) {
                        toRemove.add((String) entry.getKey());
                    }
                }
                for (String name : toRemove) {
                    warningCount.remove(name);
                    warningTime.remove(name);
                }
            }
        }
    }

    private void registerCommandUsage(String command) {
        synchronized (commandCooldown) {
            System.out.println("DEBUG: Adding command " + command + " to cooldown list");
            commandCooldown.put(command.toLowerCase(), getTime());
        }
    }

    private void registerRobUsage(String username) {
        synchronized (robCooldown) {
            System.out.println("DEBUG: Adding cooldown for " + username + " on !rob");
            robCooldown.put(username.toLowerCase(), getTime());
        }
    }

    public void printRobCooldown() {
        for (Map.Entry<String, Long> entry : robCooldown.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    public String removeRobCooldown(String username) {
        username = username.toLowerCase();
        if (robCooldown.containsKey(username)) {
            robCooldown.remove(username);
            System.out.println("Removing username from cd list");
            return "Cooldown for " + username + " reset successfully";
        }
        else {
            long cooldown = 1;
            robCooldown.put(username, cooldown);
            System.out.println("Setting cooldown to 1");
            return "Cooldown for " + username + " reset successfully";
        }
    }

    public boolean onCooldownRob(String username) {
        username = username.toLowerCase();
        if (robCooldown.containsKey(username)) {
            long lastUse = robCooldown.get(username);
            if ((getTime() - lastUse) > 3600) {
                //Over
                System.out.println("DEBUG: Cooldown for username: " + username + " is over");
                registerRobUsage(username);
                return false;
            } else {
                //Not Over
                System.out.println("DEBUG: Cooldown for username: " + username + " is NOT over");
                return true;
            }
        } else {
            registerRobUsage(username);
            return false;
        }
    }

    public String getRobCooldown(String username) {
        username = username.toLowerCase();
        if (robCooldown.containsKey(username)) {
        long lastUse = robCooldown.get(username);
        if ((getTime() - lastUse) < 3600 && (getTime() - lastUse) > 0) {
            int robcd = (int) (getTime() - lastUse) / 60;
            robcd = 60 - robcd;
            if (robcd > 1)
                return username + " -> You have " + robcd + " minutes before you can !rob again";
            else
                return username + " -> You have less than 1 minute before you can !rob again";
            }
            } else {
                return username + " -> You can !rob anytime";
            }
                return username + " -> You can !rob anytime";
        }

    public boolean onCooldown(String command) {
        command = command.toLowerCase();
        if (commandCooldown.containsKey(command)) {
            long lastUse = commandCooldown.get(command);
            if ((getTime() - lastUse) > 10) {
                //Over
                System.out.println("DEBUG: Cooldown for " + command + " is over");
                registerCommandUsage(command);
                return false;
            } else {
                //Not Over
                System.out.println("DEBUG: Cooldown for " + command + " is NOT over");
                return true;
            }
        } else {
            registerCommandUsage(command);
            return false;
        }
    }

    public void reload() {
        BotManager.getInstance().removeChannel(channel);
        BotManager.getInstance().addChannel(channel, mode);
    }

    private void setDefaults() {

        //defaults.put("channel", channel);
        defaults.put("filterCaps", false);
        defaults.put("filterOffensive", true);
        defaults.put("filterCapsPercent", 50);
        defaults.put("filterCapsMinCharacters", 0);
        defaults.put("filterCapsMinCapitals", 6);
        defaults.put("filterLinks", false);
        defaults.put("filterCaule", true);
        defaults.put("filterEmotes", false);
        defaults.put("filterSymbols", false);
        defaults.put("filterEmotesMax", 4);
        defaults.put("topic", "");
        defaults.put("commandsKey", "");
        defaults.put("commandsValue", "");
        defaults.put("commandsRepeatKey", "");
        defaults.put("commandsRepeatDelay", "");
        defaults.put("commandsRepeatDiff", "");
        defaults.put("commandsRepeatActive", "");
        defaults.put("commandsScheduleKey", "");
        defaults.put("commandsSchedulePattern", "");
        defaults.put("commandsScheduleDiff", "");
        defaults.put("commandsScheduleActive", "");
        defaults.put("autoReplyTriggers", "");
        defaults.put("autoReplyResponse", "");
        defaults.put("hostpriority", "");
        defaults.put("enablewr", false);
        defaults.put("enableQuoteGame", true);
        defaults.put("enableLove", false);
        defaults.put("enableFollowage", false);
        defaults.put("enableSubWhisper", false);
        defaults.put("enableVideoNovo", false);
        defaults.put("subWhisperMessage", "");
        defaults.put("resubWhisperMessage", "");
        defaults.put("hostnormal", "");
        defaults.put("enableHost", false);
        defaults.put("regulars", "");
        defaults.put("moderators", "");
        defaults.put("owners", "");
        defaults.put("admins", "");
        defaults.put("quotebans", "");
        defaults.put("useTopic", true);
        defaults.put("useFilters", false);
        defaults.put("enableThrow", true);
        defaults.put("enablePoints", false);
        defaults.put("enableStats", false);
        defaults.put("enableRoulette", false);
        defaults.put("enable8ball", false);
        defaults.put("enableMusic", false);
        defaults.put("enableLeague", false);
        defaults.put("summoner", "");
        defaults.put("smurf1", "");
        defaults.put("smurf2", "");
        defaults.put("smurf3", "");
        defaults.put("smurf4", "");
        defaults.put("region", "");
        defaults.put("pointsName", "points");
        defaults.put("pointsAmount", 1);
        defaults.put("pointsDelay", 1);
        defaults.put("enableQuote", false);
        defaults.put("quoteModsOnly", false);
        defaults.put("permittedDomains", "");
        defaults.put("signKicks", false);
        defaults.put("enableUptime", true);
        defaults.put("topicTime", 0);
        defaults.put("linksTO", "please ask a moderator before posting links");
        defaults.put("symbolsTO", "please don't spam symbols");
        defaults.put("uptimeMessage", "streamer has been live for (_UPTIME_)");
        defaults.put("meTO", "/me is not allowed in this channel");
        defaults.put("maxTO", "please don't spam long messages");
        defaults.put("emoteTO", "please don't spam emotes");
        defaults.put("emotesingleTO", "single emote messages are not allowed");
        defaults.put("offensiveTO", "disallowed word or phrase");
        defaults.put("capsTO", "please don't shout or talk in all caps");
        defaults.put("mode", 2);
        defaults.put("announceJoinParts", false);
        defaults.put("lastfm", "");
        defaults.put("youtubeChannelId", "");
        defaults.put("timeZone", "US/Pacific");
        defaults.put("steamID", "");
        defaults.put("logChat", false);
        defaults.put("filterMaxLength", 500);
        defaults.put("offensiveWords", "");
        defaults.put("commercialLength", 30);
        defaults.put("filterColors", false);
        defaults.put("filterMe", false);
        defaults.put("staticChannel", false);
        defaults.put("enableWarnings", true);
        defaults.put("timeoutDuration", 600);
        defaults.put("rouletteTO", 600);
        defaults.put("clickToTweetFormat", "Checkout (_CHANNEL_URL_) playing (_GAME_) on @TwitchTV");
        defaults.put("filterSymbolsPercent", 50);
        defaults.put("filterSymbolsMin", 5);
        defaults.put("commandPrefix", "!");
        defaults.put("commandRestrictions", "");
        defaults.put("commandCounts", "");
        defaults.put("emoteSet", "");
        defaults.put("subscriberRegulars", false);
        defaults.put("sIgnoreEmotes", false);
        defaults.put("sIgnoreCaule", false);
        defaults.put("vIgnoreCaule", false);
        defaults.put("sIgnoreSymbols", false);
        defaults.put("sIgnoreLinks", false);
        defaults.put("sIgnoreCaps", false);
        defaults.put("vIgnoreEmotes", false);
        defaults.put("vIgnoreSymbols", false);
        defaults.put("vIgnoreLinks", false);
        defaults.put("vIgnoreCaps", false);
        defaults.put("vipRegulars", false);
        defaults.put("filterEmotesSingle", false);
        defaults.put("subMessage", "(_1_) has subscribed!");
        defaults.put("resubMessage", "(_1_) has resubscribed for (_2_) months in a row!");
        defaults.put("subscriberAlert", false);
        defaults.put("banPhraseSeverity", 99);
        defaults.put("globalFilter", true);

        /*
        Iterator it = defaults.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String key = String.valueOf(pairs.getKey());
            String value = String.valueOf(pairs.getValue());
            if (!config.containsKey(key))
                config.setString(key, value);
        }
        */
        Config.setDefaultProperties(channel, defaults);
    }

    private void _loadProperties(String name) {
        //_prop.put("filterCaps", filterCaps);
        //_prop.put("filterOffensive", true);
        /*
        _prop.put("filterCapsPercent", filterCapsPercent);
        _prop.put("filterCapsMinCharacters", filterCapsMinCharacters);
        _prop.put("filterCapsMinCapitals", filterCapsMinCapitals);
        _prop.put("filterLinks", filterLinks);
        _prop.put("filterCaule", filterCaule);
        _prop.put("filterEmotes", filterEmotes);
        _prop.put("filterSymbols", filterSymbols);
        _prop.put("filterEmotesMax", filterEmotesMax);
        _prop.put("topic", topic);
        _prop.put("commandsKey", config.getString("commandsKey"));
        _prop.put("commandsValue", config.getString("commandsValue"));
        _prop.put("commandsRepeatKey", config.getString("commandsRepeatKey"));
        _prop.put("commandsRepeatDelay", config.getString("commandsRepeatDelay"));
        _prop.put("commandsRepeatDiff", config.getString("commandsRepeatDiff"));
        _prop.put("commandsRepeatActive", config.getString("commandsRepeatActive"));
        _prop.put("commandsScheduleKey", config.getString("commandsScheduleKey"));
        _prop.put("commandsSchedulePattern", config.getString("commandsSchedulePattern"));
        _prop.put("commandsScheduleDiff", config.getString("commandsScheduleDiff"));
        _prop.put("commandsScheduleActive", config.getString("commandsScheduleActive"));
        _prop.put("autoReplyTriggers", config.getString("autoReplyTriggers"));
        _prop.put("autoReplyResponse", config.getString("autoReplyResponse"));
        _prop.put("hostpriority", "hostpriority");
        _prop.put("enablewr", config.getBoolean("enablewr"));
        _prop.put("enableQuoteGame", true);
        _prop.put("enableLove", false);
        _prop.put("enableFollowage", config.getBoolean("enableFollowage"));
        _prop.put("enableSubWhisper", config.getBoolean("enableSubWhisper"));
        _prop.put("enableVideoNovo", config.getBoolean("enableVideoNovo"));
        _prop.put("subWhisperMessage", config.getString("subWhisperMessage"));
        _prop.put("hostnormal", "");
        _prop.put("enableHost", false);
        _prop.put("regulars", "");
        _prop.put("moderators", "");
        _prop.put("owners", config.getString("owners"));
        _prop.put("admins", "");
        _prop.put("quotebans", "");
        _prop.put("useTopic", true);
        _prop.put("useFilters", config.getBoolean("useFilters"));
        */
        //_prop.put("enableThrow", config.getBoolean("enableThrow"));
        /*
        _prop.put("enablePoints", false);
        _prop.put("enableStats", false);
        */
        //_prop.put("enableRoulette", config.getBoolean("enableRoulette"));
        /*
        _prop.put("enable8ball", config.getBoolean("enable8ball"));
        _prop.put("enableMusic", config.getBoolean("enableMusic"));
        _prop.put("enableLeague", config.getBoolean("enableLeague"));
        _prop.put("summoner", config.getString("summoner"));
        _prop.put("smurf1", config.getString("smurf1"));
        _prop.put("smurf2", config.getString("smurf2"));
        _prop.put("smurf3", config.getString("smurf3"));
        _prop.put("smurf4", config.getString("smurf4"));
        _prop.put("region", config.getString("region"));
        _prop.put("pointsName", "points");
        _prop.put("pointsAmount", 1);
        _prop.put("pointsDelay", 1);
        _prop.put("enableQuote", config.getBoolean("enableQuote"));
        _prop.put("quoteModsOnly", config.getBoolean("quoteModsOnly"));
        _prop.put("permittedDomains", config.getString("permittedDomains"));
        _prop.put("signKicks", false);
        _prop.put("topicTime", 0);
        _prop.put("linksTO", config.getString("linksTO"));
        _prop.put("symbolsTO", config.getString("symbolsTO"));
        _prop.put("meTO", config.getString("meTO"));
        _prop.put("maxTO", config.getString("maxTO"));
        _prop.put("emoteTO", config.getString("emoteTO"));
        _prop.put("emotesingleTO", config.getString("emotesingleTO"));
        _prop.put("offensiveTO", config.getString("offensiveTO"));
        _prop.put("capsTO", config.getString("capsTO"));
        _prop.put("mode", config.getInt("mode"));
        _prop.put("announceJoinParts", false);
        _prop.put("lastfm", config.getString("lastfm"));
        _prop.put("youtubeChannelId", config.getString("youtubeChannelId"));
        _prop.put("timeZone", config.getString("timeZone"));
        _prop.put("steamID", config.getString("steamID"));
        _prop.put("logChat", false);
        _prop.put("filterMaxLength", 500);
        _prop.put("offensiveWords", config.getString("offensiveWords"));
        _prop.put("commercialLength", 30);
        _prop.put("filterColors", false);
        _prop.put("filterMe", false);
        _prop.put("staticChannel", false);
        _prop.put("enableWarnings", true);
        _prop.put("timeoutDuration", 600);
        _prop.put("rouletteTO", 600);
        _prop.put("clickToTweetFormat", "Checkout (_CHANNEL_URL_) playing (_GAME_) on @TwitchTV");
        _prop.put("filterSymbolsPercent", 50);
        _prop.put("filterSymbolsMin", 5);
        _prop.put("commandPrefix", "!");
        _prop.put("commandRestrictions", config.getString("commandRestrictions"));
        _prop.put("commandCounts", "");
        _prop.put("emoteSet", "");
        _prop.put("subscriberRegulars", config.getBoolean("subscriberRegulars"));
        _prop.put("sIgnoreEmotes", config.getBoolean("sIgnoreEmotes"));
        _prop.put("sIgnoreCaule", config.getBoolean("sIgnoreCaule"));
        _prop.put("vIgnoreCaule", config.getBoolean("vIgnoreCaule"));
        _prop.put("sIgnoreSymbols", config.getBoolean("sIgnoreSymbols"));
        _prop.put("sIgnoreLinks", config.getBoolean("sIgnoreLinks"));
        _prop.put("sIgnoreCaps", config.getBoolean("sIgnoreCaps"));
        _prop.put("vIgnoreEmotes", config.getBoolean("vIgnoreEmotes"));
        _prop.put("vIgnoreSymbols", config.getBoolean("vIgnoreSymbols"));
        _prop.put("vIgnoreLinks", config.getBoolean("vIgnoreLinks"));
        _prop.put("vIgnoreCaps", config.getBoolean("vIgnoreCaps"));
        _prop.put("vipRegulars", config.getBoolean("vipRegulars"));
        _prop.put("filterEmotesSingle", false);
        _prop.put("subMessage", config.getString("subMessage"));
        _prop.put("resubMessage", config.getString("resubMessage"));
        _prop.put("subscriberAlert", config.getBoolean("subscriberAlert"));
        _prop.put("banPhraseSeverity", 99);
        _prop.put("globalFilter", true);
        */

        Config._setDefaultProperties(channel, _prop);
    }

    private void _loadCommands(String name) {
        Map<String, String> toLoad = new HashMap<>();
        String rawKeys = "";
        String rawValues = "";
        rawKeys = Config.getProperty(channel, "commandsKey");
        System.out.println("Commands keys: " + rawKeys);
        String[] _commandKeys = rawKeys.split(",");
        rawValues = Config.getProperty(channel, "commandsValue");
        System.out.println("Command values: " + rawValues);
        String[] _commandsValue = rawValues.split(",,");
        //String[] _commandsValue = Config.getProperty(channel, "commandsValue").split(",,");
        //System.out.println("Command values: " + _commandsValue.toString());

        for (int i = 0; i < _commandKeys.length; i++) {
            if (_commandKeys[i].length() > 1)
                toLoad.put(_commandKeys[i].replaceAll("[^a-zA-Z0-9]", "").toLowerCase(), _commandsValue[i]);
        }
        System.out.println("Full map:" +  toLoad.toString());
        Config._setCommands(channel, toLoad);

    }

    private void loadProperties(String name) {
        setDefaults();
        cProperties = Config.getProperties(channel);

        //channel = config.getString("channel");
        filterCaps = Boolean.parseBoolean(cProperties.get("filterCaps"));
        filterCapsPercent = Integer.parseInt(cProperties.get("filterCapsPercent"));
        filterCapsMinCharacters = Integer.parseInt(cProperties.get("filterCapsMinCharacters"));
        filterCapsMinCapitals = Integer.parseInt(cProperties.get("filterCapsMinCapitals"));
        filterLinks = Boolean.parseBoolean(cProperties.get("filterLinks"));
        filterCaule = Boolean.parseBoolean(cProperties.get("filterCaule"));
        filterOffensive = Boolean.parseBoolean(cProperties.get("filterOffensive"));
        filterEmotes = Boolean.parseBoolean(cProperties.get("filterEmotes"));

        filterSymbols = Boolean.parseBoolean(cProperties.get("filterSymbols"));
        filterSymbolsPercent = Integer.parseInt(cProperties.get("filterSymbolsPercent"));
        filterSymbolsMin = Integer.parseInt(cProperties.get("filterSymbolsMin"));

        filterEmotesMax = Integer.parseInt(cProperties.get("filterEmotesMax"));
        filterEmotesSingle = Boolean.parseBoolean(cProperties.get("filterEmotesSingle"));
        //announceJoinParts = Boolean.parseBoolean(config.getString("announceJoinParts"));
        announceJoinParts = false;
        topic = cProperties.get("topic");

        symbolsTO = cProperties.get("symbolsTO");
        linksTO = cProperties.get("linksTO");
        meTO = cProperties.get("meTO");
        capsTO = cProperties.get("capsTO");
        maxTO = cProperties.get("maxTO");
        offensiveTO = cProperties.get("offensiveTO");
        emoteTO = cProperties.get("emoteTO");
        emoteSingleTO = cProperties.get("emotesingleTO");
        topicTime = Integer.parseInt(cProperties.get("topicTime"));
        useTopic = Boolean.parseBoolean(cProperties.get("useTopic"));
        useFilters = Boolean.parseBoolean(cProperties.get("useFilters"));
        enableThrow = Boolean.parseBoolean(cProperties.get("enableThrow"));
        enablePoints = Boolean.parseBoolean(cProperties.get("enablePoints"));
        enableStats = Boolean.parseBoolean(cProperties.get("enableStats"));
        enableRoulette = Boolean.parseBoolean(cProperties.get("enableRoulette"));
        enable8ball = Boolean.parseBoolean(cProperties.get("enable8ball"));
        enableMusic = Boolean.parseBoolean(cProperties.get("enableMusic"));
        enableLeague = Boolean.parseBoolean(cProperties.get("enableLeague"));
        enableWR = Boolean.parseBoolean(cProperties.get("enableWR"));
        enableQuoteGame = Boolean.parseBoolean(cProperties.get("enableQuoteGame"));
        enableLove = Boolean.parseBoolean(cProperties.get("enableLove"));
        enableFollowage = Boolean.parseBoolean(cProperties.get("enableFollowage"));
        subWhisperMessage = cProperties.get("subWhisperMessage");
        resubWhisperMessage = cProperties.get("resubWhisperMessage");
        uptimeMessage = cProperties.get("uptimeMessage");
        enableSubWhisper = Boolean.parseBoolean(cProperties.get("enableSubWhisper"));
        enableVideoNovo = Boolean.parseBoolean(cProperties.get("enableVideoNovo"));
        summoner = cProperties.get("summoner");
        smurf1 = cProperties.get("smurf1");
        smurf2 = cProperties.get("smurf2");
        smurf3 = cProperties.get("smurf3");
        smurf4 = cProperties.get("smurf4");
        region = cProperties.get("region");
        pointsName = cProperties.get("pointsName");
        pointsAmount = Integer.parseInt(cProperties.get("pointsAmount"));
        pointsDelay = Integer.parseInt(cProperties.get("pointsDelay"));
        enableHost = Boolean.parseBoolean(cProperties.get("enableHost"));
        enableQuote = Boolean.parseBoolean(cProperties.get("enableQuote"));
        quoteModsOnly = Boolean.parseBoolean(cProperties.get("quoteModsOnly"));
        signKicks = Boolean.parseBoolean(cProperties.get("signKicks"));
        enableUptime = Boolean.parseBoolean(cProperties.get("enableUptime"));
        lastfm = cProperties.get("lastfm");
        youtubeChannelId = cProperties.get("youtubeChannelId");
        timeZone = cProperties.get("timeZone");
        steamID = cProperties.get("steamID");
        logChat = Boolean.parseBoolean(cProperties.get("logChat"));
        mode = Integer.parseInt(cProperties.get("mode"));
        filterMaxLength = Integer.parseInt(cProperties.get("filterMaxLength"));
        commercialLength = Integer.parseInt(cProperties.get("commercialLength"));
        filterColors = Boolean.parseBoolean(cProperties.get("filterColors"));
        filterMe = Boolean.parseBoolean(cProperties.get("filterMe"));
        staticChannel = Boolean.parseBoolean(cProperties.get("staticChannel"));
        clickToTweetFormat = cProperties.get("clickToTweetFormat");

        enableWarnings = Boolean.parseBoolean(cProperties.get("enableWarnings"));
        timeoutDuration = Integer.parseInt(cProperties.get("timeoutDuration"));
        rouletteTO = Integer.parseInt(cProperties.get("rouletteTO"));
        prefix = cProperties.get("commandPrefix").charAt(0) + "";
        emoteSet = cProperties.get("emoteSet");
        subscriberRegulars =  Boolean.parseBoolean(cProperties.get("subscriberRegulars"));
        sIgnoreEmotes = Boolean.parseBoolean(cProperties.get("sIgnoreEmotes"));
        sIgnoreCaule = Boolean.parseBoolean(cProperties.get("sIgnoreCaule"));
        sIgnoreSymbols = Boolean.parseBoolean(cProperties.get("sIgnoreSymbols"));
        sIgnoreLinks = Boolean.parseBoolean(cProperties.get("sIgnoreLinks"));
        sIgnoreCaps = Boolean.parseBoolean(cProperties.get("sIgnoreCaps"));
        vIgnoreEmotes = Boolean.parseBoolean(cProperties.get("vIgnoreEmotes"));
        vIgnoreSymbols = Boolean.parseBoolean(cProperties.get("vIgnoreSymbols"));
        vIgnoreLinks = Boolean.parseBoolean(cProperties.get("vIgnoreLinks"));
        vIgnoreCaps = Boolean.parseBoolean(cProperties.get("vIgnoreCaps"));
        vIgnoreCaule = Boolean.parseBoolean(cProperties.get("vIgnoreCaule"));
        vipRegulars = Boolean.parseBoolean(cProperties.get("vipRegulars"));

        String[] hostsprio = cProperties.get("hostpriority").split(",");
        String[] hostsnorm = cProperties.get("hostnormal").split(",");
        for (int i = 0; i < hostsprio.length; i++) {
            if (hostsprio[i].length() > 1)
                hostsp.add(hostsprio[i].replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
        }
        for (int i = 0; i < hostsnorm.length; i++) {
            if (hostsnorm[i].length() > 1)
                hostsn.add(hostsnorm[i].replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
        }

        String[] commandsKey = cProperties.get("commandsKey").split(",");
        String[] commandsValue = cProperties.get("commandsValue").split(",,");

        for (int i = 0; i < commandsKey.length; i++) {
            if (commandsKey[i].length() > 1) {
                commands.put(commandsKey[i].replaceAll("[^a-zA-Z0-9]", "").toLowerCase(), commandsValue[i]);
            }
        }

        String[] commandC = cProperties.get("commandCounts").split(",");
        for (int i = 0; i < commandC.length; i++) {
            if (commandC[i].length() > 0) {
                String[] parts = commandC[i].split("\\|");
                commandsCount.put(parts[0], Integer.parseInt(parts[1]));
            }
        }

        String[] commandR = cProperties.get("commandRestrictions").split(",");
        for (int i = 0; i < commandR.length; i++) {
            if (commandR[i].length() > 1) {
                String[] parts = commandR[i].split("\\|");
                commandsRestrictions.put(parts[0], Integer.parseInt(parts[1]));
            }
        }

        String[] commandsRepeatKey = cProperties.get("commandsRepeatKey").split(",");
        String[] commandsRepeatDelay = cProperties.get("commandsRepeatDelay").split(",");
        String[] commandsRepeatDiff = cProperties.get("commandsRepeatDiff").split(",");
        System.out.println(commandsRepeatDiff.toString());
        String[] commandsRepeatActive = cProperties.get("commandsRepeatActive").split(",");


        for (int i = 0; i < commandsRepeatKey.length; i++) {
            if (commandsRepeatKey[i].length() > 1) {
                RepeatCommand rc = new RepeatCommand(channel, commandsRepeatKey[i].replaceAll("[^a-zA-Z0-9]", ""), Integer.parseInt(commandsRepeatDelay[i]), Integer.parseInt(commandsRepeatDiff[i]), Boolean.parseBoolean(commandsRepeatActive[i]));
                commandsRepeat.put(commandsRepeatKey[i].replaceAll("[^a-zA-Z0-9]", ""), rc);
            }
        }

        String[] commandsScheduleKey = cProperties.get("commandsScheduleKey").split(",,");
        String[] commandsSchedulePattern = cProperties.get("commandsSchedulePattern").split(",,");
        String[] commandsScheduleDiff = cProperties.get("commandsScheduleDiff").split(",,");
        String[] commandsScheduleActive = cProperties.get("commandsScheduleActive").split(",,");


        for (int i = 0; i < commandsScheduleKey.length; i++) {
            if (commandsScheduleKey[i].length() > 1) {
                ScheduledCommand rc = new ScheduledCommand(channel, commandsScheduleKey[i].replaceAll("[^a-zA-Z0-9]", ""), commandsSchedulePattern[i], Integer.parseInt(commandsScheduleDiff[i]), Boolean.parseBoolean(commandsScheduleActive[i]));
                commandsSchedule.put(commandsScheduleKey[i].replaceAll("[^a-zA-Z0-9]", ""), rc);
            }
        }

        String[] autoReplyTriggersString = cProperties.get("autoReplyTriggers").split(",,");
        String[] autoReplyResponseString = cProperties.get("autoReplyResponse").split(",,");

        for (int i = 0; i < autoReplyTriggersString.length; i++) {
            if (autoReplyTriggersString[i].length() > 0) {
                autoReplyTrigger.add(Pattern.compile(autoReplyTriggersString[i], Pattern.CASE_INSENSITIVE));
                autoReplyResponse.add(autoReplyResponseString[i]);
            }
        }

        String[] regularsRaw = cProperties.get("regulars").split(",");
        synchronized (regulars) {
            for (int i = 0; i < regularsRaw.length; i++) {
                if (regularsRaw[i].length() > 1) {
                    regulars.add(regularsRaw[i].toLowerCase());
                }
            }
        }

        String[] moderatorsRaw = cProperties.get("moderators").split(",");
        synchronized (moderators) {
            for (int i = 0; i < moderatorsRaw.length; i++) {
                if (moderatorsRaw[i].length() > 1) {
                    moderators.add(moderatorsRaw[i].toLowerCase());
                }
            }
        }

        String[] ownersRaw = cProperties.get("owners").split(",");
        synchronized (owners) {
            for (int i = 0; i < ownersRaw.length; i++) {
                if (ownersRaw[i].length() > 1) {
                    owners.add(ownersRaw[i].toLowerCase());
                }
            }
        }
        
        String[] adminsRaw = cProperties.get("admins").split(",");
        synchronized (admins) {
            for (int i = 0; i < adminsRaw.length; i++) {
                if (adminsRaw[i].length() > 1)
                    admins.add(adminsRaw[i].toLowerCase());
            }
        }
        
        String[] quoteBansRaw = cProperties.get("quotebans").split(",");
        synchronized (quotebans) {
            for (int i = 0; i < quoteBansRaw.length; i++) {
                if (quoteBansRaw[i].length() > 1)
                    quotebans.add(quoteBansRaw[i].toLowerCase());
            }
        }

        String[] domainsRaw = cProperties.get("permittedDomains").split(",");
        synchronized (permittedDomains) {
            for (int i = 0; i < domainsRaw.length; i++) {
                if (domainsRaw[i].length() > 1) {
//                  permittedDomains.add(domainsRaw[i].toLowerCase().replaceAll("\\.", "\\\\."));
                    permittedDomains.add(domainsRaw[i].toLowerCase());

                }
            }
        }
        System.out.println(cProperties.get("offensiveWords"));
        String[] offensiveWordsRaw = cProperties.get("offensiveWords").split(",,");
        synchronized (offensiveWords) {
            synchronized (offensiveWordsRegex) {
                for (int i = 0; i < offensiveWordsRaw.length; i++) {
                    if (offensiveWordsRaw[i].length() > 1) {
                        String w = offensiveWordsRaw[i];
                        offensiveWords.add(w);
                        if (w.startsWith("REGEX:")) {
                            String line = w.substring(6);
                            System.out.println("Adding: " + line);
                            Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
                            offensiveWordsRegex.add(tempP);
                        } else {
                            String line = "(?i).*" + Pattern.quote(w) + ".*";
                            System.out.println("Adding: " + line);
                            Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
                            offensiveWordsRegex.add(tempP);
                        }

                    }
                }
            }

        }

    }

    public void setMode(int mode) {
        this.mode = mode;
        Config.setProperty(this.channel,"mode", String.valueOf(this.mode));

        if (mode == -1) {
            this.setFiltersFeature(true);
            this.setFilterEmotes(false);
            this.setFilterEmotesMax(5);
            this.setFilterSymbols(true);
            this.setFilterCaps(false);
            this.setFilterLinks(false);
            this.setFilterCaule(false);
            this.setFilterOffensive(true);
            this.setSignKicks(false);
            this.setTopicFeature(false);
            this.setThrow(false);
            this.setRoulette(false);
            this.setMusic(false);
            this.setQuote(false);
            this.setQuoteMods(false);
        }
    }

    public int getMode() {
        return mode;
    }

    private long getTime() {
        return (System.currentTimeMillis() / 1000L);
    }

    public void runCommercial() {
        if (skipNextCommercial) {
            System.out.println("Commercial skipped via command.");
            skipNextCommercial = false;
            return;
        }

        if (JSONUtil.krakenIsLive(getChannel().substring(1))) {
            String dataIn = "";
            dataIn = BotManager.postRemoteDataTwitch("https://api.twitch.tv/kraken/channels/" + getChannel().substring(1) + "/commercial", "length=" + commercialLength, 2);

            System.out.println(dataIn);
        } else {
            System.out.println(getChannel().substring(1) + " is not live. Skipping commercial.");
        }
    }
}
