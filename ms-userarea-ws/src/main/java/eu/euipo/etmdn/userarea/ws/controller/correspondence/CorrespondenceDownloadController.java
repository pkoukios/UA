/*
 * $Id:: CorrespondenceDownloadController.java 2021/04/02 12:09 tantonop
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

import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/correspondences/download")
@AllArgsConstructor
public class CorrespondenceDownloadController {

    private PdfService pdfService;

    /**
     * Downloads an incoming message as PDF
     * @param authentication the authenticated user
     * @param messageId the incoming message id
     * @return the incoming message as pdf
     */
    @GetMapping(value = "/incoming/{messageId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<byte[]> getZip(Authentication authentication, @PathVariable String messageId){
        log.info("Generating pdf for message {}",messageId);
        FileInfo fileInfo = pdfService.generatePdf(authentication.getName(),messageId,false);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                .contentLength(fileInfo.getFileContent().length)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(fileInfo.getFileContent());
    }

    /**
     * Downloads an incoming message as PDF
     * @param authentication the authenticated user
     * @param draftId the incoming draft id
     * @return the incoming message as pdf
     */
    @GetMapping(value = "/draft/{draftId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('ROLE_CORRESPONDENCE_READ_ONLY') or hasRole('ROLE_CORRESPONDENCE_READ_WRITE') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<byte[]> getDraftPdf(Authentication authentication, @PathVariable String draftId){
        log.info("Generating pdf for draft {}",draftId);
        FileInfo fileInfo = pdfService.generatePdf(authentication.getName(),draftId,true);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                .contentLength(fileInfo.getFileContent().length)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(fileInfo.getFileContent());
    }
}
