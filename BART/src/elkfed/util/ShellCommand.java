/*
 * ShellCommand.java
 *
 * Created on August 14, 2007, 10:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.util;

import java.io.IOException;

/**
 *
 * @author yannick
 */
public class ShellCommand {
    public static void runShellCommand(String cmd)
        throws IOException, InterruptedException {
        String[] args={"sh","-c",cmd};
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(args);
        StreamGobbler s1 = new StreamGobbler("OUT", p.getInputStream());
        StreamGobbler s2 = new StreamGobbler("ERR", p.getErrorStream());
        s1.start();
        s2.start();
        p.waitFor();
        System.out.println("Process finished.");
    }

    public static int runShellCommand(String... args)
        throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(args);
        StreamGobbler s1 = new StreamGobbler("OUT", p.getInputStream());
        StreamGobbler s2 = new StreamGobbler("ERR", p.getErrorStream());
        s1.start();
        s2.start();
        int retval=p.waitFor();
        return retval;
    }
    
    public static void main(String[] args)
    {
        try {
            runShellCommand("ls -l");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
