/*
 * $Id:: ShoppingCartEntity.java 2021/04/16 02:19 tantonop
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 *
 */

package eu.euipo.etmdn.userarea.persistence.entity.shoppingcart;

import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The entity ShoppingCart
 */
@Data
@Entity
@Table(name = "SHOPPINGCART")
public class ShoppingCartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartId")
    private Long id;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "CartId")
    private List<ShoppingCartApplicationEntity> applications;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "`User`")
    private String user;

}
