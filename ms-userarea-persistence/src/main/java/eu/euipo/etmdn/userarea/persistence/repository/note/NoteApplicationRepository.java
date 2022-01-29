/*
 * $Id:: NoteApplicationRepository.java 2021/08/02 04:19 dvelegra
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

package eu.euipo.etmdn.userarea.persistence.repository.note;

import eu.euipo.etmdn.userarea.persistence.entity.note.NoteApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface NoteApplicationRepository extends JpaRepository<NoteApplicationEntity, Long>, JpaSpecificationExecutor<NoteApplicationEntity> {

    /**
     * Find note by application number.
     *
     * @param number     the application number
     * @return {@link NoteApplicationEntity} the NoteApplication entity
     */
    NoteApplicationEntity findByApplicationNumber(String number);

    /**
     * Find note by application number.
     *
     * @param numbers     the collection with application numbers
     * @return {@link NoteApplicationEntity} the NoteApplication entity
     */
    List<NoteApplicationEntity> findByApplicationNumberIsIn(List<String> numbers);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints(value = {@QueryHint(name = "javax.persistence.lock.scope", value = "EXTENDED")})
    NoteApplicationEntity getNoteApplicationById(Long id);

    List<NoteApplicationEntity> findByLockedByIsNotNull();
}
