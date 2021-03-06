//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.web.linkable.master;

import com.bloatit.framework.exceptions.lowlevel.NonOptionalParameterException;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlLink;
import com.bloatit.framework.webprocessor.components.HtmlSpan;
import com.bloatit.framework.webprocessor.components.meta.HtmlBranch;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Member;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.HtmlTools;
import com.bloatit.web.components.MoneyDisplayComponent;
import com.bloatit.web.linkable.members.MemberPage;
import com.bloatit.web.url.ChangeLanguagePageUrl;
import com.bloatit.web.url.LoginPageUrl;
import com.bloatit.web.url.LogoutActionUrl;
import com.bloatit.web.url.MemberPageUrl;
import com.bloatit.web.url.SignUpPageUrl;

public class SessionBar extends HtmlDiv {

    private static String SESSION_BAR_COMPONENT_CSS_CLASS = "session_bar_component";

    protected SessionBar(Url url) {
        super();
        setId("session_bar");
        final HtmlLink loginLink = new LoginPageUrl(url.urlString()).getHtmlLink(Context.trc("Login (verb)", "Login"));
        final HtmlLink signupLink = new SignUpPageUrl().getHtmlLink(Context.trc("Sign up (verb)", "Sign up"));
        final HtmlLink changeLanguageLink = new ChangeLanguagePageUrl().getHtmlLink(Context.getLocalizator().getLanguageCode());
        add(new HtmlSpan().setCssClass("small_session_bar_component").add(changeLanguageLink));
        add(new HtmlSpan().setCssClass(SESSION_BAR_COMPONENT_CSS_CLASS).add(loginLink));
        add(new HtmlSpan().setCssClass(SESSION_BAR_COMPONENT_CSS_CLASS).add(signupLink));

    }

    protected SessionBar(final Member me) {
        super();
        if (me == null) {
            throw new NonOptionalParameterException();
        }
        setId("session_bar");

        // Display user name
        String displayName = "John Doe";
        displayName = me.getDisplayName();
        final HtmlLink memberLink = new MemberPageUrl(me).getHtmlLink(displayName);

        // Display user karma

        final HtmlBranch karma = new HtmlSpan();
        if (me.isActive()) {
            karma.setCssClass("karma");
            karma.addAttribute("title", Context.tr("My karma"));
            karma.addText(HtmlTools.compressKarma(me.getKarma()));
        }
        final HtmlLink changeLanguageLink = new ChangeLanguagePageUrl().getHtmlLink(Context.getLocalizator().getLanguageCode());
        add(new HtmlSpan().setCssClass("small_session_bar_component").add(changeLanguageLink));
        add(new HtmlSpan().setCssClass(SESSION_BAR_COMPONENT_CSS_CLASS).add(memberLink).add(karma));

        try {
            add(new HtmlSpan().setCssClass(SESSION_BAR_COMPONENT_CSS_CLASS).add(new MoneyDisplayComponent(me.getInternalAccount().getAmount(),
                                                                                                          me,
                                                                                                          Context.getLocalizator())));
        } catch (final UnauthorizedOperationException e) {
            Context.getSession()
                   .notifyWarning(Context.tr("An unexpected error prevent us from displaying your internal account amount. Please notify us."));
        }

        // Display link to private messages
        long nb;
        if ((nb = (me.getInvitationCount() + me.getMilestoneToInvoice().size())) > 0) {
            final HtmlLink messagesLink = MemberPage.myMessagesUrl(me).getHtmlLink(Context.tr("Tasks ({0})", nb));
            messagesLink.setCssClass("bold");
            final HtmlBranch componentSpan = new HtmlSpan().setCssClass(SESSION_BAR_COMPONENT_CSS_CLASS).add(messagesLink);
            add(componentSpan);
        }

        // Display logout link
        final HtmlLink logoutLink = new LogoutActionUrl(Context.getSession().getShortKey()).getHtmlLink(Context.tr("Logout"));
        add(new HtmlSpan().setCssClass(SESSION_BAR_COMPONENT_CSS_CLASS).add(logoutLink));

    }
}
