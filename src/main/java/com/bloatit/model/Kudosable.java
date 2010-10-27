package com.bloatit.model;

import javax.persistence.MappedSuperclass;

import com.bloatit.model.data.DaoKudosable;
import com.bloatit.model.data.DaoKudosable.State;
import com.bloatit.model.data.DaoUserContent;

@MappedSuperclass
public abstract class Kudosable extends UserContent {

    protected abstract DaoKudosable getDaoKudosable();
    
    public int addKudos(Member member, int value) {
        return getDaoKudosable().addKudos(member.getDao(), value);
    }

    public State getState() {
        return getDaoKudosable().getState();
    }

    public void setValidated() {
        getDaoKudosable().setValidated();
    }

    public void setRejected() {
        getDaoKudosable().setRejected();
    }
    
    @Override
    protected final DaoUserContent getDaoUserContent(){
        return getDaoKudosable();
    }

}
