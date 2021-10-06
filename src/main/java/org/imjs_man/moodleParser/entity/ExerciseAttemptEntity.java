package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

@Entity
public class ExerciseAttemptEntity {
    @Id
    private long id;
    private int exerciseState;
    private double exerciseMark;
    private String comment;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<AnswerFileEntity> answerFileList;

}
