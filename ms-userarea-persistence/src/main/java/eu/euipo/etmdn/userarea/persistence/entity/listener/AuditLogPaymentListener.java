/*
 * $Id:: AuditLogSignatureListener.java 2021/03/08 11:50
 * . * .
 *  RRRR * Copyright (c) 2012-2021 EUIPO: European Intelectual
 * . RR R . Property Organization (trademarks and designs).
 *  RRR *
 * . RR RR . ALL RIGHTS RESERVED
 * . _ .*
 * The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.persistence.entity.listener;

import eu.euipo.etmdn.userarea.common.domain.auditlog.AuditType;
import eu.euipo.etmdn.userarea.common.persistence.entity.auditlog.AuditLogEntity;
import eu.euipo.etmdn.userarea.common.persistence.entity.auditlog.AuditTypeEntity;
import eu.euipo.etmdn.userarea.common.persistence.repository.auditlog.AuditLogRepository;
import eu.euipo.etmdn.userarea.common.persistence.repository.auditlog.AuditLogTypeRepository;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatus;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import java.time.LocalDateTime;

@Slf4j
@Configurable
public class AuditLogPaymentListener {

    @Autowired
    private ObjectFactory<AuditLogRepository> repository;
    @Autowired
    private ObjectFactory<AuditLogTypeRepository> auditLogTypeRepository;

    @PostPersist
    private void afterInsertPayment(PaymentEntity paymentEntity) {
        log.info("Write audit log entry for entityId {}", paymentEntity.getId());
        AuditLogTypeRepository typeRepository = auditLogTypeRepository.getObject();
        AuditTypeEntity auditTypeEntity = typeRepository.findOneByAuditType(AuditType.PAYMENT_CREATED.getValue());
        AuditLogEntity entity = new AuditLogEntity();
        entity.setDate(LocalDateTime.now());
        entity.setEntityId(paymentEntity.getId());
        entity.setPayload(paymentEntity.toString());
        entity.setUser(paymentEntity.getPaidBy());
        entity.setType(auditTypeEntity);
        entity.setRelatedApplication(paymentEntity.getApplicationNumbers());
        repository.getObject().save(entity);
        log.info("Finished writing audit log entry for entityId {}", paymentEntity.getId());
    }

    @PostUpdate
    private void afterUpdatePayment(PaymentEntity paymentEntity) {
        log.info("Write audit log entry for entityId {}", paymentEntity.getId());
        AuditLogTypeRepository typeRepository = auditLogTypeRepository.getObject();
        AuditLogEntity entity = new AuditLogEntity();
        AuditTypeEntity auditTypeEntity = getAuditType(paymentEntity, typeRepository);
        entity.setDate(LocalDateTime.now());
        entity.setEntityId(paymentEntity.getId());
        entity.setPayload(paymentEntity.toString());
        entity.setUser(paymentEntity.getPaidBy());
        entity.setType(auditTypeEntity);
        entity.setRelatedApplication(paymentEntity.getApplicationNumbers());
        repository.getObject().save(entity);
        log.info("Finished writing audit log entry for entityId {}", paymentEntity.getId());
    }

    /**
     * sets the correct audit type
     *
     * @param paymentEntity the payment entity
     * @param typeRepository  the audit type repository
     * @return the audit type
     */
    private AuditTypeEntity getAuditType(PaymentEntity paymentEntity, AuditLogTypeRepository typeRepository) {
        if (paymentEntity.getStatus().equals(PaymentStatus.PAID)) {
            return typeRepository.findOneByAuditType(AuditType.PAYMENT_COMPLETED.getValue());
        } else if (paymentEntity.getStatus().equals(PaymentStatus.PAID_UPDATE_FO_FAILED)) {
            return typeRepository.findOneByAuditType(AuditType.PAYMENT_COMPLETED_UPDATE_FO_FAILED.getValue());
        }
        return typeRepository.findOneByAuditType(AuditType.PAYMENT_UPDATED.getValue());
    }
}