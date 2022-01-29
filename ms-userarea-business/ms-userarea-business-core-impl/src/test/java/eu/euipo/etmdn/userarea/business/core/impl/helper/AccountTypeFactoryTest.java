/*
 * $Id:: AccountTypeFactoryTest.java 2021/03/01 09:07 dvelegra
 *  
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *  
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt' 
 */

package eu.euipo.etmdn.userarea.business.core.impl.helper;

import eu.euipo.etmdn.userarea.common.business.helper.AccountTypeFactory;
import eu.euipo.etmdn.userarea.common.business.helper.MainAccountValidator;
import eu.euipo.etmdn.userarea.common.domain.ApplicantType;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.LegalStatus;
import eu.euipo.etmdn.userarea.common.domain.MainAccountType;
import eu.euipo.etmdn.userarea.common.domain.RepresentativeType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountTypeFactoryTest {

    private final DomainAccount mainAccount = DomainAccount.builder().firstName("John").surName("Cool").email("abc@123.com").build();

    @Test
    public void testAccountPersonTypeApplicantIndividual() {
        mainAccount.setMainAccountType(MainAccountType.APPLICANT.getValue());
        mainAccount.setLegalType(ApplicantType.INDIVIDUAL.getValue());
        MainAccountValidator mainAccountType = AccountTypeFactory.getAccountPersonType(mainAccount);
        assertNotNull(mainAccountType);
        assertEquals(mainAccountType, MainAccountValidator.APPLICANT_INDIVIDUAL);
    }

    @Test
    public void testAccountPersonTypeApplicantCompany() {
        mainAccount.setMainAccountType(MainAccountType.APPLICANT.getValue());
        mainAccount.setLegalType(ApplicantType.COMPANY.getValue());
        MainAccountValidator mainAccountType = AccountTypeFactory.getAccountPersonType(mainAccount);
        assertNotNull(mainAccountType);
        assertEquals(mainAccountType, MainAccountValidator.APPLICANT_COMPANY);
    }

    @Test
    public void testAccountPersonTypeRepEmployeeIndividual() {
        mainAccount.setMainAccountType(MainAccountType.REPRESENTATIVE.getValue());
        mainAccount.setLegalType(RepresentativeType.EMPLOYEE_REPRESENTATIVE.getValue());
        mainAccount.setLegalStatus(LegalStatus.INDIVIDUAL.getValue());
        MainAccountValidator mainAccountType = AccountTypeFactory.getAccountPersonType(mainAccount);
        assertNotNull(mainAccountType);
        assertEquals(mainAccountType, MainAccountValidator.REPRESENTATIVE_EMPLOYEE_INDIVIDUAL);
    }

    @Test
    public void testAccountPersonTypeRepEmployeeCompany() {
        mainAccount.setMainAccountType(MainAccountType.REPRESENTATIVE.getValue());
        mainAccount.setLegalType(RepresentativeType.EMPLOYEE_REPRESENTATIVE.getValue());
        mainAccount.setLegalStatus(LegalStatus.COMPANY.getValue());
        MainAccountValidator mainAccountType = AccountTypeFactory.getAccountPersonType(mainAccount);
        assertNotNull(mainAccountType);
        assertEquals(mainAccountType, MainAccountValidator.REPRESENTATIVE_EMPLOYEE_COMPANY);
    }

    @Test
    public void testAccountPersonTypeRepLegalPractitionerIndividual() {
        mainAccount.setMainAccountType(MainAccountType.REPRESENTATIVE.getValue());
        mainAccount.setLegalType(RepresentativeType.LEGAL_PRACTITIONER.getValue());
        mainAccount.setLegalStatus(LegalStatus.INDIVIDUAL.getValue());
        MainAccountValidator mainAccountType = AccountTypeFactory.getAccountPersonType(mainAccount);
        assertNotNull(mainAccountType);
        assertEquals(mainAccountType, MainAccountValidator.REPRESENTATIVE_LEGAL_PRACTITIONER_INDIVIDUAL);
    }

    @Test
    public void testAccountPersonTypeRepLegalPractitionerCompany() {
        mainAccount.setMainAccountType(MainAccountType.REPRESENTATIVE.getValue());
        mainAccount.setLegalType(RepresentativeType.LEGAL_PRACTITIONER.getValue());
        mainAccount.setLegalStatus(LegalStatus.COMPANY.getValue());
        MainAccountValidator mainAccountType = AccountTypeFactory.getAccountPersonType(mainAccount);
        assertNotNull(mainAccountType);
        assertEquals(mainAccountType, MainAccountValidator.REPRESENTATIVE_PRACTITIONER_COMPANY);
    }

    @Test
    public void testAccountPersonTypeRepEuipoPractitionerCompany() {
        mainAccount.setMainAccountType(MainAccountType.REPRESENTATIVE.getValue());
        mainAccount.setLegalType(RepresentativeType.EUIPO_PROFESSIONAL_PRACTITIONER.getValue());
        mainAccount.setLegalStatus(LegalStatus.COMPANY.getValue());
        MainAccountValidator mainAccountType = AccountTypeFactory.getAccountPersonType(mainAccount);
        assertNotNull(mainAccountType);
        assertEquals(mainAccountType, MainAccountValidator.REPRESENTATIVE_PRACTITIONER_COMPANY);
    }

    @Test
    public void testAccountPersonTypeRepresentativeAssociation() {
        mainAccount.setMainAccountType(MainAccountType.REPRESENTATIVE.getValue());
        mainAccount.setLegalType(RepresentativeType.ASSOCIATION.getValue());
        MainAccountValidator mainAccountType = AccountTypeFactory.getAccountPersonType(mainAccount);
        assertNotNull(mainAccountType);
        assertEquals(mainAccountType, MainAccountValidator.REPRESENTATIVE_ASSOCIATION);
    }
}