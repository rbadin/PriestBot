package com.ancientpriest.priestbot;

import java.sql.*;

public class Logging {
    private static Connection conn = null;
    private static PreparedStatement st = null;
    private static ResultSet rs = null;
    
    public static void logAdminCommand(String channel, String sender, String command) {
        channel = channel.replaceAll("#", "").toLowerCase();
        try {
            conn = DriverManager.getConnection(Constants.BASEURL + "admins", Constants.SQLUSER, Constants.SQLPASSWORD);
            st = conn.prepareStatement("INSERT INTO commands (admin_name, channel_name, command_name) VALUES (?, ?, ?)");
            st.setString(1, sender);
            st.setString(2, channel);
            st.setString(3, command);
            st.executeUpdate();
            
            System.out.println("Admin command logged");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Logging.java:24 = Error logging admin command");
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
    
    public static void logAdminMessage(String channel, String sender, String message) {
        channel = channel.replaceAll("#", "").toLowerCase();
        try {
            conn = DriverManager.getConnection(Constants.BASEURL + "admins", Constants.SQLUSER, Constants.SQLPASSWORD);
            st = conn.prepareStatement("INSERT INTO messages (admin_name, channel_name, msg) VALUES (?, ?, ?)");
            st.setString(1, sender);
            st.setString(2, channel);
            st.setString(3, message);
            st.executeUpdate();
            
            System.out.println("Admin message logged");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Logging.java:24 = Error logging admin message");
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

    public static void logCommand(String channel, String command, String value, String author) {
        channel = channel.toLowerCase();

        try {
            conn = DriverManager.getConnection(Constants.BASEURL + "logs", Constants.SQLUSER, Constants.SQLPASSWORD);
            st = conn.prepareStatement("INSERT INTO commands (command_name, command_value, channel, author) VALUES (?, ?, ?, ?)");
            st.setString(1, command);
            st.setString(2, value);
            st.setString(3, channel);
            st.setString(4, author);
            st.executeUpdate();

            System.out.println("Command logged");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.print("Error logging the points");
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
}
