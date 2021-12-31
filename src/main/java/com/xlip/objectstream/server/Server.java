package com.xlip.objectstream.server;

import com.xlip.objectstream.communication.service.EncryptionService;
import com.xlip.objectstream.server.conf.ServerConfigurations;
import com.xlip.objectstream.server.hall.RoomHolder;
import com.xlip.objectstream.server.hall.room.RegistererRoom;
import com.xlip.objectstream.server.logging.BaseLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {

        try {
            ServerSocket listener = new ServerSocket(ServerConfigurations.getIntance().serverPort);
            EncryptionService.init(ServerConfigurations.getIntance().security.vector, ServerConfigurations.getIntance().security.encriptorKey);

            BaseLogger.LOGGER.info("SERVER STARTED");

            while (true) {
                Socket socket = listener.accept();
                RoomHolder.getInstance().register(RegistererRoom.class, socket);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
