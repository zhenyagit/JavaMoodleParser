package org.imjs_man.moodleParser.test;

import org.imjs_man.moodleParser.entity.dataBase.PersonEntity;
import org.imjs_man.moodleParser.entity.supporting.SuperEntity;
import org.imjs_man.moodleParser.parser.EntityWithAuthData;
import org.imjs_man.moodleParser.parser.service.AuthData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class TestFeature {

    private final PriorityQueue<EntityWithAuthData<SuperEntity>> mainQueue = new PriorityQueue<>();


    @Test
    public void addPersonsToQueue() {
        ArrayList<PersonEntity> personEntities = new ArrayList<>();
        for (int i=0; i<5;i++)
        {
            PersonEntity temp = new PersonEntity();
            temp.setName("Name "+String.valueOf(i));
            temp.setId(i);
            personEntities.add(temp);
        }
        for (PersonEntity person : personEntities) {
            AuthData authData = new AuthData();
            EntityWithAuthData<SuperEntity> temp = new EntityWithAuthData<>(authData, person);
            mainQueue.add(temp);


        }
        while (mainQueue.peek()!=null)
        {
            EntityWithAuthData<SuperEntity> temp = mainQueue.poll();
            someMethod(temp.getAuthData(), temp.getEntity());
        }
    }

    public void rounded(SuperEntity superEntity) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(superEntity.getClass().getName());
        someMethod(clazz.newInstance());
    }

    private void someMethod(Object newInstance) {
    }


    public void someMethod(AuthData authData, SuperEntity superEntity)
    {
        System.out.println(superEntity.getId() + " Super " + superEntity.getClass().getSimpleName());
    }
    public void someMethod(AuthData authData, PersonEntity person)
    {

        System.out.println(person.getId() + " " + person.getName());
    }
}
