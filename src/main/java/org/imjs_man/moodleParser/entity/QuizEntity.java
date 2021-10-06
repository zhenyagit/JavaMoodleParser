package org.imjs_man.moodleParser.entity;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class QuizEntity {
    @Id
    private long id;
    private int quizState;
    private double maxMark;
    private double nowMark;
    private String name;
    private String href;
    @ManyToOne
    private CourseEntity courseEntity;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<QuizAttemptEntity> attemptList;

    public CourseEntity getCourseEntity() {
        return courseEntity;
    }

    public void setCourseEntity(CourseEntity courseEntity) {
        this.courseEntity = courseEntity;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getQuizState() {
        return quizState;
    }

    public void setQuizState(int quizState) {
        this.quizState = quizState;
    }

    public double getMaxMark() {
        return maxMark;
    }

    public void setMaxMark(double maxMark) {
        this.maxMark = maxMark;
    }

    public double getNowMark() {
        return nowMark;
    }

    public void setNowMark(double nowMark) {
        this.nowMark = nowMark;
    }

    public Set<QuizAttemptEntity> getAttemptList() {
        return attemptList;
    }

    public void setAttemptList(Set<QuizAttemptEntity> attemptList) {
        this.attemptList = attemptList;
    }
}
