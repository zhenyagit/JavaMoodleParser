package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.CourseEntity;
import org.imjs_man.moodleParser.entity.PersonEntity;
import org.imjs_man.moodleParser.exception.CantGetCoursesList;
import org.imjs_man.moodleParser.parser.MoodleParser;
import org.imjs_man.moodleParser.repository.CourseRepository;
import org.imjs_man.moodleParser.repository.PersonRepository;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private MoodleParser moodleParser;

    public Boolean checkId(Integer id) {
        return courseRepository.findById(id) != null;
    }

    public void loadCoursesByToken(String token) throws ParseException, CantGetCoursesList {
        PersonEntity temp = personRepository.findByToken(token);
        List<CourseEntity> courses = moodleParser.getParsedCoursesList(moodleParser.getRawCoursesList(temp.getLogin(), temp.getPassword()));
        courseRepository.saveAll(courses);
    }
    public void saveMany(List<CourseEntity> newPersons)
    {
        courseRepository.saveAll(newPersons);
    }
    public List<CourseEntity> getAllCourses()
    {
        return (List<CourseEntity>) courseRepository.findAll();
    }
}