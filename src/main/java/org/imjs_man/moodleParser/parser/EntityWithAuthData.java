package org.imjs_man.moodleParser.parser;

import com.fasterxml.classmate.GenericType;
import net.bytebuddy.description.type.TypeList;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.imjs_man.moodleParser.entity.QuizEntity;
import org.imjs_man.moodleParser.parser.service.AuthData;

public class EntityWithAuthData<T extends Comparable<T>> implements Comparable<EntityWithAuthData<T>>{
    private T entity;
    private AuthData authData;

    @Override
    public int compareTo(EntityWithAuthData<T> o) {
        return entity.compareTo(o.getEntity());
    }
    public EntityWithAuthData(T entity, AuthData authData) {
        this.entity = entity;
        this.authData = authData;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public AuthData getAuthData() {
        return authData;
    }

    public void setAuthData(AuthData authData) {
        this.authData = authData;
    }
}
