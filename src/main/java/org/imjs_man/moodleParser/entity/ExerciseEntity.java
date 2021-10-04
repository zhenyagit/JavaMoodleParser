package org.imjs_man.moodleParser.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class ExerciseEntity {
    @Id
    private long id;
    private int maxAttempts;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String exerciseText;
    private String name;
    private String href;
    @OneToMany
    private List<ExerciseAttemptEntity> exerciseAttemptEntityList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getExerciseText() {
        return exerciseText;
    }

    public void setExerciseText(String exerciseText) {
        this.exerciseText = exerciseText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<ExerciseAttemptEntity> getExerciseAttemptEntityList() {
        return exerciseAttemptEntityList;
    }

    public void setExerciseAttemptEntityList(List<ExerciseAttemptEntity> exerciseAttemptEntityList) {
        this.exerciseAttemptEntityList = exerciseAttemptEntityList;
    }
}
