package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class QuizQuestionEntity {
    @Id
    private long id;
    private String questionText;
    @OneToMany
    private List<QuizAnswerEntity> answerList;
    @OneToMany
    private List<QuizAnswerEntity> trueAnswerList;

}
