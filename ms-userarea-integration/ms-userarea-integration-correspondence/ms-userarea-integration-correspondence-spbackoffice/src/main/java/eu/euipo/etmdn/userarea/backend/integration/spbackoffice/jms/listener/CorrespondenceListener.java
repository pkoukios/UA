/*
 * $Id:: CorrespondenceListener.java 2021/10/05 11:21 tantonop
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

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.jms.listener;

import com.google.gson.Gson;
import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.MessageBOMapper;
import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.BOAttachments;
import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.BOMessage;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Attachment;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * listener class to listen for incoming jms correspondence messages
 */

@Slf4j
@AllArgsConstructor
public class CorrespondenceListener implements MessageListener {

    private Gson gson;
    private MessageService messageService;

    @Override
    public void onMessage(Message message){
        log.info("Received message");
        TextMessage textMessage = (TextMessage)message;
        try {
            String text = textMessage.getText();
            BOMessage boMessage = gson.fromJson(text,BOMessage.class);
            createMessage(boMessage);
        }
        catch(JMSException ex){
            log.error("Cannot convert message to text message for incoming message {}",message.toString());
        }
    }

    /**
     * creates a new message in db
     * @param boMessage the boMessage
     */
    private void createMessage(BOMessage boMessage){
        int numOfFiles = boMessage.getAttachments()!=null? boMessage.getAttachments().size():0;
        log.info("Received message for {} with {} number of attachments", boMessage.getApplicationId(), numOfFiles);
        eu.euipo.etmdn.userarea.common.domain.correspondence.Message message = MessageBOMapper.MAPPER.map(boMessage);
        message.setMessageStatus(MessageStatus.NEW);
        message.setReceivedDate(LocalDateTime.now());
        List<Attachment> attachments = new ArrayList<>();
        if(boMessage.getAttachments()!=null){
            for(BOAttachments boAttachments:boMessage.getAttachments()){
                Attachment attachment = new Attachment();
                attachment.setUri(boAttachments.getLink());
                attachment.setMimeType(boAttachments.getType());
                attachment.setName(boAttachments.getName());
                attachment.setSize((int)boAttachments.getSize());
                attachments.add(attachment);
            }
        }
        messageService.saveFromBo(message,attachments);
    }
}
