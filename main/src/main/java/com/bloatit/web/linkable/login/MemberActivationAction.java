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
package com.bloatit.web.linkable.login;

import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.ParamContainer.Protocol;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.components.meta.HtmlMixedText;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.context.User.ActivationState;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Member;
import com.bloatit.model.managers.MemberManager;
import com.bloatit.model.right.AuthToken;
import com.bloatit.web.linkable.master.ElveosAction;
import com.bloatit.web.url.IndexPageUrl;
import com.bloatit.web.url.MemberActivationActionUrl;
import com.bloatit.web.url.ModifyMemberPageUrl;

/**
 * A response to a form used to create a new feature
 */
@ParamContainer(value = "member/doactivate", protocol = Protocol.HTTPS)
public final class MemberActivationAction extends ElveosAction {

    @RequestParam
    private final String login;

    @RequestParam(role = Role.GET)
    private final String key;

    // Keep it for consistency
    @SuppressWarnings("unused")
    private final MemberActivationActionUrl url;

    public MemberActivationAction(final MemberActivationActionUrl url) {
        super(url);
        this.url = url;

        this.login = url.getLogin();
        this.key = url.getKey();
    }

    @Override
    protected Url doProcess() {
        final Member member = MemberManager.getMemberByLogin(login);

        if (member != null) {
            if (member.getActivationState() == ActivationState.VALIDATING) {
                if (member.activate(key)) {

                    // Auto login after activation
                    AuthToken.authenticate(member);
                    session.notifyGood(new HtmlMixedText(Context.tr("Activation success, you are now logged. You can <0::add an avatar and introduce yourself>."),
                                                         new ModifyMemberPageUrl().getHtmlLink()));

                } else {
                    session.notifyWarning(Context.tr("Wrong activation key for this member."));
                }
            } else if (member.hasEmailToActivate()) {
                if (member.activateEmail(key)) {
                    session.notifyGood(Context.tr("Email activation success."));
                } else {
                    session.notifyWarning(Context.tr("Wrong email activation key for this member."));
                }

            } else {
                session.notifyWarning(Context.tr("No activation needed for this member."));
            }
        } else {
            session.notifyWarning(Context.tr("Activation impossible on a not existing member."));
        }

        return session.getLastStablePage();
    }

    @Override
    protected Url doProcessErrors() {
        return new IndexPageUrl();
    }

    @Override
    protected Url checkRightsAndEverything() {
        return NO_ERROR; // Nothing else to check
    }

    @Override
    protected void transmitParameters() {
        // No post parameter to transmit.
    }
}
