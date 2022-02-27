package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.springframework.data.repository.CrudRepository;

public interface CourseRepository extends CrudRepository<CourseEntity, Long> {
    CourseEntity findById(long id);
}
