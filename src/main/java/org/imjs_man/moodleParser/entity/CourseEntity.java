package org.imjs_man.moodleParser.entity;


import javax.persistence.*;
import java.util.Set;

@Entity
public class CourseEntity extends SuperEntity implements Comparable<CourseEntity>{
    private String name;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;
    private int summaryformat;
    private int startdate;
    private int enddate;
    private int progress;
    private String fullnamedisplay;
    private String coursecategory;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String courseimage;
    private Boolean hasprogress;
    private Boolean isfavourite;
    private Boolean hidden;
    @ManyToMany
    private Set<PersonEntity> personEntityList;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<ExerciseEntity> exerciseEntityList;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<QuizEntity> quizEntityList;

    @Override
    public int compareTo(CourseEntity otherCourse) {
        return Integer.compare((int)getId(), (int)otherCourse.getId());
    }
    public Set<PersonEntity> getPersonEntityList() {
        return personEntityList;
    }
    public void setPersonEntityList(Set<PersonEntity> personEntityList) {
        this.personEntityList = personEntityList;
    }
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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Set<ExerciseEntity> getExerciseEntityList() {
        return exerciseEntityList;
    }
    public void setExerciseEntityList(Set<ExerciseEntity> exerciseEntityList) {
        this.exerciseEntityList = exerciseEntityList;
    }
    public Set<QuizEntity> getQuizEntityList() {
        return quizEntityList;
    }
    public void setQuizEntityList(Set<QuizEntity> quizEntityList) {
        this.quizEntityList = quizEntityList;
    }
    public void addQuizEntityList(Set<QuizEntity> quizEntities) {
        this.quizEntityList.addAll(quizEntities);
    }
    public void addExerciseEntityList(Set<ExerciseEntity> exerciseEntities) {
        this.exerciseEntityList.addAll(exerciseEntities);
    }
}
