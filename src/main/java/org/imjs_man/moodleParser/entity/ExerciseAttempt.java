package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class ExerciseAttempt {
    @Id
    private long id;
    private int exerciseState;
    private double exerciseMark;
    private String comment;
    @OneToMany
    private List<AnswerFile> answerFileList;

}
