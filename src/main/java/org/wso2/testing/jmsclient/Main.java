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

import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * @author  jeewantha.
 */
public class Main {

    public static Properties jmsClientProps;

    private void init() throws IOException{
        Properties log4jprops = new Properties();
        log4jprops.load(new FileInputStream(System.getProperty(Constants.SYS_PROP_CONF_DIR) +
                System.getProperty("file.separator")+"log4j.properties"));
        PropertyConfigurator.configure(log4jprops);

        jmsClientProps = new Properties();
        jmsClientProps.load(new FileInputStream(System.getProperty(Constants.SYS_PROP_CONF_DIR) +
                System.getProperty("file.separator")+ Constants.JMS_CLIENT_PROP_FILE_NAME));
    }

    private void run(String[] args) throws IOException, JMSClientException{

        init();

        if (args == null || args.length < 2) {
            System.out.println("Argument format: [ queueName sender|receiver (messageFile) " +
                    "(-c concurrency) (-n number) (-h headerString)]");
            throw new IllegalArgumentException("Not enough arguments given");
        }

        String queueName = args[0];

        if ("sender".equals(args[1])) {
            QueueSender queueSender = new QueueSender(queueName);

            if (args.length == 2) {
                queueSender.sendTestMessage(1);
            } else {

                String messageFile = null;
                String headerString = null;
                int concurrency = 1;
                int number = 1;
                if (args.length > 2) {
                    messageFile = args[2];
                }
                if (args.length > 3) {
                    String possibleError="";
                    try {
                        for(int i = 3; i < args.length ; i ++) {
                            if("-c".equals(args[i])) {
                                concurrency = Integer.parseInt(args[i + 1]);
                                possibleError = "Concurrency";
                            } else if ("-n".equals(args[i])) {
                                number = Integer.parseInt(args[i + 1]);
                                possibleError = "Number";
                            } else if ("-h".equals((args[i]))) {
                                headerString = args[i + 1];
                                possibleError = "HeaderString";
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("ERROR: " + possibleError + " value is not given");
                    }
                }
                queueSender.sendMessagesFromFileSystem(messageFile, headerString, concurrency,
                        number);
            }
        } else if ("receiver".equals(args[1])) {
            QueueReceiver queueReceiver = new QueueReceiver(queueName);
            queueReceiver.receiveMessages();
        }
    }



    public static void main(String[] args) throws IOException, JMSClientException {
        Main main = new Main();
        main.run(args);
    }


}
