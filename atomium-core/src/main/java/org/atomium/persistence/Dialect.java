package org.atomium.persistence;

import org.atomium.Entity;
import org.atomium.NamedValues;
import org.atomium.Query;
import org.atomium.entity.EntityMetadata;

/**
 * @author blackrush
 */
public interface Dialect {
    /**
     * build a query that will insert data in the database
     *
     * @param metadata data's metadata
     * @param values entity's values to insert
     * @return query
     */
    <T extends Entity> Query insert(EntityMetadata<T> metadata, NamedValues values);

    /**
     * build a query that will update data in the database
     * @param metadata data's metadata
     * @param values entity's values to update
     * @param <T> data's type
     * @return query
     */
    <T extends Entity> Query update(EntityMetadata<T> metadata, NamedValues values);

    /**
     * build a query that will delete data in the database
     * @param metadata data's metadata
     * @param values entity's values to delete
     * @param <T> data's type
     * @return query
     */
    <T extends Entity> Query delete(EntityMetadata<T> metadata, NamedValues values);

    /**
     * build a query that will fetch data from the database
     * @param metadata data's metadata
     * @param <T> data's type
     * @return query
     */
    <T extends Entity> Query select(EntityMetadata<T> metadata);
}
