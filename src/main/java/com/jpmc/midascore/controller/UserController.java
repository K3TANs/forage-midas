package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/balance")
    public ResponseEntity<Balance> getBalance(@RequestParam Long userId){
        Optional<UserRecord> userRecord=userRepository.findById(userId);
        return userRecord.map(user->ResponseEntity.ok(new Balance(user.getBalance())))
                .orElseGet(() -> ResponseEntity.ok(new Balance(0)));

    }
}
