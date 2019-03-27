package nl.knmi.adaguc.tools;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateFunctions {

  private static SimpleDateFormat getUTCSimpleDate(){
    String DATE_FORMAT_NOW = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC")); 
    return sdf;
  }
  private static SimpleDateFormat getUTCSimpleDate(String format){
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC")); 
    return sdf;
  }
	//Todo: convert the dateResolutionString to ISO format
	public static String dateAddStepInStringFormat(String stringDate,String dateResolutionString) throws Exception{
	  return dateAddStepInStringFormat(stringDate,dateResolutionString,1);
	}
	public static String dateAddStepInStringFormat(String stringDate,String dateResolutionString, int amount) throws ParseException {
		Date dateStartTime = null;
		dateStartTime = getUTCSimpleDate().parse(stringDate);
		long time=dateStartTime.getTime();
		Calendar cal = Calendar.getInstance();
		Date date = new Date();
		date.setTime(time);
		cal.setTime(date);
		//Add second
		if(dateResolutionString.equalsIgnoreCase("second"))cal.add(Calendar.SECOND, amount);
		//Add minute
		if(dateResolutionString.equalsIgnoreCase("minute"))cal.add(Calendar.MINUTE, amount);
		//Add hour
		if(dateResolutionString.equalsIgnoreCase("hour"))cal.add(Calendar.HOUR, amount);
		//Add days
		if(dateResolutionString.equalsIgnoreCase("day"))cal.add(Calendar.DATE, amount);
		//Add week
		if(dateResolutionString.equalsIgnoreCase("week"))cal.add(Calendar.DATE, amount*7);
    //Add decade
		if(dateResolutionString.equalsIgnoreCase("decade"))cal.add(Calendar.DATE, 10*amount);
		//Add months
		if(dateResolutionString.equalsIgnoreCase("month"))cal.add(Calendar.MONTH, amount);
		//Add season
		if(dateResolutionString.equalsIgnoreCase("season"))cal.add(Calendar.MONTH, amount*3);
		//Add years
		if(dateResolutionString.equalsIgnoreCase("year"))cal.add(Calendar.YEAR, amount);
		// Check whether we really added something
		if(cal.getTimeInMillis()==time){
			throw new ParseException("dateResolutionString is invalid in dateAddStepInStringFormat: "+dateResolutionString, 0);
		}		//return  date
		return getUTCSimpleDate().format(cal.getTime());
	}
	
    public static String getCurrentDateInISO8601(){
	    Calendar cal = Calendar.getInstance();
	    String currentISOTimeString = getUTCSimpleDate().format(cal.getTime());
	    char currentISOTimeCharArray[]=currentISOTimeString.toCharArray();
	    //Add the T and Z characters
	    currentISOTimeCharArray[10]='T';
	    currentISOTimeString=new String(currentISOTimeCharArray);
	    return currentISOTimeString;
    }
    public static String getTimeStampInMillisToISO8601(long milli){
      String currentISOTimeString = getUTCSimpleDate().format(milli);
      char currentISOTimeCharArray[]=currentISOTimeString.toCharArray();
      //Add the T and Z characters
      currentISOTimeCharArray[10]='T';
      currentISOTimeString=new String(currentISOTimeCharArray);
      return currentISOTimeString;
    }
    
    
    public static String getCurrentDateinADAGUC(){
	    String currentISOTimeString = getUTCSimpleDate("yyyyMMddHHmmss").format(getCurrentDateInMillis());
	    char currentISOTimeCharArray[]=currentISOTimeString.toCharArray();
	    currentISOTimeString=new String(currentISOTimeCharArray);
	    return currentISOTimeString;
    }
    public static String getISO8601TimeFromDate(Date date){
	    String currentISOTimeString = getUTCSimpleDate().format(date);
	    char currentISOTimeCharArray[]=currentISOTimeString.toCharArray();
	    //Add the T and Z characters
	    currentISOTimeCharArray[10]='T';
	    currentISOTimeString=new String(currentISOTimeCharArray);
	    return currentISOTimeString;
    }
    public static String timeResolutionToISO8601(String timeResolution){
        String ISO8601TimeInterval = null;
        if(timeResolution.equalsIgnoreCase("second"))ISO8601TimeInterval="PT1S";
        if(timeResolution.equalsIgnoreCase("minute"))ISO8601TimeInterval="PT1M";
        if(timeResolution.equalsIgnoreCase("hour"))ISO8601TimeInterval="PT1H";
        if(timeResolution.equalsIgnoreCase("day"))ISO8601TimeInterval="P1D";
        //Decades do not work well yet..., just like season...
        //They do not have a fixed resolution, and we do currently work with a fixed resolution.
        if(timeResolution.equalsIgnoreCase("decade"))ISO8601TimeInterval="PT10D";
        if(timeResolution.equalsIgnoreCase("month"))ISO8601TimeInterval="P1M";
        if(timeResolution.equalsIgnoreCase("season"))ISO8601TimeInterval="P3M";
        if(timeResolution.equalsIgnoreCase("year"))ISO8601TimeInterval="P1Y";
	        return ISO8601TimeInterval;
    }

    public static long getMillisFromISO8601Date(String stringDate) throws Exception {
      Date dateStartTime = null;
      try {
        dateStartTime = getUTCSimpleDate().parse(stringDate);
      } catch (ParseException e1) {
        throw new Exception("Unable to parse start date: "+stringDate+" Exception message: "+e1.getMessage());
      }
      return dateStartTime.getTime();
    }

    public static long getCurrentDateInMillis() {
      long m = System.currentTimeMillis();
      //Debug.println(""+m);
      return m ;//cal.getTimeInMillis();
      
    }
}