package com.xlip.objectstream.server.hall.room;

import com.xlip.objectstream.communication.Wrap;
import com.xlip.objectstream.communication.service.EncryptionService;
import com.xlip.objectstream.server.annotations.AllowedCommands;
import com.xlip.objectstream.server.annotations.HandleRequest;
import com.xlip.objectstream.server.hall.RoomHolder;
import com.xlip.objectstream.server.handler.ClientHandler;
import com.xlip.objectstream.server.logging.BaseLogger;
import com.xlip.objectstream.server.util.WrapHelper;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

@AllowedCommands(commands = {"REGISTER"})
public class RegistererRoom extends Room {
    Hashtable<Long, Timer> kickers = new Hashtable<>();

    public RegistererRoom(RoomCallbacks roomCallbacks) {
        super(roomCallbacks);
    }


    @Override
    public ClientHandler register(ClientHandler clientHandler) {
        ClientHandler registeredClientHandler = super.register(clientHandler);
        HashMap<String, String> encMap = new HashMap<>();
        EncryptionService encryptionService = EncryptionService.getInstance();

        encMap.put("iv", encryptionService.getIv());
        encMap.put("key", encryptionService.getSecretKey());
        Wrap wrap = Wrap.createRequest();
        wrap.setCmd("PREPARE");
        wrap.setPayload(encMap);

        registeredClientHandler.dispatch(wrap, false);

        Timer kicker = new Timer();
        kicker.schedule(new TimerTask() {
            @Override
            public void run() {
                Wrap wrap = Wrap.createResponse();
                wrap.setSuccess(false);
                wrap.setMessage("Unexpected behavior bye!");
                registeredClientHandler.dispatch(wrap, false);
                registeredClientHandler.close();
                BaseLogger.LOGGER.info("Client handler does not know how to behave, kicked !");

            }
        }, 2000);
        kickers.put(clientHandler.getId(), kicker);

        return registeredClientHandler;
    }

    private void removeKicker(Long cliendHandlerId){
        kickers.get(cliendHandlerId).cancel();
        kickers.remove(cliendHandlerId);
    }

    @HandleRequest(cmd = "REGISTER")
    public void confirmRegistration(Wrap wrap, ClientHandler clientHandler) {
        removeKicker(clientHandler.getId());
        BaseLogger.LOGGER.info("Registered : " + clientHandler.getId());
        clientHandler.dispatch(WrapHelper.welcomeWrap, true);
        RoomHolder.getInstance().moveToRoom(clientHandler, WaitingRoom.class);
    }
}
