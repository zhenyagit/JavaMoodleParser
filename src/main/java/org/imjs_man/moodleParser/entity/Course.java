package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Course {
    @Id
    private long id;
    private String name;
    @OneToMany
    private List<Exercise> exerciseList;
    @OneToMany
    private List<Quiz> quizList;

}
