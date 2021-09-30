package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class QuizAnswerEntity {
    @Id
    private long id;
    private String answerText;
}
