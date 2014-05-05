/*
 * Copyright 2007 EML Research
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elkfed.util;

import java.util.logging.Logger;

/** A class for measuring timing performance. Originally taken from
 *  edu.stanford.util package (author: Chris Manning) and modified to
 *  be implemented as a singleton using a Logger (more OO friendly).
 */
public class Timer
{
    /** The logger to be able to talk to the world */
    private static final Logger TIME_LOGGER = Logger.getAnonymousLogger();
    
    /** IMPLEMENTATION DETAIL: the singleton instance */
    private static Timer instance;
    
    /** Getter for instance */
    public static synchronized Timer getInstance()
    {
        if (instance == null)
        { instance = new Timer(); instance.setStartTime(); }
        return instance;
    }
    
    /** Stores the time of the initial operation start. */
    private long initialStartTime = System.currentTimeMillis();
    
    /** Stores the time of the current operation start. */
    private long lastStartTime = System.currentTimeMillis();

    /** This class cannot be instantiated from outside (being a singleton) */
    private Timer() { super(); }

    /** Start the timing operation. */
    public void setStartTime()
    { this.initialStartTime = System.currentTimeMillis(); }

    /** Print how long the timed operation took.
     *
     *  @param str Additional string to be printed out at end of timing
     *  @return Number of elapsed milliseconds
     */
    public long getTime(final String str)
    {
        final long elapsed = System.currentTimeMillis() - initialStartTime;
        TIME_LOGGER.info(new StringBuffer().append("Time elapsed to ").
                append(str).append(": ").append(elapsed).append(" ms").toString());
        return elapsed;
    }

    /** Print how long the timed operation took.
     *
     *  @return Number of elapsed milliseconds
     */
    public long getTime()
    { return System.currentTimeMillis() - initialStartTime; }

    /** Print how much time has passed.  Time is measured from the last
     *  <code>tick</code> call, or the last call to <code>setStartTime</code> or
     *  when the class was loaded if there has been no previous call.
     *  
     *  @param str Prefix of string printed with time
     *  @return Number of elapsed milliseconds from tick (or start)
     */
    public long tick(final String str)
    {
        final long time2 = System.currentTimeMillis();
        final long elapsed = time2 - lastStartTime;
        lastStartTime = time2;
        TIME_LOGGER.info(new StringBuffer().append("Time elapsed to ").
                append(str).append(": ").append(elapsed).append(" ms").toString());
        return elapsed;
    }
    
    /** Computes how much time has passed.  Time is measured from the last
     *  <code>tick</code> call, or the last call to <code>setStartTime</code> or
     *  when the class was loaded if there has been no previous call.
     */
    public long tick()
    {
        final long time2 = System.currentTimeMillis();
        final long elapsed = time2 - lastStartTime;
        lastStartTime = time2;
        return elapsed;
    }
}
