package org.atomium.util;

public interface InsertQueryBuilder {

	InsertQueryBuilder value(String field);
	InsertQueryBuilder value(String field, Object value);
	
}