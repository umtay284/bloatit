package com.bloatit.framework.lists;

import java.util.Iterator;

import com.bloatit.framework.Translation;
import com.bloatit.model.data.DaoTranslation;

public class TranslationIterator extends com.bloatit.framework.lists.IteratorBinder<Translation, DaoTranslation> {

    public TranslationIterator(Iterable<DaoTranslation> daoIterator) {
        super(daoIterator);
    }

    public TranslationIterator(Iterator<DaoTranslation> daoIterator) {
        super(daoIterator);
    }

    @Override
    protected Translation createFromDao(DaoTranslation dao) {
        return Translation.create(dao);
    }

}
