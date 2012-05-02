package org.atomium.repository.impl;

import org.atomium.EntityManager;
import org.atomium.PersistableEntity;
import org.atomium.repository.DeletableEntityRepository;
import org.atomium.repository.PersistableEntityRepository;
import org.atomium.util.pk.PrimaryKeyGenerator;
import org.atomium.util.query.Query;

public abstract class AbstractEntityRepository<PK, T extends PersistableEntity<PK>>
	extends AbstractSaveableEntityRepository<PK, T>
	implements PersistableEntityRepository<PK, T>, DeletableEntityRepository<PK, T>
{
	
	private final PrimaryKeyGenerator<PK> pkgen;

	protected AbstractEntityRepository(EntityManager em, PrimaryKeyGenerator<PK> pkgen) {
		super(em);
		this.pkgen = pkgen;
	}
	
	protected abstract Query buildDeleteQuery(T entity);
	protected abstract Query buildPersistQuery(T entity);

	public void delete(T entity) {
		Query query = buildDeleteQuery(entity);
		em.execute(query);
	}

	public void persist(T entity) {
		entity.setId(pkgen.next());
		Query query = buildPersistQuery(entity);
		em.execute(query);
	}

}