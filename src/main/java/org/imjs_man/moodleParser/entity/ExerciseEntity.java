package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class ExerciseEntity {
    @Id
    private long id;
    private int exerciseState;
    private int maxAttempts;
    private int nowAttempts;
    @OneToMany
    private List<ExerciseAttemptEntity> exerciseAttemptEntityList;
}
