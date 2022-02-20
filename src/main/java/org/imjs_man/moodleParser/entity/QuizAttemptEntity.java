package org.imjs_man.moodleParser.entity;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class QuizAttemptEntity extends SuperEntity implements Comparable<QuizAttemptEntity>{

    private int attemptNumber;
    private String attemptState;
    private double nowMark;
    private double maxMark;
    private String href;
    @OneToOne
    private PersonEntity owner;
    @OneToOne
    private QuizEntity quizEntity;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<QuizQuestionEntity> quizQuestionEntityList;

    @Override
    public int compareTo(QuizAttemptEntity otherQuizAttempt) {
        return Integer.compare((int)getId(), (int)otherQuizAttempt.getId());
    }

    public double getMaxMark() {
        return maxMark;
    }

    public void setMaxMark(double maxMark) {
        this.maxMark = maxMark;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getAttemptState() {
        return attemptState;
    }

    public void setAttemptState(String attemptState) {
        this.attemptState = attemptState;
    }

    public double getNowMark() {
        return nowMark;
    }

    public void setNowMark(double nowMark) {
        this.nowMark = nowMark;
    }

    public PersonEntity getOwner() {
        return owner;
    }

    public void setOwner(PersonEntity owner) {
        this.owner = owner;
    }

    public QuizEntity getQuizEntity() {
        return quizEntity;
    }

    public void setQuizEntity(QuizEntity quizEntity) {
        this.quizEntity = quizEntity;
    }

    public Set<QuizQuestionEntity> getQuizQuestionEntityList() {
        return quizQuestionEntityList;
    }

    public void setQuizQuestionEntityList(Set<QuizQuestionEntity> quizQuestionEntityList) {
        this.quizQuestionEntityList = quizQuestionEntityList;
    }
}

