package com.bloatit.model;

import java.util.Date;

import javax.persistence.MappedSuperclass;

import com.bloatit.model.data.DaoUserContent;

@MappedSuperclass
public abstract class UserContent extends Identifiable {

    protected abstract DaoUserContent getDaoUserContent();
    
	public Member getAuthor() {
		return new Member(getDaoUserContent().getAuthor());
	}

	public Date getCreationDate() {
		return getDaoUserContent().getCreationDate();
	}

	public void setAsGroup(Group asGroup) {
		getDaoUserContent().setAsGroup(asGroup.getDao());
	}

	public Group getAsGroup() {
		return new Group(getDaoUserContent().getAsGroup());
	}

	@Override
	protected final int getDaoId(){
	    return getDaoUserContent().getId();
	}
	
}
