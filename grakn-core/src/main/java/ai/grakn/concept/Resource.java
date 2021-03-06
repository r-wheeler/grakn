/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn.concept;


import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * <p>
 *     Represent a literal resource in the graph.
 * </p>
 *
 * <p>
 *     Acts as an {@link Thing} when relating to other instances except it has the added functionality of:
 *     1. It is unique to its {@link ResourceType} based on it's value.
 *     2. It has a {@link ai.grakn.concept.ResourceType.DataType} associated with it which constrains the allowed values.
 * </p>
 *
 * @author fppt
 *
 * @param <D> The data type of this resource type.
 *           Supported Types include: {@link String}, {@link Long}, {@link Double}, and {@link Boolean}
 */
public interface Resource<D> extends Thing {
    //------------------------------------- Accessors ----------------------------------
    /**
     * Retrieves the value of the Resource.
     *
     * @return The Resource itself
     */
    @CheckReturnValue
    D getValue();

    /**
     * Retrieves the type of the Resource, that is, the ResourceType of which this resource is an Thing.
     *
     * @return The ResourceType of which this resource is an Thing.
     */
    @Override
    ResourceType<D> type();

    /**
     * Retrieves the data type of this Resource's ResourceType.
     *
     * @return The data type of this Resource's type.
     */
    @CheckReturnValue
    ResourceType.DataType<D> dataType();

    /**
     * Retrieves the set of all Instances that possess this Resource.
     *
     * @return The list of all Instances that possess this Resource.
     */
    @CheckReturnValue
    Collection<Thing> ownerInstances();

    /**
     * If the Resource is unique, this method retrieves the Thing that possesses it.
     *
     * @return The Thing which is connected to a unique Resource.
     */
    @CheckReturnValue
    @Nullable
    Thing owner();

    /**
     * Creates a relation from this instance to the provided resource.
     *
     * @param resource The resource to which a relationship is created
     * @return The instance itself
     */
    @Override
    Resource resource(Resource resource);

    //------------------------------------- Other ---------------------------------
    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default Resource asResource(){
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isResource(){
        return true;
    }
}
