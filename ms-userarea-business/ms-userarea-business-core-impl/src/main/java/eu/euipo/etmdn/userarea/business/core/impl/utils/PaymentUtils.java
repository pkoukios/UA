/*
 * $Id:: PaymentUtils.java 2021/06/19 12:56 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intellectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 *
 */

package eu.euipo.etmdn.userarea.business.core.impl.utils;


import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchFilterCriteria;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;

import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.ESERVICE;
import static eu.euipo.etmdn.userarea.persistence.specification.payment.PaymentSpecification.filterByPaidStatus;
import static eu.euipo.etmdn.userarea.persistence.specification.payment.PaymentSpecification.filterByPaymentApplicationIpRightType;
import static eu.euipo.etmdn.userarea.persistence.specification.payment.PaymentSpecification.filterByPaymentOwner;
import static eu.euipo.etmdn.userarea.persistence.specification.payment.PaymentSpecification.searchTermInPaymentHistorySearchableColumns;

public class PaymentUtils {

    /**
     * Get filter specification predicates for the payment history.
     *
     * @param paymentHistorySearchFilterCriteria the payment history filtering criteria
     * @param search the search term
     * @param searchableColumns the searchable columns for the correspondence
     * @return {@link Specification<PaymentEntity>} the payment specification
     */
    public static Specification<PaymentEntity> getPaymentHistoryFilterSpecificationPredicates(final String username,
                                                                                              final PaymentHistorySearchFilterCriteria paymentHistorySearchFilterCriteria,
                                                                                              final String search,
                                                                                              final String searchableColumns) {
        Specification<PaymentEntity> paymentFilterSpecs = Specification.where(filterByPaymentOwner(username).and(filterByPaidStatus()));
        if (paymentHistorySearchFilterCriteria != null) {
            if (paymentHistorySearchFilterCriteria.getIpRightTypes() != null) {
                paymentFilterSpecs = paymentFilterSpecs.and(filterByPaymentApplicationIpRightType(paymentHistorySearchFilterCriteria.getIpRightTypes()));
            }
        }
        if (search != null) {
           paymentFilterSpecs = paymentFilterSpecs.and(searchTermInPaymentHistorySearchableColumns(Arrays.asList(StringUtils.splitPreserveAllTokens(searchableColumns, ",")), search));
        }
        return paymentFilterSpecs;
    }

    /**
     * Get application ipRightType
     *
     * @param application the application
     * @return {@link String} the application ipRightType
     */
    public static String extractIpRightType(Application application){
        if (application.getFoModule().equalsIgnoreCase(ESERVICE) && application.getEserviceName() != null) {
            return application.getEserviceName();
        } else {
            return application.getFoModule();
        }
    }

}
