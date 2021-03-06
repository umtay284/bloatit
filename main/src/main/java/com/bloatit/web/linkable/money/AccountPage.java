/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details. You should have received a copy of the GNU Affero General Public
 * License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.linkable.money;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.webprocessor.PageNotFoundException;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.ParamContainer.Protocol;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.Member;
import com.bloatit.model.Team;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.components.AccountComponent;
import com.bloatit.web.linkable.documentation.SideBarDocumentationBlock;
import com.bloatit.web.linkable.master.Breadcrumb;
import com.bloatit.web.linkable.master.LoggedElveosPage;
import com.bloatit.web.linkable.master.sidebar.TwoColumnLayout;
import com.bloatit.web.linkable.members.MemberPage;
import com.bloatit.web.linkable.team.TeamPage;
import com.bloatit.web.url.AccountPageUrl;

/**
 * <p>
 * A page used to display logged member informations.
 * </p>
 */
@ParamContainer(value = "account", protocol = Protocol.HTTPS)
public final class AccountPage extends LoggedElveosPage {

    @RequestParam(message = @tr("I cannot find the team number: ''%value%''."))
    @Optional
    private final Team team;

    private final AccountPageUrl url;

    public AccountPage(final AccountPageUrl url) {
        super(url);
        this.url = url;
        team = url.getTeam();
    }

    @Override
    protected String createPageTitle() {
        return Context.tr("Account informations");
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    public String getRefusalReason() {
        return Context.tr("You must be logged to show your account informations.");
    }

    @Override
    public HtmlElement createRestrictedContent(final Member loggedUser) throws PageNotFoundException {
        try {
            if (isTeamAccount()) {
                if (!loggedUser.hasBankTeamRight(team)) {
                    getSession().notifyWarning(tr("You haven't the right to see ''{0}'' group account.", team.getLogin()));
                    throw new PageNotFoundException();
                }
            }

            final TwoColumnLayout layout = new TwoColumnLayout(true, url);

            if (isTeamAccount()) {
                layout.addLeft(new AccountComponent(team));
            } else {
                layout.addLeft(new AccountComponent(loggedUser));
            }

            layout.addRight(new SideBarDocumentationBlock("internal_account"));
            layout.addRight(new SideBarLoadAccountBlock(team));
            layout.addRight(new SideBarWithdrawMoneyBlock(loggedUser));

            return layout;
        } catch (final UnauthorizedOperationException e) {
            throw new ShallNotPassException("Right error.", e);
        }
    }

    private boolean isTeamAccount() {
        return team != null;
    }

    @Override
    protected Breadcrumb createBreadcrumb(final Member member) {
        if (isTeamAccount()) {
            return AccountPage.generateBreadcrumb(team);
        }
        return AccountPage.generateBreadcrumb(member);

    }

    protected static Breadcrumb generateBreadcrumb(final Team team) {
        final Breadcrumb breadcrumb = TeamPage.generateBreadcrumb(team);
        breadcrumb.pushLink(new AccountPageUrl().getHtmlLink(tr("Account informations")));
        return breadcrumb;
    }

    protected static Breadcrumb generateBreadcrumb(final Member member) {
        final Breadcrumb breadcrumb = MemberPage.generateBreadcrumb(member);
        breadcrumb.pushLink(new AccountPageUrl().getHtmlLink(tr("Account informations")));
        return breadcrumb;
    }
}
