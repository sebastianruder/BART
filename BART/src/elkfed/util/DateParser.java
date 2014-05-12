/*
 * DateParser.java
 *
 * Created on July 27, 2007, 5:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.util;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author ponzo
 */
public class DateParser {
    
    private static final ParsePosition POS = new ParsePosition(0);
    
    /** IMPLEMENTATION DETAIL: the singleton instance */
    private static DateParser instance;
    
    /** Getter for instance */
    public static synchronized DateParser getInstance()
    {
        if (instance == null)
        { instance = new DateParser(); }
        return instance;
    }
    
    private final List<SimpleDateFormat> dateFormats;
    
    private DateParser()
    {
        this.dateFormats = new ArrayList<SimpleDateFormat>();
		
        dateFormats.add(new SimpleDateFormat("MM-dd"));
        dateFormats.add(new SimpleDateFormat("MMM dd"));
        dateFormats.add(new SimpleDateFormat("MMM. dd"));
        dateFormats.add(new SimpleDateFormat("MMM dd yyyy"));
		
        dateFormats.add(new SimpleDateFormat("MM-dd-yy"));
        dateFormats.add(new SimpleDateFormat("MM-dd-yyyy"));
        
        for (DateFormat format : dateFormats)
        { format.setLenient(false); }
        
    }
    
    /** Uses a date parser to normalize, extract and compare two date String */
    private boolean compareDate(String date1, String date2)
    { return compareDate(parse(date1),parse(date2)); }
    
    /** Uses a date parser to normalize, extract and compare two date String */
    private boolean compareDate(Date date1, Date date2)
    {
        final GregorianCalendar cal1 = new GregorianCalendar();
        if (date1 != null)
        { cal1.setTime(date1); }
        else
        { return false; }
        final GregorianCalendar cal2 = new GregorianCalendar();
        if (date2 != null)
        { cal2.setTime(date2); }
        else
        { return false; }
        
        return
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
         &&
           (
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
            ||
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
           );
    }
    
    public Date parse(String string)
    {	//lol1
        Date date = null;
        for (SimpleDateFormat format : dateFormats)
        {
            try {
                    POS.setIndex(0);
                    date = format.parse(string, POS);
                    if (POS.getIndex() != string.length())
                    { date = null; }
                    else
                    { break; }
            }
            catch (Exception e) {
            	continue;
            }
        }
        return date;
    }
    
    public static void main(String[] args) {
             System.out.println(new DateParser().compareDate("01-16-96 1213EST","01-16-96 1213EST"));
    }
}
