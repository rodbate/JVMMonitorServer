package com.dataeye;


import com.dataeye.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileListener.class);

    private static ArrayList<FileUpdate> list = new ArrayList<>();


    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {

            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                LOGGER.info("config path {} " + Constant.CONF_DIR);
                Path path = Paths.get(Constant.CONF_DIR);
                path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

                //
                while (true) {
                    WatchKey key = watcher.take();
                    List<WatchEvent<?>> watchEvents = key.pollEvents();
                    for (WatchEvent event : watchEvents) {
                        Path file = (Path)event.context();
                        LOGGER.info("file {} changed......", file.getFileName());
                        System.out.println(file.toString());
                        notice(Constant.CONF_DIR + File.separator + file.getFileName());
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    };


    private static void notice(String name) {
        for (FileUpdate update : list) {
            update.update(name);
        }
    }


    public synchronized static void register(FileUpdate update){
        list.add(update);
    }

    static {
        Thread thread = new Thread(runnable);
        thread.setName("File Listener Thread");
        thread.start();
    }
}
