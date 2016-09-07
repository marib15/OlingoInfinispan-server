/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myservice.mynamespace.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;

/**
 *
 * @author Martin
 */
public class ApacheProvider implements CsdlEdmProvider{

    // Service Namespace
     public static final String NAMESPACE = "OData.Demo";

    // EDM Container
     public static final String CONTAINER_NAME = "Container";
     public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
  
    public List<CsdlSchema> getSchemas() throws ODataException {
       
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);
        
        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
        schemas.add(schema);

        return schemas;
    }

    public CsdlEntityContainer getEntityContainer() throws ODataException {
        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        /*entitySets.add(getEntitySet(CONTAINER, ES_PRODUCTS_NAME)); */

    // create EntityContainer
    CsdlEntityContainer entityContainer = new CsdlEntityContainer();
    entityContainer.setName(CONTAINER_NAME);
    entityContainer.setEntitySets(entitySets);

    return entityContainer;
    }
    
     public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        // This method is invoked when displaying the service document at
        // e.g. http://localhost:8080/DemoService/DemoService.svc
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
             return entityContainerInfo;
        }
        return null;
    }
   

    
    public CsdlEnumType getEnumType(FullQualifiedName fqn) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName fqn) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlEntityType getEntityType(FullQualifiedName fqn) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlComplexType getComplexType(FullQualifiedName fqn) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<CsdlAction> getActions(FullQualifiedName fqn) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<CsdlFunction> getFunctions(FullQualifiedName fqn) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlTerm getTerm(FullQualifiedName fqn) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlEntitySet getEntitySet(FullQualifiedName fqn, String string) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlSingleton getSingleton(FullQualifiedName fqn, String string) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlActionImport getActionImport(FullQualifiedName fqn, String string) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlFunctionImport getFunctionImport(FullQualifiedName fqn, String string) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<CsdlAliasInfo> getAliasInfos() throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlAnnotations getAnnotationsGroup(FullQualifiedName fqn, String string) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
