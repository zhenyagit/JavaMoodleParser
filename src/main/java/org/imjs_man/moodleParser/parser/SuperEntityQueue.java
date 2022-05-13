package org.imjs_man.moodleParser.parser;

import org.imjs_man.moodleParser.entity.supporting.SuperEntity;
import org.imjs_man.moodleParser.exception.NoItemInQueue;
import org.imjs_man.moodleParser.parser.service.AuthData;

import java.util.ArrayList;

public class SuperEntityQueue {
    private final ArrayList<EntityWithAuthData<SuperEntity>> mainQueue = new ArrayList<>();

    public void addToQueue(EntityWithAuthData<SuperEntity> entityEntityWithAuthData)
    {
        mainQueue.add(entityEntityWithAuthData);
    }
    public void addToQueue(AuthData authData, SuperEntity superEntity)
    {
        mainQueue.add(new EntityWithAuthData<SuperEntity>(authData, superEntity));
    }
    public EntityWithAuthData<SuperEntity> getFromQueue() throws NoItemInQueue {
        if (mainQueue.size()==0)
        {
            throw new NoItemInQueue("No more items in super entity queue");
        }

        EntityWithAuthData<SuperEntity> temp = mainQueue.get(0);
        mainQueue.remove(0);
        return temp;
    }
    public Boolean isEmpty()
    {
        return (mainQueue.size()==0);
    }


}
