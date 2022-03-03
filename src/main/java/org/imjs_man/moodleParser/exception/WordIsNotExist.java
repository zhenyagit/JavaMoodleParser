package org.imjs_man.moodleParser.exception;

public class WordIsNotExist extends Exception {
    public WordIsNotExist(String message) {
        super(message);
    }
    public WordIsNotExist()
    {
        super("No word in database");
    }
}