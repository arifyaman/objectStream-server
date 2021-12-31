package com.xlip.objectstream.server.handler;

import com.xlip.objectstream.communication.LockedWrap;
import com.xlip.objectstream.communication.Wrap;
import com.xlip.objectstream.communication.service.EncryptionService;
import com.xlip.objectstream.server.hall.room.Room;
import com.xlip.objectstream.server.logging.BaseLogger;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;

public class ClientHandler extends Thread {

    @Getter
    private long id;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean dead = false;

    @Getter
    @Setter
    private ClientHandlerCallbacks clientHandlerBacks;

    @Getter
    @Setter
    private Room assignedRoom;

    public ClientHandler(Long id, Socket clientSocket) {
        this.id = id;
        this.socket = clientSocket;
        try {
            this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ignore) {
        }
    }

    @Override
    public void run() {
        while (true) {

            try {
                Object o = inputStream.readObject();
                Wrap wrap = null;

                if (o instanceof LockedWrap) {
                    wrap = EncryptionService.getInstance().resolveWrap(((LockedWrap) o).getBytes());

                } else if (o instanceof Wrap) {
                    close();
                }

                clientHandlerBacks.wrapReceived(wrap, this);
            } catch (IOException | ClassNotFoundException e) {

                dead = true;
                interrupt();
                clientHandlerBacks.disconnected(this);
                BaseLogger.LOGGER.info("Removed from Handlers " + getId());

                break;


            } catch (IllegalBlockSizeException | BadPaddingException e) {
                close();
            }
        }
    }

    public void close() {
        try {
            this.outputStream.close();
            this.inputStream.close();
            this.socket.close();
        } catch (IOException ignore) {
        }
    }


    public void dispatch(LockedWrap lockedWrap) {
        try {
            outputStream.writeObject(lockedWrap);
        } catch (IOException e) {
            BaseLogger.LOGGER.log(Level.SEVERE, " Error Dispatch: ", e);
            e.printStackTrace();
        }
    }

    public void dispatch(Wrap wrap, boolean forceLock) {
        if(forceLock){
            dispatch(EncryptionService.getInstance().lockWrap(wrap));
            return;
        }
        try {
            outputStream.writeObject(wrap);
        } catch (IOException e) {
            BaseLogger.LOGGER.log(Level.SEVERE, " Error Dispatch: ", e);
            e.printStackTrace();
        }
    }


    public interface ClientHandlerCallbacks {
        void disconnected(ClientHandler clientHandler);

        void wrapReceived(Wrap wrap, ClientHandler from);
    }
}
