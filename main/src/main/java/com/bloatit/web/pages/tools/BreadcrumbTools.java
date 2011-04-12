package com.bloatit.web.pages.tools;

import org.apache.commons.lang.NotImplementedException;

import com.bloatit.model.BankTransaction;
import com.bloatit.model.Bug;
import com.bloatit.model.Comment;
import com.bloatit.model.Contribution;
import com.bloatit.model.Description;
import com.bloatit.model.ExternalAccount;
import com.bloatit.model.Feature;
import com.bloatit.model.FileMetadata;
import com.bloatit.model.HighlightFeature;
import com.bloatit.model.InternalAccount;
import com.bloatit.model.JoinTeamInvitation;
import com.bloatit.model.Kudos;
import com.bloatit.model.Member;
import com.bloatit.model.Milestone;
import com.bloatit.model.ModelClassVisitor;
import com.bloatit.model.Offer;
import com.bloatit.model.Release;
import com.bloatit.model.Software;
import com.bloatit.model.Team;
import com.bloatit.model.Transaction;
import com.bloatit.model.Translation;
import com.bloatit.model.UserContentInterface;
import com.bloatit.web.linkable.bugs.BugPage;
import com.bloatit.web.linkable.features.FeaturePage;
import com.bloatit.web.linkable.members.MemberPage;
import com.bloatit.web.linkable.release.ReleasePage;
import com.bloatit.web.linkable.softwares.SoftwarePage;
import com.bloatit.web.linkable.team.TeamPage;
import com.bloatit.web.pages.master.Breadcrumb;

public class BreadcrumbTools {

    public static Breadcrumb generateBreadcrumb(final UserContentInterface<?> userContent) {

        return userContent.accept(new ModelClassVisitor<Breadcrumb>() {

            @Override
            public Breadcrumb visit(final ExternalAccount model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final InternalAccount model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Member model) {
                return MemberPage.generateBreadcrumb(model);
            }

            @Override
            public Breadcrumb visit(final BankTransaction model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Milestone model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Description model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Team model) {
                return TeamPage.generateBreadcrumb(model);
            }

            @Override
            public Breadcrumb visit(final HighlightFeature model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final JoinTeamInvitation model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Software model) {
                return SoftwarePage.generateBreadcrumb(model);
            }

            @Override
            public Breadcrumb visit(final Transaction model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Bug model) {
                return BugPage.generateBreadcrumb(model);
            }

            @Override
            public Breadcrumb visit(final Contribution model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final FileMetadata model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Kudos model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Comment model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Feature model) {
                return FeaturePage.generateBreadcrumb(model);
            }

            @Override
            public Breadcrumb visit(final Offer model) {
                return FeaturePage.generateBreadcrumbOffers(model.getFeature());
            }

            @Override
            public Breadcrumb visit(final Translation model) {
                throw new NotImplementedException();
            }

            @Override
            public Breadcrumb visit(final Release model) {
                return ReleasePage.generateBreadcrumb(model);
            }});
    }

}
