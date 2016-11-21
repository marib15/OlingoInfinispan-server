/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myservice.mynamespace.service;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.NotSupportedException;
import myservice.mynamespace.data.InfinispanStorage;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
/**
 *
 * @author Martin
 */
public class InfinispanEntityProcessor implements EntityProcessor{
    
    private OData odata;
    private InfinispanStorage infinispanStorage;
    private ServiceMetadata serviceMetadata;
    
    public InfinispanEntityProcessor(InfinispanStorage infinispanStorage){
        this.infinispanStorage = infinispanStorage;
        System.out.println("Trieda: InfinispanEntityProcessor, metoda: konstruktor");
    }
    
    public void init(OData odata, ServiceMetadata serviceMetadata) {
	this.odata = odata;
	this.serviceMetadata = serviceMetadata;
        System.out.println("init metoda"+ serviceMetadata);
    }
    
    public void readEntity(ODataRequest request, ODataResponse response, 
                                    UriInfo uriInfo, ContentType responseFormat) throws SerializerException, ODataApplicationException{
        System.out.println("Trieda: InfinispanEntityProcessor, metoda: readEntity");
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        System.out.println("Resource Paths:" + resourcePaths.get(0));
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        System.out.println("Entity Set" + edmEntitySet);
        Entity entity = null;

        try {
            entity = infinispanStorage.callFunctionGetEntity(edmEntitySet.getName(), keyPredicates.get(0).getText(), uriInfo);
            //System.out.println(contentJSON);
        } catch (NotSupportedException ex) {
            Logger.getLogger(InfinispanEntityProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(InfinispanEntityProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
	// 3. serialize
		EdmEntityType entityType = edmEntitySet.getEntityType();

		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build();
	 	// expand and select currently not supported
		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, options);
		InputStream entityStream = serializerResult.getContent();

		//4. configure the response object
		response.setContent(entityStream);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    
    }
    
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                           ContentType requestFormat, ContentType responseFormat){
        System.out.println("Trieda: InfinispanEntityProcessor, metoda: updateEntity");
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        try {
            infinispanStorage.callFunctionUpdate(edmEntitySet.getName(), keyPredicates.get(0).getText(), null);
        } catch (Exception ex) {
            Logger.getLogger(InfinispanEntityProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    
    }
    
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo){
        System.out.println("Trieda: InfinispanEntityProcessor, metoda: deleteEntity");
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        
        infinispanStorage.callFunctionRemove(edmEntitySet.getName(), keyPredicates.get(0).getText());
        
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    
    }

    public void createEntity(ODataRequest odr, ODataResponse odr1, UriInfo ui, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Trieda: InfinispanEntityProcessor, metoda: createEntity");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    
}
