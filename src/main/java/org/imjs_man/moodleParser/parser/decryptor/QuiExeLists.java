package org.imjs_man.moodleParser.parser.decryptor;

import org.imjs_man.moodleParser.entity.dataBase.ExerciseEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizEntity;

import java.util.ArrayList;
import java.util.Set;

public class QuiExeLists {
    private ArrayList<QuizEntity> quizes;
    private ArrayList<ExerciseEntity> exercises;

    public ArrayList<QuizEntity> getQuizes() {
        return quizes;
    }

    public void setQuizes(ArrayList<QuizEntity> quizes) {
        this.quizes = quizes;
    }

    public ArrayList<ExerciseEntity> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<ExerciseEntity> exercises) {
        this.exercises = exercises;
    }
}
