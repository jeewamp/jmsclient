package org.wso2.testing.jmsclient;

/**
 * Created by jeewantha on 3/20/14.
 */
public class JMSClientException extends Exception {

    public JMSClientException(String message) {
        super(message);
    }

    public JMSClientException(Exception e) {
        super(e);
    }

    public JMSClientException(String message, Exception e) {
        super(message, e);
    }
}
