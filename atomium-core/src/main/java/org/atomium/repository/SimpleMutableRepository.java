package org.atomium.repository;

import org.atomium.DatabaseContext;
import org.atomium.MutableEntity;
import org.atomium.MutableRepository;
import org.atomium.entity.EntityMetadata;
import org.atomium.pkey.PrimaryKey;
import org.atomium.pkey.PrimaryKeyGenerator;
import org.atomium.pkey.PrimaryKeyGenerators;

/**
 * @author blackrush
 */
public class SimpleMutableRepository<T extends MutableEntity<PK>, PK extends PrimaryKey> extends SimpleRepository<T> implements MutableRepository<T> {
    private final PrimaryKeyGenerator<PK> pkgen;

    @SuppressWarnings("unchecked")
    public SimpleMutableRepository(EntityMetadata<T> metadata) {
        super(metadata);

        this.pkgen = (PrimaryKeyGenerator<PK>) PrimaryKeyGenerators.of(metadata.getPrimaryKeyProperty().getPropertyClass()).get();
    }

    protected PrimaryKeyGenerator<PK> getPrimaryKeyGenerator() {
        return pkgen;
    }

    private void initPrimaryKeyGenerator() {
        PK pk = null;

        for (T entity : getEntities()) {
            if (pk == null || pk.compareTo(entity.getPrimaryKey()) < 0) {
                pk = entity.getPrimaryKey();
            }
        }

        getPrimaryKeyGenerator().init(pk);
    }

    @Override
    public void setUp(DatabaseContext context) {
        super.setUp(context);

        initPrimaryKeyGenerator();
    }

    @Override
    public void tearDown() {
        super.tearDown();

        getContext().getPersistenceStrategy().update(getEntities(), getEntityMetadata());
    }

    @Override
    protected void insert(T entity) {
        entity.setPrimaryKey(getPrimaryKeyGenerator().next());

        super.insert(entity);
    }

    @Override
    public boolean isPersisted(T entity) {
        return find(entity.getPrimaryKey()).isPresent();
    }

    @Override
    public void persist(T entity) {
        if (isPersisted(entity)) {
            insert(entity);
            getContext().getPersistenceStrategy().create(entity, getEntityMetadata());
        } else {
            getContext().getPersistenceStrategy().update(entity, getEntityMetadata());
        }
    }

    @Override
    public void delete(T entity) {
        if (isPersisted(entity)) {
            getContext().getPersistenceStrategy().destroy(entity, getEntityMetadata());
        }
    }
}