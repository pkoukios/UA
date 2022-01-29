/*
 * $Id:: LockReleaseScheduler.java 2021/03/11 08:50 tantonop
 * . * .
 *  RRRR * Copyright (c) 2012-2021 EUIPO: European Intelectual
 * . RR R . Property Organization (trademarks and designs).
 *  RRR *
 * . RR RR . ALL RIGHTS RESERVED
 * . _ .*
 * The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.business.core.impl.scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class that checks tables for locked entities and releases the lock if the time has passed
 */
@Component
@Slf4j
public class LockReleaseScheduler {


    @Autowired
    private ApplicationContext appContext;
    @Value("${userarea.lockRepoProcess}")
    private String lockedTables;
    @Value("${userarea.locktimeout}")
    private String locktimeout;
    private static HashMap<String,String> repoMap = new HashMap<>();

    static {
        repoMap.put("MESSAGES","messageRepository");
        repoMap.put("APPLICATIONS","applicationRepository");
        repoMap.put("NOTES","noteApplicationRepository");
        repoMap.put("ACCOUNTS","accountRepository");
    }

    /**
     * Releases the locks from the specified repositories
     */
    @SneakyThrows
    @Scheduled(cron = "${userarea.lockscheduler}")
    @SchedulerLock(name = "LockReleaseScheduler_releaseLocks",
            lockAtLeastForString = "${userarea.lockschedulerMin}", lockAtMostForString = "${userarea.lockschedulerMax}")
    public void releaseLocks(){

        List<String> alLockedTables = Arrays.asList(lockedTables.split(","));
        for(String table:alLockedTables) {
            String beanName = repoMap.get(table);
            CrudRepository repo = (CrudRepository)appContext.getBean(beanName);
            Iterable repoList = (Iterable)repo.getClass().getMethod("findByLockedByIsNotNull").invoke(repo);
            for(Object object:repoList){
                LocalDateTime lockedDate = (LocalDateTime)object.getClass().getMethod("getLockedDate").invoke(object);
                Long id = (Long)object.getClass().getMethod("getId").invoke(object);
                if(lockedDate!=null){
                    long difference = System.currentTimeMillis() - Timestamp.valueOf(lockedDate).getTime();
                    long minutes = TimeUnit.SECONDS.toMinutes(Long.parseLong(this.locktimeout) * 1000 - difference);
                    if(minutes <= 0){
                        log.debug("Releasing lock for object with id {} from table {}",id,table);
                        object.getClass().getMethod("setLockedBy",String.class).invoke(object,new Object[]{ null });
                        object.getClass().getMethod("setLockedDate",LocalDateTime.class).invoke(object,new Object[]{ null });
                        repo.save(object);
                        log.debug("Released lock for object with id {} from table {}",id,table);
                    }
                }
            }
        }
    }

}
