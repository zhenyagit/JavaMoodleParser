package org.imjs_man.moodleParser.entity.dataBase;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class IndexDictionaryEntity {
    @Id
    private long id;
    private String word;

    public IndexDictionaryEntity() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public IndexDictionaryEntity(long id, String word) {
        this.id = id;
        this.word = word;
    }
}
