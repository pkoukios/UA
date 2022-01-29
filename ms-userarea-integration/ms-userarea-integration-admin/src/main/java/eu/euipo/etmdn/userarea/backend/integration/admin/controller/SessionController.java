/*
 * $Id:: SessionController.java 2021/06/25 10:51 tantonop
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

package eu.euipo.etmdn.userarea.backend.integration.admin.controller;

import eu.euipo.etmdn.userarea.common.security.oauth2.oryhydra.config.ActiveUserStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sessions")
@AllArgsConstructor
public class SessionController {

    private ActiveUserStore activeUserStore;

    /**
     * returns all the active sessions
     * @return the number of active sessions
     */
    @GetMapping
    public ResponseEntity<Integer> getActiveSessions(){
        return new ResponseEntity<>(this.activeUserStore.getUsers().keySet().size(), HttpStatus.OK);
    }

}
