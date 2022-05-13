package org.imjs_man.moodleParser.entity.dataBase;

import com.google.gson.annotations.Expose;
import org.imjs_man.moodleParser.entity.supporting.SuperEntity;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ExerciseEntity extends SuperEntity {
    private int maxAttempts;
    @Expose()
    @Lob
    @Column(columnDefinition = "TEXT")
    private String exerciseText;
    @Expose()
    private String name;
    private String href;
    @ManyToOne
    private CourseEntity courseEntity;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<ExerciseAttemptEntity> exerciseAttemptEntityList;

    public CourseEntity getCourseEntity() {
        return courseEntity;
    }

    public void setCourseEntity(CourseEntity courseEntity) {
        this.courseEntity = courseEntity;
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

    public Set<ExerciseAttemptEntity> getExerciseAttemptEntityList() {
        return exerciseAttemptEntityList;
    }

    public void setExerciseAttemptEntityList(Set<ExerciseAttemptEntity> exerciseAttemptEntityList) {
        this.exerciseAttemptEntityList = exerciseAttemptEntityList;
    }
}
