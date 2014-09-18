package org.wso2.testing.jmsclient;

import javax.naming.InitialContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by jeewantha on 3/19/14.
 */
public class JMSQueue {
    protected String queueName = "myQueue";
    protected InitialContext initialContext;
    protected String lookupName;

    public JMSQueue(String queueName) {
        try {
            init(queueName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(String queueName) throws Exception {
        String resourcePath = System.getProperty(Constants.SYS_PROP_CONF_DIR) +
                System.getProperty("file.separator")+ Constants.JNDI_PROP_FILE_NAME;
        Properties props = new Properties();
        props.load(new FileInputStream(resourcePath));
        props.put("queue." + queueName, queueName);
        this.initialContext = new InitialContext(props);
        this.lookupName = props.getProperty(Constants.CF_LOOKUP_NAME);
        this.queueName = queueName;
    }

}
