/*
 * WikiDB.java
 *
 * Created on 12. September 2007, 18:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.knowledge;

import elkfed.config.ConfigProperties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 *
 * @author versley
 */
public class WikiDB {
    private static WikiDB _instance;
    
    /** The db connection */
    private transient Connection connection;
    
    /** The statement to the db */
    private transient Statement statement;
    
    /** Cached database results */
    private HashMap<String, String> aliasCache;
    private HashMap<String, String> redirCache;
    private HashMap<String, String> listCache;
    private HashMap<String, Integer> blockCache;
    
    
    public static WikiDB getInstance() {
        if (_instance==null) {
            _instance=new WikiDB();
        }
        return _instance;
    }
    
    /** Creates a new instance of WikiDB */
    private WikiDB() {
        try {
            connection = ConfigProperties.getInstance().getWikiDBConnection();
            statement = connection.createStatement();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("cannot connect to database",ex);
        }
        aliasCache = new HashMap<String, String>();
        listCache = new HashMap<String, String>();
        redirCache = new HashMap<String, String>();
        blockCache = new HashMap<String,Integer>();
    }
    
    public String getAlias(String antString) throws SQLException {
        if (!aliasCache.containsKey(antString)) {
            final ResultSet antSet =
                    statement.executeQuery("SELECT alias, page_ids FROM links_to WHERE alias = '" + antString + "'");
            
            if (antSet.next()) {
                aliasCache.put(antString, antSet.getString("page_ids"));
                antSet.close();
            } else {
                aliasCache.put(antString, "");
            }
        }
        return aliasCache.get(antString);
    }
    
    public String getRedir(String antString) throws SQLException {
        if (!redirCache.containsKey(antString)) {
            final ResultSet antSet =
                    statement.executeQuery("SELECT alias, page_id FROM redirects_to WHERE alias = '" + antString + "'");
            
            if (antSet.next()) {
                redirCache.put(antString, antSet.getString("page_id"));
                antSet.close();
            } else {
                redirCache.put(antString, "");
            }
        }
        return redirCache.get(antString);
    }
    
    public String getLists(String antString) throws SQLException {
        if (!listCache.containsKey(antString)) {
            final ResultSet antSet =
                    statement.executeQuery("SELECT name, lists FROM lists_dev WHERE name = '" + antString + "'");
            
            if (antSet.next()) {
                listCache.put(antString, antSet.getString("lists"));
                antSet.close();
            } else {
                listCache.put(antString, "");
            }
        }
        return listCache.get(antString);
    }
    
    public int getBlock(String pageName) {
        if (blockCache.containsKey(pageName)) {
            return blockCache.get(pageName);
        } else {
            try {
                final ResultSet antSet =
                        statement.executeQuery("SELECT block_id FROM articles WHERE article_name = BINARY'" + pageName + "'");
                int result;
                if (antSet.next()) {
                    result=antSet.getInt(1);
                } else {
                    result=-1;
                }
                blockCache.put(pageName,result);
                return result;
            } catch (SQLException ex) {
                throw new RuntimeException("cannot get block id",ex);
            }
        }
    }
}
