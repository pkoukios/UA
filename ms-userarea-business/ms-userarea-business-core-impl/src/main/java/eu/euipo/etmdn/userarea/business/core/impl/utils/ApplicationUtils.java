/*
 * $Id:: ApplicationUtils.java 2021/03/01 09:07 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.utils;

import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.business.config.IpoConfiguration;
import eu.euipo.etmdn.userarea.common.domain.FilteringDate;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.LocarnoDetails;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.euipo.etmdn.userarea.common.persistence.specification.ApplicationSpecification.filterByDate;
import static eu.euipo.etmdn.userarea.common.persistence.specification.ApplicationSpecification.filterByDraftDesignStatuses;
import static eu.euipo.etmdn.userarea.common.persistence.specification.ApplicationSpecification.filterByStatuses;
import static eu.euipo.etmdn.userarea.common.persistence.specification.ApplicationSpecification.searchTermInSearchableColumns;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.DS_EFILING_URL;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.FORM_ID_REQUEST_PARAM;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.FO_URL;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.TM_EFILING_URL;

/**
 * The ApplicationUtils class.
 */
public class ApplicationUtils {

    public static final String NOT_APPLICABLE = "N/A";

    private ApplicationUtils() { }

    /**
     * Convert fo status to UA status.(STATUS_SUBMITTED_ATTACHMENTS_PENDING --> Submitted attachments pending)
     *
     * @param status the application status
     * @return String the email name
     */
    public static String getStatusFromFo(String status) {
        String temp = StringUtils.substring(status, 7);
        String newStatus = String.join(StringUtils.SPACE, temp.split("_"));
        String lowerString = StringUtils.substring(newStatus, 1);
        return StringUtils.join(newStatus.charAt(0), lowerString.toLowerCase());
    }

    /**
     * Check if user has the role: ROLE_TRADEMARKS
     *
     * @param roles the user roles
     * @return true if the user has in authorities the ROLE_TRADEMARKS
     */
    public static boolean hasTrademarkRole(Set<String> roles) {
        return roles.stream().anyMatch(role -> role.equals("ROLE_TRADEMARKS"));
    }

    /**
     * Check if user has the role: ROLE_DESIGNS
     *
     * @param roles the user roles
     * @return true if the user has in authorities the ROLE_DESIGNS
     */
    public static boolean hasDesignRole(Set<String> roles) {
        return roles.stream().anyMatch(role -> role.equals("ROLE_DESIGNS"));
    }

    /**
     * Convert locarno classes to string.
     *
     * @param locarnoList the locarno classes list
     * @return String string representation of locarno classes
     */
    public static String convertLocarnoClassesToString(List<LocarnoDetails> locarnoList) {
        if (!CollectionUtils.isEmpty(locarnoList)) {
            List<String> locarnos = locarnoList.stream()
                    .map(loc -> StringUtils.join(loc.getMainClass(), ".", loc.getSubClass()))
                    .collect(Collectors.toList());
            return locarnos.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        }
        return NOT_APPLICABLE;
    }

    /**
     * Convert boolean to string answer(true-> Yes, false->No).
     *
     * @param flag the booleam flag
     * @return String the camel case answer(Yes or No)
     */
    public static String convertBooleanToStringAnswer(Boolean flag) {
        return getStringValue(CaseUtils.toCamelCase(BooleanUtils.toStringYesNo(flag), true));
    }

    /**
     * Convert string to string representation value.
     *
     * @param value the string
     * @return string
     */
    public static String getStringValue(String value) {
        return StringUtils.isEmpty(value) ? NOT_APPLICABLE : value;
    }

    /**
     * Convert date to default date string representation.
     *
     * @param dateTime the date
     * @return string the date in the default string format
     */
    public static String convertDateToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().toString() : NOT_APPLICABLE;
    }

    /**
     * Build the resume url for a fdraft application.
     *
     * @param draft the application draft
     * @param ipoConfiguration the IPO configuration
     * @param environment the environment property
     * @return string the resume url
     */
    public static String buildResumeUrl(Application draft, IpoConfiguration ipoConfiguration, Environment environment) {
        String redirectUrl = StringUtils.EMPTY;
        if (ApplicationType.TRADEMARK.value.equalsIgnoreCase(draft.getFoModule())) {
            redirectUrl = String.join(StringUtils.EMPTY, ipoConfiguration.getIpo().get(FO_URL), ipoConfiguration.getIpo().get(TM_EFILING_URL), FORM_ID_REQUEST_PARAM, draft.getNumber());
        } else if (ApplicationType.DESIGN.value.equalsIgnoreCase(draft.getFoModule())) {
            redirectUrl = String.join(StringUtils.EMPTY, ipoConfiguration.getIpo().get(FO_URL), ipoConfiguration.getIpo().get(DS_EFILING_URL), FORM_ID_REQUEST_PARAM, draft.getNumber());
        } else if (ApplicationType.ESERVICE.value.equalsIgnoreCase(draft.getFoModule())) {
            final String eserviceType = draft.getEserviceCode().toLowerCase().replace("_", ".");
            final String eserviceUrl = environment.getProperty("userarea.globals.ipo." + eserviceType + ".url");
            redirectUrl = String.join(StringUtils.EMPTY, ipoConfiguration.getIpo().get(FO_URL), eserviceUrl, FORM_ID_REQUEST_PARAM, draft.getNumber());
        }
        return redirectUrl;
    }


    /**
     * Get filter specification based on the group of statuses for drafts.
     *
     * @param applicationRequest the trademark application request
     * @param filterSpecs        the specification filter
     * @return {@link Specification<Application>} the specification application
     */
    public static Specification<Application> getDifferentialSpecificationForDraft(final ApplicationRequest applicationRequest, ApplicationConfiguration applicationConfiguration, Specification<Application> filterSpecs) {
        if (ApplicationType.DESIGN.value.equals(applicationRequest.getApplicationType())) {
            filterSpecs = filterSpecs.and(filterByDraftDesignStatuses(Arrays.asList(StringUtils.splitPreserveAllTokens(applicationConfiguration.getStatus().getDraft(), ","))));
        } else {
            filterSpecs = filterSpecs.and(filterByStatuses(Arrays.asList(StringUtils.splitPreserveAllTokens(applicationConfiguration.getStatus().getDraft(), ","))));
        }
        if (applicationRequest.getSearchingData() != null) {
            filterSpecs = filterSpecs.and(searchTermInSearchableColumns(Arrays.asList(StringUtils.splitPreserveAllTokens(applicationConfiguration.getSearch().getDraft(), ",")),
                    applicationRequest.getSearchingData()));
        }
        if (applicationRequest.getFilteringData().getDates() != null) {
            for (FilteringDate filteringDate : applicationRequest.getFilteringData().getDates()) {
                filterSpecs = filterSpecs.and(filterByDate(filteringDate.getDate(), filteringDate.getDateFrom(), filteringDate.getDateTo()));
            }
        }

        return filterSpecs;
    }

    /**
     * Check if an application is in draft status
     * @param applicationConfiguration the application configuration containing the draft statuses
     * @param application the application
     * @return true/false
     */
    public static boolean isDraftStatus(ApplicationConfiguration applicationConfiguration, Application application){
        if(applicationConfiguration.getStatus() == null){
            return false;
        }
        String draftStatuses = applicationConfiguration.getStatus().getDraft();
        List<String> draftStatusesList = Arrays.asList(draftStatuses.split(","));
        return draftStatusesList.contains(application.getStatus());
    }

}
