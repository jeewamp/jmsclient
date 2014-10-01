/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.testing.jmsclient;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author  jeewantha.
 */
public class QueueSender {

    String queueName;

    public QueueSender(String queueName) {
        this.queueName = queueName;

    }

    /**
     * @param filePath     to the messageFile or a directory containing messageFiles.
     * @param headerString containing JMS headers. "header1=value1;header2=value2"
     * @param concurrency
     * @param number
     */
    public void sendMessagesFromFileSystem(String filePath, String headerString,
                                           int concurrency, int number) throws JMSClientException {

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
            sendMessages(messages, headerMap, concurrency, number);
        } catch (Exception e) {
            throw new JMSClientException("Cannot send message ", e);
        }
    }

    public void sendTestMessage(int number) throws JMSClientException {

        Map headerMap = new HashMap();
        ArrayList messages = new ArrayList<String>();
        try {
            String resourcePath = System.getProperty(Constants.SYS_PROP_CONF_DIR) +
                    System.getProperty("file.separator")+ Constants.TEST_MSG_FILE_NAME;
            String message = readResource(resourcePath);
            messages.add(message);
            sendMessages(messages, headerMap, 0, number);
        } catch (Exception e) {
            throw new JMSClientException("Cannot send message ", e);
        }
    }

    /**
     * Send the messages in messages ArrayList with the headers given. One message count number of
     * times.
     */
    private void sendMessages(ArrayList<String> messages, Map<String, String> headers,
                              int concurrency, int number) throws NamingException, JMSException,
        IOException,
        InterruptedException, JMSClientException {
        ExecutorService executor = Executors.newFixedThreadPool(500);
        long time = System.currentTimeMillis();
        for (int i = 1; i <= concurrency; i++) {
            Runnable worker = new QueueSenderWorker(queueName, messages, headers, i, number);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished sending all events in "+ (System.currentTimeMillis() - time)
                + "millis.");
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
