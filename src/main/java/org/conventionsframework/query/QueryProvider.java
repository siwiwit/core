/*
 * Copyright 2011-2014 Conventions Framework.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.conventionsframework.query;

import org.conventionsframework.model.BaseEntity;
import org.conventionsframework.qualifier.Query;
import org.conventionsframework.qualifier.QueryParam;
import org.conventionsframework.qualifier.QueryParams;
import org.conventionsframework.qualifier.Service;
import org.conventionsframework.service.BaseService;
import org.conventionsframework.util.BeanManagerController;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 *
 * @author Rafaem M. Pestano - Sep 8, 2012 5:35:00 PM
 */

@Query(service = BaseService.class)
@Interceptor
public class QueryProvider<T extends BaseEntity> implements Serializable{


    @AroundInvoke
    public Object execute(InvocationContext ic) throws Exception{
        BaseService service = null;
        Query query = ic.getMethod().getAnnotation(Query.class);
        Class<? extends BaseService> serviceClass = query.service();
        if(!serviceClass.equals(BaseService.class)){
           service = (BaseService) BeanManagerController.getBeanByType(serviceClass);
        }
        javax.persistence.Query q = null;

        if(!"".equals(query.sql())){
             q = service.crud().getEntityManager().createQuery(query.sql());
        }
        else if(!"".equals(query.namedQuery())){
             q = service.crud().getEntityManager().createNamedQuery(query.namedQuery());
        }
        
        QueryParams params = ic.getMethod().getAnnotation(QueryParams.class);
        if(params != null){
            for (QueryParam queryParam : params.value()) {
                this.addParam(queryParam, q);
            }
        }
        
        QueryParam param = ic.getMethod().getAnnotation(QueryParam.class);
        if(param != null){
            this.addParam(param, q);
        }
        if(q != null){
            return q.getResultList();
        }
        else {
            return q;
        }
    }
    

    private void addParam(QueryParam param, javax.persistence.Query q){
        if(!"".equals(param.value())){
            q.setParameter(param.name(), param.value());
        }
        if(param.intValue() != -9999){
            q.setParameter(param.name(), param.intValue());
        }
       
    }
}
