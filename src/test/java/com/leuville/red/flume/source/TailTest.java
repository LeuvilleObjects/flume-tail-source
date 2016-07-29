package com.leuville.red.flume.source;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.util.concurrent.Executor;

/**
 * Created by cyril on 27/07/16.
 */
public class TailTest {

    static TailerListener listener = new TailerListenerAdapter(){

        @Override
        public void handle(String line) {
            System.out.println(line);
        }
    };
    public static void main(String[] args) {
        File newFile = new File("/home/cyril/tmp/rotatelogs/log.txt");
        Tailer tailer = Tailer.create(newFile,listener, 500);

        while(true){

        }
    }
}
