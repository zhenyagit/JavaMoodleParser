package org.imjs_man.moodleParser.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.imjs_man.moodleParser.entity.supporting.SuperEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


public class RequestCounter {

    private static final Logger logger = LoggerFactory.getLogger(RequestCounter.class);
    private final List<Long> ids = new CopyOnWriteArrayList<>();
    private final List<String> types = new CopyOnWriteArrayList<>();

    public synchronized <T extends SuperEntity> void addItem(T item)
    {
        ids.add(item.getId());
        types.add(item.getClass().getName());
//        logger.info("Item "+item.getClass().getName() + " " + item.getId()+ " added. #: " + getCount());
    }

    public Integer getCount()
    {
        return ids.size();
    }

    public synchronized <T extends SuperEntity> int findIndexOfElement(T item)
    {
        int index;
        for (index = 0; index<ids.size(); index++)
        {
            if (ids.get(index) == item.getId() && Objects.equals(types.get(index), item.getClass().getName())) {
                break;
            }
        }
        System.out.println(item.getId() + " : " + ids.get(index) + "     " + item.getClass() + " : " + types.get(index));
        return index;
    }

    public synchronized <T extends SuperEntity> void removeItem(T item)
    {
//        int indexToRemove = findIndexOfElement(item);
//      logger.info(String.valueOf(indexToRemove));
        ids.remove(item.getId());
        types.remove(item.getClass().getName());
//        logger.info("Item "+item.getClass().getName() + " " + item.getId()+ " removed. #: " + getCount());

        // todo block method or variables by while(zanyat)
    }

}
