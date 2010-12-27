/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free software: you
 * can redistribute it and/or modify it under the terms of the GNU Affero General Public
 * License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. BloatIt is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero General
 * Public License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.html.pages.idea;

import java.util.Locale;

import com.bloatit.framework.Demand;
import com.bloatit.framework.Translation;
import com.bloatit.web.html.HtmlTools;
import com.bloatit.web.html.components.standard.HtmlDiv;
import com.bloatit.web.html.components.standard.HtmlLink;
import com.bloatit.web.html.components.standard.HtmlParagraph;
import com.bloatit.web.server.Context;
import com.bloatit.web.server.Session;
import com.bloatit.web.utils.url.MemberPageUrl;

public class IdeaDescriptionComponent extends HtmlDiv {

    private final HtmlParagraph description;
    private final HtmlParagraph date;
    private final HtmlLink author;

    public IdeaDescriptionComponent(final Demand demand) {
        super();

        final Session session = Context.getSession();
        final Locale defaultLocale = session.getLanguage().getLocale();
        final Translation translatedDescription = demand.getDescription().getTranslationOrDefault(defaultLocale);
        description = new HtmlParagraph(translatedDescription.getText());

        date = new HtmlParagraph(HtmlTools.formatDate(session, demand.getCreationDate()), "description_date");

        final MemberPageUrl memberUrl = new MemberPageUrl(demand.getAuthor());

        author = memberUrl.getHtmlLink(demand.getAuthor().getLogin());

        final HtmlDiv descriptionBlock = new HtmlDiv("description_block");
        {

            final HtmlDiv descriptionFooter = new HtmlDiv("description_footer");
            {

                final HtmlDiv descriptionDetails = new HtmlDiv("description_details");
                {
                    descriptionDetails.add(author);
                    descriptionDetails.add(date);
                }

                descriptionBlock.add(descriptionDetails);

                descriptionBlock.add(description);
            }
            descriptionBlock.add(descriptionFooter);

        }
        add(descriptionBlock);
    }
}
