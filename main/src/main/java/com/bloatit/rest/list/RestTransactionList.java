package com.bloatit.rest.list;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.bloatit.framework.utils.PageIterable;
import com.bloatit.model.Transaction;
import com.bloatit.rest.list.master.RestListBinder;
import com.bloatit.rest.resources.RestTransaction;

/**
 * <p>
 * Wraps a list of Transaction into a list of RestElements
 * </p>
 * <p>
 * This class can be represented in Xml as a list of Transaction<br />
 * Example:
 * 
 * <pre>
 * {@code <Transactions>}
 *     {@code <Transaction name=Transaction1 />}
 *     {@code <Transaction name=Transaction2 />}
 * {@code </Transactions>}
 * </pre>
 * <p>
 */
@XmlRootElement
public class RestTransactionList extends RestListBinder<RestTransaction, Transaction> {
    /**
     * Creates a RestTransactionList from a {@codePageIterable<Transaction>}
     * 
     * @param collection the list of elements from the model
     */
    public RestTransactionList(final PageIterable<Transaction> collection) {
        super(collection);
    }

    /**
     * This method is provided only to be able to represent the list as XmL
     */
    @XmlElementWrapper(name = "transactions")
    @XmlElement(name = "transaction")
    public RestTransactionList getTransactions() {
        return this;
    }
}