package org.wso2.testing.jmsclient;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jeewantha on 9/17/14.
 */
public interface MessageEditor {

    public ArrayList<String> editMessages(ArrayList<String> messages, int count);

    public Map<String, String> editHeaders(Map<String, String> headers, int count);
}
