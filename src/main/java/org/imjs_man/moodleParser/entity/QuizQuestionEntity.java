package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

@Entity
public class QuizQuestionEntity {
    @Id
    private long id;
    private String questionText;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<QuizAnswerEntity> answerList;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<QuizAnswerEntity> trueAnswerList;

}
