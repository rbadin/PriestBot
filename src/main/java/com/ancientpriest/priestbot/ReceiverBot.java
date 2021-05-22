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

import com.google.common.base.Splitter;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.commons.lang3.StringUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiverBot extends PircBot {
    
    static Logger LOGGER_D = LoggerFactory.getLogger("debugLogger");
    static Logger LOGGER_R = LoggerFactory.getLogger("recordLogger");

    static ReceiverBot instance;
    Timer joinCheck;
    Timer messageUpdater;
    Random random = new Random();
    private Pattern[] linkPatterns = new Pattern[4];
    private Pattern[] symbolsPatterns = new Pattern[2];
    private int lastPing = -1;
    private char bullet[] = {'>', '+', '-', '~'};
    private int bulletPos = 0;
    private int messageCount;
    private int messagesProcessed;
    private int messagesPerMin;
    private int countToNewColor = BotManager.getInstance().randomNickColorDiff;
    private Pattern twitchnotifySubscriberPattern = Pattern.compile("^([a-z_]+) just subscribed!$", Pattern.CASE_INSENSITIVE);
    private Pattern banNoticePattern = Pattern.compile("^You are permanently banned from talking in ([a-z_]+).$", Pattern.CASE_INSENSITIVE);
    private Pattern toNoticePattern = Pattern.compile("^You are banned from talking in ([a-z_]+) for (?:[0-9]+) more seconds.$", Pattern.CASE_INSENSITIVE);
    private Pattern vinePattern = Pattern.compile(".*(vine|4).*(4|vine).*Google.*", Pattern.CASE_INSENSITIVE);
    //private Pattern youtubePattern = Pattern.compile("^(?:https?:\\/\\/)?(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*", Pattern.CASE_INSENSITIVE);
    private Pattern caulePattern = Pattern.compile(".*(░|█|ᅚ|⣿|⢾|⡏|¶|Ỏ̷͖͈ |▀|▌|▒|▓|ค|─|⠄| ้|╱|ส|⡀|▬|┃|╯).*", Pattern.CASE_INSENSITIVE);

    private Set<String> joinedChannels = new HashSet<String>();

    public ReceiverBot(String server, int port) {
        ReceiverBot.setInstance(this);
        linkPatterns[0] = Pattern.compile(".*http://.*", Pattern.CASE_INSENSITIVE);
        linkPatterns[1] = Pattern.compile(".*https://.*", Pattern.CASE_INSENSITIVE);
        linkPatterns[2] = Pattern.compile(".*[-A-Za-z0-9](\\.|\\(dot\\))([a-z]{2,})(\\W|$).*", Pattern.CASE_INSENSITIVE);
        linkPatterns[3] = Pattern.compile(".*(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\s+|:|/|$).*");

        symbolsPatterns[0] = Pattern.compile("(\\p{InPhonetic_Extensions}|\\p{InLetterlikeSymbols}|\\p{InDingbats}|\\p{InBoxDrawing}|\\p{InBlockElements}|\\p{InGeometricShapes}|\\p{InHalfwidth_and_Fullwidth_Forms}|つ|°|ຈ|░|▀|▄|̰̦̮̠ę̟̹ͦͯͯ́ͮ̊̐͌̉͑ͨ̊́́̚|U̶̧ͩͭͧ͊̅̊ͥͩ̿̔̔ͥ͌ͬ͊͋ͬ҉|Ọ̵͇̖̖|A̴͍̥̳̠̞̹ͩ̋̆ͤͅ|E̡̛͚̺̖̪͈̲̻̠̰̳̐̿)");
        symbolsPatterns[1] = Pattern.compile("[!-/:-@\\[-`{-~]");

        this.setName(BotManager.getInstance().getInstance().nick);
        this.setLogin("ReceiverPriestBot");
        this.setMessageDelay(0);
        this.updateMessagesMin();

        this.setVerbose(BotManager.getInstance().verboseLogging);
        try {
            this.connect(server, port, BotManager.getInstance().getInstance().password, null);
        } catch (NickAlreadyInUseException e) {
            logMain("[ERROR] Nickname already in use - " + this.getNick() + " " + this.getServer());
        } catch (IOException e) {
            logMain("[ERROR] Unable to connect to server - " + this.getNick() + " " + this.getServer());
        } catch (IrcException e) {
            logMain("[ERROR] Error connecting to server - " + this.getNick() + " " + this.getServer());
        }

    }

    public static ReceiverBot getInstance() {
        return instance;
    }

    public static void setInstance(ReceiverBot rb) {
        if (instance == null) {
            instance = rb;
        }
    }

    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

        Node nValue = (Node) nlList.item(0);

        return nValue.getNodeValue();
    }

    private Channel getChannelObject(String channel) {
        
        if (!channel.startsWith("#")) 
            channel = BotManager.getInstance().tempWhisperChannel;
        
        System.out.println(channel);

        Channel channelInfo = null;
        channelInfo = BotManager.getInstance().getChannel(channel);
        return channelInfo;
    }

    @Override
    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        recipient = recipient.replace(":", "");
        //System.out.println("DEBUG: Got DEOP for " + recipient);
        this.getChannelObject(channel).tagModerators.remove(recipient);
    }
    
    @Override
    protected void onWhisper(String sender, String login, String hostname, String message, String tags) {
        onMessage(sender, sender, login, hostname, message, tags);
    }
    
    @Override
    protected void onUserNotice(String channel, String sender, String login, String hostname, String message, String tags) {
        //onMessage(sender, sender, login, hostname, message, tags);
        LOGGER_D.debug("Tags: " + tags);
        Map<String, String> tagMap = mapTags(tags);
        //String[] msg = message.trim().split(" ");
        Channel channelInfo = getChannelObject(channel);
        String username = (tagMap.get("display-name") != null ? tagMap.get("display-name") : "");
        String msg_id = (tagMap.get("msg-id") != null ? tagMap.get("msg-id") : "");
        String shouldShare = (tagMap.get("msg-param-should-share-streak") != null ? tagMap.get("msg-param-should-share-streak") : "0");

        switch(msg_id) {
            case "sub":
                onNewSubscriberWhisper(channelInfo, username);
                onNewSubscriber(channelInfo, username);
                break;
            case "resub":
                String months = (tagMap.get("msg-param-cumulative-months") != null ? tagMap.get("msg-param-cumulative-months") : "0");
                String streak = (tagMap.get("msg-param-streak-months") != null ? tagMap.get("msg-param-streak-months") : "0");
                if (shouldShare.equalsIgnoreCase("0")) {
                    onNewSubscriberWhisper(channelInfo, username, months);
                    onNewSubscriber(channelInfo, username, months);               
                } else {
                    onNewSubscriberWhisper(channelInfo, username, months, streak);
                    onNewSubscriber(channelInfo, username, months, streak);      
                }
                break;
            case "subgift":
                onNewSubscriberWhisper(channelInfo, tagMap.get("msg-param-recipient-user-name"));
                onNewSubscriber(channelInfo, tagMap.get("msg-param-recipient-user-name"));
                break;

            /*
                TODO: Insert other parsing for several usernotice commands
            */
            default:
                break;

        }
    }

    @Override
    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        recipient = recipient.replace(":", "");
        //System.out.println("DEBUG: Got OP for " + recipient);
        this.getChannelObject(channel).tagModerators.add(recipient);
    }

    @Override
    protected void onConnect() {
        //Force TMI to send USERCOLOR AND SPECIALUSER messages.
        //this.sendRawLine("TWITCHCLIENT 3");
        this.sendRawLine("CAP REQ :twitch.tv/tags twitch.tv/commands");
        
    }

    @Override
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        if (!message.startsWith("SPECIALUSER") && !message.startsWith("HISTORYEND") && !message.startsWith("CLEARCHAT") && !message.startsWith("Your color"))
            LOGGER_D.debug("RB PM: " + sender + " " + message);

        Matcher m = banNoticePattern.matcher(message);
        if (m.matches()) {
            String channel = "#" + m.group(1);
            BotManager.getInstance().log("SB: Detected ban in " + channel + ". Parting..");
            BotManager.getInstance().removeChannel(channel);
        }

        m = toNoticePattern.matcher(message);
        if (m.matches()) {
            String channel = "#" + m.group(1);
            BotManager.getInstance().log("SB: Detected timeout in " + channel + ". Parting..");
            BotManager.getInstance().removeChannel(channel);
        }

    }

    @Override
    protected void onAction(String sender, String login, String hostname, String target, String action, String tags) {
        this.onMessage(target, sender, login, hostname, "/me " + action, tags);
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message, String tags) {
        LOGGER_D.debug("Tags: " + tags);
        Map<String, String> tagMap = mapTags(tags);
//        Iterator it = tagMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pairs = (Map.Entry) it.next();
//            System.out.println("'" + pairs.getKey() + "' = '" + pairs.getValue() + "'");
//        }
        LOGGER_D.debug("Parameters:" + channel);
        LOGGER_D.debug("sender:" + sender);
        if (channel.charAt(0) != '#')
            onWhisperMessage(sender, message);
        else
            onChannelMessage(channel, sender, message, tagMap);
    }

    @Override
    protected void onUserState(String channel, String tags) {
        LOGGER_D.debug("Got USERSTATE '" + tags + "' for " + channel);

        Map<String, String> tagMap = mapTags(tags);

//        Iterator it = tagMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pairs = (Map.Entry) it.next();
//            System.out.println("'" + pairs.getKey() + "' = '" + pairs.getValue() + "'");
//        }
    }
    
    protected void onWhisperMessage(String sender, String message) {
        LOGGER_D.debug("Got whisper message");
        // this.sendWhisper(this.getName(), sender, message);
        boolean isAP = false;
        boolean isAdmin = false;
        
        if (sender.equalsIgnoreCase("lucas"))
            isAP = true;
        if (BotManager.getInstance().isAdmin(sender))
            isAdmin = true;
        
        
        String[] msg = message.trim().split(" ");
    }

    protected void onChannelMessage(String channel, String sender, String message, Map<String, String> tags) {
        if (!BotManager.getInstance().verboseLogging)
            logMain("MSG: " + channel + " " + sender + " : " + message);

        Channel channelInfo = getChannelObject(channel);
        String twitchName = channelInfo.getTwitchName();
        String prefix = channelInfo.getPrefix();
        messagesProcessed++;

        //Handle future administrative messages from JTV
        if (sender.equals("jtv")) {
            return;
        }

        if (!sender.equalsIgnoreCase(this.getNick()))
            channelInfo.messageCount++; //Inc message count
        else
            messageCount++; // Inc message count that the bot sends

        //Ignore messages from self.
        if (sender.equalsIgnoreCase(this.getNick())) {
            //System.out.println("Message from bot");
            return;
        }
        
        

        //Handle twitchnotify
        if (sender.equals("twitchnotify")) {
            String[] msg = message.trim().split(" ");
            if (msg[0].length() > 2 && !message.matches("(?i).*\\b(away|to)\\b.*")) {
                onNewSubscriberWhisper(channelInfo, msg[0]);
            }
            Matcher m = twitchnotifySubscriberPattern.matcher(message);
            if (m.matches()) {
                onNewSubscriber(channelInfo, m.group(1));
            }
        }
        
        //Handle youtube links
        /*
        if (message.contains("youtube") || message.contains("youtu.be")) {
            Matcher m = youtubePattern.matcher(message);
            if (m.matches()) {
                send(channel, JSONUtil.getYoutubeInfo(m.group(1)));
            }
        }
        */


        //Split message on spaces.
        String[] msg = message.trim().split(" ");

        // ********************************************************************************
        // ****************************** User Ranks **************************************
        // ********************************************************************************

        boolean isAP = false;
        boolean isSuperAdmin = false;
        boolean isAdmin = false;
        boolean isSubAdmin = false;
        boolean isBroadcaster = false;
        boolean isChannelAdmin = false;
        boolean isOwner = false;
        boolean isOp = false;
        boolean isVIP = false;
        boolean isSub = false;
        boolean isRegular = false;
        boolean isPleb = false;
        int accessLevel = 0;

        //Check for user level based on other factors.
        
        String v3_user_type = (tags.get("user-type") != null ? tags.get("user-type") : "user");
        String v3_subscriber = (tags.get("subscriber") != null ? tags.get("subscriber") : "0");
        String v3_user_id = (tags.get("user-id") != null ? tags.get("user-id") : "0");
        //String v3_sub_status = (tags.containsKey("subscriber") ? tags.get("subscriber") : "0");
        String v3_badges = (tags.get("badges") != null ? tags.get("badges") : "0");
        System.out.println(v3_badges);
        //String[] userBadges = v3_badges.split(",");
        String[] userBadges = v3_badges.replaceAll("[^a-zA-Z,]", "").split(",");
        System.out.println("userBadges:" + Arrays.toString(userBadges));
        for (String s : userBadges) {
            switch (s.toLowerCase()) {
                case "vip":
                    isVIP = true;
                    break;
                case "subscriber": case "founder":
                    isSub = true;
                    break;
                default:
                    break;
            }
        }
        
        LOGGER_D.debug("DEBUG: v3 user-type = " + v3_user_type);
        LOGGER_D.debug("DEBUG: v3_sub_status = " + v3_subscriber);
        
        if (sender.equalsIgnoreCase("lucas"))
            isAP = true;
        if (BotManager.getInstance().checkSuperAdmin(v3_user_id))
            isSuperAdmin = true;
        if (BotManager.getInstance().checkAdmin(v3_user_id))
            isAdmin = true;
        if (BotManager.getInstance().checkSubAdmin(v3_user_id))
            isSubAdmin = true;
 //       if (BotManager.getInstance().isTagAdmin(sender) || BotManager.getInstance().isTagStaff(sender) || BotManager.getInstance().isTagGlobalMod(sender) || v3_user_type.equals("admin") || v3_user_type.equals("staff") || v3_user_type.equals("global_mod"))
 //           isAdmin = true;
        if (channel.equalsIgnoreCase("#" + sender))
            isBroadcaster = true;
        if (channelInfo.isModerator(sender) || v3_user_type.equals("mod"))
            isOp = true;
        if (channelInfo.isOwner(sender))
            isOwner = true;
        if (channelInfo.isAdmin(sender))
            isChannelAdmin = true;
        //if (v3_subscriber.equals("1") || channelInfo.isSubscriber(sender))
        //    isSub = true;
        if (channelInfo.isRegular(sender))
            isRegular = true;

        if (isRegular)
            LOGGER_D.debug(sender + " is a Regular");

        //Give users all the ranks below them
        if (isAP) {
            LOGGER_D.debug(sender + " is AncientPriest");
            isAdmin = true;
            isSuperAdmin = true;
            isSubAdmin = true;
            isBroadcaster = true;
            isOwner = true;
            isChannelAdmin = true;
            isOp = true;
            isRegular = true;
            isVIP = true;
            isSub = true;
            accessLevel = 999;
        } else if (isSuperAdmin) {
            log("RB: " + sender + " is a superadmin.");
            isAdmin = true;
            isBroadcaster = true;
            isSubAdmin = true;
            isOwner = true;
            isChannelAdmin = true;
            isOp = true;
            isRegular = true;
            isVIP = true;
            isSub = true;
            accessLevel = 100;
        } else if (isAdmin) {
            log("RB: " + sender + " is admin.");
            isSubAdmin = true;
            isBroadcaster = true;
            isOwner = true;
            isChannelAdmin = true;
            isOp = true;
            isRegular = true;
            isSub = true;
            isVIP = true;
            accessLevel = 99;
        } else if (isSubAdmin) {
            log("RB: " + sender + " is a subadmin.");
            isOwner = true;
            isOp = true;
            isRegular = true;
            isSub = true;
            isVIP = true;
            accessLevel = 9;
        } else if (isBroadcaster) {
            log("RB: " + sender + " is the broadcaster.");
            isOwner = true;
            isChannelAdmin = true;
            isOp = true;
            isRegular = true;
            isSub = true;
            isVIP = true;
            accessLevel = 8;
        } else if (isChannelAdmin) {
            log("RB: " + sender + " is a channel admin.");
            isOwner = true;
            isOp = true;
            isRegular = true;
            isSub = true;
            isVIP = true;
            accessLevel = 7;
        } else if (isOwner) {
            log("RB: " + sender + " is owner.");
            isOp = true;
            isRegular = true;
            isSub = true;
            isVIP = true;
            accessLevel = 6;
        } else if (isOp) {
            log("RB: " + sender + " is op.");
            isRegular = true;
            isVIP = true;
            isSub = true;
            accessLevel = 5;
        } else if (isSub) {
          log ("RB: " + sender + " is a SUB.");
          if (channelInfo.subscriberRegulars)
                isRegular = true;
          else
                isRegular = false;
          accessLevel = 1;
          
        } else if (isVIP) {
            log("RB: " + sender + " is VIP.");
            if (channelInfo.vipRegulars)
                isRegular = true;
            else
                isRegular = false;
            accessLevel = 1;
        } else if (isRegular) {
            log("RB: " + sender + " is regular.");
            accessLevel = 1;
        } else {
            isPleb = true;
            log("RB: " + sender + " is NOT A SUB OR VIP OR MOD OR ANYTHING!");
        }
        
        //Logging admin message
        if (isSubAdmin && !isAP)
            Logging.logAdminMessage(channel, sender, message);
        
        //Logging admin command
        if (msg[0].substring(0, 1).equalsIgnoreCase(prefix) && (isSubAdmin && !isAP))
            Logging.logAdminCommand(channel, sender, msg[0].substring(1));
        


        //!{botname} command
        if (msg[0].equalsIgnoreCase(prefix + this.getName())) {
            if (msg.length >= 2) {

                String[] newMsg = new String[msg.length - 1];
                for (int i = 1; i < msg.length; i++) {
                    newMsg[i - 1] = msg[i];
                }
                msg = newMsg;
                msg[0] = prefix + msg[0];

                message = fuseArray(msg, 0);
                System.out.println("DEBUG: Command rewritten as " + message);
            }

        }

        //Impersonation command
        if (isAdmin && msg[0].equalsIgnoreCase(prefix + "imp")) {
            logImp(sender, msg[1], msg[2], message);
            if (msg.length >= 4) {
                channelInfo = getChannelObject("#" + msg[1]);
                twitchName = channelInfo.getTwitchName();
                String originalChannel = channel;
                channel = "#" + msg[2];
                
                String[] newMsg = new String[msg.length - 3];
                for (int i = 3; i < msg.length; i++) {
                    newMsg[i - 3] = msg[i];
                }
                msg = newMsg;
                

                message = fuseArray(msg, 0);
                send(originalChannel, "Impersonating channel " + channelInfo.getChannel() + " with command: " + message + " on channel: " + channel);
                LOGGER_D.debug("Impersonating channel " + channelInfo.getChannel() + " with command: " + message);
            }

        }


        //!leave - Owner
        if ((msg[0].equalsIgnoreCase(prefix + "leave") || msg[0].equalsIgnoreCase(prefix + "remove") || msg[0].equalsIgnoreCase(prefix + "part")) && isOwner) {
            send(channel, "Leaving channel " + channelInfo.getChannel() + ".");
            BotManager.getInstance().removeChannel(channelInfo.getChannel());
            return;
        }


        // ********************************************************************************
        // ********************************** Filters *************************************
        // ********************************************************************************

        //Global banned word filter
        if (!isOp && Boolean.parseBoolean(Config.getProperty(channel,"globalFilter")) && this.isGlobalBannedWord(message)) {
            //this.secondaryBan(channel, sender, FilterType.GLOBALBAN);
            this.secondaryTO(channel, sender, 604800, FilterType.GLOBALBAN, message);
            logMain("GLOBALBAN: Global banned word timeout: " + sender + " in " + channel + " : " + message);
            logGlobalBan(channel, sender, message);
            return;
        }

        // Voluntary Filters
        
        if (channelInfo.useFilters && !isOp) {

            if (!isRegular) {
                String normalMessage = org.apache.commons.lang3.StringUtils.stripAccents(message);
                Matcher m = vinePattern.matcher(normalMessage.replaceAll(" ", ""));
                if (m.find()) {
                    logMain("VINEBAN: " + sender + " in " + channel + " : " + message);
                    //this.secondaryBan(channel, sender, FilterType.VINE);
                    logGlobalBan(channel, sender, message);
                    return;
                }
            }
            
            if (channelInfo.getFilterCaule() && (isPleb || (isRegular && !channelInfo.subscriberRegulars) || (isSub && !channelInfo.sIgnoreCaule) || (isVIP && !channelInfo.vIgnoreCaule))) {
                Matcher m = caulePattern.matcher(message.replaceAll(" ", ""));
                if (m.find()) {
                    int warningCount = 0;
                    channelInfo.incWarningCount(sender, FilterType.CAULE);
                    warningCount = channelInfo.getWarningCount(sender, FilterType.CAULE);
                    this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.CAULE, message);
                    return;
                    //logMain("CAULEBAN: " + sender + " in " + channel + " : " + message);
                    //logGlobalBan(channel, sender, message);
                }
            }

            //Me filter
            if (channelInfo.getFilterMe() && !isRegular) {
                if (msg[0].equalsIgnoreCase("/me") || message.startsWith("\u0001ACTION")) {
                    int warningCount = 0;

                    channelInfo.incWarningCount(sender, FilterType.ME);
                    warningCount = channelInfo.getWarningCount(sender, FilterType.ME);
                    this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.ME, message);

                    if (channelInfo.checkSignKicks())
                        send(channel, sender + ", " + channelInfo.getMeTO() + " - " + this.getTimeoutText(warningCount, channelInfo));

                    return;

                }

            }
            
            

            // Cap filter
            if (channelInfo.getFilterCaps() && (isPleb || (isRegular && !channelInfo.subscriberRegulars) || (isSub && !channelInfo.sIgnoreCaps) || (isVIP && !channelInfo.vIgnoreCaps))) {
                String messageNoWS = message.replaceAll("\\s", "");
                int capsNumber = getCapsNumber(messageNoWS);
                double capsPercent = ((double) capsNumber / messageNoWS.length()) * 100;
                if (channelInfo.getFilterCaps() && !(isRegular) && message.length() >= channelInfo.getfilterCapsMinCharacters() && capsPercent >= channelInfo.getfilterCapsPercent() && capsNumber >= channelInfo.getfilterCapsMinCapitals()) {
                    int warningCount = 0;

                    channelInfo.incWarningCount(sender, FilterType.CAPS);
                    warningCount = channelInfo.getWarningCount(sender, FilterType.CAPS);
                    this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.CAPS, message);

                    if (channelInfo.checkSignKicks())
                        send(channel, sender + ", " + channelInfo.getCapsTO() + " - " + this.getTimeoutText(warningCount, channelInfo));

                    return;
                }
            }

            // Link filter
            if (channelInfo.getFilterLinks() && this.containsLink(message, channelInfo) && (isPleb || (isRegular && !channelInfo.subscriberRegulars) || (isSub && !channelInfo.sIgnoreLinks) || (isVIP && !channelInfo.vIgnoreLinks))) {
                boolean result = channelInfo.linkPermissionCheck(sender);
                int warningCount = 0;
                if (result) {
                    System.out.println("Link permittied. (" + sender + ")");
                    //send(channel, "Link permitted. (" + sender + ")");
                } else {

                    channelInfo.incWarningCount(sender, FilterType.LINK);
                    warningCount = channelInfo.getWarningCount(sender, FilterType.LINK);
                    this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.LINK, message);

                    if (channelInfo.checkSignKicks())
                        send(channel, sender + ", " + channelInfo.getLinksTO() + " - " + this.getTimeoutText(warningCount, channelInfo));
                    return;
                }

            }

            // Length filter
            if (!(isRegular) && (message.length() > channelInfo.getFilterMax())) {
                int warningCount = 0;

                channelInfo.incWarningCount(sender, FilterType.LENGTH);
                warningCount = channelInfo.getWarningCount(sender, FilterType.LENGTH);
                this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.LENGTH, message);

                if (channelInfo.checkSignKicks())
                    send(channel, sender + ", " + channelInfo.getMaxTO() + " - " + this.getTimeoutText(warningCount, channelInfo));

                return;
            }

            // Symbols filter
            if (channelInfo.getFilterSymbols() && (isPleb || (isRegular && !channelInfo.subscriberRegulars) || (isSub && !channelInfo.sIgnoreSymbols) || (isVIP && !channelInfo.vIgnoreSymbols))) {
                String messageNoWS = message.replaceAll("\\s", "");
                int count = getSymbolsNumber(messageNoWS);
                double percent = (double) count / messageNoWS.length();

                if (count > channelInfo.getFilterSymbolsMin() && (percent * 100 > channelInfo.getFilterSymbolsPercent())) {
                    int warningCount = 0;
                    channelInfo.incWarningCount(sender, FilterType.SYMBOLS);
                    warningCount = channelInfo.getWarningCount(sender, FilterType.SYMBOLS);
                    this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.SYMBOLS, message);

                    if (channelInfo.checkSignKicks())
                        send(channel, sender + ", " + channelInfo.getSymbolsTO() + " - " + this.getTimeoutText(warningCount, channelInfo));

                    return;
                }
            }

            //Offensive filter
            if (!isRegular && channelInfo.getFilterOffensive()) {
                boolean isOffensive = channelInfo.isOffensive(message);
                if (isOffensive) {
                    int warningCount = 0;

                    channelInfo.incWarningCount(sender, FilterType.OFFENSIVE);
                    warningCount = channelInfo.getWarningCount(sender, FilterType.OFFENSIVE);
                    this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.OFFENSIVE, message);

                    if (channelInfo.checkSignKicks())
                        send(channel, sender + ", " + channelInfo.getOffensiveTO() + " - " + this.getTimeoutText(warningCount, channelInfo));

                    return;
                }
            }

            //Emote filter
            if (channelInfo.getFilterEmotes() && (isPleb || (isRegular && !channelInfo.subscriberRegulars) || (isSub && !channelInfo.sIgnoreEmotes) || (isVIP && !channelInfo.vIgnoreEmotes))) {
                String emote_tag = null;

                if (tags.containsKey("emotes"))               
                    emote_tag = tags.get("emotes");
                int count_emotes = 0;
                count_emotes = StringUtils.countMatches(emote_tag, "-");
                if (count_emotes > channelInfo.getFilterEmotesMax()) {
                    int warningCount = 0;

                    channelInfo.incWarningCount(sender, FilterType.EMOTES);
                    warningCount = channelInfo.getWarningCount(sender, FilterType.EMOTES);
                    this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.EMOTES, message);

                    if (channelInfo.checkSignKicks())
                        send(channel, sender + ", " + channelInfo.getEmoteTO() + " - " + this.getTimeoutText(warningCount, channelInfo));

                    return;

                }

                if (channelInfo.getFilterEmotesSingle() && checkSingleEmote(message, emote_tag)) {
                    int warningCount = 0;

                    channelInfo.incWarningCount(sender, FilterType.EMOTES);
                    warningCount = channelInfo.getWarningCount(sender, FilterType.EMOTES);
                    this.secondaryTO(channel, sender, this.getTODuration(warningCount, channelInfo), FilterType.EMOTES, message);

                    if (channelInfo.checkSignKicks())
                        send(channel, sender + ", " + channelInfo.getEmoteSingleTO() + " - " + this.getTimeoutText(warningCount, channelInfo));

                    return;

                }

            }

        }

        // ********************************************************************************
        // ***************************** Poll Voting **************************************
        // ********************************************************************************
        if (msg[0].equalsIgnoreCase(prefix + "vote")) {
            log("Matched command !vote (user entry)");
            if (channelInfo.getPoll() != null && channelInfo.getPoll().getStatus() && msg.length > 1) {
                channelInfo.getPoll().vote(sender, msg[1]);
                return;
            }
        }
        // ********************************************************************************
        // ***************************** Giveaway Voting **********************************
        // ********************************************************************************
        if (channelInfo.getGiveaway() != null && channelInfo.getGiveaway().getStatus()) {
            //Giveaway is open and accepting entries.
            channelInfo.getGiveaway().submitEntry(sender, msg[0]);
        }


        // ********************************************************************************
        // ***************************** Raffle Entry *************************************
        // ********************************************************************************
        if (msg[0].equalsIgnoreCase(prefix + "raffle") && msg.length == 1) {
            log("Matched command !raffle (user entry)");
            if (channelInfo.raffle != null) {
                channelInfo.raffle.enter(sender);
                return;
            }
        }

        // ********************************************************************************
        // ******************************* Mode Checks ************************************
        // ********************************************************************************

        //Check channel mode.
        if ((channelInfo.getMode() == 0 || channelInfo.getMode() == -1) && !isOwner)
            return;
        if (channelInfo.getMode() == 1 && !isOp)
            return;


        // ********************************************************************************
        // ********************************* Commands *************************************
        // ********************************************************************************

        //Command cooldown check
        
        if (msg[0].substring(0, 1).equalsIgnoreCase(prefix) && channelInfo.onCooldown(msg[0])) {
            if (!isOp)
                return;
        }
        
        // !echo - me
        if (msg[0].equalsIgnoreCase(prefix + "echo") && isAP) {
            send(channel, this.fuseArray(msg, 1));
            return;
        }
        
                //!time - all
        /* if (msg[0].equalsIgnoreCase(prefix + "time")) {
            log("RB: Matched command !time");
            TimeZone.setDefault(TimeZone.getTimeZone(channelInfo.getTimezone()));
            String time = new java.util.Date().toString();
            send(channel, "Time now for " + twitchName + " is " + time);
            return;
        }
                */

        // !ping - All
        if (msg[0].equalsIgnoreCase(prefix + "ping") && isOp) {
            log("Matched command !ping");
            String time = new java.util.Date().toString();
            send(channel, "Pong sent at " + time + " (" + this.fuseArray(msg, 1) + ")");
            return;
        }
        
        // !resetcount - Admin
        if (msg[0].equalsIgnoreCase(prefix + "resetcount") && isAdmin) {
            log("Matched command !resetcount");
            if (msg.length > 1) {
                channelInfo.resetCommandCount(msg[1]);
                send(channel, "Count for " + msg[1] + " successfully reset");
            } else {
                send(channel, "The syntax is: !resetcount command_name");
            }
            
        }

        // !getinfo - AP
        if (msg[0].equalsIgnoreCase(prefix + "getinfo") && isAP) {
            LOGGER_D.debug("Getting info on chat");
            send(channel, "Messages per minute (sent by the bot): " + messagesPerMin);
            send(channel, "Total messages received in this channel: " + channelInfo.messageCount);
            send(channel, "Messages processed: " + messagesProcessed);
            send(channel, "Available processors: " + Runtime.getRuntime().availableProcessors());
            send(channel, "Free memory (bytes): " + Runtime.getRuntime().freeMemory());
            return;
        }
        
        // !setcount - Admin
        if (msg[0].equalsIgnoreCase(prefix + "setcount") && isOwner) {
            LOGGER_D.debug("Matched command !setcount");
            if (msg.length > 1 && Main.isInteger(msg[2])) {
                channelInfo.setCommandCount(msg[1], Integer.parseInt(msg[2]));
                send(channel, "Count for " + msg[1] + " set to: " + msg[2]);
            } else {
                send(channel, "The syntax is !setcount command_name command_count");
            }
        }

        // !checkcount - Admin
        if (msg[0].equalsIgnoreCase(prefix + "checkcount") && isAdmin) {
            LOGGER_D.debug("Matched command !checkcount");
            String toCheck = msg[1].replaceAll("!", "");
            if (msg.length > 1) 
                send(channel, "Count for " + msg[1] + " is: " + channelInfo.getCommandCount(toCheck));
            else
                send(channel, "The syntax is: !checkcount command_name");
            
        }
        
        //!8ball - All
        if (msg[0].equalsIgnoreCase(prefix + "8ball")) {
            LOGGER_D.debug("Matched command !8ball");
            if (message.charAt(message.length() - 1) != '?') {
                return;
            } else {
                String answer = channelInfo.getAnswer();
                send(channel, "@" + sender + ", " + answer);
            }
        }
        
        //!love - All - Can be toggled
        
        if (msg[0].equalsIgnoreCase(prefix + "love") && (channelInfo.checkLove() || isAdmin)) {
            if (msg.length > 1) {
                Random rand = new Random();
                int checker = rand.nextInt(101);
                send(channel, "There is " + checker + "% ancientLove between " + sender + " and " + msg[1]);
            } else {
                send(channel, "The syntax is: !love <username>");
            }
            
        }
/*
        //!time - all
        if (msg[0].equalsIgnoreCase(prefix + "time")) {
            log("MAtched command !time");
            TimeZone.setDefault(TimeZone.getTimeZone("US/Central"));
            String time = new java.util.Date().toString();
            send(channel, "Time now is " + time);
            return;
        }
*/
        // !lockouttest - All
        if (msg[0].equalsIgnoreCase(prefix + "lockouttest")) {
            log("Matched command !lockouttest");
            send(channel, sender + ", your message was received! You are NOT locked out of chat.");
            return;
        }

        // !bothelp - All
        if (msg[0].equalsIgnoreCase(prefix + "bothelp")) {
            log("Matched command !bothelp");
            send(channel, "Suporte PriestBot - Discord: bit.ly/DiscordPriestBot | Manual: bit.ly/PriestBotManual");
            return;
        }

        // !quote - All
        if (msg[0].equalsIgnoreCase(prefix + "quote") && (channelInfo.checkQuote() || isBroadcaster)) {
            String quote;
            if ((isSub || isVIP) && msg.length > 1) {
                String id = msg[1];
                log ("Matched command !quote");
                quote = Quote.getQuote(twitchName, id);
                send(channel, quote);
            }
            else {
            log("Matched command !getrandomquote");
            quote = Quote.getRandomQuote(twitchName, channelInfo.checkQuoteGame());
            send(channel, quote);
            }
            return;
        }

        // !quotelines - Mods
        if (msg[0].equalsIgnoreCase(prefix + "quotelines") && isOp && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !quotelines");
            try {
                String quoteMsg = Quote.countQuotes(twitchName);
                send(channel, quoteMsg);
            } catch (Exception ex) {
                send(channel, "There was an error, please try again");
            }
            return;
        }

        // !findquotesby - Subs
        if (msg[0].equalsIgnoreCase(prefix + "findquotesby") && (isSub || isVIP) && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !findquotesby");
            if (msg.length > 1) {
                try {
                    String quoteMsg = Quote.findQuotesBy(twitchName, msg[1]);
                    send(channel, quoteMsg);
                } catch (Exception ex) {
                    send(channel, "There was an error");
                }
            }
            return;
        }

        // !addquote - All
        if (msg[0].equalsIgnoreCase(prefix + "addquote") && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !addquote");
            if (channelInfo.isQuoteBanned(sender) && !isOp) {
                send(channel, sender + ", you are banned from submitting quotes.");
                return;
            }
            if (msg.length > 1) {
                if (!isOp && channelInfo.checkQuoteMods()) {
                       // String quoteMsg = "";
                       // for (int i = 1; i < msg.length; i++) {
                       //     quoteMsg += msg[i] + " ";
                       // }
                    String quoteMsg = this.fuseArray(msg, 1);
                    quoteMsg.trim();
                        try {
                            Quote.submitQuote(twitchName, sender, JSONUtil.krakenGame(twitchName), quoteMsg);
                            send(channel, sender + " -> Your quote has been successfully submitted for Mod approval");
                        } catch (Exception ex) {
                            send(channel, "Error submitting the quote");
                        }
                } else {
                    //String quoteMsg = "";
                    //for (int i = 1; i < msg.length; i++) {
                    //    quoteMsg += msg[i] + " ";
                    //}
                    String quoteMsg = this.fuseArray(msg, 1);
                    quoteMsg.trim();
                    try {
                        Quote.addQuote(twitchName, sender, JSONUtil.krakenGame(twitchName), quoteMsg);
                        send(channel, "Quote successfully added");
                    } catch (Exception ex) {
                        send(channel, "Error adding quote");
                    }
                }
            return;
        }
        }

        // !editquote - Mods

        if (msg[0].equalsIgnoreCase(prefix + "editquote") && isOp && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !editquote");
            String quoteMsg = this.fuseArray(msg, 3);
            if (msg.length > 3 && (msg[2].equalsIgnoreCase("game") || msg[2].equalsIgnoreCase("text"))) {
                if (msg[2].equalsIgnoreCase("text")) {
                    try {
                        Quote.editQuote(twitchName, msg[1], quoteMsg);
                        send(channel, "Quote #" + msg[1] + " has been successfully edited");
                    } catch (Exception ex) {
                        send(channel, "Error editing the quote");
                    }
                } else if (msg[2].equalsIgnoreCase("game")) {
                    try {
                        Quote.editGame(twitchName, msg[1], quoteMsg);
                        send(channel, "Game for quote #" + msg[1] + " has been successfully edited");
                    } catch (Exception ex) {
                        send(channel, "Error editing the quote");
                    }
                }
            } else
                send(channel, "The Syntax is: !editquote ID text|game new text|new game");
            return;
        }

        // !pending - Mods

        if (msg[0].equalsIgnoreCase(prefix + "pending") && isOp) {
            log("Matched command !pending");
            String pendingQuotes = "";
            try {
                pendingQuotes = Quote.getPendingQuotes(twitchName);
                send(channel, pendingQuotes);
            } catch (Exception ex) {
                send(channel, "There was an error");
            }
            return;
        }

        // !pquote - Mods

        if (msg[0].equalsIgnoreCase(prefix + "pquote") && isOp && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !pquote");
            if (msg.length > 1) {
            String quotemsg = "";
            try {
                quotemsg = Quote.getPquote(twitchName, msg[1]);
                send(channel, quotemsg);
            } catch (Exception ex) {
                send(channel, "There was an error");
            }
        }
        return;
        }

        // !aquote - Mods
        if (msg[0].equalsIgnoreCase(prefix + "approve") && isOp && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !aquote");
            if (msg.length > 1) {
                try {
                    Quote.approveQuote(twitchName, msg[1]);
                    send(channel, "Quote successfully approved");
                } catch (Exception ex) {
                    send(channel, "There was an error approving the quote");
                    ex.printStackTrace();
                }
            }
            return;
        }

        // !reject - Mods
        if (msg[0].equalsIgnoreCase(prefix + "reject") && isOp && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !reject");
            if (msg.length > 1) {
                try {
                    Quote.rejectQuote(twitchName, msg[1]);
                    send(channel, "Quote #" + msg[1] + " successfully rejected");
                } catch (Exception ex) {
                    send(channel, "Couldn't reject quote");
                    ex.printStackTrace();
                }
            }
            return;
        }

        // !delquote - Mods
        if (msg[0].equalsIgnoreCase(prefix + "delquote") && isOp && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !delquote");
            if (msg.length > 1) {
            try {
                Quote.deleteQuote(twitchName, msg[1]);
                send(channel, "Quote #" + msg[1] + " has been successfully lit on fire and thrown down a cliff to the land of failed quotes");
            } catch (Exception ex) {
                send(channel, "Error deleting the quote");
                }
            }
            return;
        }

        // !lastquote - All
        if (msg[0].equalsIgnoreCase(prefix + "lastquote") && (channelInfo.checkQuote() || isBroadcaster)) {
            log("Matched command !lastquote");
            String quote = Quote.getLastQuote(twitchName);
            send(channel, quote);
            return;
        }

        // !findquote - Subs
        if (msg[0].equalsIgnoreCase(prefix + "findquote") && (channelInfo.checkQuote() || isBroadcaster) && (isSub || isVIP)) {
            log("Matched command !findquote");
            if (msg.length > 1) {
                try {
                    String matchMessage = this.fuseArray(msg, 1);
                    matchMessage.trim();
                    String quote = Quote.findQuote(twitchName, matchMessage);
                    send(channel, quote);
                } catch (Exception ex) {
                    send(channel, "Error getting the quote");
                }
            }
            return;
        }

        // !viewers - All
        if ((msg[0].equalsIgnoreCase(prefix + "viewers") || msg[0].equalsIgnoreCase(prefix + "lurkers"))) {
            log("Matched command !viewers");
            if (BotManager.getInstance().twitchChannels) {
                try {
                    send(channel, JSONUtil.krakenViewers(twitchName) + " viewers.");
                } catch (Exception e) {
                    send(channel, "Stream is not live.");
                }
            } else {
                try {
                    send(channel, JSONUtil.jtvViewers(twitchName) + " viewers.");
                } catch (Exception e) {
                    send(channel, "Stream is not live.");
                }
            }

            return;
        }

//        // !resolution - All
//        if (msg[0].equalsIgnoreCase(prefix + "res") || msg[0].equalsIgnoreCase(prefix + "resolution")) {
//            log("RB: Matched command !resolution");
//
//           String res = JSONUtil.getSourceRes(twitchName);
//            send(channel, "The source resolution is " + res);
//            return;
//        }
//
        //!bitrate - All
//        if (msg[0].equalsIgnoreCase(prefix + "bitrate")) {
//            log("RB: Matched command !resolution");
//
//            double bitrate = JSONUtil.getSourceBitrate(twitchName);
//            if (bitrate > 1)
//                send(channel, "The source bitrate is " + (int) bitrate + " Kbps");
//            else
//                send(channel, "Stream is not live or an error occurred.");
//            return;
//        }


        // !uptime - All
        if (msg[0].equalsIgnoreCase(prefix + "uptime") && (channelInfo.checkUptime() || isAdmin)) {
            log("RB: Matched command !uptime");
            String uptMsg = channelInfo.getUptimeMessage();

            try {
                send(channel, uptMsg);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                send(channel, "An error occurred or stream is offline.");
            }
            return;
        }
        
        // !followage - All
        if (msg[0].equalsIgnoreCase(prefix + "followage")  && (channelInfo.checkFollowage() || isAdmin)) {
            LOGGER_D.debug("Matched command !followage");
            if (msg.length == 1) {
                try {
                    String followage = JSONUtil.fCreated_at(sender, channelInfo.getTwitchName());
                    send(channel, this.getTimeFollowing(followage, sender, channelInfo.getTwitchName()));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    send(channel, "User is not following.");
                }
            } else if (msg.length == 2) {
                try {
                    String followage = JSONUtil.fCreated_at(msg[1], channelInfo.getTwitchName());
                    send(channel, this.getTimeFollowing(followage, msg[1], channelInfo.getTwitchName()));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    send(channel, "User is not following.");
                }
            } else if (msg.length == 3) {
                try {
                    String followage = JSONUtil.fCreated_at(msg[1], msg[2]);
                    send(channel, this.getTimeFollowing(followage, msg[1], msg[2]));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    send(channel, "User is not following.");
                }
            } else {
                send(channel, "Syntax: !followage [username] [channel] (username and channel are optional).");
            }
        }

        // !music - All
        if ((msg[0].equalsIgnoreCase(prefix + "music") || msg[0].equalsIgnoreCase(prefix + "lastfm")) && channelInfo.getMusic()) {
            log("RB: Matched command !music");
            send(channel, "Now playing: " + JSONUtil.lastFM(channelInfo.getLastfm()));
        }
        
        if (msg[0].equalsIgnoreCase(prefix + "table")) {
            log("RB: Matched command !table");
            Random rand = new Random();
            int tablen = rand.nextInt(4) + 1;
            switch(tablen) {
                case 1:
                    send(channel, "┻━┻ ︵ヽ(`Д´)ﾉ︵﻿ ┻━┻");
                    break;
                case 2:
                    send(channel, "(╯°□°）╯︵ ┻━┻");
                    break;
                case 3:
                    send(channel, "(ノಠ益ಠ)ノ彡┻━┻");
                    break;
                case 4:
                    send(channel, "┬─┬ノ( º _ ºノ)");
                    break;
                default: break;
            }
        }
       /* 
        if (msg[0].equalsIgnoreCase(prefix + "pot")) {
            log("RB: Matched command !pot");
            if (channelInfo.getGamble().getStatus())
                send(channel, "Current pot is: " + channelInfo.getGamble().getPrize());
            else
                send(channel, "There is no active !gamble at the moment");
        }


        if (msg[0].equalsIgnoreCase(prefix + "getwet") && isRegular) {
            log("RB: Matched command !getwet");
            if (msg.length > 1) {
                String sendmsg = this.fuseArray(msg, 1);
                send(channel, sendmsg + ", you are now wet, youCreep");
            }
            else {
                send(channel, sender + ", you are now wet, youCreep");
            }
        }
        */

        // !steam - All
        if (msg[0].equalsIgnoreCase("!steam") && !channelInfo.getSteam().equalsIgnoreCase("off")) {
            log("RB: Matched command !steam");
            if (channelInfo.getSteam().length() > 1) {

                if (channelInfo.getSteam().length() > 1) {
                    send(channel, JSONUtil.steam(channelInfo.getSteam(), "all"));
                }

            } else {
                send(channel, "Steam ID not set. Do \"!set steam [ID]\" to configure. ID must be in SteamID64 format and profile must be public.");
            }
            return;
        }

        // !game - All
        if (msg[0].equalsIgnoreCase(prefix + "game") && BotManager.getInstance().twitchChannels) {
            log("RB: Matched command !game");
            if (isOwner && msg.length > 1) {
                String game = this.fuseArray(msg, 1);
                game.trim();
                if (game.equals("-"))
                    game = "";
                try {
                    channelInfo.updateGame(game);
                    send(channel, "Game update sent.");
                } catch (Exception ex) {
                    send(channel, "Error updating game. Did you add me as an editor?");
                }

            } else {
                String game = JSONUtil.krakenGame(twitchName);
                String checker = game.toLowerCase();
                if (checker.contains(".color") || checker.contains("/color") || checker.contains(".disconnect") || checker.contains("/disconnect") || checker.contains(".ban") || checker.contains("/ban")) {
                    send(channel, "Engraçadinho você");
                    return;
                }
                if (game.length() > 0) {
                    send(channel, "Current game: " + game);
                } else {
                    send(channel, "No game set.");
                }
            }
            return;
        }
        
        // !distribute - Admin

        if (msg[0].equalsIgnoreCase(prefix + "distribute") && isAdmin) {
            List<String> chatters = new ArrayList<String>();
            chatters = JSONUtil.getChatters(twitchName);
            LOGGER_D.debug("Testing points distribution");
            LOGGER_D.debug(JSONUtil.getChatters(twitchName).toString());
            Points.distributePoints(twitchName, JSONUtil.getChatters(twitchName), Integer.parseInt(msg[1]));
            Points.logPoints(twitchName, "Passive distribution of points", "admin", chatters.toString(), new java.util.Date().toString());
            send(channel, "Points distributed");
            return;
        }
        
        // !getid - Super Admin
        
        if (msg[0].equalsIgnoreCase(prefix + "getid") && isSuperAdmin) {
            log("RB: Matched command !getid");
            if (msg.length >= 2) {
                send(channel, "@" + sender + ", " + msg[1] + "'s user ID is: " + JSONUtil.krakenUserID(msg[1]));
            } else {
                send(channel, "You must specify a user ID");
            }
            return;
        }
        
        if (msg[0].equalsIgnoreCase(prefix + "points") && (channelInfo.checkPoints() || isAdmin)) {
            log("RB: Matched command !points");
            if (msg.length == 1) {
                int getpoints = Points.getPoints(twitchName, sender);
                //int rank = channelInfo.getRankByUsername(sender) + 1;
                send(channel, sender + " -> You have " + getpoints + " " + channelInfo.getPointsName() + ".");
            } else if (msg.length > 1 && isOp) {
                int getpoints = Points.getPoints(twitchName, msg[1]);
                //int rank = channelInfo.getRankByUsername(msg[1]) + 1;
                send(channel, msg[1] + " -> You have " + getpoints + " " + channelInfo.getPointsName() + ".");
            }
            return;
        }
        

        // !status - All
        if (msg[0].equalsIgnoreCase(prefix + "status")) {
            log("RB: Matched command !status");
            if (isOwner && msg.length > 1 && BotManager.getInstance().twitchChannels) {
                String status = this.fuseArray(msg, 1);
                status.trim();
                try {
                    channelInfo.updateStatus(status);
                    send(channel, "Status update sent.");
                } catch (Exception ex) {
                    send(channel, "Error updating status. Did you add me as an editor?");
                }
            } else {
                String status = "";
                if (BotManager.getInstance().twitchChannels)
                    status = JSONUtil.krakenStatus(twitchName);
                else
                    status = JSONUtil.jtvStatus(twitchName);
                String checker = status.toLowerCase();
                if (checker.contains(".color") || checker.contains("/color") || checker.contains(".disconnect") || checker.contains("/disconnect") || checker.contains(".ban") || checker.contains("/ban")) {
                    send(channel, "Engraçadinho você");
                    return;
                }

                if (status.length() > 0) {
                    send(channel, status);
                } else {
                    send(channel, "Unable to query API.");
                }
            }
            return;
        }

        // !followme - Owner
        if (msg[0].equalsIgnoreCase(prefix + "followme") && isOwner && BotManager.getInstance().twitchChannels) {
            log("RB: Matched command !followme");
            BotManager.getInstance().followChannel(twitchName);
            send(channel, "Follow update sent.");
            return;
        }

        // !properties - Owner
        if (msg[0].equalsIgnoreCase(prefix + "properties") && isOwner && BotManager.getInstance().twitchChannels) {
            log("RB: Matched command !properties");
            send(channel, JSONUtil.getChatProperties(channelInfo.getTwitchName()));
            return;
        }

        // !commands - Op/Regular
        if (msg[0].equalsIgnoreCase(prefix + "commands") && isOp) {
            log("RB: Matched command !commands");
            send(channel, "Commands: " + channelInfo.getCommandList());
            return;
        }

        // !throw - All
        if (msg[0].equalsIgnoreCase(prefix + "throw") && (channelInfo.checkThrow() || isAdmin)) {
            log("RB: Matched command !throw");
            if (msg.length > 1) {
                String throwMessage = "";
                for (int i = 1; i < msg.length; i++) {
                    throwMessage += msg[i] + " ";
                }
                send(channel, "(╯°□°）╯︵" + throwMessage);
            }
            return;
        }

        // !topic
        if (msg[0].equalsIgnoreCase(prefix + "topic") && channelInfo.useTopic) {
            log("RB: Matched command !topic");
            if (msg.length < 2 || !isOp) {
                if (channelInfo.getTopic().equalsIgnoreCase("")) {
                    if (BotManager.getInstance().twitchChannels) {
                        String status = "";
                        if (BotManager.getInstance().twitchChannels)
                            status = JSONUtil.krakenStatus(twitchName);
                        else
                            status = JSONUtil.jtvStatus(twitchName);

                        if (status.length() > 0)
                            send(channel, status);
                        else
                            send(channel, "Unable to query API.");
                    } else {
                        send(channel, "Topic not set");
                    }
                } else {
                    send(channel, "Topic: " + channelInfo.getTopic() + " (Set " + channelInfo.getTopicTime() + " ago)");
                }
            } else if (msg.length > 1 && isOp) {
                if (msg[1].equalsIgnoreCase("unset")) {
                    channelInfo.setTopic("");
                    send(channel, "No topic is set.");
                } else {
                    channelInfo.setTopic(message.substring(7));
                    send(channel, "Topic: " + channelInfo.getTopic() + " (Set " + channelInfo.getTopicTime() + " ago)");
                }

            }
            return;
        }

        // !link
        if (msg[0].equalsIgnoreCase(prefix + "link") && isOp) {
            log("RB: Matched command !link");
            if (msg.length > 1) {
                String rawQuery = message.substring(6);
                String encodedQuery = "";
                try {
                    encodedQuery = URLEncoder.encode(rawQuery, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String url = "http://lmgtfy.com/?q=" + encodedQuery;
                send(channel, "Link to \"" + rawQuery + "\" -> " + JSONUtil.shortenURL(url));
            }
            return;
        }


        // !commercial
        if (msg[0].equalsIgnoreCase(prefix + "commercial") && BotManager.getInstance().twitchChannels) {
            log("RB: Matched command !commercial");
            if (isOwner) {
                channelInfo.runCommercial();
                send(channel, "Running commercial break");
                //send(channel, "Running a 30 second commercial. Thank you for supporting the channel!");
            }
            return;
        }

        // !skipcommercial
        if (msg[0].equalsIgnoreCase(prefix + "skipcommercial") && BotManager.getInstance().twitchChannels) {
            log("RB: Matched command !skipcommercial");
            if (isOwner) {
                channelInfo.skipNextCommercial = true;
                send(channel, "The next commercial command will be ignored. If you have the command overridden with a custom command it will still be displayed.");
            }
            return;
        }

        // !command - Ops
        if (msg[0].equalsIgnoreCase(prefix + "command") && isOp) {
            log("RB: Matched command !command");
            if (msg.length < 3) {
                send(channel, "Syntax: \"!command add/delete [name] [message]\" - Name is the command trigger without \"!\" and message is the response.");
            } else if (msg.length > 2) {
                if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
                    String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
                    String value = fuseArray(msg, 3);
                    if ((value.startsWith(".color") || value.startsWith("/color")) && !isSuperAdmin) {
                        send(channel, "Command forbidden");
                        return;
                    }
                    if ((value.startsWith(".ignore") || value.startsWith("/ignore")) && !isAP) {
                        send(channel, "Command forbidden");
                        return;
                    }
                    if ((value.startsWith(".disconnect") || value.startsWith("/disconnect")) && !isAP) {
                        send(channel, "Command forbidden");
                        return;
                    }
                    if ((value.startsWith(".ban") || value.startsWith("/ban")) && !isBroadcaster) {
                        send(channel, "Command forbidden");
                        return;
                    }
                    if (!value.contains(",,")) {
                        channelInfo.setCommand(key, value, sender);
                        send(channel, "Command added/updated.");
                    } else {
                        send(channel, "Command cannot contain double commas (\",,\").");
                    }

                } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) {
                    String key = msg[2];
                    channelInfo.removeCommand(key);
                    channelInfo.removeRepeatCommand(key);
                    channelInfo.removeScheduledCommand(key);

                    send(channel, "Command " + key + " removed.");

                } else if (msg[1].equalsIgnoreCase("restrict") && msg.length >= 4 && isOwner) {
                    String command = msg[2];
                    String levelStr = msg[3].toLowerCase();
                    int level = 0;
                    if (channelInfo.getCommand(command) != null) {
                        if (levelStr.equalsIgnoreCase("broadcaster") || levelStr.equalsIgnoreCase("caster") || levelStr.equalsIgnoreCase("streamer"))
                            level = 8;
                        if (levelStr.equalsIgnoreCase("owner") || levelStr.equalsIgnoreCase("owners"))
                            level = 6;
                        if (levelStr.equalsIgnoreCase("mod") || levelStr.equalsIgnoreCase("mods"))
                            level = 5;
                        if (levelStr.equalsIgnoreCase("regular") || levelStr.equalsIgnoreCase("regulars"))
                            level = 1;
                        if (levelStr.equalsIgnoreCase("everyone"))
                            level = 0;

                        if (channelInfo.setCommandsRestriction(command, level))
                            send(channel, prefix + command + " restricted to " + levelStr + " only.");
                        else
                            send(channel, "Error setting restriction.");
                    } else {
                        send(channel, "Command does not exist.");
                    }
                }
            }
            return;
        }
        /* Deprecated
        // !hostedby - Owner
        if (msg[0].equalsIgnoreCase(prefix + "hostedby") && isOwner) {
            log("RB: Matched command !hostedby");
            String tempH = "";
            for (String s : JSONUtil.getHosts(twitchName)) {
                tempH += s + ", ";
            }
            send(channel, "Currently being hosted by: " + tempH);
            return;
        }
        */

        // !autohost - Owner

        if (msg[0].equalsIgnoreCase(prefix + "autohost") && isBroadcaster) {
            log("RB: Matched command !host");
            if (msg.length <= 1) {
                send(channel, "Syntax: \"!autohost start/stop/add/delete [channel] [priority (1/2)]\" ");
            }
            if (msg.length > 1) {
                if (msg[1].equalsIgnoreCase("add")) {
                    if (msg[3].equals("1") || msg[3].equals("2")) {
                        int priority = Integer.parseInt(msg[3]);
                        channelInfo.setHost(msg[2], priority);
                        send(channel, "Channel: " + msg[2] + " has been added with priority: " + priority);
                    }
                } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) {
                    channelInfo.removeHost(msg[2]);
                    send(channel, "Channel: " + msg[2] + " has been successfully removed from the hosts lists");
                } else if (msg[1].equalsIgnoreCase("start")) {
                    if (channelInfo.getAH() == null) {
                        channelInfo.setAH(new AutoHost());
                    }
                    if (channelInfo.getAH().getStatus()) {
                        send(channel, "Auto host is already active");
                        return;
                    }
                    channelInfo.getAH().setStatus(true);
                    channelInfo.setAutoHost(true);
                    send(channel, "Feature: Auto Hosting is on, after 15 seconds the first channel will be hosted and it will be changed every 30 minutes.");
                    this.startAutoHostTimer(channelInfo, twitchName);
                } else if (msg[1].equalsIgnoreCase("stop")) {
                    if (channelInfo.getAH() != null) {
                        channelInfo.getAH().setStatus(false);
                        channelInfo.setAutoHost(false);
                        channelInfo.getAH().getTimer().cancel();
                        channelInfo.getAH().getTimer().purge();
                        sendCommand(channel, ".unhost");
                        System.out.println("Stopping timer");
                        send(channel, "Auto-Hosting disabled");
                    } else {
                        channelInfo.getAH().setStatus(false);
                        channelInfo.getAH().getTimer().cancel();
                    }
                } else if (msg[1].equalsIgnoreCase("list")) {
                    String tempP = "";
                    String tempN = "";

                    for (String s : channelInfo.getHostsp()) {
                        tempP += s + ", ";
                    }

                    for (String ss : channelInfo.getHostsn()) {
                        tempN += ss + ", ";
                    }

                    send(channel, "Priority Hosts: " + tempP);
                    send(channel, "Normal Hosts: " + tempN);
                } else if (msg[1].equalsIgnoreCase("status")) {
                    if (channelInfo.getAH().getStatus()) {
                        send(channel, "Hosting is active");
                    } else {
                        send(channel, "Hosting is inactive");
                    }
                }    
            }
            return;   
        } 

        // !repeat - Ops
        if (msg[0].equalsIgnoreCase(prefix + "repeat") && isOwner) {
            log("RB: Matched command !repeat");
            if (msg.length < 3) {
                if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
                    String commandsRepeatKey = "";

                    Iterator itr = channelInfo.commandsRepeat.entrySet().iterator();

                    while (itr.hasNext()) {
                        Map.Entry pairs = (Map.Entry) itr.next();
                        RepeatCommand rc = (RepeatCommand) pairs.getValue();
                        commandsRepeatKey += pairs.getKey() + " [" + (rc.active == true ? "ON" : "OFF") + "]" + " [" + rc.delay + "s]" + ", ";
                    }
                    send(channel, "Repeating commands: " + commandsRepeatKey);
                } else {
                    send(channel, "Syntax: \"!repeat add/delete [commandname] [delay in seconds] [message difference - optional]\"");
                }
            } else if (msg.length > 2) {
                if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
                    String key = msg[2];
                    try {
                        int delay = Integer.parseInt(msg[3]);
                        int difference = 1;
                        if (msg.length == 5)
                            difference = Integer.parseInt(msg[4]);

                        if (channelInfo.getCommand(key) == null || delay < 30) {
                            //Key not found or delay to short
                            send(channel, "Command not found or delay is less than 30 seconds.");
                        } else {
                            channelInfo.setRepeatCommand(key, delay, difference);
                            send(channel, "Command " + key + " will repeat every " + delay + " seconds if " + difference + " messages have passed.");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) {
                    String key = msg[2];
                    channelInfo.removeRepeatCommand(key);
                    send(channel, "Command " + key + " will no longer repeat.");

                } else if (msg[1].equalsIgnoreCase("on") || msg[1].equalsIgnoreCase("off")) {
                    String key = msg[2];
                    if (msg[1].equalsIgnoreCase("on")) {
                        channelInfo.setRepeatCommandStatus(key, true);
                        send(channel, "Repeat command " + key + " has been enabled.");
                    } else if (msg[1].equalsIgnoreCase("off")) {
                        channelInfo.setRepeatCommandStatus(key, false);
                        send(channel, "Repeat command " + key + " has been disabled.");
                    }

                }
            }
            return;
        }
        
        //!ancientspam - Admins+
        if (msg[0].equalsIgnoreCase(prefix + "ancientspam") && isAP) {
                if (msg.length > 2 && Main.isInteger(msg[1])) {
                    String toSpam = fuseArray(msg, 2);
                    for (int i = 0; i < Integer.parseInt(msg[1]); i++)
                        send(channel, toSpam);
                    return;
                }
        }

        // !schedule - Ops
        if (msg[0].equalsIgnoreCase(prefix + "schedule") && isOwner) {
            log("RB: Matched command !schedule");
            if (msg.length < 3) {
                if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
                    String commandsScheduleKey = "";

                    Iterator itr = channelInfo.commandsSchedule.entrySet().iterator();

                    while (itr.hasNext()) {
                        Map.Entry pairs = (Map.Entry) itr.next();
                        ScheduledCommand sc = (ScheduledCommand) pairs.getValue();
                        commandsScheduleKey += pairs.getKey() + " [" + (sc.active == true ? "ON" : "OFF") + "]" + ", ";
                    }
                    send(channel, "Scheduled commands: " + commandsScheduleKey);
                } else {
                    send(channel, "Syntax: \"!schedule add/delete/on/off [commandname] [pattern] [message difference - optional]\"");
                }
            } else if (msg.length > 2) {
                if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
                    String key = msg[2];
                    try {
                        String pattern = msg[3];
                        if (pattern.equals("hourly"))
                            pattern = "0 * * * *";
                        else if (pattern.equals("semihourly"))
                            pattern = "0,30 * * * *";
                        else
                            pattern = pattern.replace("_", " ");

                        int difference = 1;
                        if (msg.length == 5)
                            difference = Integer.parseInt(msg[4]);

                        if (channelInfo.getCommand(key) == null || pattern.contains(",,")) {
                            //Key not found or delay to short
                            send(channel, "Command not found or invalid pattern.");
                        } else {
                            channelInfo.setScheduledCommand(key, pattern, difference);
                            send(channel, "Command " + key + " will repeat every " + pattern + " if " + difference + " messages have passed.");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) {
                    String key = msg[2];
                    channelInfo.removeScheduledCommand(key);
                    send(channel, "Command " + key + " will no longer repeat.");

                } else if (msg[1].equalsIgnoreCase("on") || msg[1].equalsIgnoreCase("off")) {
                    String key = msg[2];
                    if (msg[1].equalsIgnoreCase("on")) {
                        channelInfo.setScheduledCommandStatus(key, true);
                        send(channel, "Scheduled command " + key + " has been enabled.");
                    } else if (msg[1].equalsIgnoreCase("off")) {
                        channelInfo.setScheduledCommandStatus(key, false);
                        send(channel, "Scheduled command " + key + " has been disabled.");
                    }

                }
            }
            return;
        }

        // !updatechannels - AP only
        if (msg[0].equalsIgnoreCase(prefix + "updatechannels") && isAP) {
            LOGGER_D.debug("Updating channels to SQL");
            //Getting channel list
            List<String> channels = new LinkedList<>();
            Iterator itr = BotManager.getInstance().channelList.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry pairs = (Map.Entry) itr.next();
                channels.add((String)pairs.getKey());
            }
            if (SQLHelper.updateChannels(channels))
                send(channel, "@Lucas, finished updating channels to SQL");
            else
                send(channel, "@Lucas, there was a failure in the attempt to update channels");
        }

        // !updatefilters - AP only
        if (msg[0].equalsIgnoreCase(prefix + "updatebanphrase") && isAP) {
            LOGGER_D.debug("Updating filters to ");
        }

        // !autoreply - Ops
        if (msg[0].equalsIgnoreCase(prefix + "autoreply") && isOp) {
            log("RB: Matched command !autoreply");
            if (msg.length < 3) {
                if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
                    for (int i = 0; i < channelInfo.autoReplyTrigger.size(); i++) {
                        String cleanedTrigger = channelInfo.autoReplyTrigger.get(i).toString().replaceAll("\\.\\*", "*").replaceAll("\\\\Q", "").replaceAll("\\\\E", "");
                        send(channel, "[" + (i + 1) + "] " + cleanedTrigger + " ---> " + channelInfo.autoReplyResponse.get(i));
                    }
                } else {
                    send(channel, "Syntax: \"!autoreply add/delete/list [pattern] [response]\"");
                }
            } else if (msg.length > 2) {
                if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
                    String pattern = msg[2].replaceAll("_", " ");
                    String response = fuseArray(msg, 3);
                    String checker = response.toLowerCase();
                    if (checker.contains(".color") || checker.contains("/color") || checker.contains(".disconnect") || checker.contains("/disconnect") || checker.contains(".ban") || checker.contains("/ban")) {
                        send(channel, "Não podemos adicionar esse autoreply");
                        return;
                    }

                    channelInfo.addAutoReply(pattern, response);
                    send(channel, "Autoreply added.");
                } else if ((msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) && msg.length > 2) {
                    if (Main.isInteger(msg[2])) {
                        int pos = Integer.parseInt(msg[2]);

                        if (channelInfo.removeAutoReply(pos))
                            send(channel, "Autoreply removed.");
                        else
                            send(channel, "Autoreply not found. Are you sure you have the correct number?");
                    }
                }
            }
            return;
        }


        // !poll - Ops
        if (msg[0].equalsIgnoreCase(prefix + "poll") && isOp) {
            log("RB: Matched command !poll");
            if (msg.length < 2) {
                send(channel, "Syntax: \"!poll create [option option ... option]\"");
            } else if (msg.length >= 2) {
                if (msg[1].equalsIgnoreCase("create")) {
                    String[] options = new String[msg.length - 2];
                    int oc = 0;
                    for (int c = 2; c < msg.length; c++) {
                        options[oc] = msg[c];
                        oc++;
                    }
                    channelInfo.setPoll(new Poll(options));
                    send(channel, "Poll created. Do '!poll start' to start voting.");
                } else if (msg[1].equalsIgnoreCase("start")) {
                    if (channelInfo.getPoll() != null) {
                        if (channelInfo.getPoll().getStatus()) {
                            send(channel, "Poll is alreay running.");
                        } else {
                            channelInfo.getPoll().setStatus(true);
                            send(channel, "Poll started. Type: !vote <option> to start voting.");
                        }
                    }
                } else if (msg[1].equalsIgnoreCase("stop")) {
                    if (channelInfo.getPoll() != null) {
                        if (channelInfo.getPoll().getStatus()) {
                            channelInfo.getPoll().setStatus(false);
                            send(channel, "Poll stopped.");
                        } else {
                            send(channel, "Poll is not running.");
                        }
                    }
                } else if (msg[1].equalsIgnoreCase("results")) {
                    if (channelInfo.getPoll() != null) {
                        send(channel, channelInfo.getPoll().getResultsString());
//                              String[] results = channelInfo.getPoll().getResults();
//                              for(int c=0;c<results.length;c++){
//                                  send(channel, results[c]);
//                              }
                    }

                }
            }
            return;
        }

        // !giveaway - Ops
        if ((msg[0].equalsIgnoreCase(prefix + "giveaway") || msg[0].equalsIgnoreCase("!ga")) && isOp) {
            log("RB: Matched command !giveaway");
            if (msg.length < 2) {
                send(channel, "Syntax: \"!giveaway create [max number] [time to run in seconds]\". Time is optional.");
            } else if (msg.length >= 2) {
                if (msg[1].equalsIgnoreCase("create")) {
                    String max = "" + 100;
                    if (msg.length > 2) {
                        max = msg[2];
                    }
                    channelInfo.setGiveaway(new Giveaway(max));
                    if (msg.length > 3 && channelInfo.getGiveaway().isInteger(msg[3])) {
                        this.startGaTimer(Integer.parseInt(msg[3]), channelInfo);
                    } else {
                        send(channel, "Giveaway created. Do !giveaway start' to start." + " Range 1-" + channelInfo.getGiveaway().getMax() + ".");
                    }
                } else if (msg[1].equalsIgnoreCase("start")) {
                    if (channelInfo.getGiveaway() != null) {
                        if (channelInfo.getGiveaway().getStatus()) {
                            send(channel, "Giveaway is alreay running.");
                        } else {
                            channelInfo.getGiveaway().setStatus(true);
                            send(channel, "Giveaway started.");
                        }
                    }
                } else if (msg[1].equalsIgnoreCase("stop")) {
                    if (channelInfo.getGiveaway() != null) {
                        if (channelInfo.getGiveaway().getStatus()) {
                            channelInfo.getGiveaway().setStatus(false);
                            send(channel, "Giveaway stopped.");
                        } else {
                            send(channel, "Giveaway is not running.");
                        }
                    }
                } else if (msg[1].equalsIgnoreCase("results")) {
                    if (channelInfo.getGiveaway() != null) {
                        send(channel, channelInfo.getGiveaway().getResultsString());
//                              String[] results = channelInfo.getGiveaway().getResults();
//                              for(int c=0;c<results.length;c++){
//                                  send(channel, results[c]);
//                              }
                    } else {
                        send(channel, "No giveaway results.");
                    }

                }
            }
            return;
        }
/*
        // !gamble - Subs

        if (msg[0].equalsIgnoreCase(prefix + "gamble") && isRegular) {
            log("RB: Matched command !gamble");
            if (msg.length >= 2) {
                if (Main.isInteger(msg[1])) {
                    int amount = Integer.parseInt(msg[1]);
                    if (channelInfo.getPoints(sender) < amount) {
                        send(channel, "You do not have enough points to start this gamble.");
                        return;
                    }
                    if (amount <= 0) {
                        send(channel, "You cannot gamble negative numbers");
                        return;
                    }
                    if (channelInfo.getGamble() == null) {
                        channelInfo.setGamble(new Gamble(amount));
                    }
                    if (channelInfo.getGamble().getStatus()) {
                        send(channel, "There is a gamble running");
                        return;
                    }
                    channelInfo.getGamble().setEnabled(true);
                    send(channel, sender + " has started a gamble. Type !join to join the gamble. You will have two minutes before the gamble closes");
                    this.startGambleTimer(channelInfo);
                    channelInfo.getGamble().setReward(amount);
                    channelInfo.getGamble().enterCreator(sender);
                    channelInfo.removePoints(sender, amount);
                } else if (msg[1].equalsIgnoreCase("disable") && isOp) {
                    if (channelInfo.getGamble() != null) {
                        channelInfo.getGamble().setEnabled(false);
                    }
                } else if (msg[1].equalsIgnoreCase("reset") && isOp) {
                    if (channelInfo.getGamble() != null) {
                        channelInfo.getGamble().reset();
                    }
                    send(channel, "Gamble has been reset");
                } else if (msg[1].equalsIgnoreCase("count") && isOp) {
                    if (channelInfo.getGamble() != null) {
                        send(channel, "There are: " + channelInfo.getGamble().count() + " entries in this gamble.");
                    }
                }
            }
        }
        */
        
        //!startbet - Channel Admin
        if (msg[0].equalsIgnoreCase(prefix + "startbet") && isChannelAdmin) {
            if (msg.length == 1) {
                if (channelInfo.getBet() == null) {
                    channelInfo.setBet(new Bet(0));
                }
                if (channelInfo.getBet().getStatus()) {
                    send(channel, "There is a bet running already.");
                    return;
                }
                channelInfo.getBet().setStatus(true);
                send(channel, sender + " has started a bet. Type !bet win/lose amount to enter the bet to see if " + twitchName +  "will win or lose this game. You will have five minutes to place your bets");
                this.startBetTimer(channelInfo, 5);
                
            } else if (msg.length == 2) {
                if (!Main.isInteger(msg[1])) {
                    send(channel, "You must enter a number in minutes for the delay");
                    return;
                } else {
                    if (channelInfo.getBet() == null) {
                        channelInfo.setBet(new Bet(0));
                    }
                    if (channelInfo.getBet().getStatus()) {
                        send(channel, "There is a bet running already.");
                        return;
                    }
                    channelInfo.getBet().setStatus(true);
                    send(channel, sender + " has started a bet. Type !bet win/lose amount to enter the bet to see if " + twitchName + " will win or lose this game. You will have " + msg[1] + " minutes to place your bets.");
                    this.startBetTimer(channelInfo, Integer.parseInt(msg[1]));

                }
            } else {
                send(channel, "The usage is: !startbet <minutes>. If no minutes are specified, a default of 5 minutes will be used");
            }
        }

        // !raffle - Ops
        if (msg[0].equalsIgnoreCase(prefix + "raffle") && isOp) {
            log("RB: Matched command !raffle");
            if (msg.length >= 2) {
                if (msg[1].equalsIgnoreCase("enable")) {
                    if (channelInfo.raffle == null) {
                        channelInfo.raffle = new Raffle();
                    }
                    channelInfo.raffle.setEnabled(true);

                    send(channel, "Raffle enabled.");
                } else if (msg[1].equalsIgnoreCase("disable")) {
                    if (channelInfo.raffle != null) {
                        channelInfo.raffle.setEnabled(false);
                    }

                    send(channel, "Raffle disabled.");
                } else if (msg[1].equalsIgnoreCase("reset")) {
                    if (channelInfo.raffle != null) {
                        channelInfo.raffle.reset();
                    }

                    send(channel, "Raffle entries cleared.");
                } else if (msg[1].equalsIgnoreCase("count")) {
                    if (channelInfo.raffle != null) {
                        send(channel, "Raffle has " + channelInfo.raffle.count() + " entries.");
                    } else {
                        send(channel, "Raffle has 0 entries.");
                    }
                } else if (msg[1].equalsIgnoreCase("winner")) {
                    if (channelInfo.raffle != null) {
                        send(channel, "Winner is " + channelInfo.raffle.pickWinner() + "!");
                    } else {
                        send(channel, "No raffle history found.");
                    }
                }
            } else {
                if (channelInfo.raffle != null) {
                    channelInfo.raffle.enter(sender);
                }
            }
            return;
        }
        
        // !follow - Op
        if (msg[0].equalsIgnoreCase(prefix + "follow") && isOp) {
            log("RB: Matched command !follow");
            String game = JSONUtil.krakenGame(msg[1]);
            send(channel, "Follow " + msg[1] + " at https://www.twitch.tv/" + msg[1] + " They were last playing: " + game);
            return;
        }
        
        //!roleta - All
        if (msg[0].equalsIgnoreCase(prefix + "roleta") && channelInfo.checkRoulette()) {
            log("RB: Matched command !roulette");
            Random rand = new Random();
            int bull = rand.nextInt(6);
            if (bull == 5) {
                if (isAdmin) {
                    send(channel, "majinSmug é mais de 8 mil HUUUUUUUUUGHHHH");
                }
                else if (isOp) {
                    send(channel, "PogChamp majin1 majin2 majin3 ..... okay, ainda bem que o streamer gosta de vc!");
                }
                else {
                    send(channel, "PogChamp majin1 majin2 majin3");
                    sendCommand(channel, ".timeout " + sender + " " + channelInfo.getRouletteTO());
                }
            }
            else {
                send(channel, "Holy PogChamp Essa passou perto.");
                log("RB: Drew bullet number: " + bull);
            }
            return;
        }
        
        
        // !roulette - All
        if (msg[0].equalsIgnoreCase(prefix + "roulette") && channelInfo.checkRoulette()) {
            log("RB: Matched command !roulette");
            Random rand = new Random();
            int bull = rand.nextInt(6);
            if (bull == 5) {
                if (isAdmin) {
                    send(channel, "You were shot, but the OP shield blocked the bullet!");
                }
                else if (isOp) {
                    send(channel, "You were shot, but the bullet bounced off your mod armor!");
                }
                else {
                    send(channel, "You were playing roulette and you were shot! RIP!");
                    sendCommand(channel, ".timeout " + sender + " " + channelInfo.getRouletteTO());
                }
            }
            else {
                send(channel, "Roulette clicks...and misses! " + sender + " survives.");
                log("RB: Drew bullet number: " + bull);
            }
            return;
        }


        // !random - Ops
        if (msg[0].equalsIgnoreCase(prefix + "random") && (isSub || isVIP)) {
            log("RB: Matched command !random");
            if (msg.length >= 2) {
                if (msg[1].equalsIgnoreCase("coin")) {
                    Random rand = new Random();
                    boolean coin = rand.nextBoolean();
                    if (coin == true)
                        send(channel, "Heads!");
                    else
                        send(channel, "Tails!");
                }
            }
            return;
        }

        // ********************************************************************************
        // ***************************** Moderation Commands ******************************
        // ********************************************************************************

        //Moderation commands - Ops
        if (isOp) {
            if (msg[0].equalsIgnoreCase("+m")) {
                if (msg.length == 2) 
                    sendCommand(channel, ".slow " + msg[1]);
                else
                    sendCommand(channel, ".slow");
            }
            if (msg[0].equalsIgnoreCase("-m")) {
                sendCommand(channel, ".slowoff");
            }
            if (msg[0].equalsIgnoreCase("+s")) {
                sendCommand(channel, ".subscribers");
            }
            if (msg[0].equalsIgnoreCase("-s")) {
                sendCommand(channel, ".subscribersoff");
            }
            if (msg[0].equalsIgnoreCase("+r9k")) 
                sendCommand(channel, ".r9kbeta");
            if (msg[0].equalsIgnoreCase("-r9k")) 
                sendCommand(channel, ".r9kbetaoff");
            if (msg[0].equalsIgnoreCase("+e")) 
                sendCommand(channel, ".emoteonly");
            if (msg[0].equalsIgnoreCase("-e")) 
                sendCommand(channel, ".emoteonlyoff");
            
            if (msg.length > 0) {
                if (msg[0].equalsIgnoreCase("+b")) {
                    sendCommand(channel, ".ban " + msg[1].toLowerCase());
                }
                if (msg[0].equalsIgnoreCase("-b")) {
                    sendCommand(channel, ".unban " + msg[1].toLowerCase());
                    sendCommand(channel, ".timeout " + msg[1].toLowerCase() + " 1");
                }
                if (msg[0].equalsIgnoreCase("+k")) {
                    sendCommand(channel, ".timeout " + msg[1].toLowerCase());
                }
                if (msg[0].equalsIgnoreCase("+p")) {
                    sendCommand(channel, ".timeout " + msg[1].toLowerCase() + " 1");
                }
            }

        }

        // !clear - Ops
        if (msg[0].equalsIgnoreCase(prefix + "clear") && isOp) {
            log("RB: Matched command !clear");
            sendCommand(channel, ".clear");
            return;
        }

        //Filters
        if (msg[0].equalsIgnoreCase(prefix + "filter") && isOwner) {
            if (msg.length < 2) {
                send(channel, "Syntax: !filter <option> [sub options]. Options: on/off, status, me, ignoresubs, enablewarnings, timeoutduration, displaywarnings, messagelength, links, pd, banphrase, caps, emotes, caule, and symbols.");
                return;
            }

            //Shift down a notch
            String[] newMsg = new String[msg.length - 1];
            for (int i = 1; i < msg.length; i++) {
                newMsg[i - 1] = msg[i];
            }
            msg = newMsg;

            //Global disable
            if (msg[0].equalsIgnoreCase("on")) {
                channelInfo.setFiltersFeature(true);
                send(channel, "Feature: Filters is on");
                return;
            } else if (msg[0].equalsIgnoreCase("off")) {
                channelInfo.setFiltersFeature(false);
                send(channel, "Feature: Filters is off");
                return;
            }

            if (msg[0].equalsIgnoreCase("status")) {
                String toSendFilter = "";
                if (channelInfo.useFilters)
                    toSendFilter += "Filters are enabled. ";
                else {
                    toSendFilter += "Filters are disabled for this channel.";
                    send(channel, toSendFilter);
                    return;
                }
                //send(channel, "Global: " + channelInfo.useFilters);
                if (channelInfo.getEnableWarnings())
                    toSendFilter += "Warnings are enabled. ";
                else
                    toSendFilter += "Warnings are disabled. ";
                
                toSendFilter += "Timeout duration is: " + channelInfo.getTimeoutDuration() + " seconds. ";
                if (channelInfo.checkSignKicks())
                    toSendFilter += "Warnings will be displayed. ";
                else
                    toSendFilter += "Warnings will not be displayed. ";
                
                //send(channel, "Enable warnings: " + channelInfo.getEnableWarnings());
                //send(channel, "Timeout duration: " + channelInfo.getTimeoutDuration());
                //send(channel, "Display warnings: " + channelInfo.checkSignKicks());
                //send(channel, "Max message length: " + channelInfo.getFilterMax());
                toSendFilter += "Max message length is: " + channelInfo.getFilterMax() + ". ";
                if (channelInfo.getSubscriberRegulars())
                    toSendFilter += "Subscribers will NOT be affected by filters. ";
                else
                    toSendFilter += "Subscribers will be affected by filters. ";
                send(channel, toSendFilter);
                //send(channel, "Ignore Subs: " + channelInfo.getSubscriberRegulars());
                send(channel, "Me(color message): " + channelInfo.getFilterMe());
                send(channel, "Links: " + channelInfo.getFilterLinks());
                send(channel, "Banned phrases: " + channelInfo.getFilterOffensive() + " ~ severity=" + Config.getProperty(channel,"banPhraseSeverity"));
                send(channel, "Caps: " + channelInfo.getFilterCaps() + " ~ percent=" + channelInfo.getfilterCapsPercent() + ", minchars=" + channelInfo.getfilterCapsMinCharacters() + ", mincaps=" + channelInfo.getfilterCapsMinCapitals());
                send(channel, "Emotes: " + channelInfo.getFilterEmotes() + " ~ max=" + channelInfo.getFilterEmotesMax() + ", single=" + channelInfo.getFilterEmotesSingle());
                send(channel, "Symbols: " + channelInfo.getFilterSymbols() + " ~ percent=" + channelInfo.getFilterSymbolsPercent() + ", min=" + channelInfo.getFilterSymbolsMin());
                send(channel, "Caule: " + channelInfo.getFilterCaule());
                send(channel, "Channel Mode: " + channelInfo.getMode());
            }

            if (msg[0].equalsIgnoreCase("me") && msg.length == 2) {
                if (msg[1].equalsIgnoreCase("on")) {
                    channelInfo.setFilterMe(true);
                    send(channel, "Feature: /me filter is on");
                } else if (msg[1].equalsIgnoreCase("off")) {
                    channelInfo.setFilterMe(false);
                    send(channel, "Feature: /me filter is off");
                }
                return;
            }
            
            if (msg[0].equalsIgnoreCase("caule") && msg.length == 2) {
                if (msg[1].equalsIgnoreCase("on")) {
                    channelInfo.setFilterCaule(true);
                    send(channel, "/me Caule NÃO ESTÁ OPEN");
                } else if (msg[1].equalsIgnoreCase("off")) {
                    channelInfo.setFilterCaule(false);
                    send(channel, "/me CAULE ESTÁ OPEN");
                }
                return;
            }

            if (msg[0].equalsIgnoreCase("enablewarnings") && msg.length == 2) {
                if (msg[1].equalsIgnoreCase("on")) {
                    channelInfo.setEnableWarnings(true);
                    send(channel, "Feature: Timeout warnings are on");
                } else if (msg[1].equalsIgnoreCase("off")) {
                    channelInfo.setEnableWarnings(false);
                    send(channel, "Feature: Timeout warnings are off");
                }
            }

            if (msg[0].equalsIgnoreCase("timeoutduration") && msg.length == 2) {
                if (Main.isInteger(msg[1])) {
                    int duration = Integer.parseInt(msg[1]);
                    channelInfo.setTimeoutDuration(duration);
                    send(channel, "Timeout duration is " + channelInfo.getTimeoutDuration());
                } else {
                    send(channel, "You must specify an integer for the duration");
                }
            }

            if (msg[0].equalsIgnoreCase("ignoresubs") && msg.length >= 2) {
                switch(msg[1].toLowerCase()) {
                    case "on":
                        channelInfo.setSubscriberRegulars(true);
                        send(channel, "Subscribers are now immune to filters.");
                        break;
                    case "off":
                        channelInfo.setSubscriberRegulars(false);
                        send(channel, "Subscribers are no longer immune to filters.");
                        break;
                    case "emotes": case "emote":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignoresubs emotes on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setSEmotes(true);
                                send(channel, "Subscribers are now immune to emotes filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setSEmotes(false);
                                send(channel, "Subscribers are no longer immune to emotes filter");
                            }
                        }
                        break;
                    case "links":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignoresubs links on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setSLinks(true);
                                send(channel, "Subscribers are now immune to links filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setSLinks(false);
                                send(channel, "Subscribers are no longer immune to links filter");
                            }
                        }
                        break;
                    case "symbols":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignoresubs symbols on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setSSymbols(true);
                                send(channel, "Subscribers are now immune to symbols filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setSSymbols(false);
                                send(channel, "Subscribers are no longer immune to symbols filter");
                            }
                        }
                        break;
                    case "caule":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignoresubs caule on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setSCaule(true);
                                send(channel, "Subscribers are now immune to caule filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setSCaule(false);
                                send(channel, "Subscribers are no longer immune to caule filter");
                            }
                        }
                        break;
                    case "caps":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignoresubs caps on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setSCaps(true);
                                send(channel, "Subscribers are now immune to caps filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setSCaps(false);
                                send(channel, "Subscribers are no longer immune to caps filter");
                            }
                        }
                        break;
                    case "todos":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignoresubs todos on/off");
                            return;
                        } else {
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setSEmotes(true);
                                channelInfo.setSLinks(true);
                                channelInfo.setSSymbols(true);
                                channelInfo.setSCaule(true);
                                channelInfo.setSCaps(true);
                                send(channel, "Subscribers are now immune to ALL filters");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setSEmotes(false);
                                channelInfo.setSLinks(false);
                                channelInfo.setSSymbols(false);
                                channelInfo.setSCaule(false);
                                channelInfo.setSCaps(false);
                                send(channel, "Subscribers are no longer immune to ALL filters");
                            }
                        }
                        break;
                    default:
                        break;
                    
                }
                
                /*
                if (msg[1].equalsIgnoreCase("on")) {
                    channelInfo.setSubscriberRegulars(true);
                    send(channel, "Subscribers are now immune to filters.");
                } else if (msg[1].equalsIgnoreCase("off")) {
                    channelInfo.setSubscriberRegulars(false);
                    send(channel, "Subscribers are no longer immune to filters.");
                }
                */
            }
            
            if (msg[0].equalsIgnoreCase("ignorevips") && msg.length >= 2) {
                switch(msg[1].toLowerCase()) {
                    case "on":
                        channelInfo.setVipRegulars(true);
                        send(channel, "Vips are now immune to filters.");
                        break;
                    case "off":
                        channelInfo.setVipRegulars(false);
                        send(channel, "Vips are no longer immune to filters.");
                        break;
                    case "emotes": case "emote":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignorevips emotes on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setVEmotes(true);
                                send(channel, "Vips are now immune to emotes filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setVEmotes(false);
                                send(channel, "Vips are no longer immune to emotes filter");
                            }
                        }
                        break;
                    case "caule":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignorevips caule on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setVCaule(true);
                                send(channel, "Vips are now immune to caule filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setVCaule(false);
                                send(channel, "Vips are no longer immune to caule filter");
                            }
                        }
                        break;
                    case "links":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignorevips links on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setVLinks(true);
                                send(channel, "Vips are now immune to links filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setVLinks(false);
                                send(channel, "Vips are no longer immune to links filter");
                            }
                        }
                        break;
                    case "symbols":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignorevips symbols on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setVSymbols(true);
                                send(channel, "Vips are now immune to symbols filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setVSymbols(false);
                                send(channel, "Vips are no longer immune to symbols filter");
                            }
                        }
                        break;
                    case "caps":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignorevips caps on/off");
                            return;
                        } else { 
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setVCaps(true);
                                send(channel, "Vips are now immune to caps filter");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setVCaps(false);
                                send(channel, "Vips are no longer immune to caps filter");
                            }
                        }
                        break;
                    case "todos":
                        if (msg.length == 2) {
                            send(channel, "Syntax: !filter ignorevips todos on/off");
                            return;
                        } else {
                            if (msg[2].equalsIgnoreCase("on")) {
                                channelInfo.setVEmotes(true);
                                channelInfo.setVLinks(true);
                                channelInfo.setVSymbols(true);
                                channelInfo.setVCaule(true);
                                channelInfo.setVCaps(true);
                                send(channel, "VIPs are now immune to ALL filters");
                            } else if (msg[2].equalsIgnoreCase("off")) {
                                channelInfo.setVEmotes(false);
                                channelInfo.setVLinks(false);
                                channelInfo.setVSymbols(false);
                                channelInfo.setVCaule(false);
                                channelInfo.setVCaps(false);
                                send(channel, "VIPs are no longer immune to ALL filters");
                            }
                        }
                        break;
                    default:
                        break;
                    
                }
                
                /*
                if (msg[1].equalsIgnoreCase("on")) {
                    channelInfo.setVipRegulars(true);
                    send(channel, "VIPs are now immune to filters.");
                } else if (msg[1].equalsIgnoreCase("off")) {
                    channelInfo.setVipRegulars(false);
                    send(channel, "VIPs are no longer immune to filters.");
                }
                */
            }


            if (msg[0].equalsIgnoreCase("displaywarnings") && msg.length == 2) {
                if (msg[1].equalsIgnoreCase("on")) {
                    channelInfo.setSignKicks(true);
                    send(channel, "Feature: Display warnings is on");
                } else if (msg[1].equalsIgnoreCase("off")) {
                    channelInfo.setSignKicks(false);
                    send(channel, "Feature: Display warnings is off");
                }
            }

            if (msg[0].equalsIgnoreCase("messagelength") && msg.length == 2) {
                if (Main.isInteger(msg[1])) {
                    channelInfo.setFilterMax(Integer.parseInt(msg[1]));
                    send(channel, "Max message length set to " + channelInfo.getFilterMax());
                } else {
                    send(channel, "Must be an integer.");
                }
            }


            // !links - Owner
            if (msg[0].equalsIgnoreCase("links")) {
                log("RB: Matched command !links");
                if (msg.length == 1) {
                    send(channel, "Syntax: \"!links on/off\"");
                } else if (msg.length == 2) {
                    if (msg[1].equalsIgnoreCase("on")) {
                        channelInfo.setFilterLinks(true);
                        send(channel, "Link filter: " + channelInfo.getFilterLinks());
                    } else if (msg[1].equalsIgnoreCase("off")) {
                        channelInfo.setFilterLinks(false);
                        send(channel, "Link filter: " + channelInfo.getFilterLinks());
                    }
                }
                return;
            }

            // !pd - Owner
            if (msg[0].equalsIgnoreCase("pd")) {
                log("RB: Matched command !pd");
                if (msg.length == 1) {
                    send(channel, "Syntax: \"!pd add/delete [domain]\" and \"!pd list\"");
                } else if (msg.length > 2) {
                    if (msg[1].equalsIgnoreCase("add")) {
                        if (channelInfo.isDomainPermitted(msg[2])) {
                            send(channel, "Domain already exists. " + "(" + msg[2] + ")");
                        } else {
                            channelInfo.addPermittedDomain(msg[2]);
                            send(channel, "Domain added. " + "(" + msg[2] + ")");
                        }
                    } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) {
                        if (channelInfo.isDomainPermitted(msg[2])) {
                            channelInfo.removePermittedDomain(msg[2]);
                            send(channel, "Domain removed. " + "(" + msg[2] + ")");
                        } else {
                            send(channel, "Domain does not exist. " + "(" + msg[2] + ")");
                        }
                    }
                } else if (msg.length > 1 && msg[1].equalsIgnoreCase("list") && isOwner) {
                    String tempList = "Permitted domains: ";
                    for (String s : channelInfo.getpermittedDomains()) {
                        tempList += s + ", ";
                    }
                    send(channel, tempList);
                }
                return;
            }

            // !banphrase - Owner
            if (msg[0].equalsIgnoreCase("banphrase")) {
                log("RB: Matched command !banphrase");
                if (isOwner)
                    log("RB: Is owner");
                if (msg.length == 1) {
                    send(channel, "Syntax: \"!banphrase on/off\", \"!banphrase add/delete [string to purge]\", \"!banphrase list\"");
                } else if (msg.length > 1) {
                    if (msg[1].equalsIgnoreCase("on")) {
                        channelInfo.setFilterOffensive(true);
                        send(channel, "Ban phrase filter is on");
                    } else if (msg[1].equalsIgnoreCase("off")) {
                        channelInfo.setFilterOffensive(false);
                        send(channel, "Ban phrase filter is off");
                    } else if (msg[1].equalsIgnoreCase("clear")) {
                        channelInfo.clearBannedPhrases();
                        send(channel, "Banned phrases cleared.");
                    } else if (msg[1].equalsIgnoreCase("list")) {
                        String tempList = "Banned phrases words: ";
                        for (String s : channelInfo.getOffensive()) {
                            tempList += s + ", ";
                        }
                        send(channel, tempList);
                    } else if (msg[1].equalsIgnoreCase("add") && msg.length > 2) {
                        String phrase = fuseArray(msg, 2);
                        if (phrase.contains(",,")) {
                            send(channel, "Cannot contain double commas (,,)");
                        } else if (channelInfo.isBannedPhrase(fuseArray(msg, 2))) {
                            send(channel, "Word already exists. " + "(" + phrase + ")");
                        } else {
                            if (phrase.startsWith("REGEX:") && !isAdmin) {
                                send(channel, "You must have Admin status to add regex phrases.");
                                return;
                            }
                            channelInfo.addOffensive(phrase);
                            send(channel, "Word added. " + "(" + phrase + ")");
                        }
                    } else if (msg[1].equalsIgnoreCase("severity")) {
                        if (msg.length > 2 && Main.isInteger(msg[2])) {
                            int severity = Integer.parseInt(msg[2]);
                            Config.setProperty(channel,"banPhraseSeverity", String.valueOf(severity));

                            send(channel, "Severity set to " + Config.getProperty(channel, "banPhraseSeverity"));
                        } else {
                            send(channel, "Severity is " + Config.getProperty(channel,"banPhraseSeverity"));
                        }
                    } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove") && msg.length > 2) {
                        String phrase = fuseArray(msg, 2);
                        channelInfo.removeOffensive(phrase);
                        send(channel, "Word removed. " + "(" + phrase + ")");
                    }
                }
                return;
            }

            // !caps - Owner
            if (msg[0].equalsIgnoreCase("caps")) {
                log("RB: Matched command !caps");
                if (msg.length == 1) {
                    send(channel, "Syntax: \"!caps on/off\", \"!caps percent/minchars/mincaps [value]\", \"!caps status\"");
                } else if (msg.length > 1) {
                    if (msg[1].equalsIgnoreCase("on")) {
                        channelInfo.setFilterCaps(true);
                        send(channel, "Caps filter: " + channelInfo.getFilterCaps());
                    } else if (msg[1].equalsIgnoreCase("off")) {
                        channelInfo.setFilterCaps(false);
                        send(channel, "Caps filter: " + channelInfo.getFilterCaps());
                    } else if (msg[1].equalsIgnoreCase("percent")) {
                        if (msg.length > 2) {
                            channelInfo.setfilterCapsPercent(Integer.parseInt(msg[2]));
                            send(channel, "Caps filter percent: " + channelInfo.getfilterCapsPercent());
                        }
                    } else if (msg[1].equalsIgnoreCase("minchars")) {
                        if (msg.length > 2 && Main.isInteger(msg[2])) {
                            channelInfo.setfilterCapsMinCharacters(Integer.parseInt(msg[2]));
                            send(channel, "Caps filter min characters: " + channelInfo.getfilterCapsMinCharacters());
                        }
                    } else if (msg[1].equalsIgnoreCase("mincaps")) {
                        if (msg.length > 2 && Main.isInteger(msg[2])) {
                            channelInfo.setfilterCapsMinCapitals(Integer.parseInt(msg[2]));
                            send(channel, "Caps filter min caps: " + channelInfo.getfilterCapsMinCapitals());
                        }
                    } else if (msg[1].equalsIgnoreCase("status")) {
                        send(channel, "Caps filter=" + channelInfo.getFilterCaps() + ", percent=" + channelInfo.getfilterCapsPercent() + ", minchars=" + channelInfo.getfilterCapsMinCharacters() + ", mincaps=" + channelInfo.getfilterCapsMinCapitals());
                    }
                }
                return;
            }

            // !emotes - Owner
            if (msg[0].equalsIgnoreCase("emotes")) {
                log("RB: Matched command !emotes");
                if (msg.length == 1) {
                    send(channel, "Syntax: \"!emotes on/off\", \"!emotes max [value]\", \"!emotes single on/off\"");
                } else if (msg.length > 1) {
                    if (msg[1].equalsIgnoreCase("on")) {
                        channelInfo.setFilterEmotes(true);
                        send(channel, "Emotes filter: " + channelInfo.getFilterEmotes());
                    } else if (msg[1].equalsIgnoreCase("off")) {
                        channelInfo.setFilterEmotes(false);
                        send(channel, "Emotes filter: " + channelInfo.getFilterEmotes());
                    } else if (msg[1].equalsIgnoreCase("max")) {
                        if (msg.length > 2 && Main.isInteger(msg[2])) {
                            channelInfo.setFilterEmotesMax(Integer.parseInt(msg[2]));
                            send(channel, "Emotes filter max: " + channelInfo.getFilterEmotesMax());
                        }
                    } else if (msg[1].equalsIgnoreCase("status")) {
                        send(channel, "Emotes filter=" + channelInfo.getFilterEmotes() + ", max=" + channelInfo.getFilterEmotesMax() + ", single=" + channelInfo.getFilterEmotesSingle());
                    } else if (msg[1].equalsIgnoreCase("single") && msg.length > 2) {
                        if (msg[2].equalsIgnoreCase("on")) {
                            channelInfo.setFilterEmotesSingle(true);
                            send(channel, "Single Emote filter: " + channelInfo.getFilterEmotesSingle());
                        } else if (msg[2].equalsIgnoreCase("off")) {
                            channelInfo.setFilterEmotesSingle(false);
                            send(channel, "Single Emote filter: " + channelInfo.getFilterEmotesSingle());
                        }
                    }
                }
                return;
            }

            // !symbols - Owner
            if (msg[0].equalsIgnoreCase("symbols")) {
                log("RB: Matched command !symbols");
                if (msg.length == 1) {
                    send(channel, "Syntax: \"!symbols on/off\", \"!symbols percent/min [value]\", \"!symbols status\"");
                } else if (msg.length > 1) {
                    if (msg[1].equalsIgnoreCase("on")) {
                        channelInfo.setFilterSymbols(true);
                        send(channel, "Symbols filter: " + channelInfo.getFilterSymbols());
                    } else if (msg[1].equalsIgnoreCase("off")) {
                        channelInfo.setFilterSymbols(false);
                        send(channel, "Symbols filter: " + channelInfo.getFilterSymbols());
                    } else if (msg[1].equalsIgnoreCase("percent")) {
                        if (msg.length > 2 && Main.isInteger(msg[2])) {
                            channelInfo.setFilterSymbolsPercent(Integer.parseInt(msg[2]));
                            send(channel, "Symbols filter percent: " + channelInfo.getFilterSymbolsPercent());
                        }
                    } else if (msg[1].equalsIgnoreCase("min")) {
                        if (msg.length > 2 && Main.isInteger(msg[2])) {
                            channelInfo.setFilterSymbolsMin(Integer.parseInt(msg[2]));
                            send(channel, "Symbols filter min symbols: " + channelInfo.getFilterSymbolsMin());
                        }
                    } else if (msg[1].equalsIgnoreCase("status")) {
                        send(channel, "Symbols filter=" + channelInfo.getFilterSymbols() + ", percent=" + channelInfo.getFilterSymbolsPercent() + ", min=" + channelInfo.getFilterSymbolsMin());
                    }
                }
                return;
            }


            return;
        }

        // !permit - Allows users to post 1 link
        if ((msg[0].equalsIgnoreCase(prefix + "permit") || msg[0].equalsIgnoreCase(prefix + "allow")) && channelInfo.getFilterLinks() && channelInfo.useFilters && isOp) {
            log("RB: Matched command !permit");
            if (msg.length == 1) {
                send(channel, "Syntax: \"!permit [username]\"");
            } else if (msg.length > 1) {
                if (!channelInfo.isRegular(msg[1])) {
                    channelInfo.permitUser(msg[1]);
                    send(channel, msg[1] + " may now post 1 link.");
                } else {
                    send(channel, msg[1] + " is a regular and does not need to be permitted.");
                }
            }
            return;
        }

        // !regular - Owner
        if (msg[0].equalsIgnoreCase(prefix + "regular") && isOwner) {
            log("RB: Matched command !regular");
            if (msg.length < 2) {
                send(channel, "Syntax: \"!regular add/delete [name]\", \"!regular list\"");
            } else if (msg.length > 2) {
                if (msg[1].equalsIgnoreCase("add")) {
                    if (channelInfo.isRegular(msg[2])) {
                        send(channel, "User already exists." + "(" + msg[2] + ")");
                    } else {
                        channelInfo.addRegular(msg[2]);
                        send(channel, "User added. " + "(" + msg[2] + ")");
                    }
                } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) {
                    if (channelInfo.isRegular(msg[2])) {
                        channelInfo.removeRegular(msg[2]);
                        send(channel, "User removed." + "(" + msg[2] + ")");
                    } else {
                        send(channel, "User does not exist. " + "(" + msg[2] + ")");
                    }
                }
            } else if (msg.length > 1 && msg[1].equalsIgnoreCase("list") && isOwner) {
                String tempList = "Regulars: ";
                for (String s : channelInfo.getRegulars()) {
                    tempList += s + ", ";
                }
                send(channel, tempList);
            }
            return;
        }

        // !mod - Owner
        if (msg[0].equalsIgnoreCase(prefix + "mod") && isOwner) {
            log("RB: Matched command !mod");
            if (msg.length < 2) {
                send(channel, "Syntax: \"!mod add/delete [name]\", \"!mod list\"");
            }
            if (msg.length > 2) {
                if (msg[1].equalsIgnoreCase("add")) {
                    if (channelInfo.isModerator(msg[2])) {
                        send(channel, "User already exists. " + "(" + msg[2] + ")");
                    } else {
                        channelInfo.addModerator(msg[2]);
                        send(channel, "User added. " + "(" + msg[2] + ")");
                    }
                } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) {
                    if (channelInfo.isModerator(msg[2])) {
                        channelInfo.removeModerator(msg[2]);
                        send(channel, "User removed. " + "(" + msg[2] + ")");
                    } else {
                        send(channel, "User does not exist. " + "(" + msg[2] + ")");
                    }
                }
            } else if (msg.length > 1 && msg[1].equalsIgnoreCase("list") && isOwner) {
                String tempList = "Moderators: ";
                for (String s : channelInfo.getModerators()) {
                    tempList += s + ", ";
                }
                send(channel, tempList);
            }
            return;
        }
        
        // !quoteban - Mods
        
        if (msg[0].equalsIgnoreCase(prefix + "quoteban") && isOp) {
            log("RB: Matched command !quoteban");
            if (msg.length == 1) {
                send(channel, "Syntax: \"!quoteban [username]\"");
            }
            if (msg.length > 1) {
                if (channelInfo.isQuoteBanned(msg[1]))
                    send(channel, "User is already quote banned");
                else {
                    channelInfo.addQuoteBanned(msg[1]);
                    send(channel, "User successfully banned. " + "(" + msg[1] + ")");
                }
            }
            return;
        }
        
        // !quoteunban - Mods
        
        if (msg[0].equalsIgnoreCase(prefix + "quoteunban") && isOp) {
            log("RB: Matched command !quoteunban");
            if (msg.length == 1) {
                send(channel, "Syntax: \"!quoteunban [username]\"");
            }
            if (msg.length > 1) {
                if (!channelInfo.isQuoteBanned(msg[1]))
                    send(channel, "User is not quote banned");
                else {
                    channelInfo.removeQuoteBanned(msg[1]);
                    send(channel, "User successfully unbanned. " + "(" + msg[1] + ")");
                }
            }
            return;
        }

        // !owner - Owner
        if (msg[0].equalsIgnoreCase(prefix + "owner") && isBroadcaster) {
            log("RB: Matched command !owner");
            if (msg.length < 2) {
                send(channel, "Syntax: \"!owner add/delete [name]\", \"!owner list\"");
            }
            if (msg.length > 2) {
                if (msg[1].equalsIgnoreCase("add")) {
                    if (channelInfo.isOwner(msg[2])) {
                        send(channel, "User already exists. " + "(" + msg[2] + ")");
                    } else {
                        channelInfo.addOwner(msg[2]);
                        send(channel, "User added. " + "(" + msg[2] + ")");
                    }
                } else if (msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("remove")) {
                    if (channelInfo.isOwner(msg[2])) {
                        channelInfo.removeOwner(msg[2]);
                        send(channel, "User removed. " + "(" + msg[2] + ")");
                    } else {
                        send(channel, "User does not exist. " + "(" + msg[2] + ")");
                    }
                }
            } else if (msg.length > 1 && msg[1].equalsIgnoreCase("list") && isOwner) {
                String tempList = "Owners: ";
                for (String s : channelInfo.getOwners()) {
                    tempList += s + ", ";
                }
                send(channel, tempList);
            }
            return;
        }

        // !massmsg
        if (msg[0].equalsIgnoreCase(prefix + "massmsg") && isAP) {
            log("RB: Matched command !massmsg");

            if (msg.length > 1) {
                String sendmsg = this.fuseArray(msg, 1);
                BotManager.getInstance().sendGlobal(sendmsg, sender);
                return;
            }
            return;
        }
        
        // !wr - all
        if (msg[0].equalsIgnoreCase(prefix + "wr") && channelInfo.checkWR()) {
            LOGGER_D.debug("Matched command !wr");
            LOGGER_D.debug("Retrieving stuff from the API");
            String game = JSONUtil.krakenGame(twitchName);
            String catregex = JSONUtil._getCategories(game);
            Pattern p = Pattern.compile(".*(" + catregex + ").*", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(JSONUtil.krakenStatus(twitchName));
            if (m.find() && msg.length == 1) {
                LOGGER_D.debug("getting category from title");
                send(channel, JSONUtil.getWR(game, m.group(1)));
                return;
            }
            if (msg.length > 1) {
                    String toCheck = this.fuseArray(msg, 1);
                    String categories = JSONUtil.getCategories(game);
                    LOGGER_D.debug("Checking if it is a category");
                    LOGGER_D.debug("Typed: " + toCheck);
                    if (!JSONUtil.isCategory(game, toCheck)) {
                        send(channel, "Please use !wr [category] using one of the categories available: " + categories);
                        return;
                    } else {
                        send(channel, JSONUtil.getWR(game, toCheck));
                        return;
                    }                   
            } else {
                String categories = JSONUtil.getCategories(game);
                send(channel, "Please use !wr [category] using one of the categories available: " + categories);
                return;
            }
        }

        // !set - Owner
        if (msg[0].equalsIgnoreCase(prefix + "set") && isOwner) {
            log("RB: Matched command !set");
            if (msg.length == 1) {
                send(channel, "Syntax: \"!set [option] [value]\". Options: topic, filters, throw, points roulette, rouletteTO signedkicks, quote, lastfm, steam, mode, chatlogging, maxlength");
            } else if (msg[1].equalsIgnoreCase("topic")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setTopicFeature(true);
                    send(channel, "Feature: Topic is on");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setTopicFeature(false);
                    send(channel, "Feature: Topic is off");
                }

            } else if (msg[1].equalsIgnoreCase("subwhisper")) {
                if (msg.length < 3) {
                    send(channel, "New subscriber whispers: " + Config.getProperty(channel,"enableSubWhisper"));
                    send(channel, "New subscriber whisper message: " + Config.getProperty(channel,"subWhisperMessage"));
                } else if (msg[2].equalsIgnoreCase("on") && msg.length == 3) {
                    channelInfo.setEnableSubWhisper(true);
                    send(channel, "Feature: Whispers for new subscribers enabled!");
                } else if (msg[2].equalsIgnoreCase("off") && msg.length == 3) {
                    channelInfo.setEnableSubWhisper(false);
                    send(channel, "Feature: Whispers for new subscribers disabled!");
                } else if (msg[2].equalsIgnoreCase("message") && msg.length > 3) {
                    channelInfo.setSubWhisperMessage(this.fuseArray(msg, 3));
                    send(channel, "Whisper message set to: " + this.fuseArray(msg, 3));
                } else if (msg[2].equalsIgnoreCase("resubmessage") && msg.length > 3) {
                    channelInfo.setResubWhisperMessage(this.fuseArray(msg, 3));
                    send(channel, "Whisper message set to: " + this.fuseArray(msg, 3));
                }
            } else if (msg[1].equalsIgnoreCase("throw")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setThrow(true);
                    send(channel, "Feature: !throw is on");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setThrow(false);
                    send(channel, "Feature: !throw is off");
                }
            } else if (msg[1].equalsIgnoreCase("timezone")) {
                channelInfo.setTimezone(msg[2]);
                send(channel, "Timezone set to: " + msg[2]);
            } else if (msg[1].equalsIgnoreCase("love")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setLove(true);
                    send(channel, "Feature: !love is on");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setLove(false);
                    send(channel, "Feature: !love is off");
                }
            } else if (msg[1].equalsIgnoreCase("8ball")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.set8ball(true);
                    send(channel, "Feature: !8ball is on");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.set8ball(false);
                    send(channel, "Feature: !8ball is off");
                }
            } else if (msg[1].equalsIgnoreCase("youtube")) {
                if (msg.length > 2) {
                    channelInfo.setYoutubeChannelID(msg[2]);
                    send(channel, "Youtube ID Channel set to: " + msg[2]);
                } else {
                    send(channel, "Your youtube ID channel is set to: " + channelInfo.getYoutubeChannelID());
                }
            } else if (msg[1].equalsIgnoreCase("videonovo")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setVideoNovo(true);
                    send(channel, "Recurso Ativado: Video Novo.");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setVideoNovo(false);
                    send(channel, "Recurso Desativado: Video Novo.");
                }
            } else if (msg[1].equalsIgnoreCase("uptime")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setUptime(true);
                    send(channel, "Feature Enabled: !uptime.");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setUptime(false);
                    send(channel, "Feature Disabled: !uptime.");
                }
            } else if (msg[1].equalsIgnoreCase("WR")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setWR(true);
                    send(channel, "Feature: !wr is on");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setWR(false);
                    send(channel, "Feature: !wr is off");
                }
            } else if (msg[1].equalsIgnoreCase("followage")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setFollowage(true);
                    send(channel, "Feature: !followage is on");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setFollowage(false);
                    send(channel, "Feature: !followage is off");
                }
            } else if (msg[1].equalsIgnoreCase("league")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setLeague(true);
                    send(channel, "Feature: !rank is on");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setLeague(false);
                    send(channel, "Feature: !rank is off");
                }
            } else if (msg[1].equalsIgnoreCase("summoner")) {
                String summoner = this.fuseArray(msg, 2);
                channelInfo.setSummoner(summoner);
                send(channel, "Summoner set to: " + summoner);
            } else if (msg[1].equalsIgnoreCase("smurf1")) {
                String smurf1 = this.fuseArray(msg, 2);
                channelInfo.setSmurf1(smurf1);
                send(channel, "Primeira smurf configurada como: " + smurf1);
            } else if (msg[1].equalsIgnoreCase("smurf2")) {
                String smurf2 = this.fuseArray(msg, 2);
                channelInfo.setSmurf2(smurf2);
                send(channel, "Segunda smurf configurada como: " + smurf2);
            } else if (msg[1].equalsIgnoreCase("smurf3")) {
                String smurf3 = this.fuseArray(msg, 2);
                channelInfo.setSmurf3(smurf3);
                send(channel, "Terceira smurf configurada como: " + smurf3);
            } else if (msg[1].equalsIgnoreCase("smurf4")) {
                String smurf4 = this.fuseArray(msg, 2);
                channelInfo.setSmurf4(smurf4);
                send(channel, "Quarta smurf configurada como: " + smurf4);
            } else if (msg[1].equalsIgnoreCase("region")) {
                String region = this.fuseArray(msg, 2);
                channelInfo.setRegion(region);
                send(channel, "Region set to: " + region);
            } else if (msg[1].equalsIgnoreCase("roulette")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setRoulette(true);
                    send(channel, "Feature: !roulette is on");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setRoulette(false);
                    send(channel, "Feature: !roulette is off");
                }
            } else if (msg[1].equalsIgnoreCase("points")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    Points.createTables(twitchName);
                    if (channelInfo.getPts() == null)
                        channelInfo.setPts(new Points());
                    if (channelInfo.getPts().getStatus()) {
                        send(channel, "Points are already active in your channel");
                        return;
                    }
                    channelInfo.getPts().setStatus(true);
                    channelInfo.setPoints(true);
                    send(channel, "Point system activated. Users will receive: " + channelInfo.getPointsAmount() + " " + channelInfo.getPointsName() + " every " + channelInfo.getPointsDelay() + " minutes.");
                    this.startPointsTimer(channelInfo);
                } else if (msg[2].equalsIgnoreCase("off")) {
                    if (channelInfo.getPts() != null) {
                        channelInfo.setPoints(false);
                        send(channel, "Earning points is disabled for this channel now");
                        channelInfo.getPts().setStatus(false);
                        channelInfo.getPts().getTimer().cancel();
                        channelInfo.getPts().getTimer().purge();
                    } else {
                        channelInfo.setPoints(false);
                        send(channel, "Earning points is disabled for this channel now");
                        channelInfo.getPts().setStatus(false);
                        channelInfo.getPts().getTimer().cancel();
                    }
                } else if (msg[2].equalsIgnoreCase("name")) {
                    channelInfo.setPointsName(this.fuseArray(msg, 3));
                    send(channel, "Points name set to: " + this.fuseArray(msg, 3));
                } else if (msg[2].equalsIgnoreCase("delay")) {
                    if (Main.isInteger(msg[3])) {
                        channelInfo.setPointsDelay(Integer.parseInt(msg[3]));
                        if (channelInfo.getPts() != null) {
                            channelInfo.getPts().getTimer().cancel();
                            channelInfo.getPts().getTimer().purge();
                            this.startPointsTimer(channelInfo);
                        }
                        send(channel, "Users will now receive " +  channelInfo.getPointsName() + " every " + msg[3] + "minutes");
                    } else
                        send(channel, "You must enter a valid number");
                } else if (msg[2].equalsIgnoreCase("amount")) {
                    if (Main.isInteger(msg[3])) {
                        if (Integer.parseInt(msg[3]) > 0 && Integer.parseInt(msg[3]) < 100) {
                            channelInfo.setPointsAmount(Integer.parseInt(msg[3]));
                            send(channel, "Points amount changed to: " + msg[3]);
                        } else
                            send(channel, "You must select a number between 1 and 99");

                    } else
                        send(channel, "You must enter a valid number");
                }
            } else if (msg[1].equalsIgnoreCase("symbolsTO")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setSymbolsTO(tempM);
                send(channel, "Timeout message for symbols set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("uptimeMessage")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setUptimeMessage(tempM);
                send(channel, "Uptime message set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("linksTO")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setLinksTO(tempM);
                send(channel, "Timeout message for links set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("maxTO")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setMaxTO(tempM);
                send(channel, "Timeout message for message length set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("offensiveTO")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setOffensiveTO(tempM);
                send(channel, "Timeout message for symbols set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("meTO")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setMeTO(tempM);
                send(channel, "Timeout message for /me set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("capsTO")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setCapsTO(tempM);
                send(channel, "Timeout message for caps set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("emoteTO")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setEmoteTO(tempM);
                send(channel, "Timeout message for emotes set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("emotesingleTO")) {
                String tempM = this.fuseArray(msg, 2);
                channelInfo.setEmoteSingleTO(tempM);
                send(channel, "Timeout message for single emotes set to: " + tempM);
            } else if (msg[1].equalsIgnoreCase("rouletteto")) {
                if (msg.length > 2) {
                    if (Main.isInteger(msg[2])) {
                    int duration = Integer.parseInt(msg[2]);
                    channelInfo.setRouletteTO(duration);
                    send(channel, "Roulette timeout duration is " + channelInfo.getRouletteTO());
                    }
                } else {
                    send(channel, "Roulette timeout duration is: " + channelInfo.getRouletteTO());
                }
            } else if (msg[1].equalsIgnoreCase("quote")) {
                if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setQuote(false);
                    send(channel, "Feature: !quote is off");
                } else if (msg[2].equalsIgnoreCase("on")) {
                try {
                    Quote.createTables(twitchName);
                    channelInfo.setQuote(true);
                    send(channel, "Feature !quote is on");
                } catch (Exception ex) {
                    send(channel, "There was an error enabling quotes");
                    ex.printStackTrace();
                }
                }
            } else if (msg[1].equalsIgnoreCase("stats")) {
                if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setStats(false);
                    send(channel, "Feature: !getstats is off");
                } else if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setStats(true);
                    send(channel, "Feature: !getstats is on");
                }
            }else if (msg[1].equalsIgnoreCase("quotegame")) {
                if (msg[2].equalsIgnoreCase("off")) { 
                    channelInfo.setQuoteGame(false);
                    send(channel, "Games will not be shown with quotes");
                } else if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setQuoteGame(true);
                    send(channel, "Games will be shown with quotes");
                }
            } else if (msg[1].equalsIgnoreCase("quotemods")) {
                if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setQuoteMods(false);
                    send(channel, "Feature: !quote is available to everyone");
                } else if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setQuoteMods(true);
                    send(channel, "Feature: !quote is available to mods only");
                }
            } else if (msg[1].equalsIgnoreCase("music")) {
                if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setMusic(false);
                    send(channel, "Feature: !music is off");
                } else if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setMusic(true);
                    send(channel, "Feature: !music is on");
                }
            } else if (msg[1].equalsIgnoreCase("lastfm")) {
                if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setLastfm("");
                    send(channel, "Feature: Lastfm is off.");
                } else {
                    channelInfo.setLastfm(msg[2]);
                    send(channel, "Feature: Lastfm user set to " + msg[2]);
                }
            } else if (msg[1].equalsIgnoreCase("steam")) {
                if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setSteam("off");
                    send(channel, "Feature: Steam is off.");
                } else {
                    channelInfo.setSteam(msg[2]);
                    send(channel, "Feature: Steam id set to " + msg[2]);
                }
            } else if (msg[1].equalsIgnoreCase("mode")) {
                if (msg.length < 3) {
                    send(channel, "Mode set to " + channelInfo.getMode() + "");
                } else if ((msg[2].equalsIgnoreCase("0") || msg[2].equalsIgnoreCase("owner")) && isBroadcaster) {
                    channelInfo.setMode(0);
                    send(channel, "Mode set to admin/owner only.");
                } else if (msg[2].equalsIgnoreCase("1") || msg[2].equalsIgnoreCase("mod")) {
                    channelInfo.setMode(1);
                    send(channel, "Mode set to admin/owner/mod only.");
                } else if (msg[2].equalsIgnoreCase("2") || msg[2].equalsIgnoreCase("everyone")) {
                    channelInfo.setMode(2);
                    send(channel, "Mode set to everyone.");
                } else if (msg[2].equalsIgnoreCase("-1") || msg[2].equalsIgnoreCase("admin") && isAdmin) {
                    channelInfo.setMode(-1);
                    send(channel, "Special moderation mode activated.");
                }
            } else if (msg[1].equalsIgnoreCase("commerciallength")) {
                if (msg.length > 2) {
                    int cLength = Integer.parseInt(msg[2]);
                    if (cLength == 30 || cLength == 60 || cLength == 90 || cLength == 120 || cLength == 150 || cLength == 180) {
                        channelInfo.setCommercialLength(cLength);
                        send(channel, "Commercial length is set to " + channelInfo.getCommercialLength() + " seconds.");
                    }
                } else {
                    send(channel, "Commercial length is " + channelInfo.getCommercialLength() + " seconds.");
                }
            } else if (msg[1].equalsIgnoreCase("tweet")) {
                if (msg.length < 3) {
                    send(channel, "ClickToTweet format: " + channelInfo.getClickToTweetFormat());
                } else {
                    String format = fuseArray(msg, 2);
                    if (!format.contains("(_TWEET_URL_)")) {
                        channelInfo.setClickToTweetFormat(format);
                        send(channel, "ClickToTweet format: " + channelInfo.getClickToTweetFormat());
                    } else {
                        send(channel, "_TWEET_URL_ is not allowed.");
                    }

                }
            } else if (msg[1].equalsIgnoreCase("prefix")) {
                if (msg.length > 2) {
                    if (msg[2].length() > 1) {
                        send(channel, "Prefix may only be 1 character.");
                    } else {
                        channelInfo.setPrefix(msg[2]);
                        send(channel, "Command prefix is " + channelInfo.getPrefix());
                    }
                } else {
                    send(channel, "Command prefix is " + channelInfo.getPrefix());
                }
            } else if (msg[1].equalsIgnoreCase("emoteset") && msg.length > 2) {
                channelInfo.setEmoteSet(msg[2]);
                send(channel, "Emote set ID set to " + channelInfo.getEmoteSet());
            } else if (msg[1].equalsIgnoreCase("subscriberregulars")) {
                if (msg[2].equalsIgnoreCase("on")) {
                    channelInfo.setSubscriberRegulars(true);
                    send(channel, "Subscribers will now be treated as regulars.");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    channelInfo.setSubscriberRegulars(false);
                    send(channel, "Subscribers will no longer be treated as regulars.");
                }
            } else if (msg[1].equalsIgnoreCase("subscriberalerts")) {
                if (msg.length < 3) {
                    send(channel, "Subscriber alerts: " + Config.getProperty(channel,"subscriberAlert"));
                    send(channel, "Subscriber alert message: " + Config.getProperty(channel,"subMessage"));
                } else if (msg[2].equalsIgnoreCase("on")) {
                    Config.setProperty(channel,"subscriberAlert", String.valueOf(true));
                    send(channel, "Subscriber alerts enabled.");
                } else if (msg[2].equalsIgnoreCase("off")) {
                    Config.setProperty(channel,"subscriberAlert", String.valueOf(false));
                    send(channel, "Subscriber alerts disabled.");
                } else if (msg[2].equalsIgnoreCase("message") && msg.length > 3) {
                    Config.setProperty(channel,"subMessage", fuseArray(msg, 3));
                    send(channel, "Subscriber alert message set to: " + Config.getProperty(channel,"subMessage"));
                }
            } else if (msg[1].equalsIgnoreCase("resubalert")) {
                if (msg.length < 3) {
                    send(channel, "Resubscriber alerts: " + Config.getProperty(channel,"subscriberAlert"));
                    send(channel, "Resubscriber alert message: " + Config.getProperty(channel,"resubMessage"));
                } else if (msg[2].equalsIgnoreCase("message") && msg.length > 3) {
                    Config.setProperty(channel,"resubMessage", fuseArray(msg, 3));
                    send(channel, "Resubscriber alert message set to: " + Config.getProperty(channel,"resubMessage"));
                }
            }
            return;
        }


        //!modchan - Mod
        if (msg[0].equalsIgnoreCase(prefix + "modchan") && isAdmin) {
            log("RB: Matched command !modchan");
            if (channelInfo.getMode() == 2) {
                channelInfo.setMode(1);
                send(channel, "Mode set to admin/owner/mod only.");
            } else if (channelInfo.getMode() == 1) {
                channelInfo.setMode(2);
                send(channel, "Mode set to everyone.");
            } else {
                send(channel, "Mode can only be changed by bot admin.");
            }
            return;
        }


        //!join
        
        if (msg[0].equalsIgnoreCase(prefix + "join") && channel.equalsIgnoreCase("#lucas")) {
            log("RB: Matched command !join");
            //send(channel, "Essa é a versão VIP do bot e canais so podem ser adicionados manualmente");

            if (!BotManager.getInstance().publicJoin) {
                send(channel, "Entre em contato com um admin em http://bit.ly/PriestBotDiscord (Sala: #solicitacao-bot) para adicionar o bot no seu canal.");
                return;
            }

            if (JSONUtil.krakenChannelExist(sender)) {
                send(channel, "Joining channel #" + sender + ".");
                boolean joinStatus = BotManager.getInstance().addChannel("#" + sender, 2);
                if (joinStatus) {
                    send(channel, "Channel #" + sender + " joined.");
                } else {
                    send(channel, "Already in channel #" + sender + ".");
                }
            } else {
                send(channel, "Unable to join " + sender + ". Try again later");
            }

            return;
        }
    

        if (msg[0].equalsIgnoreCase(prefix + "rejoin") && isSubAdmin) {
            log("RB: Matched command !rejoin");
            if (msg.length > 1) {
                if (msg[1].contains("#")) {
                    send(channel, "Rejoining channel " + msg[1] + ".");
                    boolean joinStatus = BotManager.getInstance().rejoinChannel(msg[1]);
                    if (joinStatus) {
                        send(channel, "Channel " + msg[1] + " rejoined.");
                    } else {
                        send(channel, "Bot is not assigned to channel " + msg[1] + ".");
                    }

                } else {
                    send(channel, "Invalid channel format. Must be in format #channelname.");
                }
            } else {
                send(channel, "Rejoining channel #" + sender + ".");
                boolean joinStatus = BotManager.getInstance().rejoinChannel("#" + sender);
                if (joinStatus) {
                    send(channel, "Channel #" + sender + " rejoined.");
                } else {
                    send(channel, "Bot is not assigned to channel #" + sender + ".");
                }
            }
            return;
        }

        // ********************************************************************************
        // ********************************* League Stuff *********************************
        // ********************************************************************************

        if (msg[0].equalsIgnoreCase(prefix + "rank") && channelInfo.checkLeague()) {
            log("RB: Matched command !rank");
            String region = "br";
            String summoner = "";
            boolean found = false;
            if (msg.length >= 2) {
                String match = this.fuseArray(msg, 1).toLowerCase();
                Pattern p = Pattern.compile("(.*?)\\b(br|na|euw|eune|kr|las|lan|oce|tr|ru)\\b.*", Pattern.CASE_INSENSITIVE);
                Pattern p2 = Pattern.compile("\\b(br|na|euw|eune|kr|las|lan|oce|tr|ru)\\b(.*?)", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(match);
                Matcher m2 = p2.matcher(match);
                if (m.matches()) {
                    summoner = m.group(1);
                    summoner = summoner.toLowerCase().trim();
                }
                while (m2.find()) {
                    region = m2.group(0);
                    region = region.toLowerCase();
                    summoner = match.replaceAll("\\b(br|na|euw|eune|kr|las|lan|oce|tr|ru)\\b(.*?)", "").trim();
                    found = true;
                }

                if (!found) {
                    summoner = match;
                    System.out.println("Region not entered, so defaulting to channel setting");
                }


                String rank = JSONUtil.getRank(JSONUtil.getId(summoner, region), region);
                System.out.println("Summoner: " + summoner + " Region: " + region);
                send(channel, summoner + "'s rank is: " + rank);
                return;
            } else {
                region = channelInfo.getRegion();
                summoner = channelInfo.getSummoner();
                String rank = JSONUtil.getRank(JSONUtil.getId(summoner, region), region);
                send(channel, summoner + "'s rank is: " + rank);
            }
         return;
        }

        if (msg[0].equalsIgnoreCase(prefix + "getstats") && (channelInfo.checkStats() || isAdmin)) {
            LOGGER_D.debug("Matched command !getstats");
            String region = channelInfo.getRegion();
            String summoner = "";
            boolean found = false;

            if (msg.length == 1) {
                LOGGER_D.debug("No arguments passed");
                send(channel, JSONUtil.getStatsFromLastGame(JSONUtil.getId(channelInfo.getSummoner(), region), region));
            } else if (msg.length >= 2) {
                String match = this.fuseArray(msg, 1).toLowerCase();
                Pattern p = Pattern.compile("(.*?)\\b(br|na|euw|eune|kr|las|lan|oce|tr|ru)\\b.*", Pattern.CASE_INSENSITIVE);
                Pattern p2 = Pattern.compile("\\b(br|na|euw|eune|kr|las|lan|oce|tr|ru)\\b(.*)", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(match);
                Matcher m2 = p2.matcher(match);

                if (m.matches()) {
                    summoner = m.group(1);
                    summoner = summoner.toLowerCase().trim();
                }
                while (m2.find()) {
                    region = m2.group(0);
                    region = region.toLowerCase();
                    summoner = Helper.replaceLast(match, region, "");
                    found = true;
                }

                if (!found) {
                    summoner = match;
                }

                send(channel, JSONUtil.getStatsFromLastGame(JSONUtil.getId(summoner, region), region));

            }
            return;
        }

        // ********************************************************************************
        // **************************** Administration Commands ***************************
        // ********************************************************************************
        if (msg[0].equalsIgnoreCase(prefix + "sendWhisper") && isAdmin && msg.length > 1) {
            String toSend = this.fuseArray(msg, 2);
            try {
                this.sendWhisper(this.getName(), msg[1], toSend);
                send(channel, "Whisper sent successfully");
            } catch (Exception e) {
                e.printStackTrace();
                send(channel, "Sending whisper failed.");
            }
        }
        
        if (msg[0].equalsIgnoreCase(prefix + "addhelper") && isSuperAdmin && msg.length > 1) {
            if (Config.isSubAdmin(msg[1])) {
                send(channel, "User is already a subadmin.");
                return;
            }
            Config.addSubAdmin(msg[1], sender);
            send(channel, "User ID: " + msg[1] + " has been successfully added as a subadmin");
            return;
        }
        
        if (msg[0].equalsIgnoreCase(prefix + "delhelper") && isSuperAdmin && msg.length > 1) {
            if (!Config.isSubAdmin(msg[1])) {
                send(channel, "User is not a subadmin.");
                return;
            }
            Config.removeSubAdmin(msg[1]);
            send(channel, "User ID: " + msg[1] + " has been successfully removed as a subadmin");
            return;
        }
        
        if (msg[0].equalsIgnoreCase(prefix + "addsuperadmin") && isAP && msg.length > 1) {
            if (Config.isSuperAdmin(msg[1])) {
                send(channel, "User is already a superadmin.");
                return;
            }
            Config.addSuperAdmin(msg[1], sender);
            send(channel, "User ID: " + msg[1] + " has been successfully added as a superadmin");
            return;
        }
        
        if (msg[0].equalsIgnoreCase(prefix + "delsuperadmin") && isAP && msg.length > 1) {
            if (!Config.isSuperAdmin(msg[1])) {
                send(channel, "User is not a superadmin.");
                return;
            }
            Config.removeSuperAdmin(msg[1]);
            send(channel, "User ID: " + msg[1] + " has been successfully removed as a superadmin.");
            return;
        }


        if (msg[0].equalsIgnoreCase(prefix + "admin") && msg.length > 1) {
            if (msg[1].equalsIgnoreCase("channels") && isAdmin) {
                send(channel, "Currently in " + BotManager.getInstance().channelList.size() + " channels.");
                /*
                String channelString = "";
                for (Map.Entry<String, Channel> entry : BotManager.getInstance().channelList.entrySet()) {
                    channelString += entry.getValue().getChannel() + ", ";
                }
                send(channel, "Channels: " + channelString);
                return;
                */
            } else if (msg[1].equalsIgnoreCase("join") && isSubAdmin && msg.length > 2) {
                if (msg[2].contains("#")) {
                    String toJoin = msg[2];
                    int mode = -1;
                    if (msg.length > 3 && Main.isInteger(msg[3]))
                        mode = Integer.parseInt(msg[3]);
                    send(channel, "Joining channel " + toJoin + " with mode (" + mode + ").");
                    boolean joinStatus = BotManager.getInstance().addChannel(toJoin, mode);
                    if (joinStatus) {
                        send(channel, "Channel " + toJoin + " joined.");
                    } else {
                        send(channel, "Already in channel " + toJoin + ".");
                    }

                } else {
                    send(channel, "Invalid channel format. Must be in format #channelname.");
                }
                return;
            } else if (msg[1].equalsIgnoreCase("part") && isSubAdmin && msg.length > 2) {
                if (msg[2].contains("#")) {
                    String toPart = msg[2];
                    send(channel, "Channel " + toPart + " parting...");
                    BotManager.getInstance().removeChannel(toPart);
                    send(channel, "Channel " + toPart + " parted.");
                } else {
                    send(channel, "Invalid channel format. Must be in format #channelname.");
                }
                return;
            } else if (msg[1].equalsIgnoreCase("reconnect") && isAP) {
                send(channel, "Reconnecting all servers.");
                BotManager.getInstance().reconnectAllBotsSoft();
                return;
            } else if (msg[1].equalsIgnoreCase("reload") && isAP && msg.length > 2) {
                if (msg[2].contains("#")) {
                    String toReload = msg[2];
                    send(channel, "Reloading channel " + toReload);
                    BotManager.getInstance().reloadChannel(toReload);
                    send(channel, "Channel " + toReload + " reloaded.");
                } else {
                    send(channel, "Invalid channel format. Must be in format #channelname.");
                }
                return;
            } else if (msg[1].equalsIgnoreCase("clone") && isSuperAdmin && msg.length > 3) {
                if (msg[2].startsWith("#") && msg[3].startsWith("#")) {
                    String src = msg[2];
                    String dest = msg[3];
                    try {
                        Config.cloneChannel(src, dest);
                    } catch (Exception e) {
                        e.printStackTrace();
                        send(channel, "An exception occurred running this command");
                    }
                    /*
                    try {
                        BotManager.getInstance().cloneConfig(src, dest);
                    } catch (IOException ioE) {
                        ioE.printStackTrace();
                        send(channel, "An IO exception occurred running this command and thus it was not successful.");
                    }
                    */

                    send(channel, "Channel " + src + " has been cloned to " + dest);
                    BotManager.getInstance().reloadChannel(dest);
                    send(channel, "Attempting to reload " + dest);
                } else {
                    send(channel, "Invalid channel format. Must be in format #channelname.");
                }
                return;
            } else if (msg[1].equalsIgnoreCase("color") && isAdmin && msg.length > 2) {
                sendCommand(channel, ".color " + msg[2]);
                send(channel, "Color set to " + msg[2]);
                return;
            } else if (msg[1].equalsIgnoreCase("loadfilter") && isAP) {
                BotManager.getInstance().loadGlobalBannedWords();
                BotManager.getInstance().loadBanPhraseList();
                send(channel, "Global banned filter reloaded.");
                return;
            } else if (msg[1].equalsIgnoreCase("add")) {
                if (isSuperAdmin) {
                    if (msg.length > 2) {
                        if (Config.isAdmin(msg[2])) {
                            send(channel, "User is already an admin.");
                            return;
                        }
                        Config.addAdmin(msg[2], sender);
                        send(channel, "User ID: " + msg[2] + " has been successfully added as an admin");
                        return;
                    } else {
                        send(channel, "You must specify a user ID");
                        return;
                    }
                } else {
                    send(channel, "You are not authorized to perform this action");
                    return;
                }
            } else if (msg[1].equalsIgnoreCase("remove") || msg[1].equalsIgnoreCase("delete") || msg[1].equalsIgnoreCase("del")) {
                if (isSuperAdmin) {
                    if (msg.length > 2) {
                        if (!Config.isAdmin(msg[2])) {
                            send(channel, "User is NOT an admin.");
                            return;
                        }
                        Config.removeAdmin(msg[2]);
                        send(channel, "User ID: " + msg[2] + " has been successfully removed as an admin");
                        return;
                    } else {
                        send(channel, "You must specify a user ID");
                        return;
                    }
                } else {
                    send(channel, "You are not authorized to perform this action");
                    return;
                }
            } else if (msg[1].equalsIgnoreCase("spam") && isAdmin) {
                if (msg.length > 3 && Main.isInteger(msg[2])) {
                    String toSpam = fuseArray(msg, 3);
                    for (int i = 0; i < Integer.parseInt(msg[2]); i++)
                        send(channel, toSpam + " " + (i + 1));
                    return;
                }
            }
        }
        // ********************************************************************************
        // ***************************** Info/Catch-all Command ***************************
        // ********************************************************************************

        if (msg[0].substring(0, 1).equalsIgnoreCase(prefix)) {
            String command = msg[0].substring(1);
            String value = channelInfo.getCommand(command);
            if (value != null) {
                log("RB: Matched command " + msg[0]);
                if (msg.length > 1) {
                    if (channelInfo.checkCommandRestriction(command, accessLevel)) {
                            channelInfo.incCommandsCount(command);
                            LOGGER_D.debug("Finished incrementing");
                            String fullArgs = fuseArray(msg, 1);
                            String[] args = fullArgs.trim().split(" ");
                            send(channel, sender, command, value, args);
                        /* String updatedMessage = fuseArray(msg, 1);
                        if (!updatedMessage.contains(",,")) {
                            channelInfo.setCommand(command, updatedMessage, sender);
                            LOGGER_D.debug("Logging updated command");
                            try {
                                Logging.logCommand(channel, command, updatedMessage, sender);
                            } catch(Exception e) {
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                            }
                            send(channel, "Command updated.");
                        } else {
                            send(channel, "Command cannot contain double commas (\",,\").");
                        }
                         */
                    }
                } else {
                    if (channelInfo.checkCommandRestriction(command, accessLevel)) {
                        channelInfo.incCommandsCount(command);
                        LOGGER_D.debug("Finished incrementing, now logging command.");
                        /*
                        try {
                            Logging.logCommand(channel, command, "null", sender, time);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        */
                        send(channel, sender, command, value);
                    }
                }

            }

        }

        // ********************************************************************************
        // *********************************** Auto Reply *********************************
        // ********************************************************************************
        for (int i = 0; i < channelInfo.autoReplyTrigger.size(); i++) {
            Matcher m = channelInfo.autoReplyTrigger.get(i).matcher(message);
            if (m.matches()) {

                if (!channelInfo.onCooldown(channelInfo.autoReplyTrigger.get(i).toString()))
                    send(channel, sender, channelInfo.autoReplyResponse.get(i));
            }
        }
    }

    protected void onNewSubscriberWhisper(Channel channel, String username) {
        LOGGER_D.debug("RB: New subscriber in " + channel.getTwitchName() + " " + username);
        if (channel.checkSubWhisper()) {
            sendWhisper(this.getName(), username, channel.getSubWhisperMessage());
        }
    }

    protected void onNewSubscriberWhisper(Channel channel, String username, String months) {
        LOGGER_D.debug("RB: New resubscriber in " + channel.getTwitchName() + " " + username);
        if (channel.checkSubWhisper()) {
            sendWhisper(this.getName(), username, channel.getResubWhisperMessage());
        }
    }
    
    protected void onNewSubscriberWhisper(Channel channel, String username, String months, String streak) {
        LOGGER_D.debug("RB: New resubscriber in " + channel.getTwitchName() + " " + username);
        if (channel.checkSubWhisper()) {
            sendWhisper(this.getName(), username, channel.getResubWhisperMessage());
        }
    }

    protected void onNewSubscriber(Channel channel, String username) {
        LOGGER_D.debug("RB: New subscriber in " + channel.getTwitchName() + " " + username);
        if (Boolean.parseBoolean(Config.getProperty(channel.getChannel(),"subscriberAlert"))) {
            String msgFormat = Config.getProperty(channel.getChannel(),"subMessage");
            send(channel.getChannel(), null, null, msgFormat, new String[]{username});
        }
    }

    protected void onNewSubscriber(Channel channel, String username, String months) {
        LOGGER_D.debug("RB: New resubscriber in " + channel.getTwitchName() + " - " + username);
        if (Boolean.parseBoolean(Config.getProperty(channel.getChannel(),"subscriberAlert"))) {
            String msgFormat = Config.getProperty(channel.getChannel(),"resubMessage");
            send(channel.getChannel(), null, null, msgFormat, new String[]{username, months});
        }
    }
    
    protected void onNewSubscriber(Channel channel, String username, String months, String streak) {
        LOGGER_D.debug("RB: New resubscriber in " + channel.getTwitchName() + " - " + username);
        if (Boolean.parseBoolean(Config.getProperty(channel.getChannel(),"subscriberAlert"))) {
            String msgFormat = Config.getProperty(channel.getChannel(),"resubMessage");
            send(channel.getChannel(), null, null, msgFormat, new String[]{username, months, streak});
        }
    }
    
    private Map<String, String> mapTags(String rawTags) {
        Map<String, String> tags = new HashMap<String, String>();

        StringTokenizer tokenizer = new StringTokenizer(rawTags);

        while (tokenizer.hasMoreTokens()) {
            String tag = tokenizer.nextToken(";");
            if (tag.contains("=")) {
                String[] parts = tag.split("=");
                tags.put(parts[0], (parts.length == 2 ? parts[1] : null));
            } else {
                tags.put(tag, null);
            }
        }

        return tags;
    }

    private void setRandomNickColor() {
        if (!BotManager.getInstance().randomNickColor)
            return;

        countToNewColor--;

        if (countToNewColor == 0) {
            countToNewColor = BotManager.getInstance().randomNickColorDiff;
            Color newColor = new Color(Color.HSBtoRGB(random.nextFloat(), 1.0f, 0.65f));
            System.out.println(newColor.toString());
            String hexColor = String.format("#%06X", (0xFFFFFF & newColor.getRGB()));
            System.out.println("New color: " + hexColor);
            sendCommand("#" + getNick(), ".color " + hexColor);
        }

    }

    @Override
    public void onDisconnect() {
        lastPing = -1;
        try {
            LOGGER_D.debug("INFO: Internal reconnection: " + this.getServer());
            String[] channels = this.getChannels();
            this.reconnect();
            for (int i = 0; i < channels.length; i++) {
                this.joinChannel(channels[i]);
            }
        } catch (NickAlreadyInUseException e) {
            logMain("RB: [ERROR] Nickname already in use - " + this.getNick() + " " + this.getServer());
        } catch (IOException e) {
            logMain("RB: [ERROR] Unable to connect to server - " + this.getNick() + " " + this.getServer());
        } catch (IrcException e) {
            logMain("RB: [ERROR] Error connecting to server - " + this.getNick() + " " + this.getServer());
        }

    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname) {
        Channel channelInfo = getChannelObject(channel);

        if (channelInfo == null)
            return;

        if (this.getNick().equalsIgnoreCase(sender)) {
            synchronized (joinedChannels) {
                //log("RB: Got self join for " + channel);
                if (!joinedChannels.contains(channel)) {
                    LOGGER_D.debug("DEBUG: Got self join on " + channel);
                    LOGGER_D.debug("DEBUG: Got JOIN for " + joinedChannels.size() + " " + this.getChannels().length);
                    LOGGER_D.debug("+ Adding " + channel);
                    joinedChannels.add(channel);
                }
            }
        }
    }
    
    public void send(String target, String sender, String command, String message) {
        send(target, sender, command, message, null);
    }

    public void send(String target, String sender, String message) {
        send(target, sender, null, message, null);
    }

    public void send(String target, String message) {
        send(target, null, null, message, null);
    }

    public void send(String target, String sender, String command, String message, String[] args) {
        // Channel channelInfo = getChannelObject(target);

        if (!BotManager.getInstance().verboseLogging)
            logMain("SEND: " + target + " " + getNick() + " : " + message);

        message = MessageReplaceParser.parseMessage(target, sender, command, message, args);
        boolean useBullet = false;

        if (message.startsWith("/me "))
            useBullet = false;

        //Split if message > X characters
        List<String> chunks = Splitter.fixedLength(500).splitToList(message);
        int c = 1;
        for (String chunk : chunks) {
            sendMessage(target, (useBullet ? getBullet() + " " : "") + (chunks.size() > 1 ? "[" + c + "] " : "") + chunk);
            c++;
            useBullet = false;
        }

        setRandomNickColor();
    }

    public void sendCommand(String target, String message) {
        sendMessage(target, message);
    }

    @Override
    public void onServerPing(String response) {
        super.onServerPing(response);
        lastPing = (int) (System.currentTimeMillis() / 1000);
    }

    public void log(String line) {
        if (this.getVerbose()) {
            logMain(System.currentTimeMillis() + " " + line);
        }
    }

    public void logMain(String line) {
        BotManager.getInstance().log(line);
    }
    

    public void updateMessagesMin() {
        messageUpdater = new Timer();

        int delay = 60 * 1000;

        messageUpdater.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LOGGER_D.debug("Running message per minute updater");
                messagesPerMin = messageCount;
                messageCount = 0;
            }
        }, delay, delay);
    }

    public void startJoinCheck() {

        joinCheck = new Timer();

        int delay = 720000;

        joinCheck.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                LOGGER_D.debug("Running joined list comparison");
                //String[] currentChanList = ReceiverBot.this.getChannels();
                for (Map.Entry<String, Channel> entry : BotManager.getInstance().channelList.entrySet()) {
                    boolean inList = false;
                    if (joinedChannels.contains(entry.getValue().getChannel()))
                        inList = true;

//                    for (String c : currentChanList) {
//                        if (entry.getValue().getChannel().equals(c))
//                            inList = true;
//                    }

                    if (!inList) {
                        LOGGER_D.debug(entry.getValue().getChannel() + " is not in the joined list.");
                        //ReceiverBot.this.joinChannel(entry.getValue().getChannel());
                        BotManager.getInstance().removeChannel(entry.getValue().getChannel());
                    }

                }
            }
        }, delay * 15, delay);

    }

    private void startAutoHostTimer(Channel channelInfo, String channelName) {
        if (channelInfo.getAH() != null) {
            channelInfo.getAH().setTimer(new Timer());
            int delay = 1800;
            if (!channelInfo.getAH().getStatus()) {
                channelInfo.getAH().setStatus(true);
            }

            channelInfo.getAH().getTimer().scheduleAtFixedRate(new autoHostTimer(channelInfo, channelName), (15 * 1000), (delay * 1000));
        }
    }

    private void startPointsTimer(Channel channelInfo) {
        if (channelInfo.getPts() != null) {
            channelInfo.getPts().setTimer(new Timer());
            int delay = channelInfo.getPointsDelay() * 60;
            if (!channelInfo.getPts().getStatus())
                channelInfo.getPts().setStatus(true);

            channelInfo.getPts().getTimer().scheduleAtFixedRate(new pointsTimer(channelInfo), (delay * 1000), (delay * 1000));
        }

    }

    private int getSymbolsNumber(String s) {
        int symbols = 0;
        for (Pattern p : symbolsPatterns) {
            Matcher m = p.matcher(s);
            while (m.find())
                symbols += 1;
        }
        return symbols;
    }

    private int getCapsNumber(String s) {
        int caps = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                caps++;
            }
        }

        return caps;
    }

    private boolean containsLink(String message, Channel ch) {
        String[] splitMessage = message.toLowerCase().split(" ");
        for (String m : splitMessage) {
            for (Pattern pattern : linkPatterns) {
                //System.out.println("Checking " + m + " against " + pattern.pattern());
                Matcher match = pattern.matcher(m);
                if (match.matches()) {
                    log("RB: Link match on " + pattern.pattern());
                    if (ch.checkPermittedDomain(m))
                        return false;
                    else
                        return true;
                }
            }
        }
//        for (Pattern pattern : linkPatterns) {
//            System.out.println("Checking " + message + " against " + pattern.pattern());
//            Matcher match = pattern.matcher(message);
//            if (match.matches()) {
//                System.out.println("Bypass match");
//                return true;
//            }
//        }
        return false;
    }
/*
    private int countEmotes(String message) {
        String str = message;
        int count = 0;
        for (String findStr : BotManager.getInstance().emoteSet) {
            int lastIndex = 0;
            while (lastIndex != -1) {

                lastIndex = str.indexOf(findStr, lastIndex);

                if (lastIndex != -1) {
                    count++;
                    lastIndex += findStr.length();
                }
            }
        }
        return count;
    }
*/
    private boolean checkSingleEmote(String message, String emote_tags) {
        if (emote_tags == null)
            return false;

        String[] emotes = emote_tags.split("/");
        int length = message.length();

        if (emotes.length > 1)
            return false;

        for (String e : emotes) {
            if (e.contains(","))
                continue;

            String id = e.substring(0, e.indexOf(":"));
            int first_char = Integer.parseInt(e.substring(e.indexOf(":") + 1, e.indexOf("-")));
            int last_char = Integer.parseInt(e.substring(e.indexOf("-") + 1));

            if (message.startsWith("/me ")) {
                //first_char -= 8;
                //last_char -= 8;
                length -= 4;
            }

            if (first_char == 0 && last_char == length - 1)
                return true;
        }


        return false;
    }

    public boolean isGlobalBannedWord(String message) {
        for (Pattern reg : BotManager.getInstance().globalBannedWords) {
            Matcher match = reg.matcher(message.toLowerCase());
            if (match.matches()) {
                log("RB: Global banned word matched: " + reg.toString());
                return true;
            }
        }
        return false;
    }

    private String getTimeoutText(int count, Channel channel) {
        if (channel.getEnableWarnings()) {
            if (count > 1) {
                return "temp ban";
            } else {
                return "warning";
            }
        } else {
            return "temp ban";
        }
    }

    private int getTODuration(int count, Channel channel) {
        if (channel.getEnableWarnings()) {
            if (count > 1) {
                return channel.getTimeoutDuration();
            } else {
                return 10;
            }
        } else {
            return channel.getTimeoutDuration();
        }
    }

    private void secondaryTO(final String channel, final String name, final int duration, FilterType type, String message) {


        String line = "FILTER: Issuing a timeout on " + name + " in " + channel + " for " + type.toString() + " (" + duration + ")";
        logMain(line);
        line = "FILTER: Affected Message: " + message;
        logMain(line);
        Config.logTimeout(channel, name, "null", message, type.toString(), duration);

        int iterations = BotManager.getInstance().multipleTimeout;

        for (int i = 0; i < iterations; i++) {
            Timer timer = new Timer();
            int delay = 500 * i;
            timer.schedule(new TimerTask() {
                public void run() {
                    ReceiverBot.this.sendCommand(channel, ".timeout " + name + " " + duration);
                }
            }, delay);
        }


        //Send to subscribers
        Channel channelInfo = getChannelObject(channel);
        if (BotManager.getInstance().wsEnabled)
            BotManager.getInstance().ws.sendToSubscribers(line, channelInfo);

    }

    private void secondaryBan(final String channel, final String name, FilterType type) {


        String line = "RB: Issuing a ban on " + name + " in " + channel + " for " + type.toString();
        logMain(line);

        int iterations = BotManager.getInstance().multipleTimeout;
        for (int i = 0; i < iterations; i++) {
            Timer timer = new Timer();
            int delay = 1000 * i;
            System.out.println("Delay: " + delay);
            timer.schedule(new TimerTask() {
                public void run() {
                    ReceiverBot.this.sendCommand(channel, ".ban " + name);
                }
            }, delay);
        }


        //Send to subscribers
        Channel channelInfo = getChannelObject(channel);
        if (BotManager.getInstance().wsEnabled)
            BotManager.getInstance().ws.sendToSubscribers(line, channelInfo);

    }

    private void startGaTimer(int seconds, Channel channelInfo) {
        if (channelInfo.getGiveaway() != null) {
            channelInfo.getGiveaway().setTimer(new Timer());
            int delay = seconds * 1000;

            if (!channelInfo.getGiveaway().getStatus()) {
                channelInfo.getGiveaway().setStatus(true);
                send(channelInfo.getChannel(), "> Giveaway started. (" + seconds + " seconds)");
            }

            channelInfo.getGiveaway().getTimer().schedule(new giveawayTimer(channelInfo), delay);
        }
    }
    
    private void startBetTimer(Channel channelInfo, int delay) {
        if (channelInfo.getBet() != null) {
            channelInfo.getBet().setBetTimer(new Timer());
            delay = delay * 60 * 1000;
            
            if (!channelInfo.getBet().getStatus())
                channelInfo.getBet().setStatus(true);
            
            channelInfo.getBet().getBetTimer().schedule(new betTimer(channelInfo), delay);
        }
    }

    private void startGambleTimer(Channel channelInfo) {
        if (channelInfo.getGamble() != null) {
            channelInfo.getGamble().setTimer(new Timer());
            int delay = 120 * 1000;

            if (!channelInfo.getGamble().getStatus()) {
                channelInfo.getGamble().setEnabled(true);
            }

            channelInfo.getGamble().getTimer().schedule(new gambleTimer(channelInfo), delay);
        }
    }

    private String getStreamList(String key, Channel channelInfo) throws Exception {
        URL feedSource = new URL("http://api.justin.tv/api/stream/list.xml?channel=" + channelInfo.getTwitchName());
        URLConnection uc = feedSource.openConnection();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(uc.getInputStream());
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("stream");
        if (nList.getLength() < 1)
            throw new Exception();

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                return getTagValue(key, eElement);

            }
        }

        return "";
    }

    public String getTimeStreaming(String uptime) {
        uptime = uptime.replace("Z", "UTC");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        try {
            Date then = format.parse(uptime);
            return "has been streaming for " + Main.getTimeTilNow(then) + ".";
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "An error occurred or stream is offline";
    }
    
    public String getTimeFollowing(String followage, String user, String target) {
        followage = followage.replace("Z", "UTC").replace("+00:00", "UTC");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        try {
            Date then = format.parse(followage);
            return user + " has been following " + target + " for " + this._getTimeTilNow(then) + ".";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return "An error ocurred or this user is not following";
    }

    public boolean checkStalePing() {
        if (lastPing == -1)
            return false;

        int difference = ((int) (System.currentTimeMillis() / 1000)) - lastPing;

        if (difference > BotManager.getInstance().pingInterval) {
            log("RB: Ping is stale. Last ping= " + lastPing + " Difference= " + difference);
            lastPing = -1;
            return true;
        }

        return false;
    }

    private String fuseArray(String[] array, int start) {
        String fused = "";
        for (int c = start; c < array.length; c++)
            fused += array[c] + " ";

        return fused.trim();

    }


    public String _getTimeTilNow(Date date) {
        long difference = (long) (System.currentTimeMillis() / 1000) - (date.getTime() / 1000);
        String returnString = "";
        String dayss = (difference / 86400) + " days";
        if (difference >= 31536000) {
            int years = (int) (difference / 31536000);
            returnString += years + " years ";
            difference -= years * 31536000;
        }
        if (difference >= 2592000) {
            int months = (int) (difference / 2592000);
            returnString += months + " months ";
            difference -= months * 2592000;
        }
        if (difference >= 86400) {
            int days = (int) (difference / 86400);
            returnString += days + " days";
        }
        
        return returnString + " (" + dayss + ")";
    }

    //sender, channel, target, message
    
    public void logImp(String sender, String channel, String target, String message) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        String time = new java.util.Date().toString();
        String line = time + " [ADMIN - IMP] " + sender + " used imp on " + channel + " -> " + target + " - " + "\"" +  message + "\"\n";
        
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("implog.csv", true), "UTF-8"));
            out.write(line);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendWhisper(String sender, String target, String message) {
        String senderID = JSONUtil.krakenUserID(sender);
        String targetID = JSONUtil.krakenUserID(target);
        String toSend = "{\"body\": \"" + message + "\", \"from_id\": " + senderID + ", \"to_id\": " + targetID + "}";
        System.out.println(toSend);

        try {
            LOGGER_D.debug(BotManager.sendWhisper("https://im.twitch.tv/v1/messages", toSend));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void logHost(String channel, String target, String status, String detail) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
        String time = new java.util.Date().toString();
        String line = time + " [HOST] " + status + " - " + channel + " -> " + target + " - " + detail + "\"\n";

        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("hostslog.csv", true), "UTF-8"));
            out.write(line);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void logGlobalBan(String channel, String sender, String message) {
        String line = sender + "," + channel + ",\"" + message + "\"\n";

        //System.out.print(line);
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("globalbans.csv", true), "UTF-8"));
            out.write(line);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public char getBullet() {
        if (bulletPos == bullet.length)
            bulletPos = 0;

        char rt = bullet[bulletPos];
        bulletPos++;

        return rt;

    }

    private class giveawayTimer extends TimerTask {
        private Channel channelInfo;

        public giveawayTimer(Channel channelInfo2) {
            super();
            channelInfo = channelInfo2;
        }

        public void run() {
            if (channelInfo.getGiveaway() != null) {
                if (channelInfo.getGiveaway().getStatus()) {
                    channelInfo.getGiveaway().setStatus(false);
                    ReceiverBot.this.send(channelInfo.getChannel(), "> Giveaway over.");
                }
            }
        }
    }

    private class gambleTimer extends TimerTask {
        private Channel channelInfo;

        public gambleTimer(Channel channelInfo3) {
            super();
            channelInfo = channelInfo3;
        }

        public void run() {
            if (channelInfo.getGamble().getStatus()) {
                String winner = channelInfo.getGamble().pickWinner();
                int prize = channelInfo.getGamble().getPrize();
                ReceiverBot.this.send(channelInfo.getChannel(), "Gamble is over and the winner was " + winner + ". They won: " + prize + " DPs.");
                channelInfo.addPoints(winner, prize);
                System.out.println("DEBUG: Adding Points");
                channelInfo.getGamble().setEnabled(false);
                System.out.println("DEBUG: Disabled Gamble");
                //channelInfo.getGamble().resetPrize();
                //System.out.println("DEBUG: resetting prize. It is now: " + channelInfo.getGamble().getPrize());
                channelInfo.getGamble().reset();
                System.out.println("Resetting gamblers");
            }
        }
    }
    
    private class betTimer extends TimerTask {
        private Channel channelInfo;
        private Map<String, Integer> payouts;
        private String result;
        
        public betTimer (Channel channelInfo3) {
            super();
            channelInfo = channelInfo3;
            //result = _result;
            payouts = new HashMap<String, Integer>();
        }
        
        public void run() {
            if (channelInfo.getBet().getStatus()) {
                LOGGER_D.debug("Starting to distribute points");
                payouts = channelInfo.getBet().getWinners(result);
                int prize = channelInfo.getBet().getPrize();
                for (String key : payouts.keySet()) {
                    int amount = (int) (prize * (payouts.get(key)/100.0f));
                    channelInfo.addPoints(key, amount);
                    LOGGER_D.debug("Adding: " + amount + " to " + key);
                }
                channelInfo.getBet().setStatus(false);
                channelInfo.getBet().reset();
            }
        }
    }
    
    private class pointsTimer extends TimerTask {
        private Channel channelInfo;
        private String channelName;
        private List<String> chatters;
        
        public pointsTimer(Channel channel) {
            super();
            channelInfo = channel;
            channelName = channelInfo.getChannel().substring(1);
            chatters = new ArrayList<String>();
        }

        public void run() {
            if (channelInfo.getPts().getStatus()) {
                System.out.println("Beginning point distribution");
                if (JSONUtil.krakenIsLive(channelName)) {
                    System.out.println("Checking if channel is online: Passed");
                    chatters = JSONUtil.getChatters(channelName);
                    Points.distributePoints(channelName, chatters, channelInfo.getPointsAmount());
                    Points.logPoints(channelName, "Passive distribution of points", "admin", chatters.toString(), new java.util.Date().toString());
                    return;
                } else {
                    System.out.println("Channel is not live, so not distributing points automatically");
                    return;
                }

            }
        }
    }

    private class autoHostTimer extends TimerTask {
        private Channel channelInfo;
        private String channelName;

        public autoHostTimer(Channel channelInfo4, String channelName4) {
            super();
            channelInfo = channelInfo4;
            channelName = channelName4;
        }

        public void run() {
            if (channelInfo.getAH().getStatus()) {
                System.out.println("Timer created and activated");
                if (JSONUtil.krakenIsLive(channelName)) {
                    ReceiverBot.this.sendCommand(channelInfo.getChannel(), ".unhost");
                    ReceiverBot.this.logHost(channelName, "null", "failed", "Main channel is online, so no hosting");
                    System.out.println("Channel is live so not hosting");
                    return;
                } else {
                    String target = channelInfo.getChannelToHost();
                    
                    if (target.equalsIgnoreCase("")) {
                        System.out.println("No channels from your list are currently live");
                        ReceiverBot.this.logHost(channelName, "null", "failed", "No channels from the list are live");
                        return;
                    }
                    if (!JSONUtil.krakenIsLive(target)) {
                        System.out.println("The channel: " + target + " is not currently live, so skipping this host attempt.");
                        ReceiverBot.this.logHost(channelName, target, "failed", "The channel is not currently live, so skipping this attempt");
                        return;
                    }

                    channelInfo.setLastHosted(target);
                    System.out.println("Beginning to host right now.");
                    ReceiverBot.this.logHost(channelName, target, "Success", "Hosting successful");
                    ReceiverBot.this.sendCommand(channelInfo.getChannel(), ".unhost");
                    ReceiverBot.this.sendCommand(channelInfo.getChannel(), ".host " +  target);
                }
            }
        }
    }
}
