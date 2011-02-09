package com.bloatit.model.demand;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import com.bloatit.data.DaoComment;
import com.bloatit.data.DaoDemand;
import com.bloatit.data.DaoDemand.DemandState;
import com.bloatit.data.DaoDescription;
import com.bloatit.data.DaoOffer;
import com.bloatit.data.SessionManager;
import com.bloatit.data.exceptions.NotEnoughMoneyException;
import com.bloatit.framework.exceptions.FatalErrorException;
import com.bloatit.framework.exceptions.NonOptionalParameterException;
import com.bloatit.framework.exceptions.UnauthorizedOperationException;
import com.bloatit.framework.exceptions.UnauthorizedOperationException.SpecialCode;
import com.bloatit.framework.exceptions.WrongStateException;
import com.bloatit.framework.utils.DateUtils;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.model.AuthToken;
import com.bloatit.model.CacheManager;
import com.bloatit.model.Comment;
import com.bloatit.model.Contribution;
import com.bloatit.model.Demand;
import com.bloatit.model.Description;
import com.bloatit.model.Identifiable;
import com.bloatit.model.Kudosable;
import com.bloatit.model.KudosableConfiguration;
import com.bloatit.model.Member;
import com.bloatit.model.Offer;
import com.bloatit.model.PlannedTask;
import com.bloatit.model.Project;
import com.bloatit.model.lists.CommentList;
import com.bloatit.model.lists.ContributionList;
import com.bloatit.model.lists.OfferList;
import com.bloatit.model.right.DemandRight;
import com.bloatit.model.right.RightManager.Action;

// TODO : delete comment.
//

/**
 * A demand is an idea :)
 */
public final class DemandImplementation extends Kudosable<DaoDemand> implements Demand {
    private AbstractDemandState stateObject;

    // /////////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTION
    // /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create a new DemandImplementation. This method is not protected by any right management.
     *
     * @return null if the <code>dao</code> is null.
     */
    public static DemandImplementation create(final DaoDemand dao) {
        if (dao == null || !SessionManager.getSessionFactory().getCurrentSession().contains(dao)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Identifiable<DaoDemand> created = CacheManager.get(dao);
        if (created == null) {
            return new DemandImplementation(dao);
        }
        return (DemandImplementation) created;
    }

    /**
     * Create a new demand. The right management for creating a demand is specific. (The
     * Right management system is not working in this case). You have to use the
     * {@link DemandManager#canCreate(AuthToken)} to make sure you can create a new
     * demand.
     *
     * @see DaoDemand#DaoDemand(Member,Locale,String, String)
     */
    public DemandImplementation(final Member author, final Locale locale, final String title, final String description, final Project project) {
        this(DaoDemand.createAndPersist(author.getDao(),
                                        DaoDescription.createAndPersist(author.getDao(), locale, title, description),
                                        project.getDao()));
    }

    /**
     * Use the {@link #create(DaoDemand)} method.
     */
    private DemandImplementation(final DaoDemand dao) {
        super(dao);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Can something
    // /////////////////////////////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * @see
     * com.bloatit.model.demand.DemandInterface#canAccessComment(com.bloatit.model.right
     * .RightManager.Action)
     */
    @Override
    public boolean canAccessComment(final Action action) {
        return new DemandRight.Comment().canAccess(calculateRole(this), action);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.bloatit.model.demand.DemandInterface#canAccessContribution(com.bloatit.model
     * .right.RightManager.Action)
     */
    @Override
    public boolean canAccessContribution(final Action action) {
        return new DemandRight.Contribute().canAccess(calculateRole(this), action);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.bloatit.model.demand.DemandInterface#canAccessOffer(com.bloatit.model.right
     * .RightManager.Action)
     */
    @Override
    public boolean canAccessOffer(final Action action) {
        return new DemandRight.Offer().canAccess(calculateRole(this), action);
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#canAccessDescription()
     */
    @Override
    public boolean canAccessDescription() {
        return new DemandRight.Specification().canAccess(calculateRole(this), Action.READ);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Do things.
    // /////////////////////////////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#addContribution(java.math.BigDecimal,
     * java.lang.String)
     */
    @Override
    public void addContribution(final BigDecimal amount, final String comment) throws NotEnoughMoneyException, UnauthorizedOperationException {
        new DemandRight.Contribute().tryAccess(calculateRole(this), Action.WRITE);
        getDao().addContribution(getAuthToken().getMember().getDao(), amount, comment);
        setStateObject(getStateObject().eventAddContribution());
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#addOffer(com.bloatit.model.Offer)
     */
    @Override
    public void addOffer(final Offer offer) throws UnauthorizedOperationException {
        if (offer == null) {
            throw new NonOptionalParameterException();
        }
        if (!offer.getDemand().equals(this)) {
            throw new IllegalArgumentException();
        }

        new DemandRight.Offer().tryAccess(calculateRole(this), Action.WRITE);
        if (!offer.getAuthor().equals(getAuthToken().getMember())) {
            throw new UnauthorizedOperationException(SpecialCode.CREATOR_INSERTOR_MISMATCH);
        }
        setStateObject(getStateObject().eventAddOffer(offer));
        getDao().addOffer(offer.getDao());
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#removeOffer(com.bloatit.model.Offer)
     */
    @Override
    public void removeOffer(final Offer offer) throws UnauthorizedOperationException {
        new DemandRight.Offer().tryAccess(calculateRole(this), Action.DELETE);
        if (getDao().getSelectedOffer().getId() == offer.getId()) {
            getDao().computeSelectedOffer();
        }
        setStateObject(getStateObject().eventRemoveOffer(offer));
        getDao().removeOffer(offer.getDao());
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#cancelDevelopment()
     */
    @Override
    public void cancelDevelopment() throws UnauthorizedOperationException {
        if (!getAuthToken().getMember().equals(getSelectedOffer().getAuthor())) {
            throw new UnauthorizedOperationException(SpecialCode.NON_DEVELOPER_CANCEL_DEMAND);
        }
        cancel();
        setStateObject(getStateObject().eventDeveloperCanceled());
    }

    /**
     * Cancel all the contribution on this demand.
     */
    private void cancel() {
        for (final Contribution contribution : getContributionsUnprotected()) {
            contribution.cancel();
        }
        // Maybe I should make sure everything is canceled in every Offer/batches ?
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#releaseCurrentBatch()
     */
    @Override
    public void releaseCurrentBatch() throws UnauthorizedOperationException {
        if (!getAuthToken().getMember().equals(getSelectedOffer().getAuthor())) {
            throw new UnauthorizedOperationException(SpecialCode.NON_DEVELOPER_FINISHED_DEMAND);
        }
        if (getSelectedOfferUnprotected().isFinished()) {
            throw new FatalErrorException("There is no batch left for this Offer !");
        }

        setStateObject(getStateObject().eventBatchReleased());
        // The offer really don't care to know if the current batch is under development
        // or not.
    }

    // TODO authorization
    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#validateCurrentBatch(boolean)
     */
    @Override
    public boolean validateCurrentBatch(final boolean force) {
        if (getSelectedOfferUnprotected().isFinished()) {
            throw new FatalErrorException("There is no batch left for this Offer !");
        }
        if (getDemandState() != DemandState.INCOME) {
            throw new WrongStateException();
        }
        return getSelectedOfferUnprotected().validateCurrentBatch(force);
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#addComment(java.lang.String)
     */
    @Override
    public void addComment(final String text) throws UnauthorizedOperationException {
        new DemandRight.Comment().tryAccess(calculateRole(this), Action.WRITE);
        getDao().addComment(DaoComment.createAndPersist(getAuthToken().getMember().getDao(), text));
    }

    public void notifyOfferKudos(final Offer offer, final boolean positif) {
        final boolean isSelectedOffer = offer.equals(getSelectedOfferUnprotected());
        if (positif && !isSelectedOffer) {
            if (offer.getPopularity() > getSelectedOfferUnprotected().getPopularity()) {
                getDao().setSelectedOffer(offer.getDao());
            }
        }
        if (!positif && isSelectedOffer) {
            for (final Offer thisOffer : getOffersUnprotected()) {
                if (thisOffer.getPopularity() > getSelectedOfferUnprotected().getPopularity()) {
                    getDao().computeSelectedOffer();
                }

            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.bloatit.model.demand.DemandInterface#unSelectOffer(com.bloatit.model.Offer)
     */
    @Override
    public void unSelectOffer(final Offer offer) {
        if (offer.equals(getSelectedOfferUnprotected())) {
            setSelectedOffer(null);
            getDao().computeSelectedOffer();
        }

    }

    // ////////////////////////////////////////////////////////////////////////
    // Slots and notification system
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Tells that we are in development state.
     */
    void inDevelopmentState() {
        getDao().setDemandState(DemandState.DEVELOPPING);
        new TaskDevelopmentTimeOut(this.getId(), getDao().getSelectedOffer().getCurrentBatch().getExpirationDate());
    }

    /**
     * Slot called when the demand change to {@link DiscardedState}.
     */
    void inDiscardedState() {
        getDao().setDemandState(DemandState.DISCARDED);
    }

    /**
     * Slot called when this demand state change to {@link FinishedState}.
     */
    void inFinishedState() {
        getDao().setDemandState(DemandState.FINISHED);
    }

    /**
     * Slot called when this demand state change to {@link IncomeState}.
     */
    void inIncomeState() {
        getDao().setDemandState(DemandState.INCOME);

    }

    /**
     * Slot called when this demand state change to {@link PendingState}.
     */
    void inPendingState() {
        getDao().setDemandState(DemandState.PENDING);

    }

    /**
     * Slot called when this demand state change to {@link PreparingState}.
     */
    void inPreparingState() {
        getDao().setDemandState(DemandState.PREPARING);
    }

    /**
     * Called by a {@link PlannedTask}. For now do nothing...
     */
    void developmentTimeOut() {
        setStateObject(getStateObject().eventDevelopmentTimeOut());
    }

    /**
     * Called by a {@link PlannedTask}
     */
    void selectedOfferTimeOut() {
        setStateObject(getStateObject().eventSelectedOfferTimeOut(getDao().getContribution()));
    }

    @Override
    protected void notifyValid() {
        if (getStateObject().getState() == DemandState.DISCARDED) {
            setStateObject(getStateObject().eventPopularityPending());
        }
    }

    @Override
    protected void notifyPending() {
        if (getStateObject().getState() == DemandState.DISCARDED) {
            setStateObject(getStateObject().eventPopularityPending());
        }
    }

    @Override
    protected void notifyRejected() {
        setStateObject(getStateObject().eventDemandRejected());
    }

    void setSelectedOffer(final Offer offer) {
        final Date validationDate = DateUtils.tomorrow();
        new TaskSelectedOfferTimeOut(this.getId(), validationDate);
        this.getDao().setValidationDate(validationDate);
        this.getDao().setSelectedOffer(offer.getDao());
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Offer feedBack
    // /////////////////////////////////////////////////////////////////////////////////////////

    public void setOfferIsValidated() {
        setStateObject(getStateObject().eventOfferIsValidated());
    }

    public void setBatchIsValidated() {
        setStateObject(getStateObject().eventBatchIsValidated());
    }

    public void setBatchIsRejected() {
        setStateObject(getStateObject().eventBatchIsRejected());
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Get something
    // /////////////////////////////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getValidationDate()
     */
    @Override
    public Date getValidationDate() {
        return getDao().getValidationDate();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getComments()
     */
    @Override
    public PageIterable<Comment> getComments() throws UnauthorizedOperationException {
        new DemandRight.Comment().tryAccess(calculateRole(this), Action.READ);
        return new CommentList(getDao().getCommentsFromQuery());
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getContributions()
     */
    @Override
    public PageIterable<Contribution> getContributions() throws UnauthorizedOperationException {
        new DemandRight.Contribute().tryAccess(calculateRole(this), Action.READ);
        return getContributionsUnprotected();
    }

    /**
     * @see #getContribution()
     */
    private PageIterable<Contribution> getContributionsUnprotected() {
        return new ContributionList(getDao().getContributionsFromQuery());
    }

    private static final int PROGRESSION_COEF = 42;
    private static final int PROGRESSION_CONTRIBUTION_DIVISOR = 200;
    public static final int PROGRESSION_PERCENT = 100;

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getProgression()
     */
    @Override
    public float getProgression() throws UnauthorizedOperationException {
        new DemandRight.Contribute().tryAccess(calculateRole(this), Action.READ);
        final DaoOffer currentOffer = getDao().getSelectedOffer();
        if (getDao().getOffers().isEmpty() || currentOffer == null) {
            return PROGRESSION_COEF * (1 - 1 / (1 + getDao().getContribution().floatValue() / PROGRESSION_CONTRIBUTION_DIVISOR));
        }
        if (currentOffer.getAmount().floatValue() != 0) {
            return (getDao().getContribution().floatValue() * PROGRESSION_PERCENT) / currentOffer.getAmount().floatValue();
        }
        return Float.POSITIVE_INFINITY;
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getContribution()
     */
    @Override
    public BigDecimal getContribution() throws UnauthorizedOperationException {
        new DemandRight.Contribute().tryAccess(calculateRole(this), Action.READ);
        return getDao().getContribution();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getContributionMax()
     */
    @Override
    public BigDecimal getContributionMax() throws UnauthorizedOperationException {
        new DemandRight.Contribute().tryAccess(calculateRole(this), Action.READ);
        return getDao().getContributionMax();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getContributionMin()
     */
    @Override
    public BigDecimal getContributionMin() throws UnauthorizedOperationException {
        new DemandRight.Contribute().tryAccess(calculateRole(this), Action.READ);
        return getDao().getContributionMin();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getDescription()
     */
    @Override
    public Description getDescription() throws UnauthorizedOperationException {
        new DemandRight.Description().tryAccess(calculateRole(this), Action.READ);
        return Description.create(getDao().getDescription());
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getProject()
     */
    @Override
    public Project getProject() throws UnauthorizedOperationException {
        //TODO: access right
        return Project.create(getDao().getProject());
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getOffers()
     */
    @Override
    public PageIterable<Offer> getOffers() throws UnauthorizedOperationException {
        new DemandRight.Offer().tryAccess(calculateRole(this), Action.READ);
        return getOffersUnprotected();
    }

    private PageIterable<Offer> getOffersUnprotected() {
        return new OfferList(getDao().getOffersFromQuery());
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getSelectedOffer()
     */
    @Override
    public Offer getSelectedOffer() throws UnauthorizedOperationException {
        new DemandRight.Offer().tryAccess(calculateRole(this), Action.READ);
        return getSelectedOfferUnprotected();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getValidatedOffer()
     */
    @Override
    public Offer getValidatedOffer() throws UnauthorizedOperationException {
        new DemandRight.Offer().tryAccess(calculateRole(this), Action.READ);
        if (getDao().getSelectedOffer() != null && getValidationDate().before(new Date())) {
            return getSelectedOfferUnprotected();
        }
        return null;
    }

    private Offer getSelectedOfferUnprotected() {
        return Offer.create(getDao().getSelectedOffer());
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getTitle()
     */
    @Override
    public String getTitle() throws UnauthorizedOperationException {
        return getDescription().getDefaultTranslation().getTitle();
    }

    public DemandState getDemandState() {
        return getDao().getDemandState();
    }

    public void setStateObject(final AbstractDemandState stateObject) {
        this.stateObject = stateObject;
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.demand.DemandInterface#getStateObject()
     */
    public AbstractDemandState getStateObject() {
        switch (getDao().getDemandState()) {
        case PENDING:
            if (stateObject == null || !stateObject.getClass().equals(PendingState.class)) {
                setStateObject(new PendingState(this));
            }
            break;
        case DEVELOPPING:
            if (stateObject == null || !stateObject.getClass().equals(DeveloppingState.class)) {
                setStateObject(new DeveloppingState(this));
            }
            break;
        case DISCARDED:
            if (stateObject == null || !stateObject.getClass().equals(DiscardedState.class)) {
                setStateObject(new DiscardedState(this));
            }
            break;
        case FINISHED:
            if (stateObject == null || !stateObject.getClass().equals(FinishedState.class)) {
                setStateObject(new FinishedState(this));
            }
            break;
        case INCOME:
            if (stateObject == null || !stateObject.getClass().equals(IncomeState.class)) {
                setStateObject(new IncomeState(this));
            }
            break;
        case PREPARING:
            if (stateObject == null || !stateObject.getClass().equals(PreparingState.class)) {
                setStateObject(new PreparingState(this));
            }
            break;
        default:
            assert false;
            break;
        }
        return stateObject;
    }

    // ////////////////////////////////////////////////////////////////////////
    // Kudosable configuration
    // ////////////////////////////////////////////////////////////////////////

    /**
     * @see com.bloatit.model.Kudosable#turnPending()
     */
    @Override
    protected int turnPending() {
        return KudosableConfiguration.getDemandTurnPending();
    }

    /**
     * @see com.bloatit.model.Kudosable#turnValid()
     */
    @Override
    protected int turnValid() {
        return KudosableConfiguration.getDemandTurnValid();
    }

    /**
     * @see com.bloatit.model.Kudosable#turnRejected()
     */
    @Override
    protected int turnRejected() {
        return KudosableConfiguration.getDemandTurnRejected();
    }

    /**
     * @see com.bloatit.model.Kudosable#turnHidden()
     */
    @Override
    protected int turnHidden() {
        return KudosableConfiguration.getDemandTurnHidden();
    }

}