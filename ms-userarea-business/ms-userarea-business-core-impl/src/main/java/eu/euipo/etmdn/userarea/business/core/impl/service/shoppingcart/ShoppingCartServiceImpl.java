/*
 * $Id:: ShoppingCartServiceImpl.java 2021/04/17 01:10 tantonop
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

package eu.euipo.etmdn.userarea.business.core.impl.service.shoppingcart;

import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.business.core.api.service.SignatureService;
import eu.euipo.etmdn.userarea.business.core.impl.comparator.ShoppingCartApplicationComparator;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.correspondence.search.SearchMessageType;
import eu.euipo.etmdn.userarea.common.domain.exception.EntityNotFoundException;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.common.persistence.entity.MainAccount;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchCriteriaShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartSearch;
import eu.euipo.etmdn.userarea.domain.shoppingcart.exception.ShoppingCartSecurityException;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartEntity;
import eu.euipo.etmdn.userarea.persistence.mapper.shoppingcart.ShoppingCartApplicationMapper;
import eu.euipo.etmdn.userarea.persistence.mapper.shoppingcart.ShoppingCartMapper;
import eu.euipo.etmdn.userarea.persistence.repository.shoppingcart.ShoppingCartApplicationRepository;
import eu.euipo.etmdn.userarea.persistence.repository.shoppingcart.ShoppingCartRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.euipo.etmdn.userarea.business.core.impl.utils.ShoppingCartUtils.getShoppingCartSpecification;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.ASCENDING;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private ShoppingCartRepository shoppingCartRepository;
    private ShoppingCartApplicationRepository shoppingCartApplicationRepository;
    private ApplicationConfiguration applicationConfiguration;
    private AccountService accountService;
    private SignatureService signatureService;

    /**
     * Method to create a shopping cart
     * @param username the authenticated user
     * @return the shopping cart
     */
    @Override
    public ShoppingCart create(String username) {
        log.info("Creating shopping cart for user {}",username);
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setApplications(new ArrayList<>());
        shoppingCart.setUser(username);
        shoppingCart.setCreatedDate(LocalDateTime.now());
        ShoppingCartEntity shoppingCartEntity = ShoppingCartMapper.MAPPER.mapToEntity(shoppingCart);
        shoppingCartEntity = shoppingCartRepository.save(shoppingCartEntity);
        return ShoppingCartMapper.MAPPER.mapToDomain(shoppingCartEntity);
    }

    /**
     * returns a shopping cart by user
     * @param username the authenticated user
     * @return the shopping cart
     */
    @Override
    public ShoppingCart getByUser(String username) {
        log.info("Getting shopping cart for user {}",username);
        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.getByUser(username);
        return shoppingCartEntity.map(ShoppingCartMapper.MAPPER::mapToDomain).orElse(null);
    }

    /**
     * Find a shopping cart object by database id.
     *
     * @param id the database id of the shopping cart entity object
     * @return a domain {@link ShoppingCart} object
     */
    @Override
    public ShoppingCart getById(Long id) {
      log.info("Getting shopping cart for id {}", id);
        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.getById(id);
        return shoppingCartEntity.map(ShoppingCartMapper.MAPPER::mapToDomain).orElse(null);
    }

    /**
     * checks and adds an application to the shopping cart if correct status
     * @param mainAccount the main account
     * @param application the application
     */
    @Override
    public void checkAndAddApplicationToShoppingCart(MainAccount mainAccount, Application application, String lastModifiedBy) {
        log.info("Adding application with number {} for user {} to shopping cart", application.getNumber(),mainAccount.getUsername());
        if(!application.getStatus().equalsIgnoreCase(applicationConfiguration.getStatus().getPayment())){
            log.info("Application will not be added to shopping cart because status is {}",application.getStatus());
            return;
        }
        ShoppingCart shoppingCart = getByUser(application.getMainAccount().getUsername());
        if(shoppingCart == null){
            //we dont have a shopping cart for this account so create one
            shoppingCart = create(application.getMainAccount().getUsername());
        }
        //now add the application to the shopping cart
        //first check if the application exists in the repo
        Optional<ShoppingCartApplicationEntity> shoppingCartApplicationEntityOptional = shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(application.getId());
        ShoppingCartApplicationEntity shoppingCartApplicationEntity;
        if (shoppingCartApplicationEntityOptional.isPresent()){
            shoppingCartApplicationEntity = shoppingCartApplicationEntityOptional.get();
        } else {
            //create it
            ShoppingCartApplication shoppingCartApplication = ShoppingCartApplicationMapper.MAPPER.mapApplicationToShoppingCartApplication(application, lastModifiedBy);
            if(application.getFoModule().equalsIgnoreCase(ApplicationType.TRADEMARK.value)){
                shoppingCartApplication.setIsDesign(false);
                shoppingCartApplication.setIsTrademark(true);
            }
            else if(application.getFoModule().equalsIgnoreCase(ApplicationType.DESIGN.value)){
                shoppingCartApplication.setIsDesign(true);
                shoppingCartApplication.setIsTrademark(false);
            }
            else if(application.getFoModule().equalsIgnoreCase(ApplicationType.ESERVICE.value)){
                shoppingCartApplication.setType(application.getEserviceName());
                if(application.getIpRightType().equalsIgnoreCase(SearchMessageType.DESIGNS.getValue())){
                    shoppingCartApplication.setIsDesign(true);
                    shoppingCartApplication.setIsTrademark(false);
                } else if(application.getIpRightType().equalsIgnoreCase(SearchMessageType.TRADEMARKS.getValue())){
                    shoppingCartApplication.setIsDesign(false);
                    shoppingCartApplication.setIsTrademark(true);
                }
            }
            shoppingCartApplicationEntity = ShoppingCartApplicationMapper.MAPPER.mapToEntity(shoppingCartApplication);
        }
        shoppingCartApplicationEntity.setShoppingCart(ShoppingCartMapper.MAPPER.mapToEntity(shoppingCart));
        shoppingCartApplicationRepository.save(shoppingCartApplicationEntity);
        log.info("Finished adding application with id {} to shopping cart",application.getId());
    }

    /**
     * retrieves the applications for the specific shopping cart
     * @param username the authenticated user
     * @param searchCriteriaShoppingCart the search criteria (sorting, page)
     * @param roles the assigned roles of the user
     * @return a list of applications for this shopping cart
     */
    @Override
    public ShoppingCartSearch getApplications(String username, SearchCriteriaShoppingCart searchCriteriaShoppingCart, Set<String> roles) {
        log.info("Getting applications for shopping cart for user {}",username);
        ShoppingCartSearch result = new ShoppingCartSearch();
        DomainAccount domainAccount = accountService.getMainAccount(username);
        ShoppingCart shoppingCart = getByUser(domainAccount.getUsername());
        if(shoppingCart == null){
            result.setContent(new ArrayList<>());
            return result;
        }
        Specification<ShoppingCartApplicationEntity> shoppingCartApplicationEntitySpecification = getShoppingCartSpecification(shoppingCart.getId(),roles);
        List<ShoppingCartApplicationEntity> applicationEntities = shoppingCartApplicationRepository.findAll(shoppingCartApplicationEntitySpecification);
        List<ShoppingCartApplication> applications = applicationEntities.stream().map(ShoppingCartApplicationMapper.MAPPER::mapToDomain).collect(Collectors.toList());
        if (searchCriteriaShoppingCart.getSortType().equalsIgnoreCase(ASCENDING)) {
            applications.sort(new ShoppingCartApplicationComparator(searchCriteriaShoppingCart.getSort()));
        } else {
            applications.sort(new ShoppingCartApplicationComparator(searchCriteriaShoppingCart.getSort()).reversed());
        }
        result.setContent(applications);
        return result;
    }

    /**
     * method that modifies (remove or delete) an application from the shopping cart.
     *
     * @param username the authenticated user
     * @param applicationId the application id
     * @param isApplicationDeleted flag to indicate that the application is going to be deleted
     * @param isSignatureDeleted flag to indicate that the signatures are going to be deleted
     * @return String the resume url if the action is to modify the application otherwise an empty string
     */
    @Override
    public String modifyApplication(String username, String applicationId, boolean isApplicationDeleted, boolean isSignatureDeleted) {
        log.info("Modify application with id {}",applicationId);
        DomainAccount domainAccount = accountService.getMainAccount(username);
        ShoppingCart shoppingCart = getByUser(domainAccount.getUsername());
        if(shoppingCart == null){
            log.error("Shopping cart not found for user {}",username);
            throw new EntityNotFoundException("");
        }
        Optional<ShoppingCartApplicationEntity> shoppingCartApplicationEntityOptional = shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(Long.parseLong(applicationId));
        if(!shoppingCartApplicationEntityOptional.isPresent()){
            log.error("Application with id {} not found in shopping cart", applicationId);
            throw new EntityNotFoundException(applicationId);
        }
        ShoppingCartApplicationEntity entity = shoppingCartApplicationEntityOptional.get();
        if(!entity.getShoppingCart().getId().equals(shoppingCart.getId())){
            log.error("Application does not belong to user {} shopping cart", username);
            throw new ShoppingCartSecurityException(applicationId);
        }
        shoppingCartApplicationRepository.delete(entity);
        return !isSignatureDeleted ? modifyApplications(username, entity.getNumber(), isApplicationDeleted) : StringUtils.EMPTY;
    }

    /**
     * Remove applications from shopping cart.
     * @param applicationId the application Id
     */
    @Override
    public void removeApplication(Long applicationId) {
        log.info("Remove application from shopping cart with id {}", applicationId);
        Optional<ShoppingCartApplicationEntity> shoppingCartApplicationEntityOptional = shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(applicationId);
        shoppingCartApplicationEntityOptional.ifPresent(shoppingCartApplicationEntity -> shoppingCartApplicationRepository.delete(shoppingCartApplicationEntity));
    }

    /**
     * Retrieves the applications for the specific list of application ids.
     *
     * @param ids the list with ids
     * @return a list of shopping cart applications
     */
    @Override
    public List<ShoppingCartApplication> getShoppingCartApplicationsByIds(List<Long> ids) {
        log.info("Retrieve shopping cart application from shopping cart with ids {}", ids);
        List<ShoppingCartApplicationEntity> applicationEntities = shoppingCartApplicationRepository.findByApplicationIdIn(ids);
        return applicationEntities.stream().map(ShoppingCartApplicationMapper.MAPPER::mapToDomain).collect(Collectors.toList());
    }

    /**
     * Retrieves the applications for the specific list of application ids.
     *
     * @param numbers the list with numbers
     * @return a list of shopping cart applications
     */
    @Override
    public List<ShoppingCartApplication> getShoppingCartApplicationsByNumbers(List<String> numbers) {
        log.info("Retrieve shopping cart application from shopping cart with numbers {}", numbers);
        List<ShoppingCartApplicationEntity> applicationEntities = shoppingCartApplicationRepository.findByNumberIn(numbers);
        return applicationEntities.stream().map(ShoppingCartApplicationMapper.MAPPER::mapToDomain).collect(Collectors.toList());
    }

    /**
     * Modify/delete the applications by changing the status and soft deleting the signatures.
     *
     * @param username the username
     * @param applicationNumber the applicationNumber
     * @param delete delete an application
     */
    private String modifyApplications(String username, String applicationNumber, boolean delete) {
        if (delete) {
            return signatureService.deleteApplication(username, applicationNumber);
        } else {
            return signatureService.modifyApplication(username, applicationNumber);
        }
    }

}
