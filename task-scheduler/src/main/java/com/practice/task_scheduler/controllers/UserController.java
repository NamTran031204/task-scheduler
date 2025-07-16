package com.practice.task_scheduler.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.task_scheduler.entities.dtos.UserDTO;
import com.practice.task_scheduler.entities.dtos.UserLoginDTO;
import com.practice.task_scheduler.entities.responses.UserResponse;
import com.practice.task_scheduler.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO){

        try{
            UserResponse userResponse = userService.register(userDTO);
            return ResponseEntity.ok(userResponse);

        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO){
        return ResponseEntity.ok(userService.login(userLoginDTO.getEmail(), userLoginDTO.getPassword()));
    }
}
