package com.ancientpriest.priestbot;

import java.sql.*;
import java.util.*;

/**
 * Created by bragluca on 7/15/16.
 */
public class Config {

    private static String url = Constants.BASEURL;
    private static String user = Constants.SQLUSER;
    private static String password = Constants.SQLPASSWORD;
    private static Connection conn = null;
    private static PreparedStatement st = null;
    private static ResultSet rs = null;


    public static void joinChannel(String channel) {



        if (!SQLHelper.isJoinedChannel(channel)) {
            //create commands table
            createCommandsTable(channel);
            //createa logs table
            createLogsTable(channel);
            //create filters table
            createFiltersTable(channel);
            //create autoreply table
            createAutoReplyTable(channel);
            //create userlevel table
            createUserLevelTable(channel);
            //create properties table
            createPropertiesTable(channel);
        }
    }
    
    public static void cloneChannel(String origin, String dest) {
        deleteCommandsTable(dest);
        deleteLogsTable(dest);
        deleteFiltersTable(dest);
        deleteAutoReplyTable(dest);
        deleteUserLevelTable(dest);
        deletePropertiesTable(dest);
        renameCommandsTable(origin, dest);
        renameLogsTable(origin, dest);
        renameFiltersTable(origin, dest);
        renameAutoReplyTable(origin, dest);
        renameUserLevelTable(origin, dest);
        renamePropertiesTable(origin, dest);
        SQLHelper.removeChannel(origin.toLowerCase());
    }
    // *************************************************1. COMMANDS *******************************************************
    private static void deleteCommandsTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "commands", user, password);
            st = conn.prepareStatement("DROP TABLE IF EXISTS `" + channel + "`;");
            st.executeUpdate();
            System.out.println("Commands table successfully deleted for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void renameCommandsTable(String origin, String dest) {
        try {
            conn = DriverManager.getConnection(url + "commands", user, password);
            st = conn.prepareStatement("RENAME TABLE `" + origin + "` TO `" + dest + "`;");
            st.executeUpdate();
            System.out.println("Commands table successfully renamed from: " + origin + " to: " + dest);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static void createCommandsTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "commands", user, password);
            st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + channel + "` (`id` int(32) NOT NULL AUTO_INCREMENT, `command_name` nvarchar(100) NOT NULL, `command_answer` nvarchar(600) NOT NULL, `req_level` varchar(25) NOT NULL, `added_by` varchar(100) NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `command_name` (`command_name`) ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;");
            st.executeUpdate();
            System.out.println("Commands table created successfully for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

// ********************************************************************2. LOGS ***********************************************
    public static void createLogsTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "logs", user, password);
            st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + channel + "` (`id` int(8) NOT NULL AUTO_INCREMENT, `sender` varchar(32) NOT NULL, `regex` varchar(50) NOT NULL, `type` varchar(35) NOT NULL, `message` nvarchar(300) NOT NULL, `duration` int(8) NOT NULL, `ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`) ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;");
            st.executeUpdate();
            System.out.println("Logs table created successfully for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void deleteLogsTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "logs", user, password);
            st = conn.prepareStatement("DROP TABLE IF EXISTS `" + channel + "`;");
            st.executeUpdate();
            System.out.println("Logs table successfully deleted for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void renameLogsTable(String origin, String dest) {
        try {
            conn = DriverManager.getConnection(url + "logs", user, password);
            st = conn.prepareStatement("RENAME TABLE `" + origin + "` TO `" + dest + "`;");
            st.executeUpdate();
            System.out.println("Logs table successfully renamed from: " + origin + " to: " + dest);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

// ********************************************************* 3.  FILTERS *******************************************
    public static void createFiltersTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "filters", user, password);
            st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + channel + "` (`id` int(32) NOT NULL AUTO_INCREMENT, `filter_regex` varchar(200) NOT NULL, `added_by` varchar(100) NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `filter_regex` (`filter_regex`) ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;");
            st.executeUpdate();
            System.out.println("Filters table created successfully for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void deleteFiltersTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "filters", user, password);
            st = conn.prepareStatement("DROP TABLE IF EXISTS `" + channel + "`;");
            st.executeUpdate();
            System.out.println("Filters table successfully deleted for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void renameFiltersTable(String origin, String dest) {
        try {
            conn = DriverManager.getConnection(url + "filters", user, password);
            st = conn.prepareStatement("RENAME TABLE `" + origin + "` TO `" + dest + "`;");
            st.executeUpdate();
            System.out.println("Filters table successfully renamed from: " + origin + " to: " + dest);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

// ********************************************************4. AUTO REPLY *******************************************************
    public static void createAutoReplyTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "autoreply", user, password);
            st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + channel + "` (`id` int(32) NOT NULL AUTO_INCREMENT, `ar_regex` varchar(200) NOT NULL, `ar_answer` varchar(600) NOT NULL, `added_by` varchar(100) NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `ar_regex` (`ar_regex`) ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;");
            st.executeUpdate();
            System.out.println("AutoReply table created successfully for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void deleteAutoReplyTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "autoreply", user, password);
            st = conn.prepareStatement("DROP TABLE IF EXISTS `" + channel + "`;");
            st.executeUpdate();
            System.out.println("Autoreply table successfully deleted for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void renameAutoReplyTable(String origin, String dest) {
        try {
            conn = DriverManager.getConnection(url + "autoreply", user, password);
            st = conn.prepareStatement("RENAME TABLE `" + origin + "` TO `" + dest + "`;");
            st.executeUpdate();
            System.out.println("Autoreply table successfully renamed from: " + origin + " to: " + dest);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    // ****************************************************5. USER LEVEL *******************************************************
    public static void createUserLevelTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "userlevel", user, password);
            st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + channel + "` (`id` int(32) NOT NULL AUTO_INCREMENT, `username` varchar(100) NOT NULL, `userlevel` varchar(45) NOT NULL, `added_by` varchar(100) NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `username` (`username`) ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;");
            st.executeUpdate();
            System.out.println("UserLevel table created successfully for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void deleteUserLevelTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "userlevel", user, password);
            st = conn.prepareStatement("DROP TABLE IF EXISTS `" + channel + "`;");
            st.executeUpdate();
            System.out.println("Userlevel table successfully deleted for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void renameUserLevelTable(String origin, String dest) {
        try {
            conn = DriverManager.getConnection(url + "userlevel", user, password);
            st = conn.prepareStatement("RENAME TABLE `" + origin + "` TO `" + dest + "`;");
            st.executeUpdate();
            System.out.println("Userlevel table successfully renamed from: " + origin + " to: " + dest);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    // *********************************************************6. PROPERTIES ****************************************************
    public static void createPropertiesTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "properties", user, password);
            st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + channel + "` (`id` int(32) NOT NULL AUTO_INCREMENT, `pkey` varchar(200) NOT NULL, `pvalue` varchar(8000) NOT NULL, `added_by` varchar(100) NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `pkey` (`pkey`) ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;");
            st.executeUpdate();
            System.out.println("Properties table created successfully for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void deletePropertiesTable(String channel) {
        try {
            conn = DriverManager.getConnection(url + "properties", user, password);
            st = conn.prepareStatement("DROP TABLE IF EXISTS `" + channel + "`;");
            st.executeUpdate();
            System.out.println("Properties table successfully deleted for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    private static void renamePropertiesTable(String origin, String dest) {
        try {
            conn = DriverManager.getConnection(url + "properties", user, password);
            st = conn.prepareStatement("RENAME TABLE `" + origin + "` TO `" + dest + "`;");
            st.executeUpdate();
            System.out.println("Properties table successfully renamed from: " + origin + " to: " + dest);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    // ************************************************ END TABLES **************************************************************
    public static void addCommand(String channel, String key, String value, String level, String author) {
        try {
            conn = DriverManager.getConnection(url + "commands?useUnicode=yes&characterEncoding=UTF-8", user, password);
            st = conn.prepareStatement("INSERT INTO `" + channel + "` (command_name, command_answer, req_level, added_by) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE command_answer = ?, added_by = ?");
                st.setString(1, key);
                st.setString(2, value);
                st.setString(3, level);
                st.setString(4, author);
                st.setString(5, value);
                st.setString(6, author);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static void addAdmin(String userID, String author) {
        try {
            conn = DriverManager.getConnection(url + "priestbot?useUnicode=yes&characterEncoding=UTF-8", user, password);
            st = conn.prepareStatement("INSERT INTO `admins` (userid, added_by) VALUES (?,?) ON DUPLICATE KEY UPDATE added_by = ?");
                st.setString(1, userID);
                st.setString(2, author);
                st.setString(3, author);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static void addSuperAdmin(String userID, String author) {
        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("INSERT INTO `superadmins` (userid, added_by) VALUES (?,?) ON DUPLICATE KEY UPDATE added_by = ?");
                st.setString(1, userID);
                st.setString(2, author);
                st.setString(3, author);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static void addSubAdmin(String userID, String author) {
        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("INSERT INTO `subadmins` (userid, added_by) VALUES (?,?) ON DUPLICATE KEY UPDATE added_by = ?");
                st.setString(1, userID);
                st.setString(2, author);
                st.setString(3, author);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static void logTimeout(String channel, String sender, String regex, String message, String type, int duration) {
        channel = channel.toLowerCase();
        sender = sender.toLowerCase();
        try {
            conn = DriverManager.getConnection(url + "logs?useUnicode=yes&characterEncoding=UTF-8", user, password);
            st = conn.prepareStatement("INSERT INTO `" + channel + "` (sender, regex, type, message, duration) VALUES (?,?,?,?,?)");
                st.setString(1, sender);
                st.setString(2, regex);
                st.setString(3, type);
                st.setString(4, message);
                st.setInt(5, duration);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static String getCommand(String channel, String key) {
        String toReturn = "";
        try {
            conn = DriverManager.getConnection(url + "commands?useUnicode=yes", user, password);
            st = conn.prepareStatement("SELECT * FROM `" + channel + "` WHERE command_name = ?");
            st.setString(1, key);
            rs = st.executeQuery();

            while(rs.next())
                toReturn = rs.getString(3);
            System.out.println("Returning: " + toReturn);
            return toReturn;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return toReturn;
        } finally {
            SQLHelper.closeThings(conn, st, rs);
        }
    }

    public static List<String> getCommands(String channel) {
        List<String> toReturn = new LinkedList<>();
        try {
            conn = DriverManager.getConnection(url + "commands?useUnicode=yes", user, password);
            st = conn.prepareStatement("SELECT * FROM `" + channel + "`");
            rs = st.executeQuery();

            while(rs.next())
                toReturn.add(rs.getString(2));
            System.out.println("Returning: " + toReturn.toString());
            return toReturn;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return null;
        } finally {
            SQLHelper.closeThings(conn, st, rs);
        }
    }

    public static boolean isCommand(String channel, String key) {
        try {
            conn = DriverManager.getConnection(url + "commands?useUnicode=yes", user, password);
            st = conn.prepareStatement("SELECT * FROM `" + channel + "` WHERE command_name = ?");
            st.setString(1, key);
            rs = st.executeQuery();

            if (rs.next())
                return true;
            else
                return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return false;
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static boolean isAdmin(String userID) {
        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("SELECT * FROM `admins` WHERE userid = ?");
            st.setString(1, userID);
            rs = st.executeQuery();

            if (rs.next())
                return true;
            else
                return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return false;
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static boolean isSubAdmin(String userID) {
        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("SELECT * FROM `subadmins` WHERE userid = ?");
            st.setString(1, userID);
            rs = st.executeQuery();

            if (rs.next())
                return true;
            else
                return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return false;
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static boolean isSuperAdmin(String userID) {
        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("SELECT * FROM `superadmins` WHERE userid = ?");
            st.setString(1, userID);
            rs = st.executeQuery();

            if (rs.next())
                return true;
            else
                return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return false;
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static void removeCommand(String channel, String key) {
        try {
            conn = DriverManager.getConnection(url + "commands?useUnicode=yes", user, password);
            st = conn.prepareStatement("DELETE FROM `" + channel + "` WHERE command_name = ?");
            st.setString(1, key);
            st.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static void removeAdmin(String userID) {
        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("DELETE FROM `admins` WHERE userid = ?");
            st.setString(1, userID);
            st.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static void removeSuperAdmin(String userID) {
        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("DELETE FROM `superadmins` WHERE userid = ?");
            st.setString(1, userID);
            st.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }
    
    public static void removeSubAdmin(String userID) {
        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("DELETE FROM `subadmins` WHERE userid = ?");
            st.setString(1, userID);
            st.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static void setCommandLevel(String channel, String key, String level) {
        try {
            conn = DriverManager.getConnection(url + "commands?useUnicode=yes", user, password);
            st = conn.prepareStatement("UPDATE `" + channel + "` SET req_level = ? WHERE command_name = ?");
            st.setString(1, level);
            st.setString(2, key);
            st.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static int getCommandRestriction(String channel, String key) {
        String toReturn = "0";
        try {
            conn = DriverManager.getConnection(url + "commands?useUnicode=yes", user, password);
            st = conn.prepareStatement("SELECT * FROM `" + channel + "` WHERE command_name = ?");
            st.setString(1, key);
            rs = st.executeQuery();

            while (rs.next())
                toReturn = rs.getString(4);

            return Integer.parseInt(toReturn);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return Integer.parseInt(toReturn);
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static void setProperty(String channel, String key, String value) {
        try {
            conn = DriverManager.getConnection(url + "properties?useUnicode=yes", user, password);
            st = conn.prepareStatement("UPDATE `" + channel + "` SET pvalue = ? WHERE pkey = ?");
                st.setString(1, value);
                st.setString(2, key);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static HashMap<String, String> getProperties(String channel) {
        HashMap<String, String> toReturn = new HashMap<>();
        try {
            conn = DriverManager.getConnection(url + "properties", user, password);
            st = conn.prepareStatement("SELECT * FROM `" + channel + "`");
            rs = st.executeQuery();

            while (rs.next())
                toReturn.put(rs.getString(2), rs.getString(3));
            
            System.out.println("Successfully got properties for channel: " +channel);
            return toReturn;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL ERROR!");
            return null;
        } finally {
            SQLHelper.closeThings(conn, st, rs);
        }
    }

    public static String getProperty(String channel, String key) {
        String value = "";
        try {
            conn = DriverManager.getConnection(url + "properties", user, password);
            st = conn.prepareStatement("SELECT * FROM `" + channel + "` WHERE pkey = ?");
                st.setString(1, key);
            rs = st.executeQuery();

            while (rs.next())
                value = rs.getString(3);
            System.out.println("Value returned from query: " + value);
            return value;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return value;
        } finally {
            SQLHelper.closeThings(conn, st, rs);
        }
    }

    public static void _setCommands(String channel, Map<String, String> commands) {
        try {
            conn = DriverManager.getConnection(url + "commands?useUnicode=yes", user, password);
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO `" + channel + "` (command_name, command_answer, req_level, added_by) VALUES (?, ?, ?, ?)");
            Iterator itr = commands.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry pairs = (Map.Entry) itr.next();
                String key = String.valueOf(pairs.getKey());
                String value = String.valueOf(pairs.getValue());
                st.setString(1, key);
                st.setString(2, value);
                st.setString(3, "0");
                st.setString(4, "admin");
                st.addBatch();
            }
            st.executeBatch();
            conn.commit();

            System.out.println("Successfully added commands for " + channel);

            } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static void _setDefaultProperties(String channel, Map<String, Object> defaults) {
        try {
            conn = DriverManager.getConnection(url + "properties?useUnicode=yes", user, password);
            conn.setAutoCommit(false);
            st = conn.prepareStatement("REPLACE INTO `" + channel + "` (pkey, pvalue, added_by) VALUES (?, ?, ?)");
            Iterator itr = defaults.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry pairs = (Map.Entry) itr.next();
                String key = String.valueOf(pairs.getKey());
                String value = String.valueOf(pairs.getValue());
                st.setString(1, key);
                st.setString(2, value);
                st.setString(3, "admin");
                st.addBatch();
            }
            st.executeBatch();
            conn.commit();

            System.out.println("Successfully loaded default values for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static void setDefaultProperties(String channel, Map<String, Object> defaults) {
        try {
            conn = DriverManager.getConnection(url + "properties?useUnicode=yes", user, password);
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO `" + channel + "` (pkey, pvalue, added_by) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE added_by = ?");
            Iterator itr = defaults.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry pairs = (Map.Entry) itr.next();
                String key = String.valueOf(pairs.getKey());
                String value = String.valueOf(pairs.getValue());
                st.setString(1, key);
                st.setString(2, value);
                st.setString(3, "admin");
                st.setString(4, "admin");
                st.addBatch();
            }
            st.executeBatch();
            conn.commit();

            System.out.println("Successfully loaded default config values for: " + channel);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }

    public static void setUserLevel(String channel, String username, String userlevel) {
        try {
            conn = DriverManager.getConnection(url + "userlevel?useUnicode=yes", user, password);
            st = conn.prepareStatement("INSERT INTO `" + channel + "` (username, userlevel, added_by) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE userlevel = ?");
                st.setString(1, username);
                st.setString(2, userlevel);
                st.setString(3, "broadcaster");
                st.setString(4, userlevel);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            SQLHelper._closeThings(conn, st);
        }
    }


    public static HashMap<String, String> getUserLevels(String channel) {
        /*
           id - int AUTO_INCREMENT
           username - varchar (UNIQUE)
           userlevel - varchar
           added_by - varchar
         */
        HashMap<String, String> toReturn = new HashMap<>();
        try {
            conn = DriverManager.getConnection(url + "userlevel", user, password);
            st = conn.prepareStatement("SELECT * FROM `" + channel + "`");
            rs = st.executeQuery();

            while (rs.next())
                toReturn.put(rs.getString(2), rs.getString(3));

            return toReturn;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        } finally {
            SQLHelper.closeThings(conn, st, rs);
        }
    }

    public static String getUserLevel(String channel, String username) {
        String level = "regular";
        try {
            conn = DriverManager.getConnection(url + "userlevel", user, password);
            st = conn.prepareStatement("SELECT * FROM `" + channel + "` WHERE username = ?");
            st.setString(1, username);
            rs = st.executeQuery();

            while (rs.next())
                level = rs.getString(3);

            return level;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return level;
        } finally {
            SQLHelper.closeThings(conn, st, rs);
        }
    }




  /*
    public static Map<String, Boolean> getCommandsRepeat(String channel) {
        // TODO
    }

    public static Map<Integer, Integer> getCommandsRepeatDetails(String channel) {
        // TODO
    }

    public static Map<String, String> getUserLevels(String channel) {

            TODO:
            - Staff (admin)
            - Admin (admin)
            - Global Mod (admin)
            - Bot Admin (admin)
            - Broadcaster (broadcaster)
            - Channel Admin (channel_admin)
            - Owner (owner)
            - Moderator (mod)
            - Subscriber/Regular (regular)
            - Viewer ()
            - Restricted (restricted)
         
    }
    */

}
