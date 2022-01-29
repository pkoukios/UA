/*
 * $Id:: DraftController.java 2021/04/02 10:54 tantonop
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

package eu.euipo.etmdn.userarea.ws.controller.correspondence;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.euipo.etmdn.userarea.ws.config.SPBackOfficeConfiguration;
import eu.euipo.etmdn.userarea.common.business.correspondence.DraftService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Draft;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.DraftResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.DraftResourceMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/correspondences/draft")
@AllArgsConstructor
public class DraftController {

    DraftService draftService;
    SPBackOfficeConfiguration backOfficeConfiguration;

    /**
     * Creates a new draft reply
     * @param authentication the authenticated user
     * @param messageId the correspondence message id this draft will be created for
     * @return the created draft
     */
    @PostMapping()
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_WRITE')")
    public ResponseEntity<DraftResource> createDraft(Authentication authentication, @Valid @RequestParam String messageId){
        log.info("Received draft for message {} ",messageId);
        Draft  draft = draftService.create(authentication.getName(),Long.parseLong(messageId));
        DraftResource ret = DraftResourceMapper.MAPPER.map(draft);
        log.info("Created draft with id {} for message {} ",draft.getId(),messageId);
        return new ResponseEntity<>(ret, HttpStatus.CREATED);
    }

    /**
     * Updates a draft reply
     * @param authentication the authenticated user
     * @param draftResource the updated draft information
     * @return the updated draft message
     */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_WRITE')")
    public ResponseEntity<DraftResource> updateDraft(Authentication authentication, @Valid @RequestBody DraftResource draftResource){
        log.info("Updating draft with id {} for message {} ",draftResource.getId(),draftResource.getMessageId());
        Draft draft = DraftResourceMapper.MAPPER.map(draftResource);
        draft = draftService.update(authentication.getName(),draftResource.getMessageId(),draft, false, draftResource.isAutosave());
        DraftResource ret = DraftResourceMapper.MAPPER.map(draft);
        log.info("Updated draft with id {} for message {} ",ret.getId(),ret.getMessageId());
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }


    /**
     * Return a DraftResource object by id
     * @param identifier
     * @return
     */
    @GetMapping(path = "/{identifier}")
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<DraftResource> get(@PathVariable String identifier){
        log.info("Get draft details for draft with id {}",identifier);
        Draft draft = draftService.get(identifier);
        return new ResponseEntity<>(DraftResourceMapper.MAPPER.map(draft), HttpStatus.OK);
    }

    /**
     * returns true/false if the draft is locked or not
     * @param identifier the draft identifier
     * @return Boolean true if the draft is lcoked
     */
    @GetMapping(path = "/{identifier}/locked")
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<Boolean> getIsLocked(Authentication authentication,@PathVariable String identifier){
        log.info("Check if locked for draft with id {}",identifier);
        Boolean isLocked = draftService.isDraftLocked(authentication.getName(),identifier);
        return new ResponseEntity<>(isLocked, HttpStatus.OK);
    }

    /**
     * Deletes
     * @param identifier the draft identifier
     */
    @DeleteMapping(path = "/{identifier}")
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_WRITE')")
    public ResponseEntity<Void> deleteDraft(Authentication authentication, @PathVariable String identifier){
        log.info("Deleting draft with id {}",identifier);
        draftService.delete(authentication.getName(),identifier);
        log.info("Deleted draft with id {}",identifier);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Send draft reply
     * @param authentication the authenticated user
     * @param draftResource the updated draft information
     * @return the sent draft reply
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_WRITE')")
    public ResponseEntity<Void> sendDraftReply(Authentication authentication, @Valid @RequestParam DraftResource draftResource,
                                               @RequestParam(value = "files", required = false) List<MultipartFile> files){
        log.info("Send draft reply for message {} ", draftResource.getMessageId());
        Draft draft = DraftResourceMapper.MAPPER.map(draftResource);
        Draft replyDraft = draftService.sendDraftReply(authentication.getName(), draftResource.getMessageId(), draft, files,
                String.join(StringUtils.EMPTY, backOfficeConfiguration.getServiceBackOffice(), backOfficeConfiguration.getDraftBackOffice()));
        log.info("Sent draft reply with id {} for message {} ", replyDraft.getId(), replyDraft.getMessage().getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Component
    public static class StringToDraftResourceConverter implements Converter<String, DraftResource> {

        @Autowired
        private ObjectMapper objectMapper;

        @Override
        @SneakyThrows
        public DraftResource convert(String source) {
            return objectMapper.readValue(source, DraftResource.class);
        }
    }

}


