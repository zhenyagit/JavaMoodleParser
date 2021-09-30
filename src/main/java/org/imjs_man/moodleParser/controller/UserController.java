package org.imjs_man.moodleParser.controller;

import org.imjs_man.moodleParser.exception.CantAuthoriseInMoodle;
import org.imjs_man.moodleParser.form.AuthForm;
import org.imjs_man.moodleParser.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private PersonService personService;

    @GetMapping
    public ResponseEntity auth(@RequestBody AuthForm authForm) {
        try {
            return ResponseEntity.ok(personService.auth(authForm));
        } catch (CantAuthoriseInMoodle e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
