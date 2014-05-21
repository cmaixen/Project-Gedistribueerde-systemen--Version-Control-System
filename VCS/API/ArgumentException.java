package VCS.API;

public class ArgumentException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String ErrorMessage;
	public ArgumentException(){
		ErrorMessage = "Invalid amount of arguments given!";
	}
	
	public String getErrorMessage(){
		
		return ErrorMessage;
	}
}
