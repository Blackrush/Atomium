package org.atomium;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import com.googlecode.cqengine.query.Query;
import org.atomium.caches.CQCache;
import org.atomium.caches.CacheInterface;
import org.atomium.caches.NoCache;
import org.atomium.metadata.Metadata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * @author Blackrush
 */
public final class JdbcSession extends Session {
    private final Connection connection;

    public JdbcSession(JdbcDatabase database, Connection connection) {
        super(database);
        this.connection = checkNotNull(connection, "connection");
    }

    Connection getConnection() {
        return connection;
    }

    @Override
    protected <T> CacheInterface<T> createCache(Metadata<T> metadata) {
        switch (metadata.getCacheType()) {
            case NONE:
                return new NoCache<>(metadata);
            default:
                return CQCache.of(metadata);
        }
    }

    private <T> void setGeneratedKeys(Metadata<T> meta, T instance, Statement statement, boolean fail) {
        try (ResultSet rset = statement.getGeneratedKeys()) {
            if (!rset.next()) {
                if (fail) throw new IllegalStateException("there is not any generated keys to set");
                return;
            }
            Object pkey = rset.getObject(1);
            meta.getPrimaryKey().set(instance, pkey);
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private <T> Set<T> mapMany(Metadata<T> meta, SqlQuery query) {
        try (ResultSet rset = query.query(connection)) {
            NamedValues values = NamedValues.of(rset);
            ImmutableSet.Builder<T> builder = ImmutableSet.builder();

            while (rset.next()) {
                T instance = meta.map(values);
                builder.add(instance);
            }

            return builder.build();
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw propagate(e);
        }
        getDatabase().onClosed(this);
    }

    @Override
    public JdbcDatabase getDatabase() {
        return (JdbcDatabase) super.getDatabase();
    }

    public SqlDialectInterface getDialect() {
        return getDatabase().getDialect();
    }

    @Override
    public <T> T findOne(Ref<T> ref) {
        CacheInterface<T> cache = getCache(ref.getEntityMetadata());
        T instance = cache.unique(ref);

        if (instance == null) {
            SqlQuery query = getDialect().read(checkNotNull(ref));

            try (ResultSet rset = query.query(connection)) {
                if (!rset.next()) {
                    throw new DatabaseException.NotFound(ref);
                }
                // TODO compat with sqlite JDBC driver
                //if (!rset.isLast()) {
                //    throw new DatabaseException.NonUnique();
                //}

                return ref.getEntityMetadata().map(NamedValues.of(rset));
            } catch (SQLException e) {
                throw propagate(e);
            }
        }

        return instance;
    }

    @Override
    public <T> Set<T> find(Metadata<T> target, Query<T> query) {
        SqlQuery q = getDialect().read(target, query);
        return mapMany(target, q);
    }

    @Override
    public <T> Set<T> all(Metadata<T> target) {
        SqlQuery query = getDialect().read(target);
        return mapMany(target, query);
    }

    @Override
    public <T> void persist(T instance) {
        Metadata<T> metadata = metadataOf(instance);

        if (metadata.isPersisted(instance)) {
            getDialect().update(metadata, instance).execute(connection);
        } else {
            NamedParameterStatement statement = getDialect().create(metadata, instance).statement(connection);
            statement.execute();

            if (metadata.getPrimaryKey().isAutogenerated()) {
                // will fail if there isn't generated keys
                setGeneratedKeys(metadata, instance, statement.getStatement(), true);
            }
        }
    }

    @Override
    public <T> void remove(T instance) {
        Metadata<T> meta = metadataOf(instance);

        if (!meta.isPersisted(instance)) {
            return; // just get over it
        }

        getDialect().delete(meta, instance).execute(connection);
        // marks instance as not persisted TODO improve the way to mark the entity persisted or not
        if (Primitives.allPrimitiveTypes().contains(meta.getPrimaryKey().getTarget().getRawType())) {
            meta.getPrimaryKey().set(instance, 0);
        } else {
            meta.getPrimaryKey().set(instance, null);
        }
    }

    @Override
    public <T> boolean remove(Ref<T> ref) {
        NamedParameterStatement statement = getDialect().delete(ref).statement(connection);
        return statement.executeUpdate() > 0; // returns true if there is at least one affected row
    }
}
