/*
 * $Id:: MessageController.java 2021/05/13 01:46 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intellectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Message;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.MessageResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.MessageResourceMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;



@Slf4j
@RestController
@RequestMapping("/correspondences/message")
@AllArgsConstructor
public class MessageController {

    MessageService messageService;

    /**
     * creates a new incoming message
     * @param messageResource the message resource definition
     * @param files any attachments the message might have
     * @return the message that has been created
     */
    @PostMapping()
    public ResponseEntity<MessageResource> create(@Valid @RequestParam MessageResource messageResource, @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        int numberOfFiles = files != null ? files.size() : 0;
        log.info("Received message for {} with {} number of attachments", messageResource.getApplicationId(), numberOfFiles);
        Message message = MessageResourceMapper.MAPPER.map(messageResource);
        message.setMessageStatus(MessageStatus.NEW);
        message.setReceivedDate(LocalDateTime.now());
        Message createdMessage = messageService.save(message,files);
        MessageResource createdMessageResource = MessageResourceMapper.MAPPER.map(createdMessage);
        return new ResponseEntity<>(createdMessageResource,HttpStatus.CREATED);
    }

       @Component
    public static class StringToMessageResourceConverter implements Converter<String, MessageResource> {

        @Autowired
        private ObjectMapper objectMapper;

        @Override
        @SneakyThrows
        public MessageResource convert(String source) {
            return objectMapper.readValue(source, MessageResource.class);
        }
    }

}
