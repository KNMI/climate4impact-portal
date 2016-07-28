package tools;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Tools {

  /**
   * 
   * @param file The filename to read
   * @return Bytes of the read file
   * @throws IOException
   */
  static byte[] readBytes(String file) throws IOException{
    //DebugConsole.println("readBytes "+file);
    File fileObject=new File(file);
    byte[] data=new byte[(int) fileObject.length()];
    FileInputStream pemInputStream=new FileInputStream(fileObject);
    pemInputStream.read(data);
    pemInputStream.close();
    return data;
  }
  
	public static String getExtension(String input){
		String output=input.trim();
	    int dotPos = output.lastIndexOf(".");
	    if(dotPos == -1)return null;
	    String extension = output.substring(dotPos);
		return extension;
	}

	public static String makeCleanPath(String input){
		String output=input.trim();
		output=_removeDuplicateSlashes(output);
		output=_fixDotDotDirs(output);
		return output;
	}
	private static String _removeDuplicateSlashes(String input){
		String output="";
		String[] subPaths=input.split("/");
		for(int j=0;j<subPaths.length;j++){
	
			if(subPaths[j].length()>0){
				if(subPaths[j].charAt(0)!='/'){
					if(output.length()>0){
						if(output.charAt(output.length()-1)!='/'){
							output+="/";
						}
					}else if(input.charAt(0)=='/')output="/";
					//System.out.println(subPaths[j]);
					if(!subPaths[j].equals(".")){
						output+=subPaths[j];
					}
				}
			}
			
		}
		//System.out.println("--"+output);
		return output;
	}
	private static String _fixDotDotDirs(String input){
		String output="";
		input+="/./";
		String[] subPaths=input.split("/");
		for(int j=0;j<subPaths.length-1;j++){
			if(!subPaths[j+1].equals("..")){
				if(subPaths[j].length()>0){
					if(subPaths[j].charAt(0)!='/'){
						if(output.length()>0){
							if(output.charAt(output.length()-1)!='/'){
								output+="/";
							}
						}else if(input.charAt(0)=='/')output="/";
						//System.out.println(subPaths[j]);
						if(!subPaths[j].equals(".")){
							output+=subPaths[j];
						}
					}
				}
			}else j++;
		}
		if(output.indexOf("..")>0){
			System.out.println("**"+output);
			output=_fixDotDotDirs(output);
		}
		
		//output+="/";
		return output;
	}
	
	public static void cp(String source, String destination) throws IOException{
		File inputFile = new File(source);
		
		if(inputFile.isDirectory()==true){
			mkdir(destination);
			File sourceDir = new File(source);
			for(String file : sourceDir.list()){
				cp(source+"/"+file,destination+"/"+file);
			}
			return;
		}
	 	File sourceFile=new File(source); // source
	 	BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile), 4096);
        File targetFile = new File(destination); // destination
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile), 4096);
        int theChar;
        while ((theChar = bis.read()) != -1) {
           bos.write(theChar);
        }
        bos.close();
        bis.close();
	}
	public static void mv(String source, String destination) throws IOException{
		/*File inputFile = new File(source);
		
		if(inputFile.isDirectory()==true){
			mkdir(destination);
			File sourceDir = new File(source);
			for(String file : sourceDir.list()){
				cp(source+"/"+file,destination+"/"+file);
			}
			return;
		}*/
	 	File sourceFile=new File(source); // source
	 	File targetFile = new File(destination); // destination
	 	boolean succes = sourceFile.renameTo(targetFile);
	 	if(!succes){
	 		throw new IOException("Unable to rename '"+source+"' to '"+destination+"'");
	 	}
	}
	public static void mvcp(String source, String destination) throws IOException{
		cp(source,destination);
		File d = new File(destination);
		while(!d.exists());
		rmfile(source);
	}
	
	public static void mkdir(String dir) throws IOException{
		if(dir==null)return;
		if(dir.equals("null"))return;
		File f = new File(dir);if(f.isDirectory()&&f.exists())return;
		if(dir.length()==0){
			throw new IOException("Invalid directory name specified (0 chars)");
		}
		File directory=new File(dir);
		if(directory.exists()){
			//throw new IOException("Directory already exists: '"+dir+"'");
			return;
		}
		boolean success=directory.mkdir();		
		if(!success){
			File test = new File(directory.getAbsolutePath());
			if(test.exists()==false){
				throw new IOException("mkdir: Directory could not be created and does not exist: "+directory.getAbsolutePath());
			}else{
				//throw new IOException("mkdir: Directory could not created, it exists already...");
			}
		}
	}
	public static void mksubdirs(String dir) throws IOException{
		File f = new File(dir);if(f.isDirectory()&&f.exists())return;
		String[] dirs = dir.split("/");
		String path="";
		for(int j=0;j<dirs.length;j++){
			path+=dirs[j]+"/";
			File directory = new File(path);
			if(directory.exists()==false){
				try{
					mkdir(directory.getAbsolutePath());
				}catch(Exception e){
					File test = new File(directory.getAbsolutePath());
					if(test.exists()==false){
						throw new IOException("mksubdirs: Directory could not be created and does not exist: '"+directory.getAbsolutePath()+"'\nMessage: "+e.getMessage());
					}else{
						//throw new IOException("mksubdirs: Directory could not created, it exists already...");
					}
				}
			}			
		}
	}
	
	public static boolean rm(String _file) {
		File file = new File(_file);
	    if( file.exists() ) {
	    	if(file.isDirectory()){
	    		return rmdir(_file);
	    	}
	    	else return rmfile(_file);
	    }
	    return true;
	}
	 public static boolean rmdir(String _file) {
		//System.err.println("removing dir "+_file);
		File path = new File(_file);
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	        	 rmdir(files[i].toString());
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }
	 public static boolean rmfile(String _file) {
		 //System.err.println("removing file "+_file);
			File file = new File(_file);
		    if( file.exists() ) {
		    	return file.delete();
		    }
		    return true;
		  }
	 public static String[] appendString(String[] string,String toAppend){
		int length=0;
		if(string!=null)length=string.length;	
		String grotere[] = new String[length+1];
		if(string!=null)System.arraycopy(string, 0, grotere, 0, length);
		grotere[length]=toAppend;
		string=null;
		return grotere;
	}
	 
	 static String[] ls(String origPath,String dir,String[] toAppendTo){
			File path = new File(dir);
		    if( path.exists() ) {
		      File[] files = path.listFiles();
		      for(int i=0; i<files.length; i++) {
		         if(files[i].isDirectory()) {

		        	 toAppendTo = ls(origPath,files[i].toString(),toAppendTo);
		         }
		         else {
		        	 File origFile = new File(origPath);
		        	 toAppendTo=appendString(toAppendTo,files[i].toString().substring(origFile.toString().length()+1));
		         }
		      }
		    }
		 return toAppendTo;
	 }
	 public static String[] ls(String dir){
		String[] FileList=null;
		FileList = ls(dir,dir,FileList);
	    return FileList;
	 }
	 
	public static int zip(String dir, String outFilename) throws IOException{
		String[] filesToZip=null;
		
		File tmp=new File(dir);
		if(tmp.isDirectory()==true){
			if(dir.charAt(dir.length()-1)!='/')dir+="/";
			filesToZip = ls(dir);
		}else{
			filesToZip=Tools.appendString(filesToZip, "");
		}
		
		
		if(filesToZip==null){
			throw new IOException("No files to zip in directory "+dir);
		}
		// Create a buffer for reading the files
	    byte[] buf = new byte[1024];
	    
    
        // Create the ZIP file
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
    
        // Compress the files
        for (int i=0; i<filesToZip.length; i++) {
            FileInputStream in = new FileInputStream(dir+filesToZip[i]);
    
            // Add ZIP entry to output stream.

            out.putNextEntry(new ZipEntry(filesToZip[i]));
    
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
    
            // Complete the entry
            out.closeEntry();
            in.close();
        }
    
        // Complete the ZIP file
        out.close();
    
		return 0;
	}
	static class DirAlphaComparator implements Comparator<File> {

	    // Comparator interface requires defining compare method.
	    public int compare(File filea, File fileb) {
	        //... Sort directories before files,
	        //    otherwise alphabetical ignoring case.
	        if (filea.isDirectory() && !fileb.isDirectory()) {
	            return -1;

	        } else if (!filea.isDirectory() && fileb.isDirectory()) {
	            return 1;

	        } else {
	            return filea.getName().compareToIgnoreCase(fileb.getName());
	        }
	    }
	}
	
	static public void writeFile(String fileName,String data) throws IOException{
    FileWriter fstream = new FileWriter(fileName);
    BufferedWriter out = new BufferedWriter(fstream);
    out.write(data);
    out.close();
    fstream.close();
	}
	static public String readFile(String fileName) throws IOException{
	   StringBuffer fileData = new StringBuffer(1000);
     BufferedReader reader = new BufferedReader(
             new FileReader(fileName));
     char[] buf = new char[1024];
     int numRead=0;
     while((numRead=reader.read(buf)) != -1){
         String readData = String.valueOf(buf, 0, numRead);
         fileData.append(readData);
         buf = new char[1024];
     }
     reader.close();
     return fileData.toString();

  }

	static public byte[] readFileRaw(String fileName) throws IOException{
	  Path path = Paths.get(fileName);
	  byte[] data = Files.readAllBytes(path);
	  return data;
 }
  


  public static String checkValidCharsForFile(String input) throws Exception{
    if(input.indexOf("..")!=-1){
      throw new Exception("Invalid sequence given: ..");
    }

    byte[] validTokens = {
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
        'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
        '0','1','2','3','4','5','6','7','8','9',
        '+','-','|','&','.',',','~',' ','/','_'
        };
    
    byte[] str = input.getBytes();
    for(int c=0;c<str.length;c++){
      boolean found = false;
      for(int v=0;v<validTokens.length;v++){
        if(validTokens[v] == str[c]){
          found = true;
          break;
        }
      }
      if(found == false){
        Debug.errprintln("Invalid string given: "+input);
        throw new Exception("Invalid token given: '"+Character.toString((char)str[c])+"', code ("+str[c]+").");
      }
    }
    return input;
  }

  public static void rmdir(File file) {
   rmdir(file.getAbsolutePath());
  }
}
