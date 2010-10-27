package com.bloatit.framework.lists;

import java.util.Iterator;

import com.bloatit.model.Group;
import com.bloatit.model.data.DaoGroup;

public class GroupIterator extends com.bloatit.framework.lists.IteratorBinder<Group, DaoGroup> {

    public GroupIterator(Iterable<DaoGroup> daoIterator) {
        super(daoIterator);
    }

    public GroupIterator(Iterator<DaoGroup> daoIterator) {
        super(daoIterator);
    }

    @Override
    protected Group createFromDao(DaoGroup dao) {
        return new Group(dao);
    }

}
