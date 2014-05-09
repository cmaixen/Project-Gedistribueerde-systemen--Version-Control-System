package VCS.Events;

import java.io.Serializable;

public class Acknowledgement extends Command{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Acknowledgement(String command){
		Command = command;
	}

}

