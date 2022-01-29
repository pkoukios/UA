/*
 * $Id:: ShoppingCartApplicationComparator.java 2021/04/19 05:26 tantonop
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

package eu.euipo.etmdn.userarea.business.core.impl.comparator;

import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchShoppingCartSort;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * Comparator class to sort the shopping cart applications
 */
public class ShoppingCartApplicationComparator implements Comparator<ShoppingCartApplication> {

    private SearchShoppingCartSort sort;

    public ShoppingCartApplicationComparator(SearchShoppingCartSort sort){
        this.sort = sort;
    }


    @Override
    public int compare(ShoppingCartApplication o1, ShoppingCartApplication o2) {
        if(this.sort.equals(SearchShoppingCartSort.TYPE)){
            return o1.getType().toLowerCase().compareTo(o2.getType().toLowerCase());
        }else if(this.sort.equals(SearchShoppingCartSort.APPLICATION)){
            return o1.getNumber().compareTo(o2.getNumber());
        }else if(this.sort.equals(SearchShoppingCartSort.APPLICANTS)){
            if(StringUtils.isBlank(o1.getApplicant())){
                return 1;
            }else if(StringUtils.isBlank(o2.getApplicant())){
                return -1;
            }
            return o1.getApplicant().compareTo(o2.getApplicant());
        }else if(this.sort.equals(SearchShoppingCartSort.REPRESENTATIVES)){
            if(StringUtils.isBlank(o1.getRepresentative())){
                return 1;
            }else if(StringUtils.isBlank(o2.getRepresentative())){
                return -1;
            }
            return o1.getRepresentative().compareTo(o2.getRepresentative());
        }else if(this.sort.equals(SearchShoppingCartSort.LAST_MODIFIED_BY)){
            if(StringUtils.isBlank(o1.getLastModifiedBy())){
                return 1;
            }else if(StringUtils.isBlank(o2.getLastModifiedBy())){
                return -1;
            }
            return o1.getLastModifiedBy().compareTo(o2.getLastModifiedBy());
        }else if(this.sort.equals(SearchShoppingCartSort.FEES)){
            if(o1.getFees()==null){
                return 1;
            }else if(o2.getFees()==null){
                return -1;
            }
            return o1.getFees().compareTo(o2.getFees());
        }else if(this.sort.equals(SearchShoppingCartSort.LAST_MODIFIED_DATE)){
            if(o1.getLastModifiedDate()==null){
                return 1;
            }else if(o2.getLastModifiedDate()==null){
                return -1;
            }
            return o1.getLastModifiedDate().compareTo(o2.getLastModifiedDate());
        }
        return 0;
    }

}
