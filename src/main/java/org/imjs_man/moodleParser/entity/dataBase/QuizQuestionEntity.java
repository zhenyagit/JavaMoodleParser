package org.imjs_man.moodleParser.entity.dataBase;

import com.google.gson.annotations.Expose;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class QuizQuestionEntity {
    @Id
    private long id;
    @Expose()
    private String questionText;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<QuizAnswerEntity> answerList;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<QuizAnswerEntity> trueAnswerList;

}
