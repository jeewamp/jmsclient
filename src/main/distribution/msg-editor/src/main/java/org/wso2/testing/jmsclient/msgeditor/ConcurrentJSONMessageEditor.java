package org.wso2.testing.jmsclient.msgeditor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.testing.jmsclient.MessageEditor;


import java.util.ArrayList;
import java.util.Map;

public class ConcurrentJSONMessageEditor implements MessageEditor {
    public ArrayList<String> editMessages(ArrayList<String> messages, int count) {

        ArrayList<String> newMessages = new ArrayList<String>();

        for(String message : messages) {
            JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
            String imei = jsonObject.get("IMEI").getAsString();
            String newMessage = message.replace(imei,"t"+count);
            newMessages.add(newMessage);
        }
        return newMessages;
    }

    public Map<String, String> editHeaders(Map<String, String> headers, int count) {
        return null;
    }
}