package org.wso2.testing.jmsclient;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jeewantha on 9/29/14.
 */
public class QueueSenderWorker extends JMSQueue implements Runnable {

    private int runs;
    private static volatile AtomicInteger messageNumber = new AtomicInteger(0);
    private boolean printLogs = false;
    private boolean sendAsync = false;
    ArrayList<String> messages;
    Map<String, String> headers;
    QueueConnection queueConnection;
    QueueSession queueSession;
    MessageProducer msgProducer;
    int workerId;

    public QueueSenderWorker(String queueName, ArrayList<String> messages, Map<String,
                             String> headers, int workerId, int runs){
        super(queueName);
        MessageEditor concurrentMessageEditor = getMessageEditor(Constants.CONCURRENT_MSG_EDITOR);
        if(concurrentMessageEditor != null) {
            ArrayList editedMessages = concurrentMessageEditor.editMessages(messages, workerId);
            Map editedHeaders = concurrentMessageEditor.editHeaders(headers, workerId);
            if(editedMessages != null) {
                messages = editedMessages;
            }
            if (editedHeaders != null) {
                headers = editedHeaders;
            }
        }
        this.printLogs = new Boolean(Main.jmsClientProps.getProperty(Constants.PRINT_LOGS_PROP,
                "false"));
        this.sendAsync = new Boolean(Main.jmsClientProps.getProperty(Constants.SEND_ASYNC_PROP,
                "false"));
        this.messages = messages;
        this.headers = headers;
        this.workerId = workerId;
        this.runs = runs;
    }

    public void run() {
        try {
            // Lookup connection factory
            QueueConnectionFactory connFactory = (QueueConnectionFactory) initialContext.lookup(lookupName);
            if(sendAsync) {
                ((ActiveMQConnectionFactory)connFactory).setUseAsyncSend(true);
            }
            queueConnection = connFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            // Send message sender
            Queue queue = (Queue) initialContext.lookup(queueName);
            msgProducer = queueSession.createProducer(queue);
            queueConnection.start();

            for (int number = 1; number <= runs; number++) {
                MessageEditor seriesMessageEditor = getMessageEditor(Constants.SERIES_MSG_EDITOR);
                if(seriesMessageEditor != null) {
                    ArrayList editedMessages = seriesMessageEditor.editMessages(messages, number);
                    Map editedHeaders = seriesMessageEditor.editHeaders(headers, number);
                    if(editedMessages != null) {
                        messages = editedMessages;
                    }
                    if (editedHeaders != null) {
                        headers = editedHeaders;
                    }
                }
                for (String message : messages) {
                    TextMessage tx = queueSession.createTextMessage(message);
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        tx.setStringProperty(key, value);
                    }
                    if(printLogs) {
                        printLog(workerId, number);
                    }
                    msgProducer.send(tx);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (queueConnection != null) queueConnection.close();
                if (queueSession != null) queueSession.close();
                if (msgProducer != null) msgProducer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private MessageEditor getMessageEditor(String type) {

        try {
            String meFqcn = Main.jmsClientProps.getProperty(type);
            MessageEditor messageEditor = null;
            if (meFqcn != null) {
                Class meClass = Class.forName(meFqcn);
                Constructor meClassCnstr = meClass.getConstructor();
                messageEditor = (MessageEditor) meClassCnstr.newInstance();
            }
            return messageEditor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private synchronized static void printLog(int workerId, int number) {

        System.out.println(">> count=" + messageNumber.incrementAndGet() +
                " c="+workerId+" n=" + number);
    }
}
