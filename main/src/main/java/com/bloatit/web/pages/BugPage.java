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
package com.bloatit.web.pages;

import static com.bloatit.framework.webserver.Context.tr;

import com.bloatit.framework.exceptions.RedirectException;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.PageNotFoundException;
import com.bloatit.framework.webserver.annotations.ParamConstraint;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.annotations.tr;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.HtmlParagraph;
import com.bloatit.framework.webserver.components.HtmlTitle;
import com.bloatit.framework.webserver.components.form.FormFieldData;
import com.bloatit.framework.webserver.components.form.HtmlFileInput;
import com.bloatit.framework.webserver.components.form.HtmlForm;
import com.bloatit.framework.webserver.components.form.HtmlSubmit;
import com.bloatit.framework.webserver.components.form.HtmlTextField;
import com.bloatit.framework.webserver.components.meta.XmlNode;
import com.bloatit.framework.webserver.components.renderer.HtmlRawTextRenderer;
import com.bloatit.model.Bug;
import com.bloatit.model.FileMetadata;
import com.bloatit.web.actions.ReportBugAction;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.pages.tools.CommentTools;
import com.bloatit.web.url.AddAttachementActionUrl;
import com.bloatit.web.url.BugPageUrl;
import com.bloatit.web.url.FileResourceUrl;

@ParamContainer("demand/bug")
public final class BugPage extends MasterPage {

    public static final String BUG_FIELD_NAME = "id";

    @ParamConstraint(optionalErrorMsg = @tr("The id of the project is incorrect or missing"))
    @RequestParam(name = BUG_FIELD_NAME)
    private final Bug bug;

    private final BugPageUrl url;

    public BugPage(final BugPageUrl url) {
        super(url);
        this.url = url;
        this.bug = url.getBug();
    }

    @Override
    protected void doCreate() throws RedirectException {
        session.notifyList(url.getMessages());
        if (url.getMessages().hasMessage()) {
            throw new PageNotFoundException();
        }

        final HtmlDiv box = new HtmlDiv("padding_box");

        HtmlTitle bugTitle;
        bugTitle = new HtmlTitle(bug.getTitle(), 1);
        box.add(bugTitle);

        final HtmlParagraph description = new HtmlParagraph(new HtmlRawTextRenderer(bug.getDescription()));
        box.add(description);


        //Attachements

        box.add(generateNewAttachementForm());

        for(FileMetadata attachement: bug.getFiles()) {
            final HtmlParagraph attachementPara = new HtmlParagraph();
            attachementPara.add(new FileResourceUrl(attachement).getHtmlLink(attachement.getFileName()));
            attachementPara.addText(tr(": ")+attachement.getShortDescription());
            box.add(attachementPara);
        }

        box.add(CommentTools.generateCommentList(bug.getComments()));
        box.add(CommentTools.generateNewCommentComponent(bug));

        add(box);


    }

    private XmlNode generateNewAttachementForm() {

        final AddAttachementActionUrl addAttachementActionUrl = new AddAttachementActionUrl();
        addAttachementActionUrl.setUserContent(bug);


        final HtmlForm addAttachementForm = new HtmlForm(addAttachementActionUrl.urlString());
        addAttachementForm.enableFileUpload();


        //File

        final HtmlFileInput attachementInput = new HtmlFileInput(ReportBugAction.ATTACHEMENT_CODE, Context.tr("Attachement file"));
        attachementInput.setComment("Optional. If attach a file, you must add an attachement description. Max 2go.");
        addAttachementForm.add(attachementInput);

        final FormFieldData<String> attachementDescriptionFormFieldData = addAttachementActionUrl.getAttachementDescriptionParameter().formFieldData();
        final HtmlTextField attachementDescriptionInput = new HtmlTextField(attachementDescriptionFormFieldData, Context.tr("Attachment description"));
        attachementDescriptionInput.setComment(Context.tr("Need only if you add an attachement."));
        addAttachementForm.add(attachementDescriptionInput);


        addAttachementForm.add(new HtmlSubmit(Context.tr("Add attachement")));


        return addAttachementForm;
    }

    @Override
    protected String getPageTitle() {
        if (bug != null) {
            return tr("Bug - ") + bug.getTitle();
        }
        return tr("Bug - No bug");
    }

    @Override
    public boolean isStable() {
        return true;
    }
}
