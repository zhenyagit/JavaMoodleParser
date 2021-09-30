package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class CourseEntity {
    @Id
    private long id;
    private String name;
    @OneToMany
    private List<ExerciseEntity> exerciseEntityList;
    @OneToMany
    private List<QuizEntity> quizEntityList;

}
