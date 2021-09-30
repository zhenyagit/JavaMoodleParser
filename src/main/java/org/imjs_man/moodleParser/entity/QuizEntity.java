package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class QuizEntity {
    @Id
    private long id;
    private int quizState;
    private double maxMark;
    private double nowMark;
    @OneToMany
    private List<QuizAttemptEntity> attemptList;


}
