/*
 * Copyright (C) 2010 BloatIt.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BloatIt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.html.pages;

import com.bloatit.web.actions.AccountChargingAction;
import com.bloatit.web.exceptions.RedirectException;
import com.bloatit.web.html.HtmlElement;
import com.bloatit.web.html.HtmlText;
import com.bloatit.web.html.components.standard.HtmlTitleBlock;
import com.bloatit.web.html.components.standard.form.HtmlForm;
import com.bloatit.web.html.components.standard.form.HtmlTextField;
import com.bloatit.web.utils.url.Request;
import com.bloatit.web.utils.url.UrlBuilder;

/**
 * A page used to put money onto the internal bloatit account
 */
public class AccountChargingPage extends LoggedPage {

    public AccountChargingPage(final Request request) throws RedirectException {
        super(request);
        request.setValues(this);
    }

    @Override
    protected String getTitle() {
        return session.tr("Charge your bloatit account");
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public HtmlElement generateRestrictedContent() {
        HtmlTitleBlock title = new HtmlTitleBlock(session.tr("Charge your account"),1);

        UrlBuilder chargeActionUrl = new UrlBuilder(AccountChargingAction.class);
        HtmlForm form = new HtmlForm(chargeActionUrl.buildUrl());
        {
            HtmlTextField amount = new HtmlTextField(AccountChargingAction.CHARGE_AMOUNT_CODE, "Amount");

            form.add(amount);
        }
        title.add(form);

        return title;
    }

    @Override
    public String getRefusalReason() {
        return "You need to login before you can charge your account";
    }
}
