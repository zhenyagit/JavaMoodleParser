package org.imjs_man.moodleParser.parser.decryptor;

import org.imjs_man.moodleParser.entity.dataBase.ExerciseEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizEntity;

import java.util.Set;

public class QuiExeLists {
    private Set<QuizEntity> quizes;
    private Set<ExerciseEntity> exercises;

    public Set<QuizEntity> getQuizes() {
        return quizes;
    }

    public void setQuizes(Set<QuizEntity> quizes) {
        this.quizes = quizes;
    }

    public Set<ExerciseEntity> getExercises() {
        return exercises;
    }

    public void setExercises(Set<ExerciseEntity> exercises) {
        this.exercises = exercises;
    }
}
