/*
 * $Id:: ApplicationController.java 2021/03/02 02:09 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.ws.controller;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.ExcelService;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.DraftResponse;
import eu.euipo.etmdn.userarea.common.domain.DuplicateApplicationResponse;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.Receipt;
import eu.euipo.etmdn.userarea.common.domain.ResumeDraftResponse;
import eu.euipo.etmdn.userarea.common.domain.document.FileResponse;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceRequest;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceResponse;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidationStatusEServiceType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import eu.euipo.etmdn.userarea.domain.note.NoteApplication;
import eu.euipo.etmdn.userarea.ws.domain.application.ApplicationRequestResource;
import eu.euipo.etmdn.userarea.ws.domain.application.ApplicationSearchResultResource;
import eu.euipo.etmdn.userarea.ws.mapper.application.ApplicationSearchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

/**
 * The Application controller.
 */
@Slf4j
@RestController
@Secured({"ROLE_TRADEMARKS", "ROLE_DESIGNS","ROLE_ADMINISTRATOR"})
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final AccountService accountService;
    private final ExcelService excelService;
    private final PdfService pdfService;

    /**
     * Instantiates Application controller.
     *
     * @param applicationService the application service
     * @param accountService     the account service
     * @param excelService       the excel service
     * @param pdfService         the pdf service
     */
    @Autowired
    public ApplicationController(final ApplicationService applicationService, final AccountService accountService,
                                 final ExcelService excelService, final PdfService pdfService) {
        this.applicationService = applicationService;
        this.excelService = excelService;
        this.pdfService = pdfService;
        this.accountService = accountService;
    }

    /**
     * Get Pageable and Sortable Application details.
     *
     * @param applicationType    the application type
     * @param applicationRequestResource the application request
     * @param authentication     the Authentication authentication
     * @return {@link Page<ApplicationDetails>} the applications
     */
    @PostMapping("/{applicationType}")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasAnyRole('ROLE_TRADEMARKS','ROLE_DESIGNS') and #applicationType == 'eservice') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<ApplicationSearchResultResource> getApplications(@PathVariable String applicationType,
                                                                           @RequestBody ApplicationRequestResource applicationRequestResource,
                                                                           Authentication authentication) {
        String username = authentication.getName();
        if (!accountService.isMainAccount(username)) {
            DomainAccount domainAccount = accountService.getMainAccount(username);
            if (domainAccount != null) {
                username = domainAccount.getUsername();
            }
        }
        applicationRequestResource.setUserName(username);
        applicationRequestResource.setApplicationType(applicationType);
        ApplicationRequest applicationRequest = ApplicationSearchMapper.MAPPER.map(applicationRequestResource);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequest, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        return ResponseEntity.ok(ApplicationSearchMapper.MAPPER.map(applicationSearchResult));
    }

    /**
     * Get Application receipt in pdf format.
     *
     * @param id the application id
     * @return {@link Receipt} the pdf
     */
    @GetMapping("/{applicationType}/{ipRightType}/{id}/receipt")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<Receipt> getReceipt(@PathVariable String applicationType, @PathVariable String ipRightType,
                                              @PathVariable String id, Authentication authentication) {
        String receipt = applicationService.getReceipt(authentication.getName(), Long.valueOf(id));
        if (receipt != null) {
            return ResponseEntity.ok(Receipt.builder().applicationId(id).receipt(receipt).build());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get application receipt directly from frontoffice
     */
    @GetMapping(value = "/{applicationType}/{ipRightType}/{applicationNumber}/receipt")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<byte[]> getDraftReceiptFromFO(@PathVariable String applicationType, @PathVariable String ipRightType,
                                                        @PathVariable String applicationNumber, @RequestParam(required = false) boolean isDraft,
                                                        Authentication authentication) {
        log.info("Requesting receipt for application number: {}", applicationNumber);
        boolean isDraftParam = Optional.of(isDraft).orElse(false);
        FileResponse fileResponse = applicationService.getReceiptFromFrontoffice(applicationNumber, isDraftParam);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFileName() + "\"")
                .contentLength(fileResponse.getBytes().length)
                .contentType(MediaType.parseMediaType(fileResponse.getContentType()))
                .body(fileResponse.getBytes());
    }

    /**
     * Get Application invoice in pdf format.
     *
     * @param applicationType the application type
     * @param ipRightType the application ipRightType
     * @param id the application id
     * @return the pdf
     */
    @GetMapping(value = "/{applicationType}/{ipRightType}/{id}/invoice")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<byte[]> getApplicationInvoice(@PathVariable String applicationType, @PathVariable String ipRightType,
                                                         @PathVariable String id, Authentication authentication) {
        FileInfo fileInfo = applicationService.getInvoice(authentication.getName(),Long.valueOf(id));
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                .contentLength(fileInfo.getFileContent().length)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(fileInfo.getFileContent());
    }

    /**
     * Delete draft.
     *
     * @param id the draft id
     * @return {@link DraftResponse} the draft response
     */
    @DeleteMapping("/{applicationType}/{ipRightType}/{id}")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs')")
    public ResponseEntity<DraftResponse> delete(@PathVariable String applicationType, @PathVariable String ipRightType,
                                                @PathVariable String id, Authentication authentication) {
        boolean isDeleted = applicationService.delete(authentication.getName(), Long.valueOf(id));
        if (isDeleted) {
            return ResponseEntity.ok(DraftResponse.builder().id(id).deleted(true).build());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Lock draft.
     *
     * @param id the draft id
     * @return {@link DraftResponse} the draft response
     */
    @PostMapping("/{applicationType}/{ipRightType}/{id}/lock")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs')")
    public ResponseEntity<DraftResponse> lock(@PathVariable String applicationType, @PathVariable String ipRightType,
                                              @PathVariable String id, Authentication authentication) {
        boolean isLocked = applicationService.lock(authentication.getName(), Long.valueOf(id));
        if (isLocked) {
            return ResponseEntity.ok(DraftResponse.builder().id(id).locked(true).build());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Unlock draft.
     *
     * @param id the draft id
     * @return {@link DraftResponse} the draft response
     */
    @PostMapping("/{applicationType}/{ipRightType}/{id}/unlock")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs')")
    public ResponseEntity<DraftResponse> unlock(@PathVariable String applicationType, @PathVariable String ipRightType,
                                                @PathVariable String id, Authentication authentication) {
        boolean isLocked = applicationService.unlock(authentication.getName(), Long.valueOf(id));
        if (isLocked) {
            return ResponseEntity.ok(DraftResponse.builder().id(id).locked(false).build());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Lock application note when note is edited ny user.
     *
     * @param noteApplication the application note
     * @return {@link String} the note
     */
    @PostMapping("/{applicationType}/{ipRightType}/note/lock")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs')")
    public ResponseEntity<NoteApplication> lockNote(@PathVariable String applicationType, @PathVariable String ipRightType,
                                                    @RequestBody NoteApplication noteApplication, Authentication authentication) {
        String applicationNote = applicationService.lockNote(authentication.getName(), noteApplication);
        if (applicationNote != null) {
            return ResponseEntity.ok(NoteApplication.builder().note(applicationNote).build());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Update note for a specific application.
     *
     * @return {@link String} the note
     */
    @PostMapping("/{applicationType}/{ipRightType}/note")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs')")
    public ResponseEntity<NoteApplication> updateNote(@PathVariable String applicationType, @PathVariable String ipRightType,
                                           @RequestBody NoteApplication noteApplication, Authentication authentication) {
        String applicationNote = applicationService.updateNote(authentication.getName(), noteApplication);
        if (applicationNote != null) {
            return ResponseEntity.ok(NoteApplication.builder().applicationIdentifier(noteApplication.getApplicationIdentifier()).note(applicationNote).build());
        }
        return ResponseEntity.notFound().build();
    }


    /**
     * Resume draft.
     *
     * @param id the draft database id
     * @return {@link }
     */
    @GetMapping("/{applicationType}/{ipRightType}/{id}/resume")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasRole('ROLE_TRADEMARKS') and #applicationType == 'eservice' and #ipRightType == 'trademarks') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'eservice' and #ipRightType == 'designs') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<ResumeDraftResponse> resume(@PathVariable String applicationType, @PathVariable String ipRightType,
                                                      @PathVariable Long id, Authentication authentication) {
        Optional<String> resumeDraftUrl = applicationService.getResumeDraftUrl(authentication.getName(), id);
        return resumeDraftUrl.map(url -> ResponseEntity.ok()
                .body(ResumeDraftResponse.builder()
                        .status(HttpStatus.OK.toString())
                        .url(url)
                        .build())).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Duplicate Application
     *
     * @param id the draft database id
     * @return {@link }
     */
    @GetMapping("/{applicationType}/{id}/duplicate")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or (hasRole('ROLE_DESIGNS') and #applicationType == 'design') ")
    public ResponseEntity<DuplicateApplicationResponse> duplicate(@PathVariable String applicationType, @PathVariable Long id, Authentication authentication) {
        Optional<String> resumeDraftUrl = applicationService.getDuplicateApplicationUrl(authentication.getName(), id);
        return resumeDraftUrl.map(url -> ResponseEntity.ok()
                .body(DuplicateApplicationResponse.builder()
                        .status(HttpStatus.OK.toString())
                        .url(url)
                        .build())).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Generate excel report for applications.
     *
     * @param applicationType    the application type
     * @param applicationRequestResource the application request
     * @param authentication     the Authentication authentication
     * @return {@link Resource} the generated excel report
     */
    @PostMapping("/{applicationType}/excel")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasAnyRole('ROLE_TRADEMARKS','ROLE_DESIGNS') and #applicationType == 'eservice') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<Resource> generateExcel(@PathVariable String applicationType, @RequestBody ApplicationRequestResource applicationRequestResource,
                                                  Authentication authentication) {
        String username = authentication.getName();
        if (!accountService.isMainAccount(username)) {
            DomainAccount domainAccount = accountService.getMainAccount(username);
            if (domainAccount != null) {
                username = domainAccount.getUsername();
            }
        }
        applicationRequestResource.setUserName(username);
        applicationRequestResource.setApplicationType(applicationType);
        ApplicationRequest applicationRequest = ApplicationSearchMapper.MAPPER.map(applicationRequestResource);
        final FileInfo fileInfo = excelService.generateExcel(applicationRequest, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        if (fileInfo != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .contentLength(fileInfo.getFileContent().length)
                    .body(new ByteArrayResource(fileInfo.getFileContent()));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Generate pdf report for applications.
     *
     * @param applicationType    the application type
     * @param applicationRequestResource the application request
     * @param authentication     the Authentication authentication
     * @return {@link Resource} the generated excel report
     */
    @PostMapping("/{applicationType}/pdf")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or " +
            "(hasRole('ROLE_DESIGNS') and #applicationType == 'design') or " +
            "(hasAnyRole('ROLE_TRADEMARKS','ROLE_DESIGNS') and #applicationType == 'eservice') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<Resource> generatePdf(@PathVariable String applicationType,
                                                @RequestBody ApplicationRequestResource applicationRequestResource,
                                                Authentication authentication) {
        String username = authentication.getName();
        if (!accountService.isMainAccount(username)) {
            DomainAccount domainAccount = accountService.getMainAccount(username);
            if (domainAccount != null) {
                username = domainAccount.getUsername();
            }
        }
        applicationRequestResource.setUserName(username);
        applicationRequestResource.setApplicationType(applicationType);
        ApplicationRequest applicationRequest = ApplicationSearchMapper.MAPPER.map(applicationRequestResource);
        final FileInfo fileInfo = pdfService.generatePdf(applicationRequest, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        if (fileInfo != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(fileInfo.getFileContent().length)
                    .body(new ByteArrayResource(fileInfo.getFileContent()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{applicationType}/eservices/validate")
    public ResponseEntity<ValidateEServiceResponse> validateEservice(@RequestBody @Valid ValidateEServiceRequest validateEServiceRequest, Authentication authentication) {
        ValidateEServiceResponse validateEServiceResponse = applicationService.validateEService(authentication.getName(), validateEServiceRequest);
        if (validateEServiceResponse.getStatus().equals(ValidationStatusEServiceType.INVALID)) {
            return ResponseEntity.badRequest().body(validateEServiceResponse);
        }
        return ResponseEntity.ok(validateEServiceResponse);
    }

}
