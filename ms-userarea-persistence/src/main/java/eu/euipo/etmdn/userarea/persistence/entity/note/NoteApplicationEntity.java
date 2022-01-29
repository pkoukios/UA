/*
 * $Id:: NoteApplicationEntity.java 2021/08/02 03:18 dvelegra
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

package eu.euipo.etmdn.userarea.persistence.entity.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NOTEAPPLICATION")
public class NoteApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NoteApplicationId")
    private Long id;

    @Column(name = "ApplicationNumber")
    private String applicationNumber;

    @Column(name = "Note")
    private String note;

    @CreationTimestamp
    @Column(name = "CreationDate")
    private LocalDateTime creationDate;

    @Column(name = "LastModifiedBy")
    private String lastModifiedBy;

    @UpdateTimestamp
    @Column(name = "LastModifiedDate")
    private LocalDateTime lastModifiedDate;

    @Column(name = "LockedBy")
    private String lockedBy;

    @Column(name = "LockedDate")
    private LocalDateTime lockedDate;

}
