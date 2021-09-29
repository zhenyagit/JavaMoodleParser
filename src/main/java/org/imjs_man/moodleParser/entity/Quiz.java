package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
public class Quiz extends QuizAttempt {
    @Id
    private long id;
    private int quizState;
    private double maxMark;
    private double nowMark;
//    private List<QuizAttempt> attemptList;


}
