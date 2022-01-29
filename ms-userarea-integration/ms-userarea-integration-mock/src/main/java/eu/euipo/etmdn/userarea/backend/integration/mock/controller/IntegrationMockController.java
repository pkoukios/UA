/*
 * $Id:: IntegrationMockController.java 2021/05/11 11:58 dvelegra
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

package eu.euipo.etmdn.userarea.backend.integration.mock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.euipo.etmdn.userarea.common.domain.correspondence.DraftReply;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageReceipt;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mock")
@AllArgsConstructor
public class IntegrationMockController {

    @PostMapping("/message/receipt/{identifier}")
    public ResponseEntity<Void> getMessageReceipt(@PathVariable String identifier,@RequestBody MessageReceipt messageReply){
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/draft/{identifier}")
    public ResponseEntity<Void> sendMessageReply(@PathVariable String identifier,@Valid @RequestParam DraftReply draftReply,  @RequestParam(value = "files", required = false) List<MultipartFile> files){
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Component
    public static class StringToDraftReplyConverter implements Converter<String, DraftReply> {

        @Autowired
        private ObjectMapper objectMapper;

        @Override
        @SneakyThrows
        public DraftReply convert(String source) {
            return objectMapper.readValue(source, DraftReply.class);
        }
    }


}
