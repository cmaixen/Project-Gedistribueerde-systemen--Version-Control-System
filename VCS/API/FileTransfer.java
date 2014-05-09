package VCS.API;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public interface FileTransfer {
	
	//wordt gebruikt om de files voortebereiden voor dat ze verstuurd worden.
	boolean locateFiles(String name, ArrayList<String> commitlist,String sourceDestination, ObjectOutputStream outputStream) throws IOException;

	//Versturen van Files => hier kan encryptie op gebeuren
	void sendFile(String fileName, int index, String sourceDirectory, String sourceDestination);
	
	//Downloaden van de Files;
	void downloadFiles();
	
}
