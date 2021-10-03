package org.imjs_man.moodleParser.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class CourseEntity {
    @Id
    private long id;
    private String name;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String summary;
    private int summaryformat;
    private int startdate;
    private int enddate;
    private int progress;
    private String fullnamedisplay;
    private String coursecategory;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String courseimage;
    private Boolean hasprogress;
    private Boolean isfavourite;
    private Boolean hidden;

    @OneToMany
    private List<ExerciseEntity> exerciseEntityList;
    @OneToMany
    private List<QuizEntity> quizEntityList;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getSummaryformat() {
        return summaryformat;
    }

    public void setSummaryformat(int summaryformat) {
        this.summaryformat = summaryformat;
    }

    public int getStartdate() {
        return startdate;
    }

    public void setStartdate(int startdate) {
        this.startdate = startdate;
    }

    public int getEnddate() {
        return enddate;
    }

    public void setEnddate(int enddate) {
        this.enddate = enddate;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getFullnamedisplay() {
        return fullnamedisplay;
    }

    public void setFullnamedisplay(String fullnamedisplay) {
        this.fullnamedisplay = fullnamedisplay;
    }

    public String getCoursecategory() {
        return coursecategory;
    }

    public void setCoursecategory(String coursecategory) {
        this.coursecategory = coursecategory;
    }

    public String getCourseimage() {
        return courseimage;
    }

    public void setCourseimage(String courseimage) {
        this.courseimage = courseimage;
    }

    public Boolean getHasprogress() {
        return hasprogress;
    }

    public void setHasprogress(Boolean hasprogress) {
        this.hasprogress = hasprogress;
    }

    public Boolean getIsfavourite() {
        return isfavourite;
    }

    public void setIsfavourite(Boolean isfavourite) {
        this.isfavourite = isfavourite;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExerciseEntity> getExerciseEntityList() {
        return exerciseEntityList;
    }

    public void setExerciseEntityList(List<ExerciseEntity> exerciseEntityList) {
        this.exerciseEntityList = exerciseEntityList;
    }

    public List<QuizEntity> getQuizEntityList() {
        return quizEntityList;
    }

    public void setQuizEntityList(List<QuizEntity> quizEntityList) {
        this.quizEntityList = quizEntityList;
    }
}
