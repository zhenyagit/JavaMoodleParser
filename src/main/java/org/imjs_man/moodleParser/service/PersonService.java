package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.PersonEntity;
import org.imjs_man.moodleParser.exception.CantAuthoriseInMoodle;
import org.imjs_man.moodleParser.form.AuthForm;
import org.imjs_man.moodleParser.parser.MoodleParser;
import org.imjs_man.moodleParser.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private MoodleParser moodleParser;

    public String auth(AuthForm authForm) throws CantAuthoriseInMoodle {
            return moodleParser.auth(authForm.getUsername(), authForm.getPassword());
    }

}
