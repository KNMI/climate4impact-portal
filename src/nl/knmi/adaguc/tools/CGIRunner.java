package nl.knmi.adaguc.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

public class CGIRunner {

	/**
	 * Runs a CGI program as executable on the system. Emulates the behavior of scripts in a traditional cgi-bin directory of apache http server.
	 * @param commands A string array with the executable and the arguments to this executable
	 * @param environmentVariables The environment variables used for this run
	 * @param directory The location to start the program, can be null or "" if not needed
	 * @param response Can be null, when given the content-type for the response will be set.
	 * @param outputStream A standard byte output stream in which the data of stdout is captured. Can for example be response.getOutputStream().
	 * @param postData The data to post.
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	public static void runCGIProgram(String[] commands,String[] environmentVariables,String directory,final HttpServletResponse response,OutputStream outputStream,String postData) throws InterruptedException, IOException {
		Debug.println("Working Directory: "+directory);

		class StderrPrinter implements ProcessRunner.StatusPrinterInterface{
			StringBuffer errorMessages = new StringBuffer();
			public void print(byte[] message,int bytesRead) {
				errorMessages.append(new String(message,0,bytesRead));
			}
			public void setError(String message) {
			}
			public String getError() { 
				return errorMessages.toString();   
			}
			@Override
			public boolean hasData() {
				// TODO Auto-generated method stub
				return false;
			}
		}
		class StdoutPrinter implements ProcessRunner.StatusPrinterInterface{
			boolean headersSent = false;
			boolean foundLF = false;
			boolean hasError = false;
			OutputStream output = null;
			StringBuffer header = new StringBuffer();
			public StdoutPrinter(OutputStream outputStream) {
				output=outputStream;
			}

			// boolean a = false;
			public void print(byte[] message,int bytesRead) {
				try {
					_print(message,bytesRead);
				} catch (IOException e) {
					this.hasError = true;
					//Debug.errprintln("Unable to write data: "+e.getMessage());
				}
			}

			public void _print(byte[] message,int bytesRead) throws IOException {
				//Try to extract HTML headers and Content-Type 
				if(hasError)return;
				if(headersSent==false){
					int endHeaderIndex=0;
					for(int j=0;j<bytesRead;j++){

						if(message[j] == 10){
							if(foundLF == false){
								foundLF = true;
								continue;
							}
						}else if (foundLF == true && message[j] != 13){
							foundLF=false;
							continue;
						}

						if(foundLF == true){
							if(message[j] == 10){
								headersSent = true;
								endHeaderIndex = j;
								while((message[endHeaderIndex] == 10 || message[endHeaderIndex] == 13 ) && endHeaderIndex<bytesRead){
									endHeaderIndex++;
								}
								output.write(message, endHeaderIndex,bytesRead-(endHeaderIndex));
								header.append(new String(message),0,endHeaderIndex);
								break;
							}
						}
					}
					if( headersSent == true){
						String[] headerItem=header.toString().split("\n");
						for(int h=0;h<headerItem.length;h++){
							String[] headerKVP=headerItem[h].split(":");
							if(headerKVP.length==2){
								//Debug.println(headerKVP[0].trim()+"="+headerKVP[1].trim());
								if(headerKVP[0].trim().equalsIgnoreCase("Content-Type")){
									if(response!=null){
										headerKVP[1]=headerKVP[1].replaceAll("\\n", "");
										headerKVP[1]=headerKVP[1].replaceAll("\\r", "");
										headerKVP[1]=headerKVP[1].replaceAll(" ", "");
										//Debug.println("Setting Content-Type to ["+headerKVP[1].trim()+"]");
										response.setContentType(headerKVP[1].trim());
									}
								}else{
									//Debug.println("Setting header ["+headerKVP[0].trim()+"]=["+headerKVP[1].trim()+"]");
									response.setHeader(headerKVP[0].trim(),headerKVP[1].trim());
								}
							}
						}
					}else{
						header.append(message);
					}
				}else{
					output.write(message,0,bytesRead);
				}
			}


			public void setError(String message) {
				hasError = true;
			}

			public String getError() {
				if(hasError)return "yes";
				return null;
			}

			@Override
			public boolean hasData() {
				return headersSent;
			}
		}

		ProcessRunner.StatusPrinterInterface stdoutPrinter = new StdoutPrinter(outputStream);
		ProcessRunner.StatusPrinterInterface stderrPrinter = new StderrPrinter();
		//
		//    Debug.println("Environment:");
		//    for(int j=0;j<environmentVariables.length;j++){
		//      Debug.println(environmentVariables[j]);
		//    }
		//    Debug.println("Commands:");
		//    for(int j=0;j<commands.length;j++){
		//      Debug.println(commands[j]);
		//    }

		ProcessRunner processRunner = new ProcessRunner (stdoutPrinter,stderrPrinter,environmentVariables,directory);



		long startTimeInMillis = Calendar.getInstance().getTimeInMillis();  
		Debug.println("Starting CGI.");

		processRunner.runProcess(commands,postData);


		if(processRunner.exitValue()!=0){
			Debug.errprintln("Warning: exit code: "+processRunner.exitValue());
		}
		if(stdoutPrinter.getError()!=null){
			Debug.errprintln("Warning: errors occured while writing to pipe");
		}
		if(response!=null){
			if(processRunner.exitValue()!=0&&processRunner.exitValue()!=1){
				response.setStatus(500);
				String msg="Internal server error: CGI returned code "+processRunner.exitValue();
				Debug.errprintln(msg);
				outputStream.write(msg.getBytes());
			}
			if(processRunner.exitValue()!=0){
				String errors = stderrPrinter.getError();
				Debug.errprintln("Errors during CGI execution: "+errors);
				if(errors!=null && stdoutPrinter.hasData()==false){
					if(errors.indexOf("401")!=-1){
						response.setStatus(401);
						outputStream.write(errors.getBytes());
					}else  if(errors.indexOf("403")!=-1){
						response.setStatus(403);
						outputStream.write(errors.getBytes());
					}else if(errors.indexOf("404")!=-1){
						response.setStatus(404);
						outputStream.write(errors.getBytes());
					}else if(errors.indexOf("Error opening")!=-1){
						response.setStatus(415);
						outputStream.write(errors.getBytes());
					}
				}
			}
		}

		long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
		Debug.println("Finished CGI with code "+processRunner.exitValue()+": "+" ("+(stopTimeInMillis-startTimeInMillis)+" ms)");
		try{
			outputStream.flush();
		}catch(org.apache.catalina.connector.ClientAbortException e){
			Debug.println("Stream already closed");

		}
	}
}
