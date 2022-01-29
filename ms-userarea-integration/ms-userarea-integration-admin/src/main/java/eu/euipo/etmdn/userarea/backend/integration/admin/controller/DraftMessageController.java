/*
 * $Id:: DraftController.java 2021/06/30 12:19 dvelegra
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

package eu.euipo.etmdn.userarea.backend.integration.admin.controller;

import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/drafts")
@AllArgsConstructor
public class DraftMessageController {

    private PdfService pdfService;

    /**
     * Returns all the attachments of the draft.
     *
     * @return the draft attachments
     */
    @GetMapping(value = "/download")
    public ResponseEntity<FileInfo> getDraftAttachments(@RequestParam("username") String username,
                                                        @RequestParam("draftId") String draftId) {
        return new ResponseEntity<>(this.pdfService.generatePdf(username, draftId, true), HttpStatus.OK);
    }

}
