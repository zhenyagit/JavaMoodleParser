package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AnswerFileEntity {
    @Id
    private long id;
    private String name;
    private String linkToFile;
}
