//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.model.managers;

import com.bloatit.data.DaoContribution;
import com.bloatit.data.DaoFileMetadata;
import com.bloatit.data.DaoKudos;
import com.bloatit.data.queries.DBRequests;
import com.bloatit.model.Contribution;
import com.bloatit.model.FileMetadata;
import com.bloatit.model.Kudos;
import com.bloatit.model.Kudosable;
import com.bloatit.model.UserContent;

/**
 * The Class UserContentManager is an utility class containing static methods.
 */
public final class UserContentManager {

    /**
     * Desactivated constructor on utility class.
     */
    private UserContentManager() {
        // Desactivate default ctor
    }

    /**
     * Gets a {@link UserContent} by id.
     * 
     * @param id the id
     * @return the {@link UserContent} or <code>null</code> if not found.
     */
    public static UserContent<?> getById(final Integer id) {
        final Kudosable<?> kudosable = KudosableManager.getById(id);
        if (kudosable != null) {
            return kudosable;
        }

        UserContent<?> created = Contribution.create(DBRequests.getById(DaoContribution.class, id));
        if (created != null) {
            return created;
        }

        created = FileMetadata.create(DBRequests.getById(DaoFileMetadata.class, id));
        if (created != null) {
            return created;
        }

        created = Kudos.create(DBRequests.getById(DaoKudos.class, id));
        if (created != null) {
            return created;
        }

        return null;
    }
}
