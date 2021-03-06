/*
 * Copyright 2011-2014 Conventions Framework.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 */
package org.conventionsframework.paginator;

import org.conventionsframework.model.BaseEntity;
import org.conventionsframework.model.PaginationResult;
import org.conventionsframework.model.SearchModel;
import org.conventionsframework.qualifier.Service;
import org.conventionsframework.util.BeanManagerController;
import org.conventionsframework.service.BaseService;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.conventionsframework.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * @author rmpestano
 */
@Dependent
@Service
public class Paginator<T extends BaseEntity> implements Serializable {

	private LazyDataModel<T> dataModel;
	private Integer rowCount;
	private BaseService<T> baseService;
	private SearchModel<T> searchModel;
	private boolean keepSearchInSession = true;// keep searchModel in user
												// session
	@Inject
	private SearchModelCache searchModelCache;
	@Inject
	@Service
	private Instance<BaseService<T>> baseServiceInstance;

	public Paginator() {
	}

	@Inject
	public void Paginator(InjectionPoint ip) {
		if (ip != null) {
			ParameterizedType type = (ParameterizedType) ip.getType();
			Type[] typeArgs = type.getActualTypeArguments();
			Class<T> persistentClass = ((Class<T>) typeArgs[0]);
			if (ip.getAnnotated().isAnnotationPresent(Service.class)) {
				initService(ip);
			}
			try {
				initDataModel(persistentClass);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalArgumentException(
						"could not initialize paginator datamodel: "
								+ e.getMessage());
			}
		}
	}

	private void initService(InjectionPoint ip) {
		Service paginatorService = ip.getAnnotated().getAnnotation(
				Service.class);
		try {
			if (!paginatorService.value().equals(BaseService.class)) {
				// set paginator service by type
				baseService = BeanManagerController
						.getBeanByType(paginatorService.value());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException(
					"Could not initialize Paginator service of "
							+ ip.getMember().getDeclaringClass() + ":"
							+ ex.getMessage());
		}

	}

	public Paginator(BaseService service) {
		this.baseService = service;
		initDataModel(service.getPersistentClass());
	}

	private void initDataModel(Class<T> persistentClass) {
		try {
			if (keepSearchInSession) {
				String searchKey = persistentClass.getSimpleName();
				searchModel = (SearchModel<T>) getSearchModelCache()
						.getSearchModel(searchKey);
				if (searchModel == null) {
					searchModel = new SearchModel<T>(
							persistentClass.newInstance());
					searchModelCache.addSearchModel(searchKey, searchModel);
				}
			} else {
				searchModel = new SearchModel<T>(persistentClass.newInstance());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
					"Could not initialize Paginator datamodel: "
							+ e.getMessage());
		}
		this.setDataModel(new LazyDataModel<T>() {

			@Override
			public List<T> load(int first, int pageSize, String sortField,
					SortOrder sortOrder, Map dataTableFilters) {
				PaginationResult<T> paginationResult;
				searchModel.setFirst(first);
				searchModel.setPageSize(pageSize);
				searchModel.setSortField(sortField);
				searchModel.setSortOrder(sortOrder);
				searchModel.getDatatableFilter().clear();
				searchModel.getDatatableFilter().putAll(dataTableFilters);
				paginationResult = baseService.executePagination(searchModel);
				rowCount = paginationResult.getRowCount();
				this.setRowCount(rowCount);
				return paginationResult.getPage();
			}
		});
	}

	public BaseService getBaseService() {
		return baseService;
	}

	public void setBaseService(BaseService baseService) {
		this.baseService = baseService;
		if(searchModel == null){
			initDataModel(baseService.getPersistentClass());
		}
	}

	public Integer getRowCount() {
		return rowCount;
	}

	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	public LazyDataModel<T> getDataModel() {
		return dataModel;
	}

	public void setDataModel(LazyDataModel<T> dataModel) {
		this.dataModel = dataModel;
	}

	public boolean isKeepSearchInSession() {
		return keepSearchInSession;
	}

	public void setKeepSearchInSession(boolean keepSearchInSession) {
		this.keepSearchInSession = keepSearchInSession;
	}

	public void pageListener() {
		// override to perform an action on datatable outcome event
	}

	public void sortListener() {
		// override to perform an action on datatable sort event
	}

	public void filterListener() {
		// override to perform an action on datatable filter event
	}

	public SearchModelCache getSearchModelCache() {
		if (searchModelCache == null) {// its null when Paginator is created via
										// new operator
			searchModelCache = (SearchModelCache) BeanManagerController
					.getBeanByType(SearchModelCache.class);
		}
		return searchModelCache;
	}

	public Map<String, Object> getFilter() {
		return searchModel.getFilter();
	}

	public void setFilter(Map<String, Object> filter) {
		searchModel.setFilter(filter);
	}

	public SearchModel<T> getSearchModel() {
		return searchModel;
	}

	public void resetCache() {
		getSearchModelCache().resetSearchModel(
				searchModel.getEntity().getClass().getSimpleName());
			initDataModel(baseService.getPersistentClass());
	}

	/**
	 * 
	 * methods for backward compatibility
	 */

	public T getEntity() {
		return searchModel.getEntity();
	}

	public List<T> getSelection() {
		return searchModel.getSelection();
	}

	public void setSelection(List<T> selection) {
		searchModel.setSelection(selection);
	}

	public T getSingleSelection() {
		return searchModel.getSingleSelection();
	}

	public void setSingleSelection(T singleSelection) {
		searchModel.setSingleSelection(singleSelection);
	}

	public List<T> getFilteredValue() {
		return searchModel.getFilteredValue();
	}

	public void setFilteredValue(List<T> filteredValue) {
		searchModel.setFilteredValue(filteredValue);
	}

}
