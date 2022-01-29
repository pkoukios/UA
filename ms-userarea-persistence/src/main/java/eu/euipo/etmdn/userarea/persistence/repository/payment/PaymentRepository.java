/*
 * $Id:: PaymentRepository.java 2021/05/11 11:21 achristo
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

package eu.euipo.etmdn.userarea.persistence.repository.payment;

import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long>, JpaSpecificationExecutor<PaymentEntity> {

    Optional<PaymentEntity> findByTransactionId(String transactionId);
}
