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
package com.bloatit.model.managers;

import com.bloatit.data.DaoExternalService;
import com.bloatit.data.DaoExternalServiceMembership;
import com.bloatit.data.queries.DBRequests;
import com.bloatit.model.ExternalService;
import com.bloatit.model.ExternalServiceMembership;
import com.bloatit.model.lists.ListBinder;

public class ExternalServiceManager {

    public static ExternalServiceMembership getMembershipByToken(final String token) {
        return ExternalServiceMembership.create(DaoExternalServiceMembership.getByToken(token));
    }

    public static ExternalService getByToken(final String token) {
        return ExternalService.create(DaoExternalService.getByToken(token));
    }

    public static ExternalService getById(final Integer id) {
        return ExternalService.create(DBRequests.getById(DaoExternalService.class, id));
    }

    public static ListBinder<ExternalService, DaoExternalService> getAll() {
        return new ListBinder<ExternalService, DaoExternalService>(DBRequests.getAll(DaoExternalService.class));
    }
}
