package org.rsinitsyn.bot.utils;

import java.util.Base64;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TokenUtils {

    public String getBasicToken(String clientId, String secret) {
        return Base64.getEncoder().encodeToString((clientId + ":" + secret).getBytes());
    }
}
