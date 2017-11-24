package downloader.fc;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.concurrent.Task;

public class Downloader extends Task {

    public static final int CHUNK_SIZE = 1024;
    public Boolean isSuspended = false;
    URL url;
    int content_length;
    BufferedInputStream in;

    String filename;
    File temp;
    FileOutputStream out;

    ReentrantLock lock;

    int size = 0;
    int count = 0;

    public Downloader(String uri) {
        try {
            url = new URL(uri);

            URLConnection connection = url.openConnection();
            content_length = connection.getContentLength();
            in = new BufferedInputStream(connection.getInputStream());
            String[] path = url.getFile().split("/");
            filename = path[path.length - 1];
            temp = File.createTempFile(filename, ".part");
            out = new FileOutputStream(temp);
            lock = new ReentrantLock(true);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return url.toString();
    }

    protected String download() throws InterruptedException {
        byte buffer[] = new byte[CHUNK_SIZE];

        while (count >= 0) {
            lock.lock();
            try {
                try {
                    out.write(buffer, 0, count);
                } catch (IOException e) {
                    continue;
                }

                size += count;
                updateProgress(1. * size / content_length, 1);
                Thread.sleep(500);

                try {
                    count = in.read(buffer, 0, CHUNK_SIZE);
                } catch (IOException e) {
                    continue;
                }
            } finally {
                lock.unlock();
            }
        }

        if (size < content_length) {
            temp.delete();
            throw new InterruptedException();
        }

        temp.renameTo(new File(filename));
        return filename;
    }

    public void play() {
        lock.unlock();
    }

    public void pause() {
        lock.lock();
    }

    @Override
    protected Object call() throws Exception {
        download();
        return null;
    }
};
