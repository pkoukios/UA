/*
 * $Id:: AttachmentController.java 2021/04/02 10:54 tantonop
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

import eu.euipo.etmdn.userarea.common.business.correspondence.MessageAttachmentService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.DraftAttachment;
import eu.euipo.etmdn.userarea.common.domain.document.FileResponse;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.DraftAttachmentResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.DraftAttachmentResourceMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/correspondences/attachments")
@AllArgsConstructor
public class AttachmentController {

    private MessageAttachmentService messageAttachmentService;

    /**
     * creates draft attachments
     * @param authentication the authenticated user
     * @param draftId the draft id
     * @param files the files to be saved
     * @return a list of the files created
     */
    @PostMapping("/{draftId}")
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_WRITE')")
    public ResponseEntity<List<DraftAttachmentResource>> createAttachment(Authentication authentication, @PathVariable String draftId, @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        int numberOfFiles = files != null ? files.size() : 0;
        log.info("Received {} attachments for draft with id{}", draftId, numberOfFiles);
        List<DraftAttachment> draftAttachments = messageAttachmentService.createDraftAttachments(authentication.getName(),draftId,files);
        List<DraftAttachmentResource> ret = draftAttachments.stream().map(draftAttachment -> DraftAttachmentResourceMapper.MAPPER.map(draftAttachment)).collect(Collectors.toList());
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    /**
     * Retrieves an attachment
     * @param authentication the authenticated user
     * @param attachmentId the attachment id
     * @return the binary attachment data
     */
    @GetMapping(value = "/{attachmentId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<byte[]> getAttachment(Authentication authentication, @PathVariable String attachmentId){
        log.info("Requesting attachment with id {}",attachmentId);
        FileResponse fileResponse = messageAttachmentService.getAttachment(authentication.getName(),attachmentId, true);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFileName() + "\"")
                .contentLength(fileResponse.getBytes().length)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(fileResponse.getBytes());
    }

    /**
     * Retrieves a draft attachment
     * @param authentication
     * @param draftAttachmentId
     * @return the binary draft attachment data
     */
    @GetMapping(value = "/draft/{draftAttachmentId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<byte[]> getDraftAttachment(Authentication authentication, @PathVariable String draftAttachmentId) {
        log.info("Requesting draft attachment with id {}", draftAttachmentId);
        FileResponse fileResponse = messageAttachmentService.getAttachment(authentication.getName(), draftAttachmentId, false);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFileName() + "\"")
                .contentLength(fileResponse.getBytes().length)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(fileResponse.getBytes());
    }

    /**
     * deletes an attachment
     * @param authentication the authenticated user
     * @param attachmentId the attachment id
     * @return ok response that attachment has been deleted
     */
    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_WRITE')")
    public ResponseEntity<Void> deleteAttachment(Authentication authentication, @PathVariable String attachmentId){
        messageAttachmentService.deleteAttachment(authentication.getName(),attachmentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
