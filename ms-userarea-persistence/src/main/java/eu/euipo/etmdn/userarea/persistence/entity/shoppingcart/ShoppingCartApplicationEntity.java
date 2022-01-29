/*
 * $Id:: ShoppingCartApplicationEntity.java 2021/04/16 02:59 tantonop
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
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.ThreadEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The entity ShoppingCartApplication
 */
@Data
@Entity
@Table(name = "SHOPPINGCARTAPPLICATION")
public class ShoppingCartApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShoppingCartApplicationId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CartId", referencedColumnName = "CartId")
    private ShoppingCartEntity shoppingCart;

    @Column(name = "ApplicationId")
    private Long applicationId;

    @Column(name = "FoModule")
    private String foModule;

    @Column(name = "Number")
    private String number;

    @Column(name = "Applicant")
    private String applicant;

    @Column(name = "Representative")
    private String representative;

    @Column(name = "LastModifiedDate")
    private LocalDateTime lastModifiedDate;

    @Column(name = "LastModifiedBy")
    private String lastModifiedBy;

    @Column(name = "Fees")
    private BigDecimal fees;

    @Column(name = "isTrademark")
    private Boolean isTrademark;

    @Column(name = "isDesign")
    private Boolean isDesign;


}
