package com.leuville.red.flume.source;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Created by cyril on 27/07/16.
 */
public class WatchTest {



    public static void main(String[] args) {

        File file = new File("/home/cyril/tmp/rotatelogs");
        Path path = file.toPath();
        FileSystem fs = path.getFileSystem();
        WatchService watchService = null;
        try {
            watchService = fs.newWatchService();
            path.register(watchService, ENTRY_CREATE);
            while (true){
                System.out.println("coucou");
                WatchKey key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>)watchEvent;
                    File newFile = pathEvent.context().toFile();
                    System.out.println(newFile.getAbsolutePath());
                    TailerListener listener = new TailerListenerAdapter(){

                        @Override
                        public void init(Tailer tailer) {
                            super.init(tailer);
                            System.out.println("init");
                        }

                        @Override
                        public void fileNotFound() {
                            System.out.println("file");
                        }

                        @Override
                        public void handle(String line) {
                            System.out.println(line);
                        }
                    };
                    Tailer tailer = Tailer.create(newFile,listener, 500);
                    System.out.println(tailer);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                watchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
