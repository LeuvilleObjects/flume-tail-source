package com.leuville.red.flume.source.rx;

import rx.Observable;
import rx.Subscriber;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

/**
 * Created by cyril on 28/07/16.
 */
public class OnSubscribeWatchServiceEvents implements Observable.OnSubscribe<WatchEvent<?>> {

    private final WatchService watchService;

    public OnSubscribeWatchServiceEvents(WatchService watchService) {
        this.watchService = watchService;
    }

    @Override
    public void call(Subscriber<? super WatchEvent<?>> subscriber) {
        try {
            WatchKey key = watchService.take();
            List<WatchEvent<?>> events = key.pollEvents();
            for (WatchEvent<?> event : events) {
                subscriber.onNext(event);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
