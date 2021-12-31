package com.xlip.objectstream.server.logging;



import com.xlip.objectstream.server.conf.ServerConfigurations;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BaseLogger {

    public static final Logger LOGGER = Logger.getGlobal();


    static {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "[%1$tF %1$tT] [%4$-7s] %5$s %n");

            FileHandler fh = new FileHandler(ServerConfigurations.getIntance().serverLogFile, true);

            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
