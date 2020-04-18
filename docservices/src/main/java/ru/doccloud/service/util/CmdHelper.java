package ru.doccloud.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdHelper.class);

    public static void callShell(String command, Boolean wait) {
        Process proc;
        try {
            LOGGER.debug("run command '{}'", command);
            proc = Runtime.getRuntime().exec(command);

            StreamGobbler outGobbler = new StreamGobbler(proc.getInputStream(), LOGGER::info);
            StreamGobbler errGobbler = new StreamGobbler(proc.getInputStream(), LOGGER::error);

            Executors.newSingleThreadExecutor().submit(outGobbler);
            Executors.newSingleThreadExecutor().submit(errGobbler);

            int exitCode = 0;
            if (wait) {
                exitCode = proc.waitFor();
            }
            LOGGER.debug("command res {}", exitCode);
            //assert exitCode == 0;
        } catch (Exception e) {
            LOGGER.error("Error while evaluating shell command {}", command, e);
            throw new RuntimeException("Error while evaluating shell command (" + command + ")", e);
        }

    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }

}
