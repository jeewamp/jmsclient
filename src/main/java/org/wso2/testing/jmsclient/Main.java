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

    private void run(String[] args) throws IOException, JMSClientException{

        Properties props = new Properties();
        props.load(new FileInputStream(System.getProperty(Constants.SYS_PROP_CONF_DIR) +
                System.getProperty("file.separator")+"log4j.properties"));
        PropertyConfigurator.configure(props);

        if (args == null || args.length < 2) {
            System.out.println("Argument format: [ queueName sender|receiver (messageFile) " +
                    "(count) (headerString)]");
            throw new IllegalArgumentException("Not enough arguments given");
        }

        String queueName = args[0];

        if ("sender".equals(args[1])) {
            QueueSender queueSender = new QueueSender(queueName, getMessageEditor());

            if (args.length == 2) {
                queueSender.sendTestMessage(1);
            } else {

                String messageFile = null;
                String headerString = null;
                int count = 1;
                if (args.length > 2) {
                    messageFile = args[2];
                }
                if (args.length > 3) {
                    count = Integer.parseInt(args[3]);
                }
                if (args.length > 4) {
                    headerString = args[4];
                }
                queueSender.sendMessagesFromFileSystem(messageFile, headerString, count);
            }
        } else if ("receiver".equals(args[1])) {
            QueueReceiver queueReceiver = new QueueReceiver(queueName);
            queueReceiver.receiveMessages();
        }
    }

    private MessageEditor getMessageEditor() {
        String resourcePath = System.getProperty(Constants.SYS_PROP_CONF_DIR) +
                System.getProperty("file.separator")+ Constants.JMS_CLIENT_PROP_FILE_NAME;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(resourcePath));
            String meFqcn = props.getProperty("messageEditor");
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

    public static void main(String[] args) throws IOException, JMSClientException {
        Main main = new Main();
        main.run(args);
    }


}
