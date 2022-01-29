/*
 * $Id:: ApplicationService.java 2021/03/02 02:09 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.api.service;

import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.document.FileResponse;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceRequest;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceResponse;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import eu.euipo.etmdn.userarea.domain.note.NoteApplication;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Application Interface for handling applications
 */
public interface ApplicationService {

    /**
     * Save a list of applications
     *
     * @param applications the list of applications
     */
    void saveAll(List<Application> applications);

    /**
     * Get Pageable and Sortable Application details.
     *
     * @param applicationRequest the trademark application request
     * @param roles              the logged in user's set of roles
     * @return {@link Page<Application>} the pageable application response entity
     */
    ApplicationSearchResult getApplications(final ApplicationRequest applicationRequest, Set<String> roles);

    /**
     * Get applications by fomodule and status.
     *
     * @param applicationType the applicationType
     * @param statusList      the collection with statuses
     * @return {@link Page<Application>} the list of applications
     */
    Page<Application> getApplicationsByFoModuleAndStatus(ApplicationRequest applicationRequest, ApplicationType applicationType, Collection<String> statusList);

    /**
     * Delete draft application.
     *
     * @param username the username
     * @param id       the draft application id
     * @return true if the draft is marked as deleted otherwise false
     */
    boolean delete(String username, final Long id);

    /**
     * Get Application/Draft receipt.
     *
     * @param username the username
     * @param id       the application id
     * @return String the application receipt in pdf format
     */
    String getReceipt(String username, final Long id);

    /**
     * Get resume url for draft application.
     *
     * @param username the username
     * @param id       the draft application id
     * @return String the FO resume url for the specified draft application
     */
    Optional<String> getResumeDraftUrl(String username, final Long id);

    /**
     * Lock draft application.
     *
     * @param username the username
     * @param id       the draft id
     * @return true if the draft is marked as locked otherwise false
     */
    boolean lock(String username, final Long id);

    /**
     * Unlock draft application.
     *
     * @param username the username
     * @param id       the draft id
     * @return true if the draft is marked as unlocked otherwise false
     */
    boolean unlock(String username, final Long id);

    /**
     * Lock Application note.
     *
     * @param username      the username
     * @param noteApplication the application note
     * @return String the application note
     */
    String lockNote(String username, final NoteApplication noteApplication);


    /**
     * Update Application note.
     *
     * @param username      the username
     * @param noteApplication the application note
     * @return String the application note
     */
    String updateNote(String username, final NoteApplication noteApplication);

    /**
     * Get Duplicate application url
     *
     * @param username the username
     * @param id       the application id
     * @return String the FO duplicate url for the specified application
     */
    Optional<String> getDuplicateApplicationUrl(String username, final Long id);

    /**
     * Validate initiate eService request
     *
     * @param username                the username
     * @param validateEServiceRequest The request eservice object containing the application ids
     * @return ValidateEServiceResponse The result of validation as response object
     */
    ValidateEServiceResponse validateEService(String username, final ValidateEServiceRequest validateEServiceRequest);

    /**
     * Get Initiate Eservice application url
     *
     * @param applications the list of applications
     * @param eServiceType the type of eservice
     * @return String the FO resume url for the specified draft application
     */
    Optional<String> getInitiateEservicetUrl(final List<Application> applications, String eServiceType);

    /**
     * Retrieve applications based on the number, type and status.
     *
     * @param username the logged in username
     * @param roles    the logged in user's set of roles
     * @return {@link List<Application>} the applications
     */
    List<Application> getApplicationsForSignatures(String username, final Set<String> roles);

    /**
     * Delete application.
     *
     * @param username the username
     * @param number   the application number
     */
    void deleteApplication(String username, final String number);

    /**
     * Modify application.
     *
     * @param username the username
     * @param number   the application number
     * @return the resume url if the application is modified successfully otherwise null
     */
    String modifyApplication(String username, final String number);

    Application getByIdAndLock(Long id, String username);

    Application updateAndReleaseLock(Long id, String username);

    List<Application> getApplicationsByNumberAndLock(String applicationNumber, String username);

    List<Application> updateAndReleaseApplicationsLock(List<Application> applications, String username);

    List<Application> getApplicationsByIds(List<Long> applicationIds);

    List<Application> getApplicationsByNumber(List<String> applicationIds);

    void save(Application application);

    List<Application> findByNumber(String applicationId);

    FileResponse getReceiptFromFrontoffice(String applicationNumber, boolean isDraft);

    FileInfo getInvoice(String username,Long id);
}
