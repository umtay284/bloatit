package com.bloatit.web.html.pages.master;

import com.bloatit.web.html.components.standard.HtmlDiv;

public class Notification extends HtmlDiv {

    public enum Level {
        INFO, WARNING, ERROR
    }

    public Notification(final Level level, final String message) {
        super();
        switch (level) {
        case INFO:
            setCssClass("notification_good");
            break;
        case WARNING:
            setCssClass("notification_bad");
            break;
        case ERROR:
            setCssClass("notification_error");
            break;
        }
        addText(message);
    }

}