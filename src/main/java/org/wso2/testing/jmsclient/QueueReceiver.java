package org.wso2.testing.jmsclient;

import javax.jms.*;
import java.util.Enumeration;

/**
 * Created by jeewantha on 2/24/14.
 */
public class QueueReceiver extends JMSQueue {

    private MessageConsumer queueReceiver;
    private QueueConnection queueConnection;
    private QueueSession queueSession;

    public QueueReceiver(String queueName) {
        super(queueName);
    }

    public void receiveMessages() throws JMSClientException {
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) initialContext.lookup(lookupName);
            queueConnection = connFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            //Receive message
            Queue queue = (Queue) initialContext.lookup(queueName);
            queueReceiver = queueSession.createConsumer(queue);
            queueConnection.start();
            TextMessage message;
            int messageCount = 1;
            while ((message = (TextMessage) queueReceiver.receive()) != null) {
                Enumeration e = message.getPropertyNames();
                System.out.println(messageCount);
                while (e.hasMoreElements()) {
                    String header = (String)e.nextElement();
                    System.out.println(header+ "=" +message.getStringProperty(header));
                }
                System.out.println(message.getText());
                System.out.println();
                messageCount++;
            }
            //System.out.println(messageCount);
        } catch (Exception e) {
            System.out.println(e);

        } finally {
            try {
                queueReceiver.close();
                queueSession.close();
                queueConnection.close();
            } catch (JMSException e) {
                throw new JMSClientException(e);
            }
        }
    }
}
