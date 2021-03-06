package com.bloatit.data;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.hibernate.HibernateException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.search.annotations.Field;

import com.bloatit.data.queries.QueryCollection;
import com.bloatit.framework.utils.PageIterable;

@Entity
//@formatter:off
@NamedQueries(value = { 
                       @NamedQuery(
                           name =  "follow.find",
                           query = "FROM DaoFollow " +
                                   "WHERE actor = :follower " +
                                   "AND followed = :content "),
                                   
                       @NamedQuery(
                           name =  "follow.getByFeature",
                           query = "FROM DaoFollow " +
                                   "WHERE followed = :feature "),
                                   
                       @NamedQuery(
                           name =  "follow.getByFeature.size",
                           query = "SELECT COUNT(*) " +
                           		   "FROM DaoFollow " +
                                   "WHERE followed = :feature "),
                        }

             )
//@formatter:on
public class DaoFollow extends DaoIdentifiable {

    public enum FollowState {
        /**
         * Used when a content is being actively followed
         */
        ACTIVE,
        /**
         * Used when a user manually cancels the following of a content
         */
        CANCELED,
        /**
         * Used when a content is no longer being followed because the content
         * expired
         */
        EXPIRED,
        /**
         * Used when a content is deleted
         */
        DELETE,
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Cascade(value = { CascadeType.PERSIST })
    private DaoActor actor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Cascade(value = { CascadeType.PERSIST })
    private DaoFeature followed;

    @Basic(optional = false)
    @Column(updatable = false)
    private Date creationDate;

    // NOTE : This is not used (or updated) yet
    @Basic(optional = false)
    @Column(updatable = false)
    private Date lastConsultationDate;

    @Basic(optional = false)
    @Field
    @Enumerated
    private FollowState followState;

    // ======================================================================
    // Construction
    // ======================================================================

    /**
     * Creates a new Money Withdrawal, with a default state of ACTIVE, or
     * reactivate an already existing follow
     */
    public static DaoFollow createAndPersist(final DaoActor actor, final DaoFeature followed) {
        final DaoFollow alreadyFollowed = getFollow(followed, actor);

        if (alreadyFollowed != null) {
            // Already exists
            switch (alreadyFollowed.getFollowState()) {
                case CANCELED:
                case DELETE:
                case EXPIRED:
                    alreadyFollowed.setState(FollowState.ACTIVE);
                    break;
            }
            return alreadyFollowed;
        } else {
            final DaoFollow follow = new DaoFollow(actor, followed);
            try {
                SessionManager.getSessionFactory().getCurrentSession().save(follow);
            } catch (final HibernateException e) {
                SessionManager.getSessionFactory().getCurrentSession().getTransaction().rollback();
                throw e;
            }
            return follow;
        }
    }

    private DaoFollow(final DaoActor actor, final DaoFeature followed) {
        this.actor = actor;
        this.followed = followed;
        this.creationDate = new Date();
        this.lastConsultationDate = new Date();
        this.followState = FollowState.ACTIVE;
    }

    public void delete() {
        SessionManager.getSessionFactory().getCurrentSession().delete(this);
    }

    // ======================================================================
    // Getters
    // ======================================================================

    /**
     * The actor following a content
     */
    public DaoActor getActor() {
        return actor;
    }

    /**
     * @return The content being followed
     */
    public DaoFeature getFollowed() {
        return followed;
    }

    /**
     * @return the date where the user started to follow the content
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @return the last date the user checked the followed content
     */
    public Date getLastConsultationDate() {
        return lastConsultationDate;
    }

    /**
     * @return the state of the content ()
     */
    public FollowState getFollowState() {
        return followState;
    }

    // ======================================================================
    // Setters
    // ======================================================================

    private void setState(final FollowState newState) {
        this.followState = newState;
    }

    // ======================================================================
    // Static accessors
    // ======================================================================

    /**
     * @return the state of the follow matching both <code>content</code> and
     *         <code>follower</code> or null if none matches
     */
    public static DaoFollow getFollow(final DaoFeature content, final DaoActor follower) {
        return (DaoFollow) SessionManager.getNamedQuery("follow.find").setEntity("follower", follower).setEntity("content", content).uniqueResult();
    }

    /**
     * @param feature the feature for which you want to find follows
     * @return all the follow for the matching <code>feature</code>
     */
    public static PageIterable<DaoFollow> getFollow(final DaoFeature feature) {
        return new QueryCollection<DaoFollow>("follow.getByFeature").setEntity("feature", feature);
    }

    // ======================================================================
    // Visitor
    // ======================================================================

    @Override
    public <ReturnType> ReturnType accept(final DataClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    /**
     * Instantiates a new dao join team invitation.
     */
    protected DaoFollow() {
        super();
    }

    // ======================================================================
    // equals and hashcode
    // ======================================================================

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actor == null) ? 0 : actor.hashCode());
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((followState == null) ? 0 : followState.hashCode());
        result = prime * result + ((followed == null) ? 0 : followed.hashCode());
        result = prime * result + ((lastConsultationDate == null) ? 0 : lastConsultationDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DaoFollow other = (DaoFollow) obj;
        if (actor == null) {
            if (other.actor != null) {
                return false;
            }
        } else if (!actor.equals(other.actor)) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (followState != other.followState) {
            return false;
        }
        if (followed == null) {
            if (other.followed != null) {
                return false;
            }
        } else if (!followed.equals(other.followed)) {
            return false;
        }
        if (lastConsultationDate == null) {
            if (other.lastConsultationDate != null) {
                return false;
            }
        } else if (!lastConsultationDate.equals(other.lastConsultationDate)) {
            return false;
        }
        return true;
    }
}
