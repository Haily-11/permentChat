package com.shanyangcode.userservice.controller;

import com.shanyangcode.userservice.service.UserSessionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RequestMapping("/api/user")
@RestController
public class UserSessionController {

        @Resource
        UserSessionService userSessionService;

        @GetMapping("/get/receivers")
        List<Long> getUserIdBySessionId(@RequestParam("sessionId") Long sessionId){
            return userSessionService.getUserIdBySessionId(sessionId);
        }

        @GetMapping("/get/sessions")
        List<Long> getSessionIdByUserId(@RequestParam("userId") Long userId){
            return userSessionService.getSessionIdsByUserId(userId);
        }
}

