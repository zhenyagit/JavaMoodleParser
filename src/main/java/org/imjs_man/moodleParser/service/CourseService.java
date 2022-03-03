package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.imjs_man.moodleParser.parser.MoodleParser;
import org.imjs_man.moodleParser.repository.CourseRepository;
import org.imjs_man.moodleParser.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public Boolean isExist(long id) {
        return courseRepository.existsById(id);
    }

    public CourseEntity getById(long id)
    {
        return courseRepository.findById(id);
    }
    public void saveAll(ArrayList<CourseEntity> newCourses)
    {
        courseRepository.saveAll(newCourses);
    }
    public ArrayList<CourseEntity> getAllCourses()
    {
        return (ArrayList<CourseEntity>) courseRepository.findAll();
    }
    public void saveOne(CourseEntity courseEntity)
    {
        courseRepository.save(courseEntity);
    }
    public ArrayList<CourseEntity> getForIndexing()
    {
        return courseRepository.findByIndexesLowIsNull();
    }

}