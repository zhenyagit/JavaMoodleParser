package org.imjs_man.moodleParser.parser;

import org.imjs_man.moodleParser.prettyTable.PrettyTable;
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
        types.add(item.getClass().getSimpleName());
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
        ids.remove(item.getId());
        types.remove(item.getClass().getSimpleName());
    }

    public List<Long> getIds() {
        return ids;
    }

    public List<String> getTypes() {
        return types;
    }
}
