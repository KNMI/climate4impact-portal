package jpa;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import tools.DebugConsole;



/**
 * This is an Application Scoped bean that holds the JPA
 * EntityManagerFactory.  By making this bean Applciation scoped the
 * EntityManagerFactory resource will be created only once for the application
 * and cached here.
 *
 *@author Gordon Yorke
 */
 public class JPAStaticResourceBean {
    protected static EntityManagerFactory emf;
    protected static Date creationDate;
    /*
     * Lazily acquire the EntityManagerFactory and cache it.
     */
     @SuppressWarnings("rawtypes")
	public static EntityManagerFactory getEMF (){
       Logger log = Logger.getLogger("org.hibernate");
       log.setLevel(Level.WARNING);
        if (emf == null){
           
            emf = Persistence.createEntityManagerFactory("impactportal", new java.util.HashMap());
            
            creationDate = Calendar.getInstance().getTime();
            //DebugConsole.println("Creating new emf");
            
        }else{
        	//DebugConsole.println("Using existing emf");
        }
        /*
         * Check if we need to refresh the emf (prefent timeouts now and then)
         */
        Date currentDate =  Calendar.getInstance().getTime();;
        //1000*60*5 == 5 minutes.
        if(currentDate.getTime()-1000*60*60 > creationDate.getTime()){
        	DebugConsole.println("Destroying and recreating emf");
        	emf.close();
        	emf  = null;
        	return getEMF();
        }
        //Otherwise return existing emf
        return emf;
    }
}