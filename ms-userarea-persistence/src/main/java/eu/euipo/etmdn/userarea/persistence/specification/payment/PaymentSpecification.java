/*
 * $Id:: PaymentSpecification.java 2021/04/19 01:24 dvelegra
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

package eu.euipo.etmdn.userarea.persistence.specification.payment;

import eu.euipo.etmdn.userarea.domain.payment.PaymentStatus;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentApplicationEntity_;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity_;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartApplicationEntity_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification class for the payments
 */
public class PaymentSpecification {

    public static Specification<PaymentEntity> filterByPaymentOwner(String username) {
        return (root, query, cb) -> cb.equal(root.get(PaymentEntity_.OWNER), username);
    }

    public static Specification<PaymentEntity> filterByPaymentApplicationIpRightType(List<String> ipRightTypes) {
        return (root, query, cb) -> {
            final Path<PaymentEntity> paymentEntityPath = root.join(PaymentEntity_.PAYMENT_APPLICATIONS).get(PaymentApplicationEntity_.IP_RIGHT_TYPE);
            query.groupBy(root.get(PaymentEntity_.ID));
            return paymentEntityPath.in(ipRightTypes);
        };
    }

    public static Specification<PaymentEntity> filterByPaidStatus() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Predicate predicate = cb.equal(root.get(PaymentEntity_.STATUS), PaymentStatus.PAID);
            predicates.add(predicate);
            predicate = cb.equal(root.get(PaymentEntity_.STATUS), PaymentStatus.PAID_UPDATE_FO_FAILED);
            predicates.add(predicate);
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<PaymentEntity> searchTermInPaymentHistorySearchableColumns(List<String> columns, String term) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String column : columns) {
                Predicate newPredicate = cb.like(root.get(column), getString(term));
                predicates.add(newPredicate);
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static String getString(String term) {
        if (!term.contains("%")) {
            term = "%" + term + "%";
        }
        return term;
    }

}
