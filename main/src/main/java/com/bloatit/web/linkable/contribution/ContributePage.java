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
package com.bloatit.web.linkable.contribution;

import static com.bloatit.framework.webserver.Context.tr;

import com.bloatit.framework.exceptions.RedirectException;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.annotations.ParamConstraint;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.annotations.tr;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.HtmlTitleBlock;
import com.bloatit.framework.webserver.components.form.FieldData;
import com.bloatit.framework.webserver.components.form.HtmlForm;
import com.bloatit.framework.webserver.components.form.HtmlMoneyField;
import com.bloatit.framework.webserver.components.form.HtmlSubmit;
import com.bloatit.framework.webserver.components.form.HtmlTextArea;
import com.bloatit.framework.webserver.components.meta.HtmlElement;
import com.bloatit.model.Feature;
import com.bloatit.web.components.SideBarFeatureBlock;
import com.bloatit.web.linkable.features.FeaturePage;
import com.bloatit.web.pages.LoggedPage;
import com.bloatit.web.pages.documentation.SideBarDocumentationBlock;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.pages.master.TwoColumnLayout;
import com.bloatit.web.url.CheckContributionActionUrl;
import com.bloatit.web.url.ContributePageUrl;

/**
 * A page that hosts the form used to contribute on a Feature
 */
@ParamContainer("contribute")
public final class ContributePage extends LoggedPage {


    @ParamConstraint(optionalErrorMsg=@tr("The process is closed, expired, missing or invalid."))
    @RequestParam
    private final ContributionProcess process;

    private final ContributePageUrl url;

    public ContributePage(final ContributePageUrl url) {
        super(url);
        this.url = url;
        process = url.getProcess();
    }

    @Override
    public void processErrors() throws RedirectException {
        addNotifications(url.getMessages());
        if (url.getMessages().hasMessage()) {
            session.notifyList(url.getMessages());
            throw new RedirectException(Context.getSession().getLastStablePage());
        }

    }

    @Override
    public HtmlElement createRestrictedContent() throws RedirectException {

        final TwoColumnLayout layout = new TwoColumnLayout(true);
        layout.addLeft(generateContributeForm());

        layout.addRight(new SideBarFeatureBlock(process.getFeature()));
        layout.addRight(new SideBarDocumentationBlock("markdown"));

        return layout;
    }

    public HtmlElement generateContributeForm() {
        final CheckContributionActionUrl formActionUrl = new CheckContributionActionUrl(process);

        final HtmlForm contribForm = new HtmlForm(formActionUrl.urlString());

        // Input field : choose amount
        final FieldData amountData = formActionUrl.getAmountParameter().pickFieldData();
        final HtmlMoneyField contribInput = new HtmlMoneyField(amountData.getName(), tr("Choose amount: "));
        String suggestedAmountValue = amountData.getSuggestedValue();
        if(suggestedAmountValue != null) {
            contribInput.setDefaultValue(suggestedAmountValue);
        } else if (process.getAmount() != null){
            contribInput.setDefaultValue(process.getAmount().toPlainString());
        }
        contribInput.addErrorMessages(amountData.getErrorMessages());
        contribInput.setComment(Context.tr("The minimun is 1€. Don't use cents."));

        // Input field : comment
        final FieldData commentData = formActionUrl.getCommentParameter().pickFieldData();
        final HtmlTextArea commentInput = new HtmlTextArea(commentData.getName(), tr("Comment: "), 3, 60);
        String suggestedCommentValue = commentData.getSuggestedValue();
        if(suggestedCommentValue != null) {
            commentInput.setDefaultValue(suggestedCommentValue);
        } else if (process.getComment() != null){
            commentInput.setDefaultValue(process.getComment());
        }
        commentInput.addErrorMessages(commentData.getErrorMessages());
        commentInput.setComment(Context.tr("Optional. The comment will be publicly visible in the contribution list. Max 140 characters."));

        final HtmlSubmit submitButton = new HtmlSubmit(tr("Contribute"));

        // Create the form
        contribForm.add(contribInput);
        contribForm.add(commentInput);
        contribForm.add(submitButton);

        final HtmlTitleBlock contribTitle = new HtmlTitleBlock(tr("Contribute"), 1);
        contribTitle.add(contribForm);

        final HtmlDiv group = new HtmlDiv();
        group.add(contribTitle);

        return group;
    }

    @Override
    protected String getPageTitle() {
        return tr("Contribute to a feature");
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public String getRefusalReason() {
        return tr("You must be logged to contribute");
    }

    @Override
    protected Breadcrumb getBreadcrumb() {
        return ContributePage.generateBreadcrumb(process.getFeature(), process);
    }

    public static Breadcrumb generateBreadcrumb(Feature feature, ContributionProcess process) {
        Breadcrumb breadcrumb = FeaturePage.generateBreadcrumbContributions(feature);

        breadcrumb.pushLink(new ContributePageUrl(process).getHtmlLink(tr("Contribute")));

        return breadcrumb;
    }
}