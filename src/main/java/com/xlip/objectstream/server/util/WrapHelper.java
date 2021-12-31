package com.xlip.objectstream.server.util;

import com.xlip.objectstream.communication.Wrap;
import com.xlip.objectstream.communication.service.EncryptionService;
import com.xlip.objectstream.communication.sub.WrapType;

import java.util.HashMap;

public class WrapHelper {

    public static Wrap notAllowedWrap = Wrap.builder().wrapType(WrapType.RESPONSE).success(false).message("This is not allowed.").code(-10).build();
    public static Wrap doesNotSupportedWrap = Wrap.builder().wrapType(WrapType.RESPONSE).success(false).message("Your request is not supported.").code(-11).build();
    public static Wrap welcomeWrap = Wrap.builder().wrapType(WrapType.RESPONSE).success(true).message("Welcome").code(0).build();
    public static Wrap okWrap = Wrap.builder().wrapType(WrapType.RESPONSE).success(true).message("OK").code(1).build();

    public static Wrap changeEncWrap(String iv, String key) {
        HashMap<String, String> encMap = new HashMap<>();
        encMap.put("iv", iv);
        encMap.put("key", key);

        return Wrap.builder().wrapType(WrapType.REQUEST).payload(encMap).cmd("CHANGE_ENC").build();
    }

}
