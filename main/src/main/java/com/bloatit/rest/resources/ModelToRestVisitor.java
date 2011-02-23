package com.bloatit.rest.resources;

import com.bloatit.model.BankTransaction;
import com.bloatit.model.Batch;
import com.bloatit.model.Bug;
import com.bloatit.model.Comment;
import com.bloatit.model.Contribution;
import com.bloatit.model.Demand;
import com.bloatit.model.Description;
import com.bloatit.model.ExternalAccount;
import com.bloatit.model.FileMetadata;
import com.bloatit.model.Group;
import com.bloatit.model.HighlightDemand;
import com.bloatit.model.InternalAccount;
import com.bloatit.model.JoinGroupInvitation;
import com.bloatit.model.Kudos;
import com.bloatit.model.Member;
import com.bloatit.model.Offer;
import com.bloatit.model.Project;
import com.bloatit.model.Release;
import com.bloatit.model.Transaction;
import com.bloatit.model.Translation;
import com.bloatit.rest.RestElement;

public class ModelToRestVisitor implements ModelClassVisitor<RestElement<?>> {

    @Override
    public RestElement<ExternalAccount> visit(final ExternalAccount model) {
        return new RestExternalAccount(model);
    }

    @Override
    public RestElement<InternalAccount> visit(final InternalAccount model) {
        return new RestInternalAccount(model);
    }

    @Override
    public RestElement<Member> visit(final Member model) {
        return new RestMember(model);
    }

    @Override
    public RestElement<BankTransaction> visit(final BankTransaction model) {
        return new RestBankTransaction(model);
    }

    @Override
    public RestElement<Batch> visit(final Batch model) {
        return new RestBatch(model);
    }

    @Override
    public RestElement<Description> visit(final Description model) {
        return new RestDescription(model);
    }

    @Override
    public RestElement<Group> visit(final Group model) {
        return new RestGroup(model);
    }

    @Override
    public RestElement<HighlightDemand> visit(final HighlightDemand model) {
        return new RestHighlightDemand(model);
    }

    @Override
    public RestElement<JoinGroupInvitation> visit(final JoinGroupInvitation model) {
        return new RestJoinGroupInvitation(model);
    }

    @Override
    public RestElement<Project> visit(final Project model) {
        return new RestProject(model);
    }

    @Override
    public RestElement<Transaction> visit(final Transaction model) {
        return new RestTransaction(model);
    }

    @Override
    public RestElement<Bug> visit(final Bug model) {
        return new RestBug(model);
    }

    @Override
    public RestElement<Contribution> visit(final Contribution model) {
        return new RestContribution(model);
    }

    @Override
    public RestElement<FileMetadata> visit(final FileMetadata model) {
        return new RestFileMetadata(model);
    }

    @Override
    public RestElement<Kudos> visit(final Kudos model) {
        return new RestKudos(model);
    }

    @Override
    public RestElement<Comment> visit(final Comment model) {
        return new RestComment(model);
    }

    @Override
    public RestElement<Demand> visit(final Demand model) {
        return new RestDemand(model);
    }

    @Override
    public RestElement<Offer> visit(final Offer model) {
        return new RestOffer(model);
    }

    @Override
    public RestElement<Translation> visit(final Translation model) {
        return new RestTranslation(model);
    }

    @Override
    public RestElement<Release> visit(final Release model) {
        return new RestRelease(model);
    }
}