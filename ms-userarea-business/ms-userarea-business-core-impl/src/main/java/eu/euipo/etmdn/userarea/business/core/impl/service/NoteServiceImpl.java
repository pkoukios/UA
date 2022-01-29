/*
 * $Id:: NoteServiceImpl.java 2021/08/05 02:12 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.service;

import eu.euipo.etmdn.userarea.business.core.api.service.NoteService;
import eu.euipo.etmdn.userarea.common.domain.exception.NoteEntityLockedException;
import eu.euipo.etmdn.userarea.domain.note.NoteApplication;
import eu.euipo.etmdn.userarea.persistence.entity.note.NoteApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.repository.note.NoteApplicationRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * The Note service.
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class NoteServiceImpl implements NoteService {

    private NoteApplicationRepository noteApplicationRepository;

    /**
     * Save a note.
     *
     * @param noteApplicationEntity the note application entity
     */
    public void save(NoteApplicationEntity noteApplicationEntity) {
        noteApplicationRepository.save(noteApplicationEntity);
    }

    /**
     * Save a note.
     *
     * @param applicationNumber the application number
     * @return {@link NoteApplicationEntity}
     */
    public NoteApplicationEntity getNoteByApplicationNumber(String applicationNumber) {
        return noteApplicationRepository.findByApplicationNumber(applicationNumber);
    }

    /**
     * Retrieve the note for the specified list with application numbers.
     *
     * @param applicationNumbers the application numbers
     * @return {@link List <NoteApplication>}
     */
    @Override
    public List<NoteApplicationEntity> getNotesByApplicationNumbers(List<String> applicationNumbers) {
        return noteApplicationRepository.findByApplicationNumberIsIn(applicationNumbers);
    }


    /**
     * Update and release lock on note.
     *
     * @param username the unique payment transaction id provided by the external platform
     * @param noteApplicationEntity the note application entity
     * @param note the note string
     * @return {@link NoteApplicationEntity} the note application entity
     */
    @Override
    @Transactional
    public NoteApplicationEntity updateAndReleaseLock(String username, NoteApplicationEntity noteApplicationEntity, String note) {
        username = username.trim();
        if (!StringUtils.isEmpty(noteApplicationEntity.getLockedBy())) {
            if (!noteApplicationEntity.getLockedBy().equalsIgnoreCase(username)) {
                throw new NoteEntityLockedException(noteApplicationEntity.getLockedBy());
            }
        }
        noteApplicationEntity.setNote(note);
        noteApplicationEntity.setLastModifiedBy(username);
        noteApplicationEntity.setLockedBy(null);
        noteApplicationEntity.setLockedDate(null);
        return noteApplicationRepository.saveAndFlush(noteApplicationEntity);
    }

    /**
     * Retrieve note and lock.
     *
     * @param username the unique payment transaction id provided by the external platform
     * @param applicationNumber the application number
     * @return {@link NoteApplicationEntity}
     */
    @Override
    @Transactional
    public NoteApplicationEntity getNoteByApplicationNumberAndLock(String username, String applicationNumber) {
        log.info("Retrieving note with application number {}", applicationNumber);
        username = username.trim();
        NoteApplicationEntity noteApplication = noteApplicationRepository.findByApplicationNumber(applicationNumber);
        if (StringUtils.isEmpty(noteApplication.getLockedBy()) || (noteApplication.getLockedBy() != null && username.equalsIgnoreCase(noteApplication.getLockedBy().trim()))) {
            noteApplication.setLockedBy(username);
            noteApplication.setLockedDate(LocalDateTime.now());
            noteApplicationRepository.saveAndFlush(noteApplication);
        } else {
            if (!StringUtils.isEmpty(noteApplication.getLockedBy())) {
                if (!noteApplication.getLockedBy().equalsIgnoreCase(username)) {
                    throw new NoteEntityLockedException(noteApplication.getLockedBy());
                }
            }
        }
        return noteApplication;
    }

}