/*
 * Copyright 2016 Lucas Braga
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.*;
/**
 *
 * @author Lucas Braga
 */
public class Points {
    private static String url = Constants.BASEURL + "points";
    private static String user = Constants.SQLUSER;
    private static String password = Constants.SQLPASSWORD;
    private static String logurl = Constants.BASEURL + "logs";
    private static Connection conn = null;
    private static PreparedStatement st = null;
    private static ResultSet rs = null;
    private Timer pointsTimer;
    private boolean active;
    
    public Points() {
        active = false;
    }
    
    public Timer getTimer() {
        return pointsTimer;
    }
    
    public void setTimer(Timer t) {
        pointsTimer = t;
    }
    
    public void setStatus(boolean setting) {
        active = setting;
    }
    
    public boolean getStatus() {
        return active;
    }
    
    public static void logPoints(String channel, String action, String whodunnit, String target, String date) {
        channel = channel.toLowerCase();
        
        
        try {
            conn = DriverManager.getConnection(logurl, user, password);
            st = conn.prepareStatement("INSERT INTO points (date, channel, whodunnit, target, action) VALUES (?, ?, ?, ?, ?)");
            st.setString(1, date);
            st.setString(2, channel);
            st.setString(3, whodunnit);
            st.setString(4, target);
            st.setString(5, action);
            st.executeUpdate();
            
            System.out.println("Points logged");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void createTables(String channel) {
        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + channel + " (`id` int(32) NOT NULL AUTO_INCREMENT, `name` varchar(255) NOT NULL, `points` int(32) NOT NULL, PRIMARY KEY (`id`), UNIQUE (`name`) ) ENGINE=MyISAM  DEFAULT CHARSET=latin1;");
            st.executeUpdate();
            System.out.println("Tables successfully created");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("There was an error creating the table");
        } finally {
            try {
                if (st != null)
                    st.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void distributePoints(String channel, List<String> users, int amount) {
        
        try {
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO " + channel + " (name, points) VALUES (?, ?) ON DUPLICATE KEY UPDATE points = points + ?");
            for (String name : users) {
                st.setString(1, name);
                st.setInt(2, amount);
                st.setInt(3, amount);
                st.addBatch();
            }
            st.executeBatch();
            conn.commit();
            
            System.out.println("Successfully distributed points");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Error distributing points");
        } finally {
            try {
                if (st != null) 
                    st.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error closing out connection and statement");
            }
        
        }

    }
    
    public static int getPoints(String channel, String username) {
        username = username.toLowerCase();
        channel = channel.toLowerCase();
        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("SELECT * from " + channel + " WHERE name = ?");
            st.setString(1, username);
            rs = st.executeQuery();
            
            if (!rs.next()) 
                return 0;
            else {
                do {
                    return rs.getInt(3);
                } while (rs.next());
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Points.java: Error while trying to access the db");
            return 0;
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
                System.out.println("Points.java: Error closing shit");
            }
        }
    }
    
    
    public static int addPoints(String channel, String username, int newPoints) {
        PreparedStatement st2 = null;
        username = username.toLowerCase();
        channel = channel.toLowerCase();
        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("INSERT INTO " + channel + " (name, points) VALUES (?, ?) ON DUPLICATE KEY UPDATE points = points + ?");
            st.setString(1, username);
            st.setInt(2, newPoints);
            st.setInt(3, newPoints);
            st.executeUpdate();
            
            st2 = conn.prepareStatement("Select * from " + channel + " WHERE name = ?");
            st2.setString(1, username);
            rs = st2.executeQuery();
            
            if (!rs.next())
                return newPoints;
            else {
                do {
                    return rs.getInt(3);
                } while (rs.next());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -696969;
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
    
    public static int removePoints(String channel, String username, int newPoints) {
        PreparedStatement st2 = null;
        username = username.toLowerCase();
        channel = channel.toLowerCase();
        int toremove = 0 - newPoints;
        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("INSERT INTO " +  channel + " (name, points) VALUES (?, ?) ON DUPLICATE KEY UPDATE points = points - ?");
            st.setString((1), username);
            st.setInt(2, toremove);
            st.setInt(3, newPoints);
            st.executeUpdate();
            
            st2 = conn.prepareStatement("Select * from " +  channel + " WHERE name = ?");
            st2.setString(1, username);
            rs = st2.executeQuery();
            
            if (!rs.next())
                return toremove;
            else {
                do {
                    return rs.getInt(3);
                } while (rs.next());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -696969;
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
}
