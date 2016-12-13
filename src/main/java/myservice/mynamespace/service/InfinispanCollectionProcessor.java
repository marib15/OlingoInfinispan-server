/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myservice.mynamespace.service;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import myservice.mynamespace.data.InfinispanStorage;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

/**
 *
 * @author Martin
 */
public class InfinispanCollectionProcessor implements EntityCollectionProcessor{
    
    private OData odata;
    private ServiceMetadata serviceMetadata;
    private InfinispanStorage infinispanStorage;
        
    public InfinispanCollectionProcessor(InfinispanStorage infinispanStorage) {
        this.infinispanStorage = infinispanStorage;
        System.out.println("Trieda: InfinispanCollectionProcessor, metoda: konstruktor");
    }
    
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
	this.serviceMetadata = sm;   
    }

    public void readEntityCollection(ODataRequest odr, ODataResponse odr1, UriInfo uriInfo, ContentType ct) throws ODataApplicationException, ODataLibraryException {
            System.out.println("Trieda: InfinispanCollectionProcesor, metoda: readEntityCollection");
            // 1st: retrieve the requested EntitySet from the uriInfo (representation of the parsed URI)
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		// in our example, the first segment is the EntitySet
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
                Entity entity = null;
            try {
                // 2nd: fetch the data from backend for this requested EntitySetName and deliver as EntitySet
                 Entity entityCollection = infinispanStorage.callFunctionGet(edmEntitySet.getName(),null,uriInfo);
            } catch (Exception ex) {
                Logger.getLogger(InfinispanCollectionProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
		
		// 3rd: Check if filter system query option is provided and apply the expression if necessary
		FilterOption filterOption = uriInfo.getFilterOption();
		/* if(filterOption != null) {
			// Apply $filter system query option
			try {
			      List<Entity> entityList = entityCollection.getEntities();
			      Iterator<Entity> entityIterator = entityList.iterator();
			      
			      // Evaluate the expression for each entity
			      // If the expression is evaluated to "true", keep the entity otherwise remove it from the entityList
			      while (entityIterator.hasNext()) {
			    	  // To evaluate the the expression, create an instance of the Filter Expression Visitor and pass
			    	  // the current entity to the constructor
			    	  Entity currentEntity = entityIterator.next();
			    	  Expression filterExpression = filterOption.getExpression();
			    	  MapQueryExpressionVisitor expressionVisitor = new MapQueryExpressionVisitor(currentEntity);
			    	  
			    	  // Start evaluating the expression
			    	  Object visitorResult = filterExpression.accept(expressionVisitor);
			    	  
			    	  // The result of the filter expression must be of type Edm.Boolean
			    	  if(visitorResult instanceof Boolean) {
			    		  if(!Boolean.TRUE.equals(visitorResult)) {
			    		    // The expression evaluated to false (or null), so we have to remove the currentEntity from entityList
			    		    entityIterator.remove();
			    		  }
			    	  } else {
			    		  throw new ODataApplicationException("A filter expression must evaulate to type Edm.Boolean", 
			    		      HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			    	  }
			      }

			    } catch (ExpressionVisitException e) {
			      throw new ODataApplicationException("Exception in filter evaluation",
			          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			    }
                }*/
    }
   
    
}
