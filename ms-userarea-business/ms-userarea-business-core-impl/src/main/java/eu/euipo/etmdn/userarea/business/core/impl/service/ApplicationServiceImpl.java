/*
 * $Id:: ApplicationServiceImpl.java 2021/03/02 02:09 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.service;


import org.springframework.context.annotation.Lazy;
import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.NoteService;
import eu.euipo.etmdn.userarea.business.core.api.service.PaymentService;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.business.config.IpoConfiguration;
import eu.euipo.etmdn.userarea.common.business.event.AuditEventPublisher;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.business.utils.LockUtils;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.auditlog.AuditType;
import eu.euipo.etmdn.userarea.common.domain.document.FileResponse;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceRequest;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceResponse;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidationStatusEServiceType;
import eu.euipo.etmdn.userarea.common.domain.exception.EntityNotFoundException;
import eu.euipo.etmdn.userarea.common.domain.exception.FrontofficeReceiptNotFoundException;
import eu.euipo.etmdn.userarea.common.domain.exception.FrontofficeServerException;
import eu.euipo.etmdn.userarea.common.domain.exception.ServiceUnavailableException;
import eu.euipo.etmdn.userarea.common.domain.exception.ValidateEserviceException;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.common.persistence.entity.QualifiedService;
import eu.euipo.etmdn.userarea.common.persistence.repository.ApplicationRepository;
import eu.euipo.etmdn.userarea.common.persistence.repository.QualifiedServiceRepository;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import eu.euipo.etmdn.userarea.domain.application.SearchCriteriaApplication;
import eu.euipo.etmdn.userarea.domain.note.NoteApplication;
import eu.euipo.etmdn.userarea.persistence.entity.note.NoteApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import eu.euipo.etmdn.userarea.persistence.mapper.note.NoteApplicationMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.buildResumeUrl;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.hasDesignRole;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.hasTrademarkRole;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.SignatureUtils.deleteSignaturesInFO;
import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.ESERVICE_INITIATE_VALIDATION_FAIL;
import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.ESERVICE_INITIATE_VALIDATION_MULTIPLICITY_FAIL;
import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.ESREVICE_INITIATE_VALIDATION_VALID;
import static eu.euipo.etmdn.userarea.common.persistence.specification.ApplicationSpecification.filterByStatuses;
import static eu.euipo.etmdn.userarea.common.persistence.specification.ApplicationSpecification.filterByUsername;
import static eu.euipo.etmdn.userarea.common.persistence.specification.ApplicationSpecification.filterSignatures;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.ALL;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.APPLICATION_DRAFT_STATUS_INITIALIZED;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.DS_EFILING_URL;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.DUPLICATE_ID_REQUEST_PARAM;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.FO_URL;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.INITIATE_REQUEST_PARAM;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.INITIATE_TYPE_REQUEST_PARAM;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.IP_RIGHT_TYPE_DESIGNS;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.IP_RIGHT_TYPE_TRADEMARKS;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.ONE;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.TM_EFILING_URL;

/**
 * The Application service.
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private QualifiedServiceRepository qualifiedServiceRepository;
    @Autowired
    private NoteService noteService;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private IpoConfiguration ipoConfiguration;
    @Autowired
    private Environment env;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AuditEventPublisher auditEventPublisher;
    @Autowired
    @Lazy
    private PaymentService paymentService;
    @Autowired
    @Lazy
    private PdfService pdfService;
    @Value("${userarea.globals.ipo.fo.url}")
    private String frontofficeUrl;
    @Value("${userarea.signature.frontoffice.receiptEndpoint}")
    private String frontofficeReceiptEndpoint;
    @Value("${userarea.signature.frontoffice.deleteEndpoint}")
    private String frontofficeSignatureDeleteEndpoint;
    @Value("${userarea.applications.service}")
    private String myApplicationsService;
    @Value("${userarea.applications.applicationsEndPoint}")
    private String myApplicationsEndpoint;

    /**
     * Save an application to the database
     *
     * @param application The application entity
     */
    @Override
    public void save(Application application) {
        applicationRepository.save(application);
    }

    /**
     * Save a list of applications
     *
     * @param applications the list of applications
     */
    @Override
    public void saveAll(List<Application> applications) {
        applicationRepository.saveAll(applications);
    }

    /**
     * Get Pageable and Sortable Trademark details.
     *
     * @param applicationRequest the trademark application request
     * @param roles              the logged in user's set of roles
     * @return {@link ApplicationSearchResult} the pageable trademark application response entity
     */
    @Override
    public ApplicationSearchResult getApplications(final ApplicationRequest applicationRequest, final Set<String> roles) {
        log.info("Get pageable applications");
        SearchCriteriaApplication searchCriteriaApplication = new SearchCriteriaApplication();
        searchCriteriaApplication.setApplicationType(applicationRequest.getApplicationType());
        searchCriteriaApplication.setColumns(applicationRequest.getColumns());
        searchCriteriaApplication.setFilteringData(applicationRequest.getFilteringData());
        searchCriteriaApplication.setIsDraft(applicationRequest.getIsDraft());
        searchCriteriaApplication.setPaginationData(applicationRequest.getPaginationData());
        searchCriteriaApplication.setSearchingData(applicationRequest.getSearchingData());
        searchCriteriaApplication.setSortingData(applicationRequest.getSortingData());
        searchCriteriaApplication.setUserName(applicationRequest.getUserName());
        searchCriteriaApplication.setRoles(new ArrayList<>(roles));
        ApplicationSearchResult applicationSearchResult;
        //TODO: enable back once security is working
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String token = "";
//        if(authentication!=null) {
//            DefaultOidcUser principal = (DefaultOidcUser) authentication.getPrincipal();
//            token = principal.getIdToken().getTokenValue();
//        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //TODO: enable back once security is working
       // headers.setBearerAuth(token);
        HttpEntity<SearchCriteriaApplication> entity = new HttpEntity<>(searchCriteriaApplication, headers);
        try {
            log.info("Calling myApplications ms to fetch application details");
            applicationSearchResult = restTemplate.postForObject(myApplicationsService + myApplicationsEndpoint, entity, ApplicationSearchResult.class);
        }
        catch(Exception ex){
            log.error("Service Unavailable Exception error while calling myApplications ms to fetch application details");
            throw new ServiceUnavailableException("Failed to fetch data from "+myApplicationsService + myApplicationsEndpoint);
        }
        if(applicationSearchResult != null  && applicationSearchResult.getContent() != null) {
            List<ApplicationDetails> applicationDetailsList = applicationSearchResult.getContent();
            List<NoteApplication> noteApplicationList;
            Map<String, String> applicationNotesMap = new HashMap<>();
            List<String> applicationNumbers = applicationSearchResult.getContent().stream()
                    .map(ApplicationDetails::getUniqueNumber)
                    .collect(Collectors.toList());
            List<NoteApplicationEntity> noteApplicationEntityList = noteService.getNotesByApplicationNumbers(new ArrayList<>(applicationNumbers));
            if (CollectionUtils.isNotEmpty(noteApplicationEntityList)) {
                noteApplicationList = NoteApplicationMapper.MAPPER.map(noteApplicationEntityList);
                applicationNotesMap = noteApplicationList.stream().collect(Collectors.toMap(NoteApplication::getApplicationIdentifier, NoteApplication::getNote));
            }
            for (ApplicationDetails applicationDetails : applicationDetailsList) {
                applicationDetails.setNote(applicationNotesMap.get(applicationDetails.getUniqueNumber()));
            }
        }
        return applicationSearchResult;
    }

    /**
     * Get applications by fomodule and status.
     *
     * @param applicationType the applicationType
     * @param statusList      the collection with statuses
     * @return {@link List<Application>} the list of applications
     */
    @Override
    public Page<Application> getApplicationsByFoModuleAndStatus(final ApplicationRequest applicationRequest, ApplicationType applicationType, Collection<String> statusList) {
        log.info("Get applications by foModule and status");
        Pageable pageable = PageRequest.of(applicationRequest.getPaginationData().getPage(), applicationRequest.getPaginationData().getSize(),
                Sort.by(Sort.Direction.fromString(applicationRequest.getSortingData().getSortType()), applicationRequest.getSortingData().getSortColumn()));
        return applicationRepository.findByFoModuleAndStatusIsIn(applicationType.value, statusList, pageable);
    }

    /**
     * Get Application receipt.
     *
     * @param username the username
     * @param id       the applicationId
     * @return String the application receipt in pdf format
     */
    @Override
    public String getReceipt(String username, final Long id) {
        log.info("Get application receipt from UA database with id=[{}]", id);
        username = username.trim();
        Application application = applicationRepository.findById(id).orElse(null);
        return application != null && accountService.isAllowedToModifyApplication(username, application.getMainAccount().getUsername()) ?
                application.getReceipt() : null;
    }

    /**
     * Lock Application note.
     *
     * @param username      the username
     * @param noteApplication the application note
     * @return String the application note
     */
    @Override
    public String lockNote(String username, final NoteApplication noteApplication) {
        log.info("Lock application note with application identifier=[{}] ", noteApplication.getApplicationIdentifier());
        username = username.trim();
        NoteApplication noteApplicationToBeLocked = NoteApplicationMapper.MAPPER.map(noteService.getNoteByApplicationNumberAndLock(username, noteApplication.getApplicationIdentifier()));
        return noteApplicationToBeLocked.getNote();
    }

    /**
     * Update Application note.
     *
     * @param username the username
     * @param noteApplication the application note
     * @return String the application note
     */
    @Override
    public String updateNote(String username, final NoteApplication noteApplication) {
        log.info("Update application note with application identifier=[{}] ", noteApplication.getApplicationIdentifier());
        username = username.trim();
        NoteApplication noteApplicationToBeUpdated;
        NoteApplicationEntity noteApplicationEntity;
        noteApplicationEntity = noteService.getNoteByApplicationNumber(noteApplication.getApplicationIdentifier());
        noteApplicationToBeUpdated = NoteApplicationMapper.MAPPER.map(noteApplicationEntity);
        if (noteApplicationToBeUpdated == null) {
            noteApplicationToBeUpdated = new NoteApplication();
            noteApplicationToBeUpdated.setNote(noteApplication.getNote());
            noteApplicationEntity = NoteApplicationMapper.MAPPER.map(noteApplicationToBeUpdated);
            noteApplicationEntity.setLastModifiedBy(username);
            noteApplicationEntity.setApplicationNumber(noteApplication.getApplicationIdentifier());
            noteService.save(noteApplicationEntity);
            return noteApplication.getNote();
        }
        noteService.updateAndReleaseLock(username, noteApplicationEntity, noteApplication.getNote());
        return noteApplicationToBeUpdated.getNote();
    }

    /**
     * Get the resume url for a draft application.
     *
     * @param username the username
     * @param id       the draft id
     * @return String the redirect url to FO that will resume the draft
     */
    @Override
    public Optional<String> getResumeDraftUrl(String username, final Long id) {
        log.info("Resume draft application with id=[{}]", id);
        username = username.trim();
        Application draft = applicationRepository.findByIdAndStatusIsIn(id, Arrays.asList(StringUtils.splitPreserveAllTokens(applicationConfiguration.getStatus().getDraft(), ",")));
        if(draft == null){
            throw new EntityNotFoundException("Application not found");
        }
        Application app = this.getByIdAndLock(draft.getId(), username);
        if(accountService.isAllowedToModifyApplication(username, app.getMainAccount().getUsername())){
            return Optional.of(buildResumeUrl(draft, ipoConfiguration, env));
        }
        return Optional.empty();
    }

    /**
     * Get Duplicate application url
     *
     * @param username the username
     * @param id       the application id
     * @return String the FO duplicate url for the specified application
     */
    @Override
    public Optional<String> getDuplicateApplicationUrl(String username, final Long id) {
        log.info("Retrieve duplicate application with id=[{}]", id);
        username = username.trim();
        Application application = applicationRepository.findById(id).orElse(null);
        String redirectUrl = StringUtils.EMPTY;
        if (application != null && accountService.isAllowedToModifyApplication(username, application.getMainAccount().getUsername())) {
            if (ApplicationType.TRADEMARK.value.equalsIgnoreCase(application.getFoModule())) {
                redirectUrl = String.join(StringUtils.EMPTY, ipoConfiguration.getIpo().get(FO_URL),
                        ipoConfiguration.getIpo().get(TM_EFILING_URL), DUPLICATE_ID_REQUEST_PARAM, application.getNumber());
            } else if (ApplicationType.DESIGN.value.equalsIgnoreCase(application.getFoModule())
                    && ApplicationUtils.isDraftStatus(applicationConfiguration,application)) {
                    redirectUrl = String.join(StringUtils.EMPTY, ipoConfiguration.getIpo().get(FO_URL),
                            ipoConfiguration.getIpo().get(DS_EFILING_URL), DUPLICATE_ID_REQUEST_PARAM, application.getNumber());
            }
        }
        return Optional.of(redirectUrl);
    }

    /**
     * Delete draft application.
     *
     * @param username the username
     * @param id       the draft id
     * @return true if the draft is marked as deleted otherwise false
     */
    @Override
    public boolean delete(String username, final Long id) {
        log.info("Delete application with id=[{}]", id);
        username = username.trim();
        Application draft = applicationRepository.findByIdAndStatusIsIn(id, Arrays.asList(StringUtils.splitPreserveAllTokens(applicationConfiguration.getStatus().getDraft(), ",")));
        if (draft != null) {
            Application app = this.getByIdAndLock(draft.getId(), username);
            if (app != null && accountService.isAllowedToModifyApplication(username, app.getMainAccount().getUsername())) {
                List<Application> applicationList = applicationRepository.findByNumber(app.getNumber());
                applicationList.forEach(application -> application.setDeleted(true));
                applicationRepository.saveAll(applicationList);
                auditEventPublisher.publishCustomEvent("Delete draft application " + app.getNumber(),username, AuditType.APPLICATION.getValue());
                return true;
            }
        }
        return false;
    }

    /**
     * Lock draft.
     *
     * @param username the username
     * @param id       the draft id
     * @return true if the draft is marked as locked otherwise false
     */
    @Override
    public boolean lock(String username, final Long id) {
        log.info("Lock application with id=[{}]", id);
        username = username.trim();
        Application draft = applicationRepository.findByIdAndStatusIsIn(id, Arrays.asList(StringUtils.splitPreserveAllTokens(applicationConfiguration.getStatus().getDraft(), ",")));
        if (draft != null && accountService.isAllowedToModifyApplication(username, draft.getMainAccount().getUsername())) {
            List<Application> applicationList = applicationRepository.findByNumber(draft.getNumber());
            applicationList.forEach(application -> application.setLocked(true));
            applicationRepository.saveAll(applicationList);
            return true;
        }
        return false;
    }

    /**
     * Unlock draft.
     *
     * @param username the username
     * @param id       the draft id
     * @return true if the draft is marked as unlocked otherwise false
     */
    @Override
    public boolean unlock(String username, final Long id) {
        log.info("Unlock application with id=[{}]", id);
        username = username.trim();
        Application draft = applicationRepository.findByIdAndStatusIsIn(id, Arrays.asList(StringUtils.splitPreserveAllTokens(applicationConfiguration.getStatus().getDraft(), ",")));
        if (draft != null && accountService.isAllowedToModifyApplication(username, draft.getMainAccount().getUsername())) {
            List<Application> applicationList = applicationRepository.findByNumber(draft.getNumber());
            applicationList.forEach(application -> application.setLocked(false));
            applicationRepository.saveAll(applicationList);
            return true;
        }
        return false;
    }

    /**
     * Validate initiate eService request
     *
     * @param username                the username
     * @param validateEServiceRequest The request eservice object containing the application ids
     * @return ValidateEServiceResponse The result of validation as response object
     */
    @Override
    public ValidateEServiceResponse validateEService(String username, final ValidateEServiceRequest validateEServiceRequest) {
        log.info("Validate eservices request");
        // Instantiate the invalidApplications list
        Set<Long> invalidApplicationIds = new HashSet<>();
        Set<String> invalidApplicationNumbers = new HashSet<>();
        // Retrieve requested application
        List<Application> requestedApplicationsForEservice = applicationRepository.findAllById(validateEServiceRequest.getApplicationIds());
        // Implement validation logic
        // 1. Load validation rule from database
        QualifiedService qualifiedServiceRule = qualifiedServiceRepository.findFirstByService(validateEServiceRequest.getEserviceType());
        if (qualifiedServiceRule == null) {
            throw new ValidateEserviceException("No validation rule available for the specified EserviceType");
        }
        // 2. Validate multiplicity
        String allowedMultiplicity = qualifiedServiceRule.getMultiplicity();
        if (ONE.equalsIgnoreCase(allowedMultiplicity) && requestedApplicationsForEservice.size() > 1) {
            return ValidateEServiceResponse.builder()
                    .status(ValidationStatusEServiceType.INVALID)
                    .message(ESERVICE_INITIATE_VALIDATION_MULTIPLICITY_FAIL)
                    .multiplicityError(true)
                    .invalidApplicationIds(
                            requestedApplicationsForEservice.stream()
                                    .map(Application::getId)
                                    .collect(Collectors.toSet())
                    )
                    .invalidApplicationNumbers(
                            requestedApplicationsForEservice.stream()
                                    .map(Application::getNumber)
                                    .collect(Collectors.toSet()))
                    .build();
        }
        // 3. Validate Eligibility from status
        String allowedEligibility = qualifiedServiceRule.getEligibility();
        Set<String> eligibleStatuses = new HashSet<>(Arrays.asList(allowedEligibility.split(",")));
        if(!ALL.equals(allowedEligibility)) {
            for (Application application : requestedApplicationsForEservice) {
                if (!eligibleStatuses.contains(application.getStatus().toUpperCase())) {
                    invalidApplicationIds.add(application.getId());
                    invalidApplicationNumbers.add(application.getNumber());
                }
            }
        }
        Optional<String> initiateEservicetUrl = getInitiateEservicetUrl(requestedApplicationsForEservice, validateEServiceRequest.getEserviceType());
        if (invalidApplicationIds.isEmpty()) {
            return ValidateEServiceResponse.builder()
                    .status(ValidationStatusEServiceType.VALID)
                    .message(ESREVICE_INITIATE_VALIDATION_VALID)
                    .multiplicityError(false)
                    .invalidApplicationIds(invalidApplicationIds)
                    .invalidApplicationNumbers(invalidApplicationNumbers)
                    .redirectUrl(initiateEservicetUrl.orElse(StringUtils.EMPTY)).build();
        }
        return ValidateEServiceResponse.builder()
                .status(ValidationStatusEServiceType.INVALID)
                .message(ESERVICE_INITIATE_VALIDATION_FAIL)
                .invalidApplicationIds(invalidApplicationIds)
                .invalidApplicationNumbers(invalidApplicationNumbers)
                .build();
    }

    /**
     * Get Initiate Eservice application url
     *
     * @param applications the application ids
     * @return String the FO resume url for the specified draft application
     */
    @Override
    public Optional<String> getInitiateEservicetUrl(final List<Application> applications, final String eServiceType) {
        log.info("Retrieve initiated eservice url");
        if (applications.isEmpty() || StringUtils.isBlank(eServiceType)) {
            return Optional.empty();
        }
        final String eserviceTypeUrl = eServiceType.toLowerCase().replace("_", ".");
        final String eserviceUrl = env.getProperty("userarea.globals.ipo." + eserviceTypeUrl + ".url");
        String redirectUrl = String.join(StringUtils.EMPTY, ipoConfiguration.getIpo().get(FO_URL), eserviceUrl, INITIATE_REQUEST_PARAM,
                applications.stream()
                        .map(Application::getNumber)
                        .collect(Collectors.joining("-")),
                INITIATE_TYPE_REQUEST_PARAM, Arrays.stream(eServiceType.toLowerCase().split("_", 2))
                        .findFirst().orElseThrow(() -> new ValidateEserviceException(StringUtils.EMPTY)));
        return Optional.of(redirectUrl);
    }

    /**
     * Retrieve applications based on the number, type and status.
     *
     * @param username the logged in username
     * @param roles    the logged in user's set of roles
     * @return {@link List<Application>} the applications
     */
    @Override
    public List<Application> getApplicationsForSignatures(String username, final Set<String> roles) {
        log.info("Retrieve applications for signatures");
        username = username.trim();
        List<String> modules = new ArrayList<>();
        List<String> ipRightTypes = new ArrayList<>();
        if (hasTrademarkRole(roles) && !hasDesignRole(roles)) {
            modules.add(ApplicationType.TRADEMARK.value);
            ipRightTypes.add(IP_RIGHT_TYPE_TRADEMARKS);
        } else if (hasDesignRole(roles) && !hasTrademarkRole(roles)) {
            modules.add(ApplicationType.DESIGN.value);
            ipRightTypes.add(IP_RIGHT_TYPE_DESIGNS);
        } else {
            modules.addAll(Arrays.asList(ApplicationType.TRADEMARK.value, ApplicationType.DESIGN.value));
            ipRightTypes.addAll(Arrays.asList(IP_RIGHT_TYPE_TRADEMARKS, IP_RIGHT_TYPE_DESIGNS));
        }
        Specification<Application> filterSpecs = Specification.where(filterByUsername(accountService.getMainAccount(username).getUsername())
                .and(filterByStatuses(Arrays.asList(applicationConfiguration.getStatus().getSignature(), applicationConfiguration.getStatus().getPayment()))
                        .and(filterSignatures(modules, ipRightTypes))));
        return applicationRepository.findAll(filterSpecs);
    }

    /**
     * Retrieve a list of applications from a list of application numbers
     *
     * @param applicationNumbers the application numbers
     * @return A list of applications
     */
    @Override
    public List<Application> getApplicationsByNumber(List<String> applicationNumbers) {
        log.info("Retrieve applications by number(s)");
        List<Application> applications = applicationRepository.findByNumberIsIn(applicationNumbers);
        if (CollectionUtils.isEmpty(applications)) {
            throw new EntityNotFoundException("Application not found");
        }
        return applications;
    }

    /**
     * Retrieve a list of applications from a list of application numbers
     *
     * @param applicationIds the application Ids
     * @return A list of applications
     */
    @Override
    public List<Application> getApplicationsByIds(List<Long> applicationIds) {
        log.info("Retrieve applications by id(s):[{}]", applicationIds);
        List<Application> applications = applicationRepository.findByIdIsIn(applicationIds);
        if (CollectionUtils.isEmpty(applications)) {
            throw new EntityNotFoundException("Application not found");
    }
        return applications;
    }

    /**
     * Find an application by application number
     *
     * @param applicationNumber the unique identifier across services
     * @return a list of applications
     */
    @Override
    public List<Application> findByNumber(String applicationNumber) {
        log.info("Retrieve applications by number)");
        List<Application> applications = applicationRepository.findByNumber(applicationNumber);
        if (CollectionUtils.isEmpty(applications)) {
            throw new EntityNotFoundException("Application not found");
        }
        return applications;
    }

    /**
     * Retrieve application receipt from frontoffice.
     *
     * @param applicationNumber the application number
     * @param isDraft flag that indicates if the application is in draft status or not.
     * @return a list of applications
     */
    @Override
    public FileResponse getReceiptFromFrontoffice(String applicationNumber, boolean isDraft) {
        log.info(">>> Requesting receipt from frontoffice...");
        FileResponse fileResponse = new FileResponse();
        String url = frontofficeUrl + frontofficeReceiptEndpoint + applicationNumber;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("isDraft", isDraft);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        try {
            HttpsURLConnection.setDefaultHostnameVerifier ((hostname, session) -> true);
            ResponseEntity<byte[]> response = restTemplate.getForEntity(builder.toUriString(), byte[].class);
            fileResponse.setFileName(applicationNumber + "_receipt" + ".pdf");
            fileResponse.setBytes(response.getBody());
            fileResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            log.info(">>> Received {}_receipt.pdf", applicationNumber);
        } catch (HttpClientErrorException ex) {
            log.error(">>> Cannot find receipt for applicationNumber: {}", applicationNumber);
            log.error(ex.getLocalizedMessage());
            throw new FrontofficeReceiptNotFoundException("Could not get application receipt");
        } catch (HttpServerErrorException ex) {
            log.error(">>> Frontoffice could not process the request for applicationNumber: {}", applicationNumber);
            log.error(ex.getLocalizedMessage());
            throw new FrontofficeServerException("Could not get application receipt. Frontoffice Server Error");
        }

        return fileResponse;
    }

    /**
     * @param username the logged in username
     * @param id the application id
     * @return FileInfo the pdf
     */
    @Override
    public FileInfo getInvoice(String username,Long id) {
        PaymentApplicationEntity paymentApplication = paymentService.getPaymentApplicationByApplicationId(id);
        if(paymentApplication != null){
            PaymentEntity payment = paymentApplication.getPayment();
            if(payment != null && StringUtils.isNotEmpty(payment.getTransactionId())) {
                return pdfService.generatePdf(username, payment.getTransactionId());
            }
        }
        return null;
    }

    /**
     * Delete application.
     *
     * @param username the logged in username
     * @param number   the application number
     */
    @Override
    public void deleteApplication(String username, final String number) {
        log.info("Delete an application by number");
        username = username.trim();
        List<Application> applications = this.getApplicationsByNumberAndLock(number, username);
        if (CollectionUtils.isEmpty(applications)) {
            throw new EntityNotFoundException(number);
        }
        List<Application> ret = new ArrayList<>();
        for (Application application : applications) {
            if (accountService.isAllowedToModifyApplication(username, application.getMainAccount().getUsername())) {
                application.setDeleted(true);
                ret.add(application);
            }
        }
        applicationRepository.saveAll(ret);
        auditEventPublisher.publishCustomEvent("Delete application " + ret.get(0).getNumber(), username, AuditType.APPLICATION.getValue());
    }

    /**
     * Modify application.
     *
     * @param username the logged in username
     * @param number   the application number
     * @return true if the application is modified successfully otherwise false.
     */
    @Override
    public String modifyApplication(String username, final String number) {
        log.info("Modify an application by number");
        username = username.trim();
        List<Application> applications = this.getApplicationsByNumberAndLock(number, username);
        if (CollectionUtils.isEmpty(applications)) {
            throw new EntityNotFoundException(number);
        }
        List<Application> ret = new ArrayList<>();
        for (Application application : applications) {
            if (accountService.isAllowedToModifyApplication(username.trim(), application.getMainAccount().getUsername())) {
                application.setStatus(APPLICATION_DRAFT_STATUS_INITIALIZED);
                ret.add(application);
            }
        }
        applicationRepository.saveAll(ret);
        auditEventPublisher.publishCustomEvent("Modify application " + ret.get(0).getNumber(), username, AuditType.APPLICATION.getValue());
        // Deletes all signatures from xml saved in frontoffice
        deleteSignaturesInFO(number, restTemplate, frontofficeUrl, frontofficeSignatureDeleteEndpoint);
        return buildResumeUrl(ret.get(0), ipoConfiguration, env);
    }


    /**
     * Retrieves an application by id and locks the application.
     *
     * @param id       the application id
     * @param userName the authenticated username
     * @return the application
     */
    @Override
    @Transactional
    public Application getByIdAndLock(Long id, String userName) {
        log.info("Lock an application with id=[{}] by username=[{}]", id, userName);
        Application application = applicationRepository.getApplicationById(id);
        userName = userName.trim();
        if (application != null) {
            if (StringUtils.isEmpty(application.getLockedBy()) || (application.getLockedBy() != null && userName.equalsIgnoreCase(application.getLockedBy().trim()))) {
                application.setLockedBy(userName);
                application.setLockedDate(LocalDateTime.now());
                application = applicationRepository.save(application);
            } else {
                LockUtils.checkApplicationLockedAndThrowException(application, userName);
            }
        }
        return application;
    }

    /**
     * Updates application and releases is lock.
     *
     * @param id       the application id
     * @param username the authenticated user
     * @return the application
     */
    @Override
    @Transactional
    public Application updateAndReleaseLock(Long id, String username) {
        log.info("Releasing application lock with id=[{}] for user=[{}]", id, username);
        username = username.trim();
        Application application = applicationRepository.getApplicationById(id);
        LockUtils.checkApplicationLockedAndThrowException(application, username);
        application.setLockedBy(null);
        application.setLockedDate(null);
        return applicationRepository.save(application);
    }

    /**
     * Retrieves a list of applications locked
     *
     * @param applicationNumber the application number
     * @param username          the authenticated user
     * @return a list of locked applications
     */
    @Override
    @Transactional
    public List<Application> getApplicationsByNumberAndLock(String applicationNumber, String username) {
        log.info("Finding all applications with number {} and lock them", applicationNumber);
        username = username.trim();
        List<Application> ret = new ArrayList<>();
        List<Application> applications = applicationRepository.findByNumber(applicationNumber);
        for (Application application : applications) {
            if (StringUtils.isEmpty(application.getLockedBy()) || (application.getLockedBy() != null && username.equalsIgnoreCase(application.getLockedBy().trim()))) {
                application.setLockedBy(username);
                application.setLockedDate(LocalDateTime.now());
                ret.add(applicationRepository.save(application));
            } else {
                LockUtils.checkApplicationLockedAndThrowException(application, username);
            }
        }
        return ret;
    }

    /**
     * save and releases the lock for a list of applications
     *
     * @param applications the application list
     * @param username     the authenticated user
     * @return a list of applications
     */
    @Transactional
    @Override
    public List<Application> updateAndReleaseApplicationsLock(List<Application> applications, String username) {
        log.info("Releasing application locks for user {}", username);
        username = username.trim();
        List<Application> ret = new ArrayList<>();
        for (Application application : applications) {
            LockUtils.checkApplicationLockedAndThrowException(application, username);
            application.setLockedBy(null);
            application.setLockedDate(null);
            ret.add(applicationRepository.save(application));
        }
        return ret;
    }

}