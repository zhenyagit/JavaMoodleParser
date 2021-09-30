package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.PersonEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


public interface PersonRepository extends CrudRepository<PersonEntity, Long> {
    PersonEntity findByToken(String token);
    PersonEntity findById(long token);
}
