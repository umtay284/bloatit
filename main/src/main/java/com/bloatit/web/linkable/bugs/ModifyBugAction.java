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
package com.bloatit.web.linkable.bugs;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import com.bloatit.data.DaoBug.BugState;
import com.bloatit.data.DaoBug.Level;
import com.bloatit.framework.webprocessor.annotations.MaxConstraint;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.components.form.FormComment;
import com.bloatit.framework.webprocessor.components.form.FormField;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Bug;
import com.bloatit.model.right.AuthToken;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.linkable.master.ElveosAction;
import com.bloatit.web.url.BugPageUrl;
import com.bloatit.web.url.ModifyBugActionUrl;
import com.bloatit.web.url.ModifyBugPageUrl;

/**
 * A response to a form used to create a new feature
 */
@ParamContainer("bugs/%bug%/domodify")
public final class ModifyBugAction extends ElveosAction {

    @Optional
    @RequestParam(role = Role.POST)
    @MaxConstraint(max = 120, message = @tr("The reason must be %constraint% chars length max."))
    @FormField(label = @tr("Reason"), isShort = false)
    @FormComment(@tr("Optional. Enter why you want to modify this bug."))
    private final String reason;

    @NonOptional(@tr("You must indicate a bug level"))
    @RequestParam(role = Role.POST)
    @FormField(label = @tr("New Level"), isShort = true)
    private final BindedLevel level;

    @NonOptional(@tr("You must indicate a bug state"))
    @RequestParam(role = Role.POST)
    @FormField(label = @tr("New state"), isShort = true)
    private final BindedState state;

    @NonOptional(@tr("A bug change must be linked to a bug"))
    @RequestParam(role = Role.PAGENAME)
    private final Bug bug;

    private final ModifyBugActionUrl url;

    public ModifyBugAction(final ModifyBugActionUrl url) {
        super(url);
        this.url = url;

        this.level = url.getLevel();
        this.state = url.getState();
        this.bug = url.getBug();
        this.reason = url.getReason();
    }

    @Override
    protected Url doProcess() {
        final Level currentLevel = bug.getErrorLevel();
        final BugState currentState = bug.getState();

        if (currentLevel == level.getLevel() && currentState == state.getState()) {
            session.notifyWarning(Context.tr("You must change at least a small thing on the bug to modify it."));
            return doProcessErrors();
        }

        if (state.getState() == BugState.PENDING && currentState != BugState.PENDING) {
            session.notifyWarning(Context.tr("You cannot set a bug to the pending state."));
            return doProcessErrors();
        }

        String changes = "";
        if (currentLevel != level.getLevel()) {
            changes += tr("%LEVEL% {0} => {1}", "%" + BindedLevel.getBindedLevel(currentLevel) + "%", "%" + level + "%") + "\n";
        }

        if (currentState != state.getState()) {
            changes += tr("%STATE% {0} => {1}", "%" + BindedState.getBindedState(currentState) + "%", "%" + state + "%") + "\n";
        }

        try {
            bug.setErrorLevel(level.getLevel());
            if (state.getState() == BugState.DEVELOPING) {
                bug.setDeveloping();
            } else if (state.getState() == BugState.RESOLVED) {
                bug.setResolved();
            }
            if (reason == null) {
                bug.addComment(changes);
            } else {
                bug.addComment(changes + "\n%REASON%\n" + reason);
            }
        } catch (final UnauthorizedOperationException e) {
            session.notifyError(Context.tr("You are not allowed to change the state of this bug."));
            return doProcessErrors();
        }

        return new BugPageUrl(bug);
    }

    @Override
    protected Url doProcessErrors() {
        return Context.getSession().getLastVisitedPage();
    }

    @Override
    protected Url checkRightsAndEverything() {
        if (!AuthToken.isAuthenticated()) {
            session.notifyError(Context.tr("You must be logged in to modify a bug report."));
            return new ModifyBugPageUrl(bug);
        }
        return NO_ERROR;
    }

    @Override
    protected void transmitParameters() {
        session.addParameter(url.getReasonParameter());
        session.addParameter(url.getBugParameter());
        session.addParameter(url.getLevelParameter());
        session.addParameter(url.getStateParameter());
    }

}
