package com.bloatit.framework.webserver.url;

import com.bloatit.framework.webserver.masters.Linkable;

public class UrlStringBinder extends Url {

    private final String url;

    public UrlStringBinder(final String url) {
        super("");
        this.url = url;
    }

    @Override
    public Linkable createPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doRegister() {
        throw new UnsupportedOperationException();

    }

    @Override
    public UrlComponent clone() {
        return new UrlStringBinder(url);
    }

    @Override
    protected void constructUrl(final StringBuilder sb) {
        sb.append(url);
    }

}