package org.imjs_man.moodleParser.parser;

import com.fasterxml.classmate.GenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.imjs_man.moodleParser.entity.SuperEntity;

import java.util.ArrayList;
import java.util.Objects;


public class RequestCounter {

    private static final Logger logger = LoggerFactory.getLogger(RequestCounter.class);
    private final ArrayList<Long> ids= new ArrayList<>();
    private final ArrayList<String> types= new ArrayList<>();

    public <T extends SuperEntity> void addItem(T item)
    {
        ids.add(item.getId());
        types.add(item.getClass().getName());
        logger.info("Item "+item.getClass().getName() + " " + item.getId()+ " added. #: " + getCount());
    }

    public Integer getCount()
    {
        return ids.size();
    }

    public <T extends SuperEntity> int findIndexOfElement(T item)
    {
        int index;
        for (index = 0; index<ids.size(); index++)
        {
            if (ids.get(index) == item.getId() && Objects.equals(types.get(index), item.getClass().getName()))
                break;
        }
        return index;
    }

    public <T extends SuperEntity> void removeItem(T item)
    {
        int indexToRemove = findIndexOfElement(item);
        ids.remove(indexToRemove);
        types.remove(indexToRemove);
        logger.info("Item "+item.getClass().getName() + " " + item.getId()+ " removed. #: " + getCount());

    }

}
