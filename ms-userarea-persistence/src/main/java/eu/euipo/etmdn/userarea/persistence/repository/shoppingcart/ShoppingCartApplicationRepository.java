/*
 * $Id:: ShoppingCartApplicationRepository.java 2021/04/17 12:22 tantonop
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

package eu.euipo.etmdn.userarea.persistence.repository.shoppingcart;

import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartApplicationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface ShoppingCartApplicationRepository extends JpaRepository<ShoppingCartApplicationEntity,Long>, JpaSpecificationExecutor<ShoppingCartApplicationEntity> {

    Optional<ShoppingCartApplicationEntity> getShoppingCartApplicationEntityByApplicationId(Long id);
    List<ShoppingCartApplicationEntity> findByApplicationIdIn(List<Long> ids);
    List<ShoppingCartApplicationEntity> findByNumberIn(List<String> numbers);

}
