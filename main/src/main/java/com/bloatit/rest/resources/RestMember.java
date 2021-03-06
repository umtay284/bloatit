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
package com.bloatit.rest.resources;

import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import com.bloatit.framework.restprocessor.RestElement;
import com.bloatit.framework.restprocessor.RestServer.RequestMethod;
import com.bloatit.framework.restprocessor.annotations.REST;
import com.bloatit.framework.restprocessor.exception.RestException;
import com.bloatit.framework.webprocessor.annotations.ConversionErrorException;
import com.bloatit.framework.webprocessor.context.User;
import com.bloatit.framework.webprocessor.url.Loaders;
import com.bloatit.framework.xcgiserver.HttpReponseField.StatusCode;
import com.bloatit.model.Member;
import com.bloatit.model.managers.MemberManager;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.rest.list.RestFeatureList;
import com.bloatit.rest.list.RestMemberList;

/**
 * <p>
 * Representation of a Member for the ReST RPC calls
 * </p>
 * <p>
 * This class should implement any methods from Member that needs to be called
 * through the ReST RPC. Every such method needs to be mapped with the
 * {@code @REST} interface.
 * <p>
 * ReST uses the four HTTP request methods <code>GET</code>, <code>POST</code>,
 * <code>PUT</code>, <code>DELETE</code> each with their own meaning. Please
 * only bind the according to the following:
 * <li>GET list: List the URIs and perhaps other details of the collection's
 * members.</li>
 * <li>GET list/id: Retrieve a representation of the addressed member of the
 * collection, expressed in an appropriate Internet media type.</li>
 * <li>POST list: Create a new entry in the collection. The new entry's URL is
 * assigned automatically and is usually returned by the operation.</li>
 * <li>POST list/id: Treat the addressed member as a collection in its own right
 * and create a new entry in it.</li>
 * <li>PUT list: Replace the entire collection with another collection.</li>
 * <li>PUT list/id: Replace the addressed member of the collection, or if it
 * doesn't exist, create it.</li>
 * <li>DELETE list: Delete the entire collection.</li>
 * <li>DELETE list/id: Delete the addressed member of the collection.</li>
 * </p>
 * </p>
 * <p>
 * This class will be serialized as XML (or maybe JSON who knows) to be sent
 * over to the client RPC. Hence this class needs to be annotated to indicate
 * which methods (and/or fields) are to be matched in the XML data. For this
 * use:
 * <li>@XmlRootElement at the root of the class</li>
 * <li>@XmlElement on each method/attribute that will yield <i>complex</i> data</li>
 * <li>@XmlAttribute on each method/attribute that will yield <i>simple</i> data
 * </li>
 * <li>Methods that return a list need to be annotated with @XmlElement and to
 * return a RestMemberList</li>
 * </p>
 */
@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.NONE)
public class RestMember extends RestElement<Member> {
    private Member model;

    // ---------------------------------------------------------------------------------------
    // -- Constructors
    // ---------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private RestMember() {
        super();
    }

    protected RestMember(final Member model) {
        this.model = model;
    }

    // ---------------------------------------------------------------------------------------
    // -- Static generic methods
    // ---------------------------------------------------------------------------------------

    /**
     * Finds the RestMember matching the <code>id</code>
     * 
     * @param id the id of the RestMember
     */
    @REST(name = "members", method = RequestMethod.GET)
    public static RestMember getById(final int id) {
        final RestMember restMember = new RestMember(MemberManager.getById(id));
        if (restMember.isNull()) {
            return null;
        }
        return restMember;
    }

    /**
     * Finds the list of all (valid) RestMember
     */
    @REST(name = "members", method = RequestMethod.GET)
    public static RestMemberList getAll() {
        return new RestMemberList(MemberManager.getAll());
    }

    // ---------------------------------------------------------------------------------------
    // -- Static Custom GETTERS
    // ---------------------------------------------------------------------------------------

    @REST(name = "members", method = RequestMethod.GET, params = { "login" })
    public static RestMember getByLogin(final String login) {
        final RestMember restMember = new RestMember(MemberManager.getMemberByLogin(login));
        if (restMember.isNull()) {
            return null;
        }
        return restMember;
    }

    // ---------------------------------------------------------------------------------------
    // -- Static Custom PUTTERS
    // ---------------------------------------------------------------------------------------

    /**
     * @statusCode 400 if invalid country and/or language code
     * @statusCode 406 if there is already a member with the given login / email
     * @statusCode 406 if the password does not match requirements
     */
    @REST(name = "members", method = RequestMethod.PUT, params = { "login", "password", "email", "language", "country" })
    public static RestMember createMember(final String login, final String password, final String email, final String language, final String country)
            throws RestException {
        if (language.length() != 2 || country.length() != 2) {
            throw new RestException(StatusCode.ERROR_CLI_400_BAD_REQUEST, "Country and language must represent a valid country/language code.");
        }
        if (MemberManager.loginExists(login) || MemberManager.emailExists(email)) {
            throw new RestException(StatusCode.ERROR_CLI_406_NOT_ACCEPTABLE, "Login or email already in use.");
        }
        // TODO check password (do this on the Model level)

        final RestMember restMember = new RestMember(new Member(login, password, email, new Locale(language, country)));
        if (restMember.isNull()) {
            return null;
        }
        return restMember;
    }

    // ---------------------------------------------------------------------------------------
    // -- Custom PUTTERS
    // ---------------------------------------------------------------------------------------

    @REST(name = "setfollow", method = RequestMethod.PUT, params = { "followall", "followallwithmail" })
    public RestMember setFollow(final String followAll, final String followallWithMail) throws RestException {
        try {

            final boolean isFollowAll = Loaders.fromStr(Boolean.class, followAll);
            final boolean isFollowallWithMail = Loaders.fromStr(Boolean.class, followallWithMail);

            model.setGlobalFollow(isFollowAll);
            model.setGlobalFollowWithMail(isFollowallWithMail);

        } catch (final ConversionErrorException e) {
            throw new RestException(StatusCode.ERROR_CLI_400_BAD_REQUEST, "Bad format for one of the parameters", e);
        } catch (final UnauthorizedOperationException e) {
            throw new RestException(StatusCode.ERROR_CLI_403_FORBIDDEN, "Permission denied", e);
        }

        return this;
    }

    // ---------------------------------------------------------------------------------------
    // -- XML Getters
    // ---------------------------------------------------------------------------------------

    @XmlAttribute
    @XmlID
    public String getId() {
        return model.getId().toString();
    }

    // public PageIterable<JoinTeamInvitation> getReceivedInvitation(State
    // state) {
    // return model.getReceivedInvitation(state);
    // }

    @XmlAttribute(name = "name")
    public String getDisplayName() {
        return model.getDisplayName();
    }

    @XmlAttribute(name = "email")
    public String getEmail() {
        try {
            return model.getEmail();
        } catch (final UnauthorizedOperationException e) {
            return null;
        }
    }

    @XmlAttribute(name = "followall")
    public Boolean getFollowAll() {
        try {
            return model.isGlobalFollow();
        } catch (final UnauthorizedOperationException e) {
            return null;
        }
    }

    @XmlAttribute(name = "followallwithmail")
    public Boolean getFollowAllWithMail() {
        try {
            return model.isGlobalFollowWithMail();
        } catch (final UnauthorizedOperationException e) {
            return null;
        }
    }

    @XmlAttribute(name = "creation_date")
    public Date getCreationDate() {
        try {
            return model.getDateCreation();
        } catch (final UnauthorizedOperationException e) {
            return null;
        }
    }

    @XmlElement(name = "internalaccount")
    @XmlIDREF
    public RestInternalAccount getInternalAccount() {
        try {
            return new RestInternalAccount(model.getInternalAccount());
        } catch (final UnauthorizedOperationException e) {
            return null;
        }
    }

    @XmlElement(name = "karma")
    public int getKarma() throws RestException {
        return model.getKarma();
    }

    @XmlElement
    public RestFeatureList getFeatures() {
        return new RestFeatureList(model.getFeatures(false));
    }

    // ---------------------------------------------------------------------------------------
    // -- Utils
    // ---------------------------------------------------------------------------------------

    @Override
    public boolean isNull() {
        return (model == null);
    }

    /**
     * Package method to find the model
     */
    User getModel() {
        return model;
    }

    void setModel(final Member model) {
        this.model = model;
    }

}
