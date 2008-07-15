/*
 * Created on 2006-apr-05
 *
 * Copyright (C) 2006 Royal Library of Sweden.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.archive.wayback.core;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Generic class for simple key-value pair lookup using BDBJE.
 *
 * @author oskar.grenholm@kb.se
 * @version $Date$, $Revision$
 */
public class BDBMap {

    // Acts as a mapping between an ID and a timestamp to surf at.
    // The dir should probably be configurable somehow.
    private static String BDB_DIR = System.getProperty("java.io.tmpdir") +
    	"/wayback/bdb";
    private static Properties bdbMaps = new Properties();

    protected Environment env = null;
    protected Database db = null;
    protected String name;
    protected String dir;
    
    /**
     * consturctor
     * @param name of database
     * @param dir path of directory where dbd files should be stored. The 
     * directory is created if it does not exist.
     */
    public BDBMap(String name, String dir) {
        this.name = name;
        this.dir = dir;
        init();        
    }
    
    protected void init() {
        try {
            EnvironmentConfig envConf = new EnvironmentConfig();
            envConf.setAllowCreate(true);
            File envDir = new File(dir);
            if (!envDir.exists())
                envDir.mkdirs();
            env = new Environment(envDir, envConf);
            
            DatabaseConfig dbConf = new DatabaseConfig();
            dbConf.setAllowCreate(true);
            dbConf.setSortedDuplicates(false);
            db = env.openDatabase(null, name, dbConf);  
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * persistantly store key-value pair 
     * @param keyStr
     * @param valueStr
     */
    public void put(String keyStr, String valueStr) {
        try {
            DatabaseEntry key = new DatabaseEntry(keyStr.getBytes("UTF-8"));
            DatabaseEntry data = new DatabaseEntry(valueStr.getBytes("UTF-8"));
            db.put(null, key, data);            
         } catch (DatabaseException e) {
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * retrieve the value assoicated with keyStr from persistant storage
     * @param keyStr
     * @return String value associated with key, or null if no key is found
     * or an error occurs
     */
    public String get(String keyStr) {
        String result = null;
        try {
            DatabaseEntry key = new DatabaseEntry(keyStr.getBytes("UTF-8"));
            DatabaseEntry data = new DatabaseEntry();
            if (db.get(null, key, data, LockMode.DEFAULT) == 
            	OperationStatus.SUCCESS) {
            	
                byte[] bytes = data.getData();
                result = new String(bytes, "UTF-8");
            }
         } catch (DatabaseException e) {
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    
	/**
	 * @param context
	 * @return singleton BDBMap for the context
	 */
	public static BDBMap getContextMap(String context) {
    	if(context == null) context = "";
    	if(context.startsWith("/")) {
    		context = context.substring(1);
    	}
		BDBMap map = null;
    	synchronized(Timestamp.class) {
    		if(!bdbMaps.containsKey(context)) {
    			File bdbDir = new File(BDB_DIR,context);
    			bdbMaps.put(context,new BDBMap(context, 
    					bdbDir.getAbsolutePath()));
    		}
    		map = (BDBMap) bdbMaps.get(context);
    	}
    	return map;
	}
    /**
     * return the timestamp associated with the identifier argument, or now
     * if no value is associated or something goes wrong.
     * @param context 
     * @param ip
     * @return timestamp string value
     */
    public static String getTimestampForId(String context, String ip) {
    	BDBMap bdbMap = getContextMap(context);
        String dateStr = bdbMap.get(ip);
        return (dateStr != null) ? dateStr : Timestamp.currentTimestamp().getDateStr();
    }
    
   /**
    * associate timestamp time with idenfier ip persistantly 
    * @param context 
    * @param ip
    * @param time
    */
    public static void addTimestampForId(String context, String ip, String time) {
    	BDBMap bdbMap = getContextMap(context);
    	bdbMap.put(ip, time);
    }
}
