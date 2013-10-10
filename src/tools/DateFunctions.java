package tools;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFunctions {
	public static SimpleDateFormat ISO8601DateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	//Todo: convert the dateResolutionString to ISO format
	public static String dateAddStepInStringFormat(String stringDate,String dateResolutionString) throws Exception{
		Date dateStartTime = null;
		try {
			dateStartTime = ISO8601DateTimeFormat.parse(stringDate);
		} catch (ParseException e1) {
			throw new Exception("Unable to parse start date: "+stringDate+" Exception message: "+e1.getMessage());
		}
		if(dateResolutionString==null){
			throw new Exception("dateResolutionString in dateAddStepInStringFormat == null");
		}
		long time=dateStartTime.getTime();
		Calendar cal = Calendar.getInstance();
		Date date = new Date();
		date.setTime(time);
		cal.setTime(date);
		//Add second
		if(dateResolutionString.equalsIgnoreCase("second"))cal.add(Calendar.SECOND, 1);
		//Add minute
		if(dateResolutionString.equalsIgnoreCase("minute"))cal.add(Calendar.MINUTE, 1);
		//Add hour
		if(dateResolutionString.equalsIgnoreCase("hour"))cal.add(Calendar.HOUR, 1);
		//Add days
		if(dateResolutionString.equalsIgnoreCase("day"))cal.add(Calendar.DATE, 1);
		//Add decade
		if(dateResolutionString.equalsIgnoreCase("decade"))cal.add(Calendar.DATE, 10);
		//Add months
		if(dateResolutionString.equalsIgnoreCase("month"))cal.add(Calendar.MONTH, 1);
		//Add season
		if(dateResolutionString.equalsIgnoreCase("season"))cal.add(Calendar.MONTH, 3);
		//Add years
		if(dateResolutionString.equalsIgnoreCase("year"))cal.add(Calendar.YEAR, 1);
		// Check whether we really added something
		if(cal.getTimeInMillis()==time){
			throw new Exception("dateResolutionString is invalid in dateAddStepInStringFormat: "+dateResolutionString);
		}		//return  date
		return ISO8601DateTimeFormat.format(cal.getTime());
	}
	
	  public static String getCurrentDateInISO8601(){
	    	String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		    String currentISOTimeString = sdf.format(cal.getTime())+"Z";
		    char currentISOTimeCharArray[]=currentISOTimeString.toCharArray();
		    //Add the T and Z characters
		    currentISOTimeCharArray[10]='T';
		    currentISOTimeString=new String(currentISOTimeCharArray);
		    return currentISOTimeString;
	    }
	    public static String getCurrentDateinADAGUC(){
	    	String DATE_FORMAT_NOW = "yyyyMMddHHmmss";
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		    String currentISOTimeString = sdf.format(cal.getTime());
		    char currentISOTimeCharArray[]=currentISOTimeString.toCharArray();
		    currentISOTimeString=new String(currentISOTimeCharArray);
		    return currentISOTimeString;
	    }
	    public static String getISO8601TimeFromDate(Date date){
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    String currentISOTimeString = sdf.format(date)+"Z";
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
}