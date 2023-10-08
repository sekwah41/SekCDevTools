package com.sekwah.sekcdevtools;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class ChangeWatcher {


    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    public static final Logger LOGGER = LogManager.getLogger("SekC Dev Tools: Change Watcher");
    private final Runnable runnable;

    public ChangeWatcher(File resourceFolder, Runnable runnable) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.runnable = runnable;
        var watchPath = resourceFolder.toPath();
        this.keys = new HashMap<>();
        watchFolder(watchPath);
        LOGGER.info("Watching all files in {}", watchPath);
    }

    /**
     * Recursively watch for file changes, Re-run if a file is created or destroyed.
     * @param watchFolder
     */
    private void watchFolder(final Path watchFolder) throws IOException {
        Files.walkFileTree(watchFolder, EnumSet.of(FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                var key = dir.register(watcher,
                        ENTRY_CREATE,
                        ENTRY_DELETE,
                        ENTRY_MODIFY);
                var registered = keys.get(key);
                if(registered == null) {
                    LOGGER.info("Watching: {}", dir);
                } else {
                    if (!dir.equals(registered)) {
                        LOGGER.info("Update: {} -> {}", registered, dir);
                    }
                }
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // Disable Watcher
    public void close() throws IOException {
        try {
            watcher.close();
        } catch (IOException e) {
            LOGGER.error("Problem closing watcher", e);
        }
    }


    private static int ticksSinceLastReload = 0;
    public void processEvents() {
        ticksSinceLastReload++;
        WatchKey key;
        do {
            key = watcher.poll();
            if(key == null) {
                continue;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                LOGGER.info("Unexpected key triggered {}", dir);
                continue;
            }

            for(WatchEvent<?> event: key.pollEvents()) {
                var kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                var context = event.context();
                if(context instanceof Path path) {
                    var child = dir.resolve(path);

                    LOGGER.info("Event {} on {}", kind.name(), child);

                    if(kind == ENTRY_CREATE) {
                        try  {
                            if (Files.isDirectory(child)) {
                                watchFolder(child);
                            }
                        } catch (IOException e) {
                            LOGGER.error("Problem watching new folder {}", dir);
                        }
                    } else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("Ticks since last reload: " + ticksSinceLastReload);
                        if(ticksSinceLastReload > 20 * 3) {
                            ticksSinceLastReload = 0;
                            this.runnable.run();
                        } else {
                            LOGGER.info("Update skipped due to being too soon");
                        }
                        LOGGER.info("File changed!!!! {}", dir);
                    }
                }
            }
            boolean valid = key.reset();
            if(!valid) {
                keys.remove(key);
            }
        } while(key != null);
    }
}
