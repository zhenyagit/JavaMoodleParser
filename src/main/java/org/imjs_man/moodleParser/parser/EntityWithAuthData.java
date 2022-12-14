package org.imjs_man.moodleParser.parser;

import org.imjs_man.moodleParser.entity.supporting.SuperEntity;
import org.imjs_man.moodleParser.parser.service.AuthData;

public class EntityWithAuthData<T extends Comparable<T>> implements Comparable<EntityWithAuthData<T>>{
    private T entity;
    private AuthData authData;

    public EntityWithAuthData(AuthData authData, T entity) {
        this.entity = entity;
        this.authData = authData;
    }

    @Override
    public int compareTo(EntityWithAuthData<T> o) {
        return entity.compareTo(o.getEntity());
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
