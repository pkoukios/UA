/*
 * $Id:: JMSConfig.java 2021/10/05 02:16 tantonop
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 *
 */

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.jms.configuration;

import com.google.gson.Gson;
import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.jms.listener.CorrespondenceListener;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class JMSConfig {

    @Autowired
    private Gson gson;

    @Autowired
    MessageService messageService;

    @Value("${jms.activemq.connection.factory}")
    String jmsConnectionFactory;

    @Value("${jms.activemq.context.factory}")
    String jmsContextFactory;

    @Value("${jms.activemq.broker-url}")
    String brokerUrl;

    @Value("${jms.activemq.user}")
    String userName;

    @Value("${jms.activemq.password}")
    String password;

    @Value("${jms.activemq.listen.message.queue}")
    String listenQueue;

    private Session session;
    private Connection connection;


    public void init(){
        Properties initialProperties = new Properties();
        initialProperties.put(InitialContext.INITIAL_CONTEXT_FACTORY, jmsContextFactory);
        initialProperties.put(InitialContext.PROVIDER_URL, brokerUrl);
        initialProperties.put(InitialContext.SECURITY_PRINCIPAL, userName);
        initialProperties.put(InitialContext.SECURITY_CREDENTIALS, password);
        InitialContext context = null;

        try{
            context = new InitialContext(initialProperties);
            QueueConnectionFactory factory = (QueueConnectionFactory) context.lookup(jmsConnectionFactory);
            Queue destination = (Queue) context.lookup(listenQueue);
            this.connection = factory.createConnection(userName, password);

            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer consumer = this.session.createConsumer(destination);
            consumer.setMessageListener(new CorrespondenceListener(gson,messageService));
            connection.start();
            log.info("CORRESPONDENCE SUBSCRIBER INITIATED");
        }
        catch (NamingException | JMSException e) {
           log.error("Error subscribing to jms queue with error {}",e.getMessage());
        }
    }

    @Scheduled(fixedRate = 5000)
    public void restoreSession(){
        if(this.session==null){
            init();
        }
        ActiveMQSession session = (ActiveMQSession)this.session;
        if(session.getCoreSession().isClosed()){
            init();
        }

    }


}
