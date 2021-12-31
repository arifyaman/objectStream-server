package com.xlip.objectstream.server.conf;

import com.xlip.objectstream.server.Server;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;


public class ServerConfigurations {
    public String env;
    public int serverPort;
    public String serverLogFile;
    public Security security;
    public WaitingRoom waitingRoom;
    public HibernateConfiguration hibarnate;


    private static ServerConfigurations intance = new ServerConfigurations();

    public static ServerConfigurations getIntance() {
        return intance;
    }


    static {

        Yaml yaml = new Yaml();
        try {
            String env = System.getProperty("profile");
            File initialFile = null;


            try {
                URL url = Server.class.getResource(env + ".yml");
                initialFile = new File(url.getFile());
            } catch (Exception e) {
                initialFile = new File(env + ".yml");
            }
            InputStream targetStream = new FileInputStream(initialFile);
            intance = yaml.loadAs(targetStream, ServerConfigurations.class);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public static class Security {
        public String encriptorKey;
        public String vector;
        public Long changeEncryptionPeriod;
    }

    public static class WaitingRoom {
        public int maxPoolSize;
    }

    public static class HibernateConfiguration {
        public String driverClass;
        public String url;
        public String username;
        public String password;
        public String showSql;
        public String sessionContextClass;
        public String ddlAuto;
        public String maxActive;
        public String dialect;
    }

}
