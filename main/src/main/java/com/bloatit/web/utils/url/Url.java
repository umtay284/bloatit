package com.bloatit.web.utils.url;

import com.bloatit.web.server.Context;

public abstract class Url extends UrlComponent {

    private final String name;

    protected Url(final String name) {
        super();
        this.name = name;
    }

    @Override
    protected void constructUrl(final StringBuilder sb) {
        if (Context.getSession() != null) {
            sb.append("/").append(Context.getSession().getLanguage().getCode());
        }
        sb.append("/").append(name);
        super.constructUrl(sb);
    }
}
