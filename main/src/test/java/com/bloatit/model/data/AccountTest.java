package com.bloatit.model.data;

import com.bloatit.common.FatalErrorException;
import com.bloatit.model.data.DaoExternalAccount.AccountType;
import com.bloatit.model.data.util.NonOptionalParameterException;

public class AccountTest extends ModelTestUnit {

    public void testDaoAccountDaoActor() {
        final DaoMember localTom = DaoMember.getByLogin(tom.getLogin());
        localTom.setExternalAccount(DaoExternalAccount.createAndPersist(localTom, AccountType.IBAN, "Bank code !"));

        final DaoGroup localB219 = DaoGroup.getByName(b219.getLogin());
        localB219.setExternalAccount(DaoExternalAccount.createAndPersist(localB219, AccountType.IBAN, "Bank code !"));

        try {
            localTom.setExternalAccount(DaoExternalAccount.createAndPersist(null, AccountType.IBAN, "Bank code !"));
            fail();
        } catch (final NonOptionalParameterException e) {
            assertTrue(true);
        }
        try {
            localTom.setExternalAccount(DaoExternalAccount.createAndPersist(localTom, null, "Bank code !"));
            fail();
        } catch (final NonOptionalParameterException e) {
            assertTrue(true);
        }
        try {
            localTom.setExternalAccount(DaoExternalAccount.createAndPersist(localTom, AccountType.IBAN, null));
            fail();
        } catch (final NonOptionalParameterException e) {
            assertTrue(true);
        }
        try {
            localTom.setExternalAccount(DaoExternalAccount.createAndPersist(localTom, AccountType.IBAN, ""));
            fail();
        } catch (final NonOptionalParameterException e) {
            assertTrue(true);
        }

        try {
            localTom.setExternalAccount(DaoExternalAccount.createAndPersist(localB219, AccountType.IBAN, "code"));
            fail();
        } catch (final FatalErrorException e) {
            assertTrue(true);
        }
    }

}
