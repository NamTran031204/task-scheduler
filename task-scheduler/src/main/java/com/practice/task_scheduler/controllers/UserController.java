package com.practice.task_scheduler.controllers;

import com.practice.task_scheduler.entities.dtos.UserDTO;
import com.practice.task_scheduler.entities.dtos.UserLoginDTO;
import com.practice.task_scheduler.entities.responses.CalendarResponse;
import com.practice.task_scheduler.entities.responses.UserListResponse;
import com.practice.task_scheduler.entities.responses.UserResponse;
import com.practice.task_scheduler.services.CalendarService;
import com.practice.task_scheduler.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    private final CalendarService calendarService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO){
        UserResponse userResponse = userService.register(userDTO);
        return ResponseEntity.ok(userResponse);

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO){
        return ResponseEntity.ok(userService.login(userLoginDTO.getEmail(), userLoginDTO.getPassword()));
    }

    @PostMapping(
            value = "/update_avatar/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadAvatar(
            @PathVariable("id") Long id,
            @ModelAttribute("file") MultipartFile file
    ){
        return ResponseEntity.ok(userService.updateAvatar(id, file));

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/get-all-user")
    public ResponseEntity<?> getAllUser(
            @Param("record") int record,
            @Param("page") int page
    ){
        PageRequest request = PageRequest.of(page, record, Sort.by("username").descending());
        Page<UserResponse> userResponsePage = userService.getAllUser(request);

        int totalPage = userResponsePage.getTotalPages();

        List<UserResponse> userResponses =userResponsePage.getContent();

        return ResponseEntity.ok(UserListResponse.builder()
                        .totalPage(totalPage)
                        .userResponses(userResponses)
                        .build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInfor(
            @PathVariable("id") long id,
            @ModelAttribute("username") String username,
            @ModelAttribute("fullname") String fullName,
            @ModelAttribute("avatar") MultipartFile avatar
    ){
        userService.updateInfor(id, username, fullName, avatar);
        return ResponseEntity.ok("update complete");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") long id){
        userService.deleteUser(id);
        return ResponseEntity.ok("Delete Complete");
    }

    @GetMapping("/calendar-tasks/{userId}")
    public ResponseEntity<?> calendar(
            @PathVariable("userId") long userId,
            @RequestParam("start-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("end-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        return ResponseEntity.ok(calendarService.getTasksForCalendar(userId, startDate, endDate));
    }
}
