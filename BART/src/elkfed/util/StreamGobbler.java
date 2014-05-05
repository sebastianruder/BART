/*
 * StreamGobbler.java
 *
 * Created on August 14, 2007, 10:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author yannick
 */
public class StreamGobbler implements Runnable {
    String name;
    InputStream is;
    Thread thread;
    
    public StreamGobbler(String name, InputStream is) {
        this.name = name;
        this.is = is;
    }
    
    public void start() {
        thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            while (true) {
                String s = br.readLine();
                if (s == null) break;
                System.out.println("[" + name + "] " + s);
            }
            
            is.close();
            
        } catch (Exception ex) {
            System.out.println("Problem reading stream " + name + "... :" + ex);
            ex.printStackTrace();
        }
    }
}
