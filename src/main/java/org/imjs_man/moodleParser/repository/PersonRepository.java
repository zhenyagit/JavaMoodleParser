package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.PersonEntity;
import org.springframework.data.repository.CrudRepository;


public interface PersonRepository extends CrudRepository<PersonEntity, Long> {
    PersonEntity findByToken(String token);
    PersonEntity findById(long token);
}
