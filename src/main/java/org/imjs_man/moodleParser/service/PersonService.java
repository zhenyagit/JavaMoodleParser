package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.dataBase.PersonEntity;
import org.imjs_man.moodleParser.exception.CantAuthoriseInMoodle;
import org.imjs_man.moodleParser.form.AuthForm;
import org.imjs_man.moodleParser.parser.MoodleParser;
import org.imjs_man.moodleParser.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private MoodleParser moodleParser;

    public String auth(AuthForm authForm) throws CantAuthoriseInMoodle
    {
        PersonEntity newPerson = moodleParser.auth(authForm.getUsername(), authForm.getPassword());
        return newPerson.getToken();
//        return "asdasd";
    }
    public String getTokenById(long id)
    {
        return personRepository.findById(id).getToken();
    }
    public Boolean checkToken(String token)
    {
        return personRepository.findByToken(token) != null;
    }
    public Boolean checkId(long id)
    {
        return personRepository.findById(id) != null;
    }
    public List<PersonEntity> getPersonsToParse()
    {
        return (List<PersonEntity>) personRepository.findAll();
    }
    public void savePerson(PersonEntity person)
    {
        personRepository.save(person);
    }


}
