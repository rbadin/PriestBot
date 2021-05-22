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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class Quote {

    private static String url = Constants.BASEURL + "quotes";
    private static String user = Constants.SQLUSER;
    private static String password = Constants.SQLPASSWORD;

    public static void createTables(String channel) {
                channel = channel.replace("#", "");
                Connection conn = null;
                PreparedStatement st = null;
                PreparedStatement st2 = null;
                String channelsubmit = channel + "submit";

        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + channel + " (`id` bigint(20) NOT NULL AUTO_INCREMENT, `author` varchar(255) NOT NULL, `quote` varchar(8000) NOT NULL, `game` varchar(8000) NOT NULL, `status` int(6) NOT NULL DEFAULT '1', PRIMARY KEY (`id`) ) ENGINE=MyISAM  DEFAULT CHARSET=latin1;");
            st2 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + channelsubmit + " (`id` bigint(20) NOT NULL AUTO_INCREMENT, `author` varchar(255) NOT NULL, `quote` varchar(8000) NOT NULL, `game` varchar(8000) NOT NULL, `status` int(6) NOT NULL DEFAULT '1', PRIMARY KEY (`id`) ) ENGINE=MyISAM  DEFAULT CHARSET=latin1;");
            st.executeUpdate();
            st2.executeUpdate();

            System.out.println("Tables successfully created");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            System.out.println("Ooopsie");
        } finally {
            try {
                if (st != null || st2 != null) {
                    st.close();
                    st2.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void editQuote(String channel, String id, String newtext) {
                channel = channel.replace("#", "");
                Connection conn = null;
                PreparedStatement st = null;
                int quoteId = Integer.parseInt(id);

        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("UPDATE " + channel + " set quote = ? WHERE id = ?");
            st.setString(1, newtext);
            st.setInt(2, quoteId);
            st.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            System.out.println("Ooopsie");
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void editGame(String channel, String id, String newgame) {
                channel = channel.replace("#", "");
                Connection conn = null;
                PreparedStatement st = null;
                int quoteId = Integer.parseInt(id);

        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.prepareStatement("UPDATE " + channel + " set game = ? WHERE id = ?");
            st.setString(1, newgame);
            st.setInt(2, quoteId);
            st.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            System.out.println("Ooopsie");
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

	public static String getRandomQuote(String channel, boolean setting) {
                channel = channel.replace("#", "");
        		Connection conn = null;
        		PreparedStatement st = null;
        		ResultSet rs = null;
                int id = 0;
                String quote = null;
                String game = null;

        try {
        	conn = DriverManager.getConnection(url, user, password);
        	st = conn.prepareStatement("SELECT * FROM " + channel + " ORDER BY RAND() limit 1");
        	rs = st.executeQuery();

        	while (rs.next()) {
        		id = rs.getInt(1);
        		quote = rs.getString(3);
        		game = rs.getString(4);
        	}
                if (setting == true)
                    return "Quote #" + id + ": " + quote + " [" + game + "]";
                else
                    return "Quote #" + id + ": " + quote;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        	return "Error accessing the Quote database";
        } finally {
        	try {
        		if (rs != null) {
        			rs.close();
        		}
        		if (st != null) {
        			st.close();
        		}
        		if (conn != null) {
        			conn.close();
        		}
        	} catch (SQLException ex) {
                System.out.println(ex.getMessage());
        		return "Error closing the statements";
        	}
        } 
        }

        public static String submitQuote(String channel, String author, String game, String quote) {
                channel = channel.replace("#", "");
                String channelsubmit = channel + "submit";
                Connection conn = null;
                PreparedStatement st = null;

                try {
                    conn = DriverManager.getConnection(url, user, password);
                    st = conn.prepareStatement("INSERT INTO " + channelsubmit + "(author, game, quote) VALUES(?,?,?)");
                    st.setString(1, author);
                    st.setString(2, game);
                    st.setString(3, quote);
                    st.executeUpdate();
                    System.out.println("Quote successfully submitted");
                    return "Quote submitted";
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println(ex.getMessage());
                    System.out.println("Error adding quote");
                    return "Error";
                } finally {
                    try {
                        if (st != null) {
                            st.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        System.out.println("Error closing the connections");
                        ex.printStackTrace();
                        return "Error closing the connections";
                    }
                }
        }

        public static String getPendingQuotes(String channel) {
                Connection conn = null;
                PreparedStatement st = null;
                int checker = 0;
                ResultSet rs = null;
                String response = "";
                String channelsubmit = channel + "submit";

                try {
                    conn = DriverManager.getConnection(url, user, password);
                    st = conn.prepareStatement("SELECT * FROM " + channelsubmit + " ORDER BY id ASC LIMIT 25");
                    rs = st.executeQuery();

                    while (rs.next()) {
                        response += "#" + rs.getInt(1) + " ";
                        checker = rs.getInt(1);
                    }

                    if (checker != 1) {
                    System.out.println("The pending quotes are: " + response);
                    return "The pending quotes are: " + response;
                    }
                    else {
                        return "There are no pending quotes";
                    }
                } catch (SQLException ex) {
                    System.out.println("Oops");
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                    return "Failed to get quotes";
                } finally {
                    try {
                        if (st != null) {
                            st.close();
                        }
                        if (rs != null) {
                            rs.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
        }

        public static String approveQuote(String channel, String id) {
                Connection conn = null;
                PreparedStatement st = null;
                PreparedStatement st2 = null;
                PreparedStatement st3 = null;
                ResultSet rs = null;
                String channelsubmit = channel + "submit";
                int quoteId = Integer.parseInt(id);
                String quotetoadd = "";
                String game = "";
                String author = "";

                try {
                    conn = DriverManager.getConnection(url, user, password);
                    st = conn.prepareStatement("SELECT * FROM " + channelsubmit + " WHERE id = ?");
                    st.setInt(1, quoteId);
                    rs = st.executeQuery();

                    while (rs.next()) {
                        quotetoadd = rs.getString(3);
                        author = rs.getString(2);
                        game = rs.getString(4);
                    }

                    System.out.println("Quote retrieved from DB");

                    st2 = conn.prepareStatement("INSERT INTO " + channel + "(author, game, quote) VALUES(?,?,?)");
                    st2.setString(1, author);
                    st2.setString(2, game);
                    st2.setString(3, quotetoadd);
                    st2.executeUpdate();

                    System.out.println("Quote approved");

                    st3 = conn.prepareStatement("DELETE FROM " + channelsubmit + " WHERE id = ?");
                    st3.setInt(1, quoteId);
                    st3.executeUpdate();

                    System.out.println("Quote deleted from the pending database");

                    return "Quote #" + quoteId + " has been approved";
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return "Error approving the quote";
                } finally {
                    try {
                        if (st != null || st2 != null || st3 != null) {
                            st.close();
                            st2.close();
                            st3.close();
                        }
                        if (rs != null) {
                            rs.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
        }

        public static String getPquote(String channel, String id) {
                Connection conn = null;
                PreparedStatement st = null;
                ResultSet rs = null;
                int quoteId = Integer.parseInt(id);
                String channelsubmit = channel + "submit";
                String author = "";
                String game = "";
                String quote = "";

                try {
                    conn = DriverManager.getConnection(url, user, password);
                    st = conn.prepareStatement("SELECT * FROM " + channelsubmit + " WHERE id = ?");
                    st.setInt(1, quoteId);
                    rs = st.executeQuery();

                    while (rs.next()) {
                        quote = rs.getString(3);
                        author = rs.getString(2);
                        game = rs.getString(4);
                    }

                    System.out.println("Quote: " + quote);
                    System.out.println("Game: " + game);
                    System.out.println("Author: " + author);
                    return "Quote #" + quoteId + ": " + quote + " [" + game + "] - added by: " + author;
                } catch (SQLException ex) { 
                    ex.printStackTrace();
                    System.out.println(ex.getMessage());
                    System.out.println("Ooops");
                    return "Failed to request quote";
                } finally {
                    try {
                        if (st != null) { 
                            st.close();
                        }
                        if (rs != null) {
                            rs.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
        }

        public static String rejectQuote(String channel, String id) {
                Connection conn = null;
                PreparedStatement st = null;
                int quoteId = Integer.parseInt(id);
                String channelsubmit = channel + "submit";

                try {
                    conn = DriverManager.getConnection(url, user, password);
                    st = conn.prepareStatement("DELETE FROM " + channelsubmit + " WHERE id = ?");
                    st.setInt(1, quoteId);
                    st.executeUpdate();

                    System.out.println("Quote successfully deleted");
                    return "Quote rejected";
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Failed");
                    return "Failed to reject quote";
                } finally {
                    try {
                        if (st != null) {
                            st.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
        }


        public static String getQuote(String channel, String id) {
                Connection conn = null;
                PreparedStatement st = null;
                ResultSet rs = null;
                String quote = null;
                String game = null;
                int quoteid = Integer.parseInt(id);

        try {
                conn = DriverManager.getConnection(url, user, password);
                st = conn.prepareStatement("SELECT * FROM " + channel + " WHERE id = ?");
                st.setInt(1, quoteid);
                rs = st.executeQuery();

                while (rs.next()) {
                        quote = rs.getString(3);
                        game = rs.getString(4);
                }
                return "Quote #" + id + ": " + quote + " [" + game + "]";
        } catch(SQLException ex) {
                System.out.println(ex.getMessage());
                return "Quote #" + id + " does not exist.";
        } finally {
                try {
                        if (rs != null) {
                                rs.close();
                        }
                        if (st != null) {
                                st.close();
                        }
                        if (conn != null) {
                                conn.close();
                        }
                } catch (SQLException ex) {
                        return "Error closing the statements";
                }
        }
        }

        public static String addQuote(String channel, String author, String game, String quote) {
                Connection conn = null;
                PreparedStatement st = null;

                try {
                        conn = DriverManager.getConnection(url, user, password);
                        st = conn.prepareStatement("INSERT INTO " + channel + "(author, game, quote) VALUES(?,?,?)");
                        st.setString(1, author);
                        st.setString(2, game);
                        st.setString(3, quote);
                        st.executeUpdate();
                        System.out.println("Quote successfully added");
                        return "Quote added";
                } catch (SQLException ex) {
                        ex.printStackTrace();
                        System.out.println(ex.getMessage());
                        System.out.println("Error adding the quote");
                        return "Quote not added";
                } finally {
                        try {
                                if (st != null) {
                                        st.close();
                                }
                                if (conn != null) {
                                        conn.close();
                                }
                        } catch (SQLException ex) {
                                System.out.println("Error closing the connection");
                                return "Error closing the connection";
                        }
                }
        }

        public static String deleteQuote(String channel, String id) {
                Connection conn = null;
                PreparedStatement st = null;
                int quoteId = Integer.parseInt(id);
        try {
                conn = DriverManager.getConnection(url, user, password);
                st = conn.prepareStatement("DELETE FROM " + channel + " WHERE id = ?");
                st.setInt(1, quoteId);
                st.executeUpdate();
                return "Quote #" + quoteId + " has been successfully lit on fire and thrown down a cliff to the land of failed quotes";
        } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
                return "There was an error deleting the quote";
        } finally {
                try {
                        if (st != null) {
                                st.close();
                        }
                        if (conn != null) {
                                conn.close();
                        }
                } catch (SQLException ex) {
                        return "Error closing the connection";
                }
        }
        }

        public static String deleteQuotesByAuthor(String channel, String author) {
                Connection conn = null;
                PreparedStatement st = null;
        try {
                conn = DriverManager.getConnection(url, user, password);
                st = conn.prepareStatement("DELETE FROM " + channel + " WHERE author = ?");
                st.setString(1, author);
                st.executeUpdate();
                return "Quotes from " + author + " have been deleted";
        } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
                return "There was an error deleting quotes";
        } finally {
                try {
                        if (st != null) {
                                st.close();
                        }
                        if (conn != null) {
                                conn.close();
                        }
                } catch (SQLException ex) {
                        return "Error closing the connection";
                }
        }
        }

        public static String findQuote(String channel, String text) {
                Connection conn = null;
                PreparedStatement st = null;
                ResultSet rs = null;
                String quotes = "";
                String match = "%" + text + "%";
        try {
                conn = DriverManager.getConnection(url, user, password);
                st = conn.prepareStatement("SELECT * FROM " + channel + " WHERE quote LIKE ? LIMIT 25");
                st.setString(1, match);
                rs = st.executeQuery();

                while (rs.next()) {
                        quotes += "#" + rs.getInt(1) + " ";
                }
                System.out.println(rs);
                return "Quotes containing the word/phrase " + text + " are: " + quotes;
        } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println(ex.getMessage());
                return "Error accessing the quote database";
        } finally {
                 try {
                        if (rs != null) {
                                rs.close();
                        }
                        if (st != null) {
                                st.close();
                        }
                        if (conn != null) {
                                conn.close();
                        }
                } catch (SQLException ex) {
                        return "Error closing the connection";
                }
        }
        }

        public static String findQuotesBy(String channel, String author) {
                Connection conn = null;
                PreparedStatement st = null;
                ResultSet rs = null;
                String quotes = "";
        try {
                conn = DriverManager.getConnection(url, user, password);
                st = conn.prepareStatement("SELECT * FROM " + channel + " WHERE author = ? LIMIT 25");
                st.setString(1, author);
                rs = st.executeQuery();

                while (rs.next()) {
                        quotes += "#" + rs.getInt(1) + " ";
                }
                return "Quotes added by " + author + " are: " + quotes;
        } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println(ex.getMessage());
                return "Error accessing the quote database";
        } finally {
                try {
                        if (rs != null) {
                                rs.close();
                        }
                        if (st != null) {
                                st.close();
                        }
                        if (conn != null) {
                                conn.close();
                        }
                } catch (SQLException ex) {
                        return "Error closing the connection";
                }
        }
        }

        public static String countQuotes(String channel) {
                Connection conn = null;
                PreparedStatement st = null;
                ResultSet rs = null;
                int numbers = 0;
        try {
                conn = DriverManager.getConnection(url, user, password);
                st = conn.prepareStatement("SELECT COUNT(*) from " + channel);
                rs = st.executeQuery();

                while (rs.next()) {
                        numbers = rs.getInt(1);
                }
                return "Total number of quotes: " + numbers;
        } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println(ex.getMessage());
                return "Error accessing the database";
        } finally {
                try {
                        if (rs != null) {
                                rs.close();
                        }
                        if (st != null) {
                                st.close();
                        }
                        if (conn != null) {
                                conn.close();
                        }
                } catch (SQLException ex) {
                        return "Error closing the connection";
                } 
        }
        }

        public static String getLastQuote(String channel) {
                Connection conn = null;
                PreparedStatement st = null;
                ResultSet rs = null;
                int id = 0;
                String quote = null;
                String game = null;
        try {
                conn = DriverManager.getConnection(url, user, password);
                st = conn.prepareStatement("SELECT * FROM " + channel + " ORDER BY id desc LIMIT 1");
                rs = st.executeQuery();

                while (rs.next()) {
                        id = rs.getInt(1);
                        quote = rs.getString(3);
                        game = rs.getString(4);
                }
                return "Quote #" + id + ": " + quote + " [" + game + "]";
        } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println(ex.getMessage());
                return "Error accessing the database";
        } finally {
                 try {
                        if (rs != null) {
                                rs.close();
                        }
                        if (st != null) {
                                st.close();
                        }
                        if (conn != null) {
                                conn.close();
                        }
                } catch (SQLException ex) {
                        return "Error closing the connection";
                } 
        }
        }
}