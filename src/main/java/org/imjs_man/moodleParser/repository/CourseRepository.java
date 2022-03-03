package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface CourseRepository extends CrudRepository<CourseEntity, Long> {
    CourseEntity findById(long id);
    ArrayList<CourseEntity>  findByIndexesLowIsNull();
}
