package com.leuville.red.flume.source;

import com.nflabs.grok.Grok;
import com.nflabs.grok.Match;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.flume.*;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;

/**
 * Created by cyril on 27/07/16.
 */
public class RotateLogsSource extends AbstractSource implements Configurable, EventDrivenSource {

    private static final Logger logger  = LoggerFactory.getLogger(RotateLogsSource.class);
    private String path;
    private String pattern;

    @Override
    public synchronized void start() {
        logger.info("Rotate Logs source starting with path:{}", path);
        /* Grok grok = Grok.EMPTY;
        InputStream inputStream = getClass().getResourceAsStream("/pattern.txt");
        grok.addPatternFromReader(new InputStreamReader(inputStream));
        grok.compile(pattern);
        Match match = grok.match("error.log");
*/
        final ChannelProcessor channelProcessor = getChannelProcessor();
        DirectoryStream<Path> directoryStream = null;
        try {
            directoryStream = Files.newDirectoryStream(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Observable<String> from = Observable.from(directoryStream).filter(new Func1<Path, Boolean>() {
            @Override
            public Boolean call(Path path) {
                Path fileName = path.getFileName();
                return pattern.equals(fileName.toString());
            }
        }).flatMap(new Func1<Path, Observable<String>>() {
            @Override
            public Observable<String> call(final Path path) {
                return Observable.create(new Observable.OnSubscribe<String>() {


                    @Override
                    public void call(final Subscriber<? super String> subscriber) {
                        Tailer tailer = Tailer.create(path.toFile(), new TailerListenerAdapter() {

                            @Override
                            public void handle(String line) {
                                subscriber.onNext(line);
                            }
                        });
                    }
                });
            }
        });

        from.subscribeOn(Schedulers.newThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String line) {
                logger.debug("new line : {" + line + "}");
                Event event = EventBuilder.withBody(line.getBytes());
                channelProcessor.processEvent(event);
            }
        });
        super.start();
        logger.info("Rotate Logs source started");

    }

    @Override
    public void configure(Context context) {
        path = context.getString("path");
        pattern = context.getString("pattern");


    }

}
