/*
 * Copyright (c) 2018.
 *
 * This file is part of av.
 *
 * av is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * av is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with av.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avbot.contracts.database.query;

import com.avbot.database.query.ChangeableStatement;
import com.avbot.database.query.QueryBuilder;

@FunctionalInterface
public interface ChangeableClosure {

    /**
     * Runs the changeable closure function, setting up all the columns that should be
     * updated in the {@link QueryBuilder#update(ChangeableClosure) update} method.
     *
     * @param statement The changeable statement used to update the records in the query builder.
     */
    void run(ChangeableStatement statement);
}
