package org.wso2.testing.jmsclient.msgeditor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.testing.jmsclient.MessageEditor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class SeriesJSONMessageEditor implements MessageEditor {
    public ArrayList<String> editMessages(ArrayList<String> messages, int count) {

        ArrayList<String> newMessages = new ArrayList<String>();

        for(String message : messages) {
            JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
            String dateTime = jsonObject.get("DateTime").getAsString();

            //SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date= null;
            try {
                //"2013-04-18-19.01.33.080"
                date = dateFormat.parse(dateTime);
                date.setTime(date.getTime() + 60*60*1000);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            String newMessage = message.replace(dateTime, dateFormat.format(date));
            newMessages.add(newMessage);
        }
        return newMessages;
    }

    public Map<String, String> editHeaders(Map<String, String> headers, int count) {
        return null;
    }
}