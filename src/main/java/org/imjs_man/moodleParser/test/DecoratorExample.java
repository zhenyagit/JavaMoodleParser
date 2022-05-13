package org.imjs_man.moodleParser.test;

import org.imjs_man.moodleParser.entity.supporting.SuperEntity;

public class DecoratorExample extends SuperEntity {
    private SuperEntity superEntity;
    public DecoratorExample(SuperEntity entity)
    {
        superEntity = entity;
    }
    public void someProcess()
    {

    }

}
