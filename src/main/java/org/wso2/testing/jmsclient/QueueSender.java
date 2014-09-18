package org.wso2.testing.jmsclient;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.NamingException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by jeewantha on 2/24/14.
 */
public class QueueSender extends JMSQueue {

    private QueueSession queueSession;
    private QueueConnection queueConnection;
    private MessageProducer queueSender;
    private MessageEditor messageEditor;

    public QueueSender(String queueName, MessageEditor messageEditor) {
        super(queueName);
        this.messageEditor = messageEditor;
    }

    /**
     * @param filePath     to the messageFile or a directory containing messageFiles.
     * @param headerString containing JMS headers. "header1=value1;header2=value2"
     * @param count
     */
    public void sendMessagesFromFileSystem(String filePath, String headerString, int count) throws JMSClientException {

        Map headerMap = headerString == null ? new HashMap() : getHeadersFromString(headerString);

        File inputFile = new File(filePath);
        File[] files;

        if (inputFile.isDirectory()) {
            files = inputFile.listFiles();
            Arrays.sort(files);
        } else if (inputFile.isFile()) {
            files = new File[]{inputFile};
        } else {
            throw new JMSClientException("Cannot read messages from given path");
        }

        ArrayList messages = new ArrayList<String>();

        try {
            for (File nextFile : files) {
                String message = readFile(nextFile);
                messages.add(message);
            }
            sendMessages(messages, headerMap, count);
        } catch (Exception e) {
            throw new JMSClientException("Cannot send message ", e);
        }
    }

    public void sendTestMessage(int count) throws JMSClientException {

        Map headerMap = new HashMap();
        ArrayList messages = new ArrayList<String>();
        try {
            String resourcePath = System.getProperty(Constants.SYS_PROP_CONF_DIR) +
                    System.getProperty("file.separator")+ Constants.TEST_MSG_FILE_NAME;
            String message = readResource(resourcePath);
            messages.add(message);
            sendMessages(messages, headerMap, count);
        } catch (Exception e) {
            throw new JMSClientException("Cannot send message ", e);
        }
    }

    /**
     * Send the messages in messages ArrayList with the headers given. One message count number of
     * times.
     */
    private void sendMessages(ArrayList<String> messages, Map<String, String> headers, int count) throws NamingException, JMSException, IOException, InterruptedException, JMSClientException {
        try {
            // Lookup connection factory
            QueueConnectionFactory connFactory = (QueueConnectionFactory) initialContext.lookup(lookupName);
            queueConnection = connFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            // Send message sender
            Queue queue = (Queue) initialContext.lookup(queueName);
            queueSender = queueSession.createProducer(queue);
            queueConnection.start();

            // create the message to send

            for (int j = 1; j <= count; j++) {
                if(messageEditor != null) {
                    ArrayList editedMessages = messageEditor.editMessages(messages, j);
                    Map editedHeaders = messageEditor.editHeaders(headers, j);
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
                    System.out.println("sending... count = " + j);
                    queueSender.send(tx);
                    Thread.sleep(1);
                }
            }
        }
        catch (Exception e) {
            throw new JMSClientException("Error when Seding message",e);
        }
        finally {
            if (queueConnection != null) queueConnection.close();
            if (queueSession != null) queueSession.close();
            if (queueSender != null)queueSender.close();
        }
    }

    /**
     * @param headerString format should be "A=B;C=D;E=F"
     * @return
     * @throws ArrayIndexOutOfBoundsException
     */
    private Map getHeadersFromString(String headerString) throws ArrayIndexOutOfBoundsException {

        HashMap headers = new HashMap();
        StringTokenizer tokenizer = new StringTokenizer(headerString.trim(), ",");
        while (tokenizer.hasMoreElements()) {
            String[] headerVal = tokenizer.nextToken().split("=");
            String header = headerVal[0].trim();
            String value = headerVal[1].trim();
            headers.put(header, value);
        }
        return headers;
    }

    private String readResource(String path) throws IOException, URISyntaxException {

        FileInputStream fileInputStream = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));

        StringBuilder fileContents = new StringBuilder();
        String currentLine;

        try {
            while ((currentLine = reader.readLine()) != null) {
                fileContents.append(currentLine);
            }
            return fileContents.toString().replace("\n", "").replace("\r", "");
        } finally {
            reader.close();
        }
    }

    private String readFile(File file) throws IOException {

        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString().replace("\n", "").replace("\r", "");
        } finally {
            scanner.close();
        }
    }

}
