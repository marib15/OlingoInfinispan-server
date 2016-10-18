/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myservice.mynamespace.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
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
     
     // Entity Types Names
    public static final String ET_JSON_NAME = "JSON";
    public static final FullQualifiedName ET_JSON_FQN = new FullQualifiedName(NAMESPACE, ET_JSON_NAME);

    // EDM Container
     public static final String CONTAINER_NAME = "Container";
     public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
     
     public static final String ES_JSONS_NAME = "JSONS";
  
    public List<CsdlSchema> getSchemas() throws ODataException {
        
        // create Schema
        CsdlSchema schema = new CsdlSchema();
         schema.setNamespace(NAMESPACE);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
        entityTypes.add(getEntityType(ET_JSON_FQN));
        schema.setEntityTypes(entityTypes);

        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
        schemas.add(schema);

        return schemas;
    }

    public CsdlEntityContainer getEntityContainer() throws ODataException {
        // create EntitySets
        System.out.println("getEntityContainer dosiahnute");
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        entitySets.add(getEntitySet(CONTAINER, ES_JSONS_NAME));

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
     
    public CsdlEntityType getEntityType(FullQualifiedName fqn) throws ODataException {
        System.out.println("getEntityType dosiahnute");
        System.out.println("fqn z getEntityType" + fqn);
        if (fqn.equals(ET_JSON_FQN)){
        
            CsdlProperty id = new CsdlProperty().setName("ID").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty json = new CsdlProperty().setName("json").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");
            
            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(ET_JSON_NAME);
            entityType.setProperties(Arrays.asList(id, json));
            entityType.setKey(Collections.singletonList(propertyRef));
            
            return entityType;

        }
        return null;
    }
   
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

        if(entityContainer.equals(CONTAINER)){
            if(entitySetName.equals(ES_JSONS_NAME)){
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(ES_JSONS_NAME);
                entitySet.setType(ET_JSON_FQN);

            return entitySet;
            }
        }

        return null;
    }
    
    public CsdlEnumType getEnumType(FullQualifiedName fqn) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName fqn) throws ODataException {
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
        return null;
    }

    public CsdlAnnotations getAnnotationsGroup(FullQualifiedName fqn, String string) throws ODataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
