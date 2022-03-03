package org.imjs_man.moodleParser.exception;

public class BufferIsNotLoaded extends Exception {
    public BufferIsNotLoaded(String message) {
        super(message);
    }
    public BufferIsNotLoaded()
    {
        super("You forgot to load the buffer!");
    }
}
