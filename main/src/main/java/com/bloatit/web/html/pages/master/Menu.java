package com.bloatit.web.html.pages.master;


import com.bloatit.web.html.components.standard.HtmlDiv;
import com.bloatit.web.html.components.standard.HtmlLink;
import com.bloatit.web.html.components.standard.HtmlList;
import com.bloatit.web.html.components.standard.HtmlListItem;
import com.bloatit.web.html.pages.DemandsPage;
import com.bloatit.web.html.pages.IndexPage;
import com.bloatit.web.html.pages.MembersListPage;
import com.bloatit.web.html.pages.PageNotFound;
import com.bloatit.web.html.pages.SpecialsPage;
import com.bloatit.web.server.Context;
import com.bloatit.web.server.Session;
import com.bloatit.web.utils.url.UrlBuilder;

public class Menu extends HtmlDiv {

    protected Menu() {
        super();

        final Session s = Context.getSession();
        setId("main_menu");

        final HtmlList primaryList = new HtmlList();

        primaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(DemandsPage.class).buildUrl(), s.tr("Demands"))));
        primaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(IndexPage.class).buildUrl(), s.tr("Projects"))));
        primaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(IndexPage.class).buildUrl(), s.tr("Groups"))));
        primaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(MembersListPage.class).buildUrl(), s.tr("Members"))));

        final HtmlList secondaryList = new HtmlList();

        secondaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(SpecialsPage.class).buildUrl(), s.tr("Specials page"))));
        secondaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(PageNotFound.class).buildUrl(), s.tr("Contact"))));
        secondaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(PageNotFound.class).buildUrl(), s.tr("Documentation"))));
        secondaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(PageNotFound.class).buildUrl(), s.tr("About BloatIt"))));
        secondaryList.addItem(new HtmlListItem(new HtmlLink(new UrlBuilder(PageNotFound.class).buildUrl(), s.tr("Press"))));

        add(primaryList);
        add(secondaryList);

    }

}