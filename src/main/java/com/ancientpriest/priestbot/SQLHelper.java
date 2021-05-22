/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ancientpriest.priestbot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.*;
/**
 *
 * @author bragluca
 */
public class SQLHelper {
    private static String url = Constants.BASEURL;
    private static String user = Constants.SQLUSER;
    private static String password = Constants.SQLPASSWORD;
    
    public static void updateLeagueChampions(Map<Integer, String> champs) {
        Connection conn = null;
        PreparedStatement st = null;
        
        try {
            conn = DriverManager.getConnection(url + "static", user, password);
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO champions(champ_id, champ_name) VALUES (?, ?)");
            for (Map.Entry<Integer, String> entry : champs.entrySet()) {
                st.setInt(1, entry.getKey());
                st.setString(2, entry.getValue());
                st.addBatch();
            }
            st.executeBatch();
            conn.commit();
            
            System.out.println("DEBUG: League champions updated");
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            _closeThings(conn, st);
        }
    }

    public static void closeThings(Connection conn, PreparedStatement st, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (st != null)
                st.close();
            if (conn != null)
                conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void _closeThings(Connection conn, PreparedStatement st) {
        try {
            if (st != null)
                st.close();
            if (conn != null)
                conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



    public static boolean updateChannels(List<String> channels) {
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO channel(channel) VALUES (?)");
            for (String s : channels) {
                st.setString(1, s.toLowerCase());
                st.addBatch();
            }
            st.executeBatch();
            conn.commit();

            System.out.println("DEBUG: Finished updating channels");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            _closeThings(conn, st);
        }
    }

    public static boolean addChannel(String channel, int mode) {
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("INSERT INTO channel(channel, level) VALUES (?, ?) ON DUPLICATE KEY UPDATE channel = ?");
            st.setString(1, channel);
            st.setInt(2, mode);
            st.setString(3, channel);
            st.executeUpdate();
            System.out.println("Channel successfully added");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            _closeThings(conn, st);
        }
    }

    public static boolean removeChannel(String channel) {
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("DELETE FROM channel WHERE channel = ?");
            st.setString(1, channel);
            st.executeUpdate();
            System.out.println("Channel successfully removed");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            _closeThings(conn, st);
        }
    }

    public static boolean isJoinedChannel(String channel) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url + "priestbot", user, password);
            st = conn.prepareStatement("SELECT channel FROM channel WHERE channel = ?");
            st.setString(1, channel);
            rs = st.executeQuery();

            if (rs.next())
                return true;
            else
                return false;

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("SQLHelper.java:76 - Error while trying to access the db");
            return false;
        } finally {
            closeThings(conn, st, rs);
        }
    }

    public static String getChampionNameByID(String id) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        
        try {
            conn = DriverManager.getConnection(url + "static", user, password);
            st = conn.prepareStatement("SELECT champ_name FROM champions WHERE champ_id = ?");
            st.setString(1, id);
            rs = st.executeQuery();
            
            if (!rs.next()) 
                return "Error";
            else {
                do {
                    return rs.getString(1);
                } while (rs.next());
            }
                
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("SQLHelper.java:76 - Error while trying to access the db");
            return "error";
        } finally {
            closeThings(conn, st, rs);
        }
    }
}
