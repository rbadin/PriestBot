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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


 class JSONUtil {

     private static String riotApiKey = Constants.RIOTAPIKEY;
     private static String youtubeApiKey = Constants.YOUTUBEAPIKEY;

//    public static void krakenStreams() throws Exception{
//        JSONParser parser = new JSONParser();
//        Object obj = parser.parse(BotManager.getRemoteContent("https://api.twitch.tv/kraken/streams/mlglol"));
//
//        JSONObject jsonObject = (JSONObject) obj;
//
//        JSONObject stream = (JSONObject)(jsonObject.get("stream"));
//        Long viewers = (Long)stream.get("viewers");
//        System.out.println("Viewers: " + viewers);
//    }

    public static Long krakenViewers(String channel) {
        channel = krakenUserID(channel);
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/streams/" + channel, 5));

            JSONObject jsonObject = (JSONObject) obj;

            JSONObject stream = (JSONObject) (jsonObject.get("stream"));
            if (stream == null)
                return (long) 0;

            Long viewers = (Long) stream.get("viewers");
            return viewers;
        } catch (Exception ex) {
            ex.printStackTrace();
            return (long) 0;
        }

    }

    public static String getUptime(String channel) {
        String uptime = krakenCreated_at(channel);
        System.out.println("Uptime: uptime");
        uptime = uptime.replace("Z", "UTC");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        try {
            Date then = format.parse(uptime);
            return Main.getTimeTilNow(then);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "An error occurred or stream is offline";
    }



    public static String krakenUserID(String username) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/users?login=" + username, 5));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray users = (JSONArray) jsonObject.get("users");
            if (users.size() > 0) {
                JSONObject index0 = (JSONObject) users.get(0);
                String id = (String) index0.get("_id");
                System.out.println("The user: " + username + "'s id is: " + id);
                return id;
            } else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    public static Long jtvViewers(String channel) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://api.justin.tv/api/stream/summary.json?channel=" + channel));

            JSONObject jsonObject = (JSONObject) obj;

            Long viewers = (Long) jsonObject.get("viewers_count");
            return viewers;
        } catch (Exception ex) {
            ex.printStackTrace();
            return (long) 0;
        }

    }

    public static String jtvStatus(String channel) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://api.justin.tv/api/channel/show/" + channel + ".json"));

            JSONObject jsonObject = (JSONObject) obj;

            String status = (String) jsonObject.get("status");
            return status;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Unable to query API";
        }

    }
    
    public static boolean isCategory(String game, String category) {
        String encodedgame = game;
        category = category.toLowerCase();
        try {
            encodedgame = URLEncoder.encode(game, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            JSONParser parser = new JSONParser();
            System.out.println("Encoded game: " + encodedgame);
            System.out.println("Game: " + game);
            Object obj = parser.parse(BotManager.getRemoteContent("https://www.speedrun.com/api_records.php?game=" + encodedgame));
            
            JSONObject jsonObject = (JSONObject) obj;
            
            JSONObject cats = (JSONObject) jsonObject.get(game);
            
            Set<String> categories = new HashSet<String>();
            
            for (Object key : cats.keySet()) {
                System.out.println(key.toString().toLowerCase());
                categories.add(key.toString().toLowerCase());
            }
            
            if (categories.contains(category)) {
                return true;
            }

            /*
            Set<String> categories = new HashSet<String>();
            
            for (Object key : cats.keySet()) {
                System.out.println(key.toString().toLowerCase());
                categories.add(key.toString().toLowerCase());
            }
            
            if (categories.contains(category)) {
                return true;
            }
                    */
            
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    public static String getWR(String game, String category) {
        String encodedgame = game;
        boolean found = false;
        String cat2 = category;
        try {
            encodedgame = URLEncoder.encode(game, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("https://www.speedrun.com/api_records.php?game=" + encodedgame));
            
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject cats = (JSONObject) jsonObject.get(game);
            
            for (Object single : cats.keySet()) {
                if (single.toString().equalsIgnoreCase(category)) {
                    found = true;
                    System.out.println("Found category: " + cat2);
                    cat2 = single.toString();
                    System.out.println("Assigned: " + cat2);
                }
            }
            
            JSONObject cat = (JSONObject) cats.get(cat2);
            
            String player = (String) cat.get("player");
            String video = (String) cat.get("video");
            //String.valueOf((Long) ((JSONObject)stats).get("minionsKilled"));
            String time = String.valueOf((Long) ((JSONObject)cat).get("time"));
            int timecalc = Integer.parseInt(String.valueOf(time));
            String timereturn = "";
            
            if (timecalc >= 3600) {
                int hours = (timecalc / 3600);
                String _hours = Integer.toString(hours);
                if (hours < 10)
                    _hours = "0" + hours;
                timereturn += _hours + ":";
                timecalc -= hours * 3600;
            }
            if (timecalc >= 60) {
                int minutes = (timecalc / 60);
                String _minutes = Integer.toString(minutes);
                if (minutes < 10)
                    _minutes = "0" + minutes;
                timereturn += _minutes + ":";
                timecalc -= minutes * 60;
            }
            
            String _timecalc = Integer.toString(timecalc);
            if (timecalc < 10) 
                _timecalc = "0" + timecalc;
            
            timereturn += _timecalc;
            
            return "World record for: " + game + "(" + cat2 + ") is " + timereturn + " by " + player + ". VOD: " + video;
            } catch (Exception ex) {
                ex.printStackTrace();
                return "There was an error accessing the database. Did you type the exact category?";
            }
        
    }
    
    public static String _getCategories(String game) {
        String encodedgame = game;
        try {
            encodedgame = URLEncoder.encode(game, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            JSONParser parser = new JSONParser();
            System.out.println("Encoded game: " + encodedgame);
            System.out.println("Game: " + game);
            Object obj = parser.parse(BotManager.getRemoteContent("https://www.speedrun.com/api_records.php?game=" + encodedgame));
            
            JSONObject jsonObject = (JSONObject) obj;
            
            JSONObject cats = (JSONObject) jsonObject.get(game);
            
            StringBuilder sb = new StringBuilder();
            
            for (Object key : cats.keySet()) {
                sb.append(key.toString());
                sb.append("|");
            }
            
            String finalString = sb.toString();
            
            StringBuilder b = new StringBuilder(finalString);
            b.replace(finalString.lastIndexOf("|"), finalString.lastIndexOf("|") + 1, "" );
            finalString = b.toString();
            return finalString;
                
            
            
            
            //System.out.println(jsonObject);
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return "No categories for this game were found.";
        }
    }
    
    public static String getCategories(String game) {
        String encodedgame = game;
        try {
            encodedgame = URLEncoder.encode(game, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            JSONParser parser = new JSONParser();
            System.out.println("Encoded game: " + encodedgame);
            System.out.println("Game: " + game);
            Object obj = parser.parse(BotManager.getRemoteContent("https://www.speedrun.com/api_records.php?game=" + encodedgame));
            
            JSONObject jsonObject = (JSONObject) obj;
            
            JSONObject cats = (JSONObject) jsonObject.get(game);
            
            StringBuilder sb = new StringBuilder();
            
            for (Object key : cats.keySet()) {
                sb.append(key.toString());
                sb.append(", ");
            }
            
            String finalString = sb.toString();
            
            StringBuilder b = new StringBuilder(finalString);
            b.replace(finalString.lastIndexOf(","), finalString.lastIndexOf(",") + 1, "." );
            finalString = b.toString();
            return "Available categories for " + game + ": " + finalString;
                
            
            
            
            //System.out.println(jsonObject);
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return "No categories for this game were found.";
        }
    }
    
    public static String fCreated_at(String username, String channel) {
        channel = krakenUserID(channel);
        username = krakenUserID(username);
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/users/" + username + "/follows/channels/" + channel, 5));
            
            JSONObject jsonObject = (JSONObject) obj;
            
            String created_at = (String) jsonObject.get("created_at");
            return created_at;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "(error)";
        }
    }

    public static String krakenCreated_at(String channel) {
        channel = krakenUserID(channel);
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/streams/" + channel, 5));

            JSONObject jsonObject = (JSONObject) obj;

            JSONObject stream = (JSONObject) (jsonObject.get("stream"));
            if (stream == null)
                return "(offline)";

            String viewers = (String) stream.get("created_at");
            return viewers;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "(error)";
        }

    }


    public static String krakenStatus(String channel) {
        channel = krakenUserID(channel);
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/channels/" + channel, 5));

            JSONObject jsonObject = (JSONObject) obj;

            String status = (String) jsonObject.get("status");

            if (status == null)
                status = "(Not set)";

            return status;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "(Error querying API)";
        }

    }

    public static String krakenGame(String channel) {
        channel = krakenUserID(channel);
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/channels/" + channel, 5));

            JSONObject jsonObject = (JSONObject) obj;

            String game = (String) jsonObject.get("game");

            if (game == null)
                game = "(Not set)";

            return game;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "(Error querying API)";
        }

    }

    public static String lastFM(String user) {
        String api_key = BotManager.getInstance().LastFMAPIKey;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=" + user + "&format=json&limit=1&api_key=" + api_key));

            JSONObject jsonObject = (JSONObject) obj;

            JSONObject recenttracks = (JSONObject) (jsonObject.get("recenttracks"));
            if (recenttracks.get("track") instanceof JSONArray) {
                JSONArray track = (JSONArray) recenttracks.get("track");

                JSONObject index0 = (JSONObject) track.get(0);
                String trackName = (String) index0.get("name");
                JSONObject artistO = (JSONObject) index0.get("artist");
                String artist = (String) artistO.get("#text");

                return trackName + " by " + artist;

            } else {
                return "(Nothing)";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "(Error querying API)";
        }

    }

    public static String steam(String userID, String retValues) {
        String api_key = BotManager.getInstance().SteamAPIKey;

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?steamids=" + userID + "&key=" + api_key));

            JSONObject jsonObject = (JSONObject) obj;

            JSONObject response = (JSONObject) (jsonObject.get("response"));
            JSONArray players = (JSONArray) response.get("players");

            if (players.size() > 0) {
                JSONObject index0 = (JSONObject) players.get(0);
                String profileurl = (String) index0.get("profileurl");
                String gameextrainfo = (String) index0.get("gameextrainfo");
                String gameserverip = (String) index0.get("gameserverip");
                String gameid = (String) index0.get("gameid");

                if (retValues.equals("profile"))
                    return JSONUtil.shortenURL(profileurl);
                else if (retValues.equals("game"))
                    return (gameextrainfo != null ? gameextrainfo : "(unavailable)");
                else if (retValues.equals("server"))
                    return (gameserverip != null ? gameserverip : "(unavailable)");
                else if (retValues.equals("store"))
                    return (gameid != null ? "http://store.steampowered.com/app/" + gameid : "(unavailable)");
                else
                    return "Profile: " + JSONUtil.shortenURL(profileurl) + (gameextrainfo != null ? ", Game: " + gameextrainfo : "") + (gameserverip != null ? ", Server: " + gameserverip : "");

            } else {
                return "Error querying API";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Error querying API";
        }
    }

    public static String shortenURL(String url) {
        String login = BotManager.getInstance().bitlyLogin;
        String api_key = BotManager.getInstance().bitlyAPIKey;

        try {
            String encodedURL = "";
            try {
                encodedURL = URLEncoder.encode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://api.bitly.com/v3/shorten?login=" + login + "&apiKey=" + api_key + "&longUrl=" + encodedURL + "&format=json"));

            JSONObject jsonObject = (JSONObject) obj;
            String status_txt = (String) jsonObject.get("status_txt");

            if (status_txt.equalsIgnoreCase("OK")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                String shortenedUrl = (String) data.get("url");
                return shortenedUrl;
            } else {
                return url;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return url;
        }
    }

    public static String urlEncode(String data) {
        try {
            data = URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static boolean krakenIsLive(String channel) {
        channel = krakenUserID(channel);
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/streams/" + channel, 5));

            JSONObject jsonObject = (JSONObject) obj;

            JSONObject stream = (JSONObject) (jsonObject.get("stream"));

            if (stream != null)
                return true;
            else
                return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static boolean krakenChannelExist(String channel) {
        if (BotManager.getInstance().twitchChannels == false)
            return true;
        channel = krakenUserID(channel);
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/channels/" + channel, 5));

            JSONObject jsonObject = (JSONObject) obj;

            String _id = (String) jsonObject.get("_id");

            return (_id != null);
        } catch (Exception ex) {
            //ex.printStackTrace();
            return false;
        }

    }

    public static boolean krakenOutdatedChannel(String channel) {

        return false; //TODO: Temp bypass to disable check

//        if (BotManager.getInstance().twitchChannels == false)
//            return false;
//
//        try {
//            JSONParser parser = new JSONParser();
//            Object obj = parser.parse(BotManager.getRemoteContentTwitch("https://api.twitch.tv/kraken/channels/" + channel, 2));
//
//            JSONObject jsonObject = (JSONObject) obj;
//
//            Object statusO = jsonObject.get("status");
//            Long status;
//            if (statusO != null) {
//                status = (Long) statusO;
//                if (status == 422 || status == 404) {
//                    System.out.println("Channel " + channel + " returned status: " + status + ". Parting channel.");
//                    return true;
//                }
//            }
//
//            String updatedAtString = (String) jsonObject.get("updated_at");
//            //System.out.println("Time: " + updatedAtString);
//
//            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//            format.setTimeZone(java.util.TimeZone.getTimeZone("US/Pacific"));
//            long differenceDay = 0;
//
//            try {
//                Date then = format.parse(updatedAtString);
//                long differenceSec = (long) (System.currentTimeMillis() / 1000) - (then.getTime() / 1000);
//                differenceDay = (long) (differenceSec / 86400);
//            } catch (Exception exi) {
//                exi.printStackTrace();
//            }
//
//            if (differenceDay > 30) {
//                System.out.println("Channel " + channel + " not updated in " + differenceDay + " days. Parting channel.");
//                return true;
//            }
//
//        } catch (Exception ex) {
//            return false;
//        }
//
//        return false;

    }

    public static Long updateTMIUserList(String channel, Set<String> staff, Set<String> admins, Set<String> mods) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://tmi.twitch.tv/group/user/" + channel + "/chatters"));

            JSONObject jsonObject = (JSONObject) obj;

            Long chatter_count = (Long) jsonObject.get("chatter_count");

            JSONObject chatters = (JSONObject) jsonObject.get("chatters");


            JSONArray staffJO = (JSONArray) chatters.get("staff");
            for (Object user : staffJO) {
                staff.add((String) user);
            }

            JSONArray adminsJO = (JSONArray) chatters.get("admins");
            for (Object user : adminsJO) {
                admins.add((String) user);
            }

            JSONArray modsJO = (JSONArray) chatters.get("moderators");
            for (Object user : modsJO) {
                mods.add((String) user);
            }

            return chatter_count;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Long(-1);
        }

    }
    
    public static Set<String> getMods(String channel) {
        Set<String> chatters = new HashSet<String>();
        channel = channel.toLowerCase();
        
        try {
            JSONParser parser = new JSONParser();
            
            Object o = parser.parse(BotManager.getRemoteContent("http://tmi.twitch.tv/group/user/" + channel + "/chatters"));
            
            JSONObject p = (JSONObject) o;
            
            JSONObject list = (JSONObject) p.get("chatters");
            
            JSONArray mods = (JSONArray) list.get("moderators");
            for (Object user : mods)
                chatters.add((String) user);
            
            return chatters;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Set<String> getSpecialUsers (String channel) {
        Set<String> chatters = new HashSet<String>();
        channel = channel.toLowerCase();
        
        try {
            JSONParser parser = new JSONParser();
            
            Object o = parser.parse(BotManager.getRemoteContent("http://tmi.twitch.tv/group/user/" + channel + "/chatters"));
            
            JSONObject p = (JSONObject) o;
            
            JSONObject list = (JSONObject) p.get("chatters");
            
            JSONArray staff = (JSONArray) list.get("staff");
            for (Object user : staff)
                chatters.add((String) user);
            
            JSONArray admins = (JSONArray) list.get("admins");
            for (Object user : admins)
                chatters.add((String) user);
            
            JSONArray gmods = (JSONArray) list.get("global_mods");
            for (Object user : gmods)
                chatters.add((String) user);
            
            return chatters;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static List<String> getChatters(String channel) {
        List<String> chatters = new LinkedList<String>();
        channel = channel.toLowerCase();
        
        try {
            JSONParser parser = new JSONParser();
            
            Object o = parser.parse(BotManager.getRemoteContent("http://tmi.twitch.tv/group/user/" + channel + "/chatters"));
            
            JSONObject p = (JSONObject) o;
            
            JSONObject list = (JSONObject) p.get("chatters");
            
            JSONArray staff = (JSONArray) list.get("staff");
            for (Object user : staff)
                chatters.add((String) user);
            
            JSONArray broadcaster = (JSONArray) list.get("broadcaster");
            for (Object user : broadcaster)
                chatters.add((String) user);
            
            JSONArray vips = (JSONArray) list.get("vips");
            for (Object user : vips)
                chatters.add((String) user);
            
            JSONArray admins = (JSONArray) list.get("admins");
            for (Object user : admins)
                chatters.add((String) user);
            
            JSONArray gmods = (JSONArray) list.get("global_mods");
            for (Object user : gmods)
                chatters.add((String) user);
            
            JSONArray mods = (JSONArray) list.get("moderators");
            for (Object user : mods)
                chatters.add((String) user);
            
            JSONArray viewers = (JSONArray) list.get("viewers");
            for (Object user : viewers)
                chatters.add((String) user);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            return chatters;
        }
    }

    public static Double getSourceBitrate(String channel) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://api.justin.tv/api/stream/list.json?channel=" + channel));

            JSONArray outerArray = (JSONArray) obj;

            if (outerArray.size() == 1) {
                JSONObject channelObject = (JSONObject) outerArray.get(0);
                Double bitrate = (Double) channelObject.get("video_bitrate");
                return bitrate;
            }

            return new Double(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Double(0);
        }

    }

    public static String getSourceRes(String channel) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://api.justin.tv/api/stream/list.json?channel=" + channel));

            JSONArray outerArray = (JSONArray) obj;

            if (outerArray.size() == 1) {
                JSONObject channelObject = (JSONObject) outerArray.get(0);
                Long width = (Long) channelObject.get("video_width");
                Long height = (Long) channelObject.get("video_height");
                return width + "x" + height;
            }

            return "Unable to retrieve data";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Unable to retrieve data";
        }

    }

    public static String getChatProperties(String channel) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://api.twitch.tv/api/channels/" + channel + "/chat_properties"));

            JSONObject jsonObject = (JSONObject) obj;

            Boolean hide_chat_links = (Boolean) jsonObject.get("hide_chat_links");
            Boolean devchat = (Boolean) jsonObject.get("devchat");
            Boolean eventchat = (Boolean) jsonObject.get("eventchat");
            Boolean require_verified_account = (Boolean) jsonObject.get("require_verified_account");

            String response = "Hide links: " + hide_chat_links + ", Require verified account: " + require_verified_account;

            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "(Error querying API)";
        }

    }

    public static String getId(String summoner, String region) {
        summoner = summoner.toLowerCase();
        summoner = summoner.replaceAll(" ", "");
        region = region.toLowerCase();

        switch (region) {
            case "br":
                region = "br1";
                break;
            case "na":
                region = "na1";
                break;
            case "ru":
                break;
            case "kr":
                break;
            case "oce":
                region = "oc1";
                break;
            case "jp":
                region = "jp1";
                break;
            case "eune":
                region = "eun1";
                break;
            case "euw":
                region = "euw1";
                break;
            case "lan":
                region = "la1";
                break;
            case "las":
                region = "la2";
                break;
            case "tr":
                region = "tr1";
                break;
            case "pbe":
                region = "pbe1";
                break;
            default:
                region = "br1";

        }

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("https://" + region + ".api.riotgames.com/lol/summoner/v4/summoners/by-name/" + summoner + "?api_key=" + riotApiKey));

            JSONObject jsonObject = (JSONObject) obj;

            String id = (String) jsonObject.get("id");

            return id;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "0";
        }
    }
    // Object -> items (Array) -> id (object) -> videoId
    
    public static String getVideoNovo(String channelId) {
        String videoId = "";
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("https://www.googleapis.com/youtube/v3/search?part=id&channelId=" + channelId + "&maxResults=1&order=date&key=" + youtubeApiKey));
            JSONObject jsonObject = (JSONObject) obj;
            
            JSONArray items = (JSONArray) jsonObject.get("items");
            
            for (Object o : items) {
                JSONObject id = (JSONObject) ((JSONObject)o).get("id");
                videoId = (String) id.get("videoId");
            }
            
            return videoId;
        } catch (Exception e) {
            e.printStackTrace();
            return "There was a problem accessing Youtube API";
        }
    }

    public static String getYoutubeInfo(String id) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://gdata.youtube.com/feeds/api/videos/" + id + "?v=2&alt=jsonc"));

            JSONObject jsonObject = (JSONObject) obj;

            JSONObject data = (JSONObject) jsonObject.get("data");

            String title = (String) data.get("title");
            String uploader = (String) data.get("uploader");
            Long duration = (Long) data.get("duration");
            int durationcalc = Integer.parseInt(String.valueOf(duration));
            String durationreturn = "";
            if (durationcalc >= 3600) {
                int hours = (durationcalc / 3600);
                durationreturn += hours + "h ";
                durationcalc -= hours * 3600;
            }
            if (durationcalc >= 60) {
                int minutes = (durationcalc / 60);
                durationreturn += minutes + "m ";
                durationcalc -= minutes * 60;
            }
            
            durationreturn += durationcalc + "s ";

            return "Title: " + title + " | Duration: " + durationreturn + " | Uploaded by: " + uploader;
        } catch (Exception ex) {

            ex.printStackTrace();
            return "There was an error";
        }
    }
    
    public static Map<Integer, String> getChampions() {
        HashMap<Integer, String> rtr = new HashMap<Integer, String>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent(("https://global.api.pvp.net/api/lol/static-data/na/v1.2/champion?api_key=" + riotApiKey)));
            
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject data = (JSONObject) jsonObject.get("data");
            
            for (Object key : data.keySet()) {
                String champName = (String) key;
                JSONObject value = (JSONObject) data.get(key);
                String id = String.valueOf((Long) ((JSONObject)value).get("id"));
                int idr = Integer.parseInt(id);
                rtr.put(idr, champName);               
            }
            
            return rtr;
        } catch (Exception e) {
            e.printStackTrace();
            return rtr;
        }
    }
// 550923 - Jukes ID BR (do1dera)
// 15742327 - Rakin ID BR (triste mas feliz)

    public static String getRank(String id, String region) {
        String tier = "";
        String division = "";
        String lp = "";
        region = region.toLowerCase();
        boolean isSeries = false;
        String progress = "";
        String finalString = "";
        String dehmerda = "";

        switch (region) {
            case "br":
                region = "br1";
                break;
            case "na":
                region = "na1";
                break;
            case "ru":
                break;
            case "kr":
                break;
            case "oce":
                region = "oc1";
                break;
            case "jp":
                region = "jp1";
                break;
            case "eune":
                region = "eun1";
                break;
            case "euw":
                region = "euw1";
                break;
            case "lan":
                region = "la1";
                break;
            case "las":
                region = "la2";
                break;
            case "tr":
                region = "tr1";
                break;
            case "pbe":
                region = "pbe1";
                break;
            default:
                region = "br1";

        }
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("https://" + region + ".api.riotgames.com/lol/league/v4/entries/by-summoner/" + id + "?api_key=" + riotApiKey));

            JSONArray ranks = (JSONArray) obj;

            for (Object o : ranks) {
                String queue = (String) ((JSONObject)o).get("queueType");
                if (!queue.equalsIgnoreCase("RANKED_SOLO_5x5"))
                    continue;
                tier = (String) ((JSONObject)o).get("tier");
                division = (String) ((JSONObject)o).get("rank");
                lp = String.valueOf((Long) ((JSONObject)o).get("leaguePoints"));
                if (lp.equalsIgnoreCase("100")) {
                    Object series = ((JSONObject)o).get("miniSeries");
                    progress = (String) ((JSONObject)series).get("progress");
                    isSeries = true;
                    progress = progress.replaceAll("N", "-").replaceAll("W", "✔").replaceAll("L", "✘");
                    switch (progress.length()) {
                        case 5:
                            dehmerda = "MD5";
                            break;
                        case 3:
                            dehmerda = "MD3";
                            break;
                        default:
                            break;
                    }
                /*
                StringBuilder sb = new StringBuilder();
                char[] chars = progress.toCharArray();
                for (char c : chars) {
                    sb.append(c);
                    sb.append("-");
                }
                finalString = sb.toString();
                
                StringBuilder b = new StringBuilder(finalString);
                b.replace(finalString.lastIndexOf("-"), finalString.lastIndexOf("-") + 1, "");
                finalString = b.toString();
                    */
                }
                
                

            }
            if (tier == "" || lp == "")
                return "Unranked";
            else {
                if (isSeries) 
                   return tier + " " + division + " (" + dehmerda + ": " + progress + ")"; 
                else
                    return tier + " " + division + " (" + lp + " LP)";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "(Error querying the API)";
        }
    }
             
    public static String getRankFlex(String id, String region) {
        String tier = "";
        String division = "";
        String lp = "";
        region = region.toLowerCase();
        boolean isSeries = false;
        String progress = "";
        String finalString = "";
        String dehmerda = "";

         switch (region) {
             case "br":
                 region = "br1";
                 break;
             case "na":
                 region = "na1";
                 break;
             case "ru":
                 break;
             case "kr":
                 break;
             case "oce":
                 region = "oc1";
                 break;
             case "jp":
                 region = "jp1";
                 break;
             case "eune":
                 region = "eun1";
                 break;
             case "euw":
                 region = "euw1";
                 break;
             case "lan":
                 region = "la1";
                 break;
             case "las":
                 region = "la2";
                 break;
             case "tr":
                 region = "tr1";
                 break;
             case "pbe":
                 region = "pbe1";
                 break;
             default:
                 region = "br1";

         }
         try {
             JSONParser parser = new JSONParser();
             Object obj = parser.parse(BotManager.getRemoteContent("https://" + region + ".api.riotgames.com/lol/league/v4/entries/by-summoner/" + id + "?api_key=" + riotApiKey));

             JSONArray ranks = (JSONArray) obj;

             for (Object o : ranks) {
                String queue = (String) ((JSONObject)o).get("queueType");
                if (!queue.equalsIgnoreCase("RANKED_FLEX_SR"))
                    continue;
                tier = (String) ((JSONObject)o).get("tier");
                division = (String) ((JSONObject)o).get("rank");
                lp = String.valueOf((Long) ((JSONObject)o).get("leaguePoints"));
                if (lp.equalsIgnoreCase("100")) {
                    Object series = ((JSONObject)o).get("miniSeries");
                    progress = (String) ((JSONObject)series).get("progress");
                    isSeries = true;
                    progress = progress.replaceAll("N", "-").replaceAll("W", "✔").replaceAll("L", "✘");
                    switch (progress.length()) {
                        case 5:
                            dehmerda = "MD5";
                            break;
                        case 3:
                            dehmerda = "MD3";
                            break;
                        default:
                            break;
                    }
                /*
                StringBuilder sb = new StringBuilder();
                char[] chars = progress.toCharArray();
                for (char c : chars) {
                    sb.append(c);
                    sb.append("-");
                }
                finalString = sb.toString();
                
                StringBuilder b = new StringBuilder(finalString);
                b.replace(finalString.lastIndexOf("-"), finalString.lastIndexOf("-") + 1, "");
                finalString = b.toString();
                    */
                }
                    
                
                

            }
            if (tier == "" || lp == "")
                return "Unranked";
            else {
                if (isSeries) 
                   return tier + " " + division + " (" + dehmerda + ": " + progress + ")"; 
                else
                    return tier + " " + division + " (" + lp + " LP)";
            }
         } catch (Exception ex) {
             ex.printStackTrace();
             return "(Error querying the API)";
         }
     }

     public static String getRankTFT(String id, String region) {
         String tier = "";
         String division = "";
         String lp = "";
         region = region.toLowerCase();

         switch (region) {
             case "br":
                 region = "br1";
                 break;
             case "na":
                 region = "na1";
                 break;
             case "ru":
                 break;
             case "kr":
                 break;
             case "oce":
                 region = "oc1";
                 break;
             case "jp":
                 region = "jp1";
                 break;
             case "eune":
                 region = "eun1";
                 break;
             case "euw":
                 region = "euw1";
                 break;
             case "lan":
                 region = "la1";
                 break;
             case "las":
                 region = "la2";
                 break;
             case "tr":
                 region = "tr1";
                 break;
             case "pbe":
                 region = "pbe1";
                 break;
             default:
                 region = "br1";

         }
         try {
             JSONParser parser = new JSONParser();
             Object obj = parser.parse(BotManager.getRemoteContent("https://" + region + ".api.riotgames.com/lol/league/v4/entries/by-summoner/" + id + "?api_key=" + riotApiKey));

             JSONArray ranks = (JSONArray) obj;

             for (Object o : ranks) {
                 String queue = (String) ((JSONObject)o).get("queueType");
                 if (!queue.equalsIgnoreCase("RANKED_TFT"))
                     continue;
                 tier = (String) ((JSONObject)o).get("tier");
                 division = (String) ((JSONObject)o).get("rank");
                 lp = String.valueOf((Long) ((JSONObject)o).get("leaguePoints"));

             }
             if (tier == "" || lp == "")
                 return "Unranked";
             else
                 return tier + " " + division + " (" + lp + " LP)";
         } catch (Exception ex) {
             ex.printStackTrace();
             return "(Error querying the API)";
         }
     }
/*
     public static String checkIfWon(String summoner, String region) {
         summoner = summoner.toLowerCase();
         try {
             JSONParser parser = new JSONParser();
             Object obj = parser.parse(BotManager.getRemoteContent("https://" + region + ".api.pvp.net/api/lol/" + region + "/v1.3/game/by-summoner/" + this.getId(summoner, region) + "/recent?api_key=84434619-6d76-4f7c-a5d3-0e486196c81e"));

             JSONObject jsonObject = (JSONObject) obj;

             JSONArray games = (JSONArray) jsonObject.get("games");

             JSONObject check = (JSONObject) games.get(0);

             JSONObject stats = (JSONObject) check.get("stats");

             boolean win = Boolean.parseBoolean((String) stats.get("win"));



         }
     }
     */

     public static String getStatsFromLastGame(String id, String region) {
         String kills = "0";
         String deaths = "0";
         String assists = "0";
         String timePlayed = "";
         String cs = "0";
         String mk = "0";
         String nk = "0";
         Long duration = (long) 0;
         int sum;
         try {
             JSONParser parser = new JSONParser();
             Object obj = parser.parse(BotManager.getRemoteContent("https://" + region + ".api.pvp.net/api/lol/" + region + "/v1.3/game/by-summoner/" + id + "/recent?api_key=" + riotApiKey));

             JSONObject jsonObject = (JSONObject) obj;

             JSONArray games = (JSONArray) jsonObject.get("games");
             JSONObject check = (JSONObject) games.get(0);
             
             String champ = String.valueOf((Long) ((JSONObject)check).get("championId"));
             
             champ = SQLHelper.getChampionNameByID(champ);
             JSONObject stats = (JSONObject) check.get("stats");
             //lp = String.valueOf((Long) ((JSONObject)p).get("leaguePoints"));
             
             if (stats.containsKey("championsKilled"))            
                kills = String.valueOf((Long) ((JSONObject)stats).get("championsKilled"));
             if (stats.containsKey("numDeaths"))
                deaths = String.valueOf((Long) ((JSONObject)stats).get("numDeaths"));
             if (stats.containsKey("assists"))
                assists = String.valueOf((Long) ((JSONObject)stats).get("assists"));
             if (stats.containsKey("minionsKilled"))
                cs = String.valueOf((Long) ((JSONObject)stats).get("minionsKilled"));
             if (stats.containsKey("largestMultiKill"))
                 mk = String.valueOf((Long) ((JSONObject)stats).get("largestMultiKill"));
             if (stats.containsKey("neutralMinionsKilled"))
                 nk = String.valueOf((Long) ((JSONObject)stats).get("neutralMinionsKilled"));
             if (stats.containsKey("timePlayed"))
                 duration = (Long) stats.get("timePlayed");

             int durationcalc = Integer.parseInt(String.valueOf(duration));

             if (durationcalc >= 3600) {
                 int hours = (durationcalc / 3600);
                 timePlayed += hours + "h ";
                 durationcalc -= hours * 3600;
             }
             if (durationcalc >= 60) {
                 int minutes = (durationcalc / 60);
                 timePlayed += minutes + "m ";
                 durationcalc -= minutes * 60;
             }

            timePlayed += durationcalc + "s ";
             
             sum = Integer.parseInt(cs) + Integer.parseInt(nk);
             
             cs = String.valueOf(sum);
             
             if (mk.equalsIgnoreCase("2"))
                 mk = "Double Kill";
             else if (mk.equalsIgnoreCase("3"))
                 mk = "Triple Kill";
             else if (mk.equalsIgnoreCase("4"))
                 mk = "Quadra Kill";
             else if (mk.equalsIgnoreCase("5"))
                 mk = "Penta Kill";
             

             Boolean win = (Boolean) stats.get("win");

             if (win)
                 return "Last game: WIN | Playing as: " + champ + " | Stats = KDA: " + kills + "/" + deaths + "/" + assists + " | CS: " + cs + " | Largest Multi Kill: " + mk + " | Game Duration: " + timePlayed;
             else
                 return "Last game: LOSS | Playing as: " +  champ + " | Stats = KDA: " + kills + "/" + deaths + "/" + assists + " | CS: " + cs + " | Largest Multi Kill: " + mk + " | Game Duration: " + timePlayed;

         } catch (Exception e) {
             e.printStackTrace();
             return "(Error querying the API)";
         }
     }

    public static List<String> getHosts(String channel) {
        List<String> hosts = new LinkedList<String>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("http://chatdepot.twitch.tv/rooms/" + channel + "/hosts"));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray hostss = (JSONArray) jsonObject.get("hosts");

            for (Object o : hostss) {
                String name = (String) ((JSONObject)o).get("host");
                hosts.add(name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return hosts;
        }
    }
/*
    public static List<String> getEmotes() {
        List<String> emotes = new LinkedList<String>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(BotManager.getRemoteContent("https://api.twitch.tv/kraken/chat/emoticons"));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray emoticons = (JSONArray) jsonObject.get("emoticons");

            for (Object o : emoticons) {
                String name = (String) ((JSONObject)o).get("regex");

                if(!name.matches("\\w+"))
                    continue;

                if (name.length() > 2)
                    emotes.add(name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return emotes;
        }

    }
*/

}
