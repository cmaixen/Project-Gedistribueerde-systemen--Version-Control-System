package VCS.Events;

import java.io.Serializable;
import java.util.UUID;

 
public class FileEvent  extends Command  implements Serializable{
 
    public FileEvent() {
    	Command = "FileEvent";
    	versionnumber = UUID.randomUUID();
    }
 
    private static final long serialVersionUID = 1L;
 
    private String destinationDirectory;
    private String sourceDirectory;
    private String filename;
    private UUID versionnumber;
    private long fileSize;
    private byte[] fileData;
    private String status;
    private int remainder;
 
    public String getDestinationDirectory() {
        return destinationDirectory;
    }
    
    public UUID getVersionNumber() {
        return versionnumber;
    }
    
 
 
    public void setDestinationDirectory(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }
 
    public String getSourceDirectory() {
        return sourceDirectory;
    }
 
    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }
 
    public String getFilename() {
        return filename;
    }
 
    public void setFilename(String filename) {
        this.filename = filename;
    }
 
    public long getFileSize() {
        return fileSize;
    }
 
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
 
    public String getStatus() {
        return status;
    }
 
    public void setStatus(String status) {
        this.status = status;
    }
 
    public byte[] getFileData() {
        return fileData;
    }
 
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
    
    public void setVersionnumber(UUID givenversionnumber){
    	versionnumber = givenversionnumber;
    }
 
    public int getRemainder() {
        return remainder;
    }
 
    public void setRemainder(int remainder) {
        this.remainder = remainder;
    }
}