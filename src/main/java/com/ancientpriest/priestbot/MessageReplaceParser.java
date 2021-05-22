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

public class MessageReplaceParser {

    public static String parseMessage(String channel, String sender, String command, String message, String[] args) {
        Channel ci = BotManager.getInstance().getChannel(channel);
        Random rand = new Random();
        int randNum = rand.nextInt(101);
        List<String> chatters = new LinkedList<String>();
        chatters = JSONUtil.getChatters(channel.substring(1));
        String randomChatter = chatters.get(rand.nextInt(chatters.size()));
        

        if (sender != null && message.contains("(_USER_)"))
            message = message.replace("(_USER_)", sender);
        if (channel != null && message.contains("(_CHANNEL_)"))
            message = message.replace("(_CHANNEL_)", channel.substring(1));
        if (message.contains("(_UPTIME_)"))
            message = message.replace("(_UPTIME_)", JSONUtil.getUptime(channel.substring(1)));
        if (message.contains("(_GAME_)"))
            message = message.replace("(_GAME_)", JSONUtil.krakenGame(channel.substring(1)));
        if (message.contains("(_STATUS_)"))
            message = message.replace("(_STATUS_)", JSONUtil.krakenStatus(channel.substring(1)));
        if (message.contains("(_PERCENT_)"))
            message = message.replace("(_PERCENT_)", String.valueOf(randNum) + "%");
        if (message.contains("(_RANDOMVIEWER_)"))
            message = message.replace("(_RANDOMVIEWER_)", randomChatter);
        if (message.contains("$randomviewer"))
            message = message.replace("$randomviewer", randomChatter);
        if (message.contains("(_RANDOM_)"))
            message = message.replace("(_RANDOM_)", String.valueOf(randNum));
        if (message.contains("$percent"))
            message = message.replace("$percent", String.valueOf(randNum) + "%");
        if (message.contains("$random"))
            message = message.replace("$random", String.valueOf(randNum));
        if (message.contains("(_VIEWERS_)"))
            message = message.replace("(_VIEWERS_)", "" + JSONUtil.krakenViewers(channel.substring(1)));
//        if (message.contains("(_CHATTERS_)"))
//            message = message.replace("(_CHATTERS_)", "" + ReceiverBot.getInstance().getUsers(channel).length);
        if (message.contains("(_SONG_)"))
            message = message.replace("(_SONG_)", JSONUtil.lastFM(ci.getLastfm()));
        if (message.contains("(_SONG_)"))
            message = message.replace("(_SONG_)", JSONUtil.lastFM(ci.getLastfm()));
        if (message.contains("(_STEAM_PROFILE_)"))
            message = message.replace("(_STEAM_PROFILE_)", JSONUtil.steam(ci.getSteam(), "profile"));
        if (message.contains("(_STEAM_GAME_)"))
            message = message.replace("(_STEAM_GAME_)", JSONUtil.steam(ci.getSteam(), "game"));
        if (message.contains("(_STEAM_SERVER_)"))
            message = message.replace("(_STEAM_SERVER_)", JSONUtil.steam(ci.getSteam(), "server"));
        if (message.contains("(_STEAM_STORE_)"))
            message = message.replace("(_STEAM_STORE_)", JSONUtil.steam(ci.getSteam(), "store"));
        if (message.contains("(_BOT_HELP_)"))
            message = message.replace("(_BOT_HELP_)", BotManager.getInstance().bothelpMessage);
        if (message.contains("(_CHANNEL_URL_)"))
            message = message.replace("(_CHANNEL_URL_)", "twitch.tv/" + channel.substring(1));
        if (message.contains("(_TWEET_URL_)")) {
            String url = JSONUtil.shortenURL("https://twitter.com/intent/tweet?text=" + JSONUtil.urlEncode(MessageReplaceParser.parseMessage(channel, sender, command, ci.getClickToTweetFormat(), args)));
            message = message.replace("(_TWEET_URL_)", url);
        }
        if (message.contains("(_COMMANDS_)")) {
            message = message.replace("(_COMMANDS_)", ci.getCommandList());
        }
        if (message.contains("(_ELO_)")) {
            message = message.replace("(_ELO_)", JSONUtil.getRank(JSONUtil.getId(ci.getSummoner(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELO1_)")) {
            message = message.replace("(_ELO1_)", JSONUtil.getRank(JSONUtil.getId(ci.getSmurf1(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELO2_)")) {
            message = message.replace("(_ELO2_)", JSONUtil.getRank(JSONUtil.getId(ci.getSmurf2(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELO3_)")) {
            message = message.replace("(_ELO3_)", JSONUtil.getRank(JSONUtil.getId(ci.getSmurf3(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELO4_)")) {
            message = message.replace("(_ELO4_)", JSONUtil.getRank(JSONUtil.getId(ci.getSmurf4(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_SUMMONER_)")) {
            message = message.replace("(_SUMMONER_)", ci.getSummoner());
        }
        if (message.contains("(_SMURF1_)")) {
            message = message.replace("(_SMURF1_)", ci.getSmurf1());
        }
        if (message.contains("(_SMURF2_)")) {
            message = message.replace("(_SMURF2_)", ci.getSmurf2());
        }
        if (message.contains("(_SMURF3_)")) {
            message = message.replace("(_SMURF3_)", ci.getSmurf3());
        }
        if (message.contains("(_SMURF4_)")) {
            message = message.replace("(_SMURF4_)", ci.getSmurf4());
        }
        if (message.contains("$summoner")) {
            message = message.replace("$summoner", ci.getSummoner());
        }
        if (message.contains("$smurf1")) {
            message = message.replace("$smurf1", ci.getSmurf1());
        }
        if (message.contains("$smurf2")) {
            message = message.replace("$smurf2", ci.getSmurf2());
        }
        if (message.contains("$smurf3")) {
            message = message.replace("$smurf3", ci.getSmurf3());
        }
        if (message.contains("$smurf4")) {
            message = message.replace("$smurf4", ci.getSmurf4());
        }
        if (message.contains("(_ELOFLEX_)")) {
            message = message.replace("(_ELOFLEX_)", JSONUtil.getRankFlex(JSONUtil.getId(ci.getSummoner(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOFLEX1_)")) {
            message = message.replace("(_ELOFLEX1_)", JSONUtil.getRankFlex(JSONUtil.getId(ci.getSmurf1(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOFLEX2_)")) {
            message = message.replace("(_ELOFLEX2_)", JSONUtil.getRankFlex(JSONUtil.getId(ci.getSmurf2(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOFLEX3_)")) {
            message = message.replace("(_ELOFLEX3_)", JSONUtil.getRankFlex(JSONUtil.getId(ci.getSmurf3(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOFLEX4_)")) {
            message = message.replace("(_ELOFLEX4_)", JSONUtil.getRankFlex(JSONUtil.getId(ci.getSmurf4(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOTFT_)")) {
            message = message.replace("(_ELOTFT_)", JSONUtil.getRankTFT(JSONUtil.getId(ci.getSummoner(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOTFT1_)")) {
            message = message.replace("(_ELOTFT1_)", JSONUtil.getRankTFT(JSONUtil.getId(ci.getSmurf1(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOTFT2_)")) {
            message = message.replace("(_ELOTFT2_)", JSONUtil.getRankTFT(JSONUtil.getId(ci.getSmurf2(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOTFT3_)")) {
            message = message.replace("(_ELOTFT3_)", JSONUtil.getRankTFT(JSONUtil.getId(ci.getSmurf3(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_ELOTFT4_)")) {
            message = message.replace("(_ELOTFT4_)", JSONUtil.getRankTFT(JSONUtil.getId(ci.getSmurf4(), ci.getRegion()), ci.getRegion()));
        }
        if (message.contains("(_VIDEONOVO_)")) {
            String videoId = JSONUtil.getVideoNovo(ci.getYoutubeChannelID());
            if (videoId.contains("problem"))
                message = message.replace("(_VIDEONOVO_)", "ERROR");
            else
                message = message.replace("(_VIDEONOVO_)", "https://www.youtube.com/watch?v=" + videoId);
        }
        if (message.contains("(_LATESTVIDEO_)")) {
            String videoId = JSONUtil.getVideoNovo(ci.getYoutubeChannelID());
            if (videoId.contains("problem"))
                message = message.replace("(_LATESTVIDEO_)", "ERROR");
            else
                message = message.replace("(_LATESTVIDEO_)", "https://www.youtube.com/watch?v=" + videoId);
        }
        if (command != null && message.contains("(_COUNT_)"))
            message = message.replace("(_COUNT_)", ci.getCommandCount(command));

        if (message.contains("$touser")) {
            if (args != null)
                message = message.replace("$touser", args[0]);
            else
                message = message.replace("$touser", randomChatter);
        }
        if (message.contains("(_TOUSER_)")) {
            if (args != null)
                message = message.replace("(_TOUSER_)", args[0]);
            else
                message = message.replace("(_TOUSER_)", randomChatter);
        }

        if (args != null) {
            int argCounter = 1;
            for (String argument : args) {
                if (message.contains("(_" + argCounter + "_)"))
                    message = message.replace("(_" + argCounter + "_)", argument);
                argCounter++;
            }
        }
       if (message.toLowerCase().contains("!color") || message.toLowerCase().contains(".color") || (message.toLowerCase().contains("/disconnect") || message.toLowerCase().contains(".disconnect") || message.toLowerCase().contains(".ban") || message.toLowerCase().contains("/ban"))) {
            message = "Impossível executar essa mensagem";
        }

        return message;
    }
}
