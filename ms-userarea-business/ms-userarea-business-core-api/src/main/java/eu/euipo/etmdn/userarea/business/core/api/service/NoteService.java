/*
 * $Id:: NoteService.java 2021/08/05 01:38 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.api.service;

import eu.euipo.etmdn.userarea.persistence.entity.note.NoteApplicationEntity;
import java.util.List;

public interface NoteService {

    /**
     * Save a note.
     *
     * @param noteApplicationEntity the note application entity
     */
    void save(NoteApplicationEntity noteApplicationEntity);

    /**
     * Retrieve the note for the given application number.
     *
     * @param applicationNumber the application number
     * @return {@link NoteApplicationEntity}
     */
    NoteApplicationEntity getNoteByApplicationNumber(String applicationNumber);

    /**
     * Retrieve the note for the specified list with application numbers.
     *
     * @param applicationNumbers the application numbers
     * @return {@link List<NoteApplicationEntity>}
     */
    List<NoteApplicationEntity> getNotesByApplicationNumbers(List<String> applicationNumbers);

    /**
     * Retrieve note and lock.
     *
     * @param username the unique payment transaction id provided by the external platform
     * @param applicationNumber the application number
     * @return {@link NoteApplicationEntity}
     */
    NoteApplicationEntity getNoteByApplicationNumberAndLock(String username, String applicationNumber);

    /**
     * Update and release lock on note.
     *
     * @param username the unique payment transaction id provided by the external platform
     * @param noteApplicationEntity the note application entity
     * @param note the note string
     * @return {@link NoteApplicationEntity} the note application entity
     */
    NoteApplicationEntity updateAndReleaseLock(String username, NoteApplicationEntity noteApplicationEntity, String note);


}
