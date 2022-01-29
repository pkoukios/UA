/*
 * $Id:: CorrespondenceController.java 2021/04/06 12:59 tantonop
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

import eu.euipo.etmdn.userarea.common.business.correspondence.MessageService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageSearchResult;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageSearchResultContent;
import eu.euipo.etmdn.userarea.common.domain.correspondence.search.SearchCriteria;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.MessageSearchResultResource;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.SearchCriteriaResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.MessageSearchResultResourceMapper;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.SearchCriteriaResourceMapper;
import eu.euipo.etmdn.userarea.ws.config.SPBackOfficeConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/correspondences/message")
@AllArgsConstructor
public class CorrespondenceController {

    MessageService messageService;
    SPBackOfficeConfiguration spBackOfficeConfiguration;

    /**
     * gets all messages defined in the search criteria
     * @param searchCriteriaResource the search criteria
     * @return the messages based on the search criteria
     */
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageSearchResultResource> getAllMessages(Authentication authentication, @Valid @RequestBody SearchCriteriaResource searchCriteriaResource){
        log.info("Retrieving all correspondence");
        SearchCriteria searchCriteria = SearchCriteriaResourceMapper.MAPPER.map(searchCriteriaResource);
        MessageSearchResult messageSearchResult = messageService.getAllCorrespondencePerSearchCriteria(authentication.getName(), searchCriteria);
        MessageSearchResultResource ret = MessageSearchResultResourceMapper.MAPPER.map(messageSearchResult);
        return new ResponseEntity<>(ret, HttpStatus.OK);

    }

    /**
     * method to retrieve all the new correspondence
     * @return the number of correspondence with status UNREAD
     */
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    @GetMapping("/incoming/new")
    public ResponseEntity<String> getNewIncomingMessageCount(Authentication authentication){
        log.info("Received request for new incoming message count");
        String ret = String.valueOf(messageService.getNewIncomingCorrespondence(authentication.getName(),
                AuthorityUtils.authorityListToSet(authentication.getAuthorities())));
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    /**
     * gets the correspondence details
     * @param authentication the authenticated user
     * @param identifier the message id
     * @return the details of the message
     */
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    @PostMapping("/detail/{identifier}")
    public ResponseEntity<MessageSearchResultContent> getCorrespondenceDetails(Authentication authentication,
                                                                               @PathVariable String identifier,
                                                                               @Valid @RequestBody SearchCriteriaResource searchCriteriaResource){
        log.info("Retrieve details for message id {} and user {}",identifier,authentication.getName());
        SearchCriteria searchCriteria = SearchCriteriaResourceMapper.MAPPER.map(searchCriteriaResource);
        MessageSearchResultContent ret = messageService.getMessageDetails(authentication.getName(), identifier,
                spBackOfficeConfiguration.getServiceBackOffice(), spBackOfficeConfiguration.getReceiptBackOffice(),searchCriteria);
        return new ResponseEntity<>(ret,HttpStatus.OK);
    }

    /**
     * method to retrieve all the expiring correspondence counter.
     * @return the number of correspondence that expired
     */
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    @PostMapping("/expiring")
    public ResponseEntity<String> getExpiringMessagesCount(Authentication authentication, @Valid @RequestBody SearchCriteriaResource searchCriteriaResource){
        log.info("Received request for expiring message counter");
        SearchCriteria searchCriteria = SearchCriteriaResourceMapper.MAPPER.map(searchCriteriaResource);
        String ret = String.valueOf(messageService.getExpiringCorrespondences(authentication.getName(),
                AuthorityUtils.authorityListToSet(authentication.getAuthorities()), searchCriteria.getFilterCriteria()));
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

}
