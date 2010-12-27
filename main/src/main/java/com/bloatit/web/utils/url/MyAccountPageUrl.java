package com.bloatit.web.utils.url;

import com.bloatit.web.exceptions.RedirectException;

@SuppressWarnings("unused")
public class MyAccountPageUrl extends Url {
    public static String getName() {
        return "myaccount";
    }

    @Override
    public com.bloatit.web.html.pages.MyAccountPage createPage() throws RedirectException {
        return new com.bloatit.web.html.pages.MyAccountPage(this);
    }

    public MyAccountPageUrl(final Parameters params, final Parameters session) {
        this();
        parseParameters(params, false);
        parseParameters(session, true);
    }

    public MyAccountPageUrl() {
        super(getName());
    }

    @Override
    protected void doRegister() {
    }

    @Override
    public MyAccountPageUrl clone() {
        final MyAccountPageUrl other = new MyAccountPageUrl();
        return other;
    }
}
