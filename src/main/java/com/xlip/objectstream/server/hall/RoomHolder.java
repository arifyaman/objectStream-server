package com.xlip.objectstream.server.hall;

import com.xlip.objectstream.communication.Wrap;
import com.xlip.objectstream.communication.service.EncryptionService;
import com.xlip.objectstream.server.conf.ServerConfigurations;
import com.xlip.objectstream.server.hall.room.RegistererRoom;
import com.xlip.objectstream.server.hall.room.Room;
import com.xlip.objectstream.server.hall.room.WaitingRoom;
import com.xlip.objectstream.server.handler.ClientHandler;
import com.xlip.objectstream.server.util.RandomString;
import com.xlip.objectstream.server.util.WrapHelper;
import lombok.Getter;

import java.net.Socket;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class RoomHolder implements Room.RoomCallbacks {

    @Getter
    private static final RoomHolder instance = new RoomHolder();
    private final Hashtable<Class<? extends Room>, Room> rooms = new Hashtable<>();
    private final Hashtable<Long, ClientHandler> clientHandlerMap = new Hashtable<>();
    private EncryptionChangeNotifier encryptionChangeNotifier;


    public RoomHolder() {
        rooms.put(RegistererRoom.class, new RegistererRoom(this));
        rooms.put(WaitingRoom.class, new WaitingRoom(this));

        encryptionChangeNotifier = new EncryptionChangeNotifier();
        encryptionChangeNotifier.start();
    }

    public ClientHandler register(Class<? extends Room> roomClass, Socket clientSocket) {
        Room room = rooms.get(roomClass);
        try {
            clientSocket.setSoTimeout(0);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        ClientHandler clientHandler = new ClientHandler(System.currentTimeMillis(), clientSocket);
        clientHandlerMap.put(clientHandler.getId(), clientHandler);
        clientHandler.setClientHandlerBacks(this);

        if (room == null) return clientHandler;

        room.register(clientHandler);
        clientHandler.start();

        return clientHandler;
    }

    public ClientHandler moveToRoom(ClientHandler clientHandler, Class<? extends Room> roomClass) {
        Room room = rooms.get(roomClass);
        if (room != null) {
            clientHandler.getAssignedRoom().removeFromRoom(clientHandler);
            room.register(clientHandler);
        }

        return clientHandler;
    }

    public void dispatchToAll(Wrap wrap) {
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            clientHandler.dispatch(wrap, true);
        }
    }

    @Override
    public void disconnected(ClientHandler clientHandler) {
        clientHandlerMap.remove(clientHandler.getId());
    }

    @Override
    public void wrapReceived(Wrap wrap, ClientHandler from) {

    }

    private class EncryptionChangeNotifier {
        private Timer timer;
        private long period;

        public EncryptionChangeNotifier() {
            this.period = ServerConfigurations.getIntance().security.changeEncryptionPeriod;
            timer = new Timer();
        }

        public void start() {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String newIv = new RandomString(16, new SecureRandom()).nextString();
                    String newKey = new RandomString(16, new SecureRandom()).nextString();
                    dispatchToAll(WrapHelper.changeEncWrap(newIv, newKey));
                    EncryptionService.init(newIv, newKey);
                }
            }, period, period);
        }

        public void cancel() {
            timer.cancel();
        }
    }
}
