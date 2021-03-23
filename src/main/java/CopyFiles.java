import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CopyFiles implements Runnable{

    private Thread t;
    private String threadName;
    private File originFile;
    private File destFile;

    public CopyFiles(String threadName, File originFile, File destFile) {
        this.threadName = threadName;
        this.originFile = originFile;
        this.destFile = destFile;
    }

    public void run() {
        System.out.println("Running " + threadName);
        try {
            FileUtils.moveFile(originFile, destFile);
        } catch (IOException e) {
            System.out.println("não conseguiu mover o arquivo: " + originFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public void start () {
        System.out.println("Starting " +  threadName );

        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
