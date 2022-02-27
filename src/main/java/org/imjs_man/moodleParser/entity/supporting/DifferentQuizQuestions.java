package org.imjs_man.moodleParser.entity.supporting;

import org.imjs_man.moodleParser.entity.dataBase.ComparisonQuizQuestionEntity;

import java.util.ArrayList;

public class DifferentQuizQuestions {
    private ArrayList<ComparisonQuizQuestionEntity> comparisonQuizQuestions;

    public ArrayList<ComparisonQuizQuestionEntity> getComparisonQuizQuestions() {
        return comparisonQuizQuestions;
    }

    public void setComparisonQuizQuestions(ArrayList<ComparisonQuizQuestionEntity> comparisonQuizQuestions) {
        this.comparisonQuizQuestions = comparisonQuizQuestions;
    }
}
