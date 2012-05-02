package org.atomium.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.atomium.Entity;
import org.atomium.EntityManager;
import org.atomium.exception.LoadingException;
import org.atomium.repository.EntityRepository;
import org.atomium.util.Action1;
import org.atomium.util.Filter;
import org.atomium.util.query.SelectQueryBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractEntityRepository<PK, T extends Entity<PK>>
	implements EntityRepository<PK, T>
{
	
	private boolean loaded;
	protected final EntityManager em;
	protected final Map<PK, T> entities = Maps.newConcurrentMap();
	
	protected AbstractEntityRepository(EntityManager em) {
		this.em = em;
	}
	
	protected abstract SelectQueryBuilder buildLoadQuery();
	protected abstract T load(ResultSet result) throws SQLException;
	protected abstract void unhandledException(Exception e);
	
	protected SelectQueryBuilder select(String table, String... fields) {
		return em.builder().select(table, fields);
	}
	
	protected void beforeLoading() throws LoadingException {
		
	}
	
	protected void afterLoading() throws LoadingException {
		
	}
	
	public int load() throws LoadingException {
		beforeLoading();
		
		em.query(buildLoadQuery(), new Action1<ResultSet>() {
			public Void invoke(ResultSet arg1) throws Exception {
				while (arg1.next()) {
					T entity = load(arg1);
					if (entity != null) {
						entities.put(entity.id(), entity);
					}
				}
				return null;
			}
		});
		
		loaded = true;
		
		afterLoading();
		return entities.size();
	}
	
	public boolean loaded() {
		return loaded;
	}
	
	public int count() {
		return entities.size();
	}
	
	public T find(PK pk) {
		return entities.get(pk);
	}
	
	public Iterator<T> iterator() {
		return entities.values().iterator();
	}
	
	public Collection<T> filter(Filter<T> filter) {
		List<T> result = Lists.newArrayList();
		for (T entity : entities.values()) {
			try {
				if (filter.invoke(entity)) {
					result.add(entity);
				}
			} catch (Exception e) {
				unhandledException(e);
			}
		}
		return result;
	}
	
}
