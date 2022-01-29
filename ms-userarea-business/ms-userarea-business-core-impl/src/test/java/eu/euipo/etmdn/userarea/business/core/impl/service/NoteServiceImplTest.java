/*
 * $Id:: NoteServiceImplTest.java 2021/08/05 02:39 dvelegra
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

import eu.euipo.etmdn.userarea.common.domain.exception.NoteEntityLockedException;
import eu.euipo.etmdn.userarea.persistence.entity.note.NoteApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.repository.note.NoteApplicationRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NoteServiceImplTest {

    @InjectMocks
    private NoteServiceImpl noteService;

    @Mock
    private NoteApplicationRepository noteApplicationRepository;

    private NoteApplicationEntity noteApplicationEntity;
    private NoteApplicationEntity noteApplication;
    private static final String USERNAME = "abc@xyz.com";
    private static final String NOTE = "Test note";
    private static final String APPLICATION_NUMBER = "12345";

    @Before
    public void setUp() {
        noteApplicationEntity = NoteApplicationEntity.builder().applicationNumber(APPLICATION_NUMBER).note(NOTE).build();
        noteApplication = NoteApplicationEntity.builder().applicationNumber(APPLICATION_NUMBER).note(NOTE).build();
    }

    @Test
    public void testSaveNote() {
        noteService.save(noteApplicationEntity);
        verify(noteApplicationRepository, times(1)).save(any(NoteApplicationEntity.class));
    }

    @Test
    public void testGetNoteByApplicationNumber() {
        when(noteApplicationRepository.findByApplicationNumber(APPLICATION_NUMBER)).thenReturn(noteApplicationEntity);
        NoteApplicationEntity noteApplication = noteService.getNoteByApplicationNumber(APPLICATION_NUMBER);
        assertNotNull(noteApplication);
    }

    @Test
    public void testGetNoteByApplicationNumbers() {
        when(noteApplicationRepository.findByApplicationNumberIsIn(Collections.singletonList(APPLICATION_NUMBER))).thenReturn(Collections.singletonList(noteApplicationEntity));
        List<NoteApplicationEntity> noteApplicationList = noteService.getNotesByApplicationNumbers(Collections.singletonList(APPLICATION_NUMBER));
        assertNotNull(noteApplicationList);
    }

    @Test
    public void testGetNoteAndLock() {
        when(noteApplicationRepository.findByApplicationNumber(any(String.class))).thenReturn(noteApplicationEntity);
        NoteApplicationEntity noteApplication = noteService.getNoteByApplicationNumberAndLock(USERNAME, APPLICATION_NUMBER);
        assertNotNull(noteApplication);
    }

    @Test(expected = NoteEntityLockedException.class)
    public void testGetNoteAndLockLocked() {
        noteApplicationEntity.setLockedBy("test");
        when(noteApplicationRepository.findByApplicationNumber(any(String.class))).thenReturn(noteApplicationEntity);
        noteService.getNoteByApplicationNumberAndLock(USERNAME, APPLICATION_NUMBER);
    }

    @Test
    public void testReleaseLock() {
        noteApplicationEntity.setLockedBy(USERNAME);
        when(noteApplicationRepository.saveAndFlush(noteApplicationEntity)).thenReturn(noteApplication);
        NoteApplicationEntity noteApplication = noteService.updateAndReleaseLock(USERNAME, noteApplicationEntity, NOTE);
        assertNotNull(noteApplication);
        verify(noteApplicationRepository, times(1)).saveAndFlush(any(NoteApplicationEntity.class));
    }

    @Test(expected = NoteEntityLockedException.class)
    public void testReleasedLockNotAllowed() {
        noteApplicationEntity.setLockedBy("test");
        noteService.updateAndReleaseLock(USERNAME, noteApplicationEntity, NOTE);
    }

}