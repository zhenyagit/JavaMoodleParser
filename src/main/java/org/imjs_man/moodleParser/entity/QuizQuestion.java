package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class QuizQuestion {
    @Id
    private long id;
    private String questionText;
    @OneToMany
    private List<QuizAnswer> answerList;
    @OneToMany
    private List<QuizAnswer> trueAnswerList;

}
