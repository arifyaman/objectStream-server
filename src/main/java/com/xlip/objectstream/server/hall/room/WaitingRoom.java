package com.xlip.objectstream.server.hall.room;

import com.xlip.objectstream.communication.Wrap;
import com.xlip.objectstream.communication.sub.WrapType;
import com.xlip.objectstream.server.annotations.AllowedCommands;
import com.xlip.objectstream.server.annotations.HandleRequest;
import com.xlip.objectstream.server.handler.ClientHandler;
import com.xlip.objectstream.server.logging.BaseLogger;
import com.xlip.objectstream.server.util.WrapHelper;

import java.util.Timer;
import java.util.TimerTask;

@AllowedCommands(commands = {"OK","1","2","3"})
public class WaitingRoom extends Room {
    Timer notifier;

    public WaitingRoom(RoomCallbacks roomCallbacks) {
        super(roomCallbacks);
        notifier = new Timer();

        notifier.schedule(new TimerTask() {
            @Override
            public void run() {
                Wrap notification = Wrap.builder().wrapType(WrapType.RESPONSE).message("Hi!").success(true).build();
                broadcast(notification);
            }
        }, 3000,1000);
    }

    @HandleRequest(cmd = "OK")
    public void handleOk(Wrap wrap, ClientHandler clientHandler) {
        BaseLogger.LOGGER.info(wrap.toString() + " OK : " + clientHandler.getId());
        clientHandler.dispatch(WrapHelper.okWrap, true);
    }

}
