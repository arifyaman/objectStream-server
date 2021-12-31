package com.xlip.objectstream.server.hall.room;

import com.xlip.objectstream.communication.Wrap;
import com.xlip.objectstream.server.annotations.AllowedCommands;
import com.xlip.objectstream.server.annotations.HandleRequest;
import com.xlip.objectstream.server.handler.ClientHandler;
import com.xlip.objectstream.server.util.WrapHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

public abstract class Room implements ClientHandler.ClientHandlerCallbacks {
    protected Hashtable<Long, ClientHandler> clientHandlerHashtable = new Hashtable<>();
    private RoomCallbacks roomCallbacks;

    public Room(RoomCallbacks roomCallbacks) {
        this.roomCallbacks = roomCallbacks;
    }

    public ClientHandler register(ClientHandler clientHandler) {
        clientHandlerHashtable.put(clientHandler.getId(), clientHandler);
        clientHandler.setClientHandlerBacks(this);
        clientHandler.setAssignedRoom(this);
        return clientHandler;
    }

    public void removeFromRoom(ClientHandler clientHandler) {
        clientHandlerHashtable.remove(clientHandler.getId());
    }

    @Override
    public void disconnected(ClientHandler clientHandler) {
        clientHandlerHashtable.remove(clientHandler.getId());
        this.roomCallbacks.disconnected(clientHandler);
    }

    @Override
    public void wrapReceived(Wrap wrap, ClientHandler from) {
        AllowedCommands allowedCommands = this.getClass().getAnnotation(AllowedCommands.class);
        for (String command : allowedCommands.commands()) {
            if (wrap.getCmd().equals(command)) {
                final Method[] allMethods = this.getClass().getDeclaredMethods();
                for (Method method : allMethods) {
                    if (method.isAnnotationPresent(HandleRequest.class)) {
                        HandleRequest handleRequestAnnotation = method.getAnnotation(HandleRequest.class);
                        if (handleRequestAnnotation.cmd().equals(wrap.getCmd())) {
                            try {
                                method.invoke(this, wrap, from);
                                return;
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                from.dispatch(WrapHelper.doesNotSupportedWrap, true);
                return;
            }
        }
        from.dispatch(WrapHelper.notAllowedWrap, true);
    }

    public void broadcast(Wrap wrap) {
        this.clientHandlerHashtable.forEach((l, c) -> {
            c.dispatch(wrap, true);
        });
    }


    public interface RoomCallbacks extends ClientHandler.ClientHandlerCallbacks {

    }
}
