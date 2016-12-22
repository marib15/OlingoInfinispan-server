package myservice.mynamespace.service;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

/**
 *
 * @author Martin Ribaric
 */
public class InfinispanCollectionProcessor implements EntityCollectionProcessor{
    
    private OData odata;
    private ServiceMetadata serviceMetadata;
    private InfinispanStorage infinispanStorage;
        
    public InfinispanCollectionProcessor(InfinispanStorage infinispanStorage) {
        this.infinispanStorage = infinispanStorage;
    }
    
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
	this.serviceMetadata = sm;   
    }

    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
            // 1st: retrieve the requested EntitySet from the uriInfo (representation of the parsed URI)
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		// in our example, the first segment is the EntitySet
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
                Entity entityCollection = null;
            try {
                // 2nd: fetch the data from backend for this requested EntitySetName and deliver as EntitySet
                 entityCollection = infinispanStorage.callFunctionGet(edmEntitySet.getName(),uriInfo);
            } catch (Exception ex) {
                Logger.getLogger(InfinispanCollectionProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (entityCollection != null){
                EdmEntityType entityType = edmEntitySet.getEntityType();

		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build();
	 	// expand and select currently not supported
		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entityCollection, options);
		InputStream entityStream = serializerResult.getContent();

		//4. configure the response object
		response.setContent(entityStream);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
            }    
            else {
            response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
            }
    }
   
    
}
