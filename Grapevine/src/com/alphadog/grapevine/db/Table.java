package com.alphadog.grapevine.db;

import java.util.List;

public interface Table<T> {
	
	public String getTableName();
	
	public T findById(long id);
	
	public List<T> findAll();

	public T create(T newElement);
	
}
