package org.imjs_man.moodleParser.entity.dataBase;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.imjs_man.moodleParser.entity.supporting.SuperEntity;
import javax.persistence.*;

@Entity
@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class),
        @TypeDef(name = "int-array", typeClass = IntArrayType.class)
})
public class ComparisonQuizQuestionEntity extends SuperEntity implements Comparable<ComparisonQuizQuestionEntity>{

    private int state;
    private double mark;
    private double maxMark;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String name;
    @Type(type = "string-array")
    @Column(columnDefinition = "text[]")
    private String[] listOfQuestions;
    @Type(type = "string-array")
    @Column(columnDefinition = "text[]")
    private String[] listOfVariants;
    @Type(type = "int-array")
    @Column(columnDefinition = "int[]")
    private Integer[] listOfChoice;
    @Type(type = "int-array")
    @Column(columnDefinition = "int[]")
    private Integer[] listOfResult;
    @OneToOne
    private PersonEntity owner;
    @ManyToOne
    private QuizAttemptEntity quizAttemptEntity;

    @Override
    public int compareTo(ComparisonQuizQuestionEntity otherQuizQuestion) {
        return Integer.compare((int)getId(), (int)otherQuizQuestion.getId());
    }

    public double getMaxMark() {
        return maxMark;
    }

    public void setMaxMark(double maxMark) {
        this.maxMark = maxMark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PersonEntity getOwner() {
        return owner;
    }

    public void setOwner(PersonEntity owner) {
        this.owner = owner;
    }

    public QuizAttemptEntity getQuizAttemptEntity() {
        return quizAttemptEntity;
    }

    public void setQuizAttemptEntity(QuizAttemptEntity quizAttemptEntity) {
        this.quizAttemptEntity = quizAttemptEntity;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public double getMark() {
        return mark;
    }

    public void setMark(double mark) {
        this.mark = mark;
    }

    public String[] getListOfQuestions() {
        return listOfQuestions;
    }

    public void setListOfQuestions(String[] listOfQuestions) {
        this.listOfQuestions = listOfQuestions;
    }

    public String[] getListOfVariants() {
        return listOfVariants;
    }

    public void setListOfVariants(String[] listOfVariants) {
        this.listOfVariants = listOfVariants;
    }

    public Integer[] getListOfChoice() {
        return listOfChoice;
    }

    public void setListOfChoice(Integer[] listOfChoice) {
        this.listOfChoice = listOfChoice;
    }

    public Integer[] getListOfResult() {
        return listOfResult;
    }

    public void setListOfResult(Integer[] listOfResult) {
        this.listOfResult = listOfResult;
    }

    //    public ArrayList<String> getListOfQuestions() {
//        return listOfQuestions;
//    }
//
//    public void setListOfQuestions(ArrayList<String> listOfQuestions) {
//        this.listOfQuestions = listOfQuestions;
//    }
//
//    public ArrayList<String> getListOfVariants() {
//        return listOfVariants;
//    }
//
//    public void setListOfVariants(ArrayList<String> listOfVariants) {
//        this.listOfVariants = listOfVariants;
//    }
//
//    public ArrayList<Integer> getListOfChoice() {
//        return listOfChoice;
//    }
//
//    public void setListOfChoice(ArrayList<Integer> listOfChoice) {
//        this.listOfChoice = listOfChoice;
//    }
//
//    public ArrayList<Boolean> getListOfResult() {
//        return listOfResult;
//    }
//
//    public void setListOfResult(ArrayList<Boolean> listOfResult) {
//        this.listOfResult = listOfResult;
//    }
}
