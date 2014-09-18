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
import java.util.Enumeration;

/**
 * @author  jeewantha.
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
