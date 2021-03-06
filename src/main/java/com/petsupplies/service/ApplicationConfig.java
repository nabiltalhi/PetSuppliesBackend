/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.petsupplies.service;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author ntalhi
 */
@javax.ws.rs.ApplicationPath("rest")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.petsupplies.service.AccountFacadeREST.class);
        resources.add(com.petsupplies.service.CategoryFacadeREST.class);
        resources.add(com.petsupplies.service.NewCrossOriginResourceSharingFilter.class);
        resources.add(com.petsupplies.service.ProductFacadeREST.class);
        resources.add(com.petsupplies.service.SessionFacadeREST.class);
    }
    
}
