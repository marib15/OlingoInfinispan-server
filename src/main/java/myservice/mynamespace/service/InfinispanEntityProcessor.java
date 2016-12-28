package myservice.mynamespace.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.NotSupportedException;
import myservice.mynamespace.data.CachedValue;
import myservice.mynamespace.data.InfinispanStorage;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
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
 * @author Martin Ribaric
 */
public class InfinispanEntityProcessor implements EntityProcessor{
    
    private OData odata;
    private final InfinispanStorage infinispanStorage;
    private ServiceMetadata serviceMetadata;
    
    public InfinispanEntityProcessor(InfinispanStorage infinispanStorage){
        this.infinispanStorage = infinispanStorage;
    }
    
    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
	this.odata = odata;
	this.serviceMetadata = serviceMetadata;
    }
    
    @Override
    public void readEntity(ODataRequest request, ODataResponse response, 
                                    UriInfo uriInfo, ContentType responseFormat) throws SerializerException, ODataApplicationException{
      
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        Entity entity = null;

        try {
            entity = infinispanStorage.callFunctionGetEntity(edmEntitySet.getName(), keyPredicates.get(0).getText(), uriInfo);
        } catch (NotSupportedException ex) {
            Logger.getLogger(InfinispanEntityProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(InfinispanEntityProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        if (entity != null){
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
        else {
            response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
        }
    }
    
    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                           ContentType requestFormat, ContentType responseFormat) throws DeserializerException{
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        
        InputStream requestInputStream = request.getBody();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(requestInputStream)));
        StringBuilder sb = new StringBuilder();
        String readLine;
        CachedValue cachedValue = null;
        
        try {
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
            cachedValue = new CachedValue(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(InfinispanEntityProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            infinispanStorage.callFunctionPut(edmEntitySet.getName(), 
                    keyPredicates.get(0).getText(), cachedValue);
        } catch (Exception ex) {
            Logger.getLogger(InfinispanEntityProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    
    }
    
    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo){
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        
        infinispanStorage.callFunctionRemove(edmEntitySet.getName(), keyPredicates.get(0).getText());
        
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        
        
        InputStream requestInputStream = request.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(ct);
        DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
        Entity requestEntity = result.getEntity();
        CachedValue cachedValue = new CachedValue((String)requestEntity.getProperty("json").getValue());
        
        Entity responseEntity = infinispanStorage.callFunctionPost(edmEntitySet.getName(),
                (String)requestEntity.getProperty("ID").getValue(), cachedValue, true);
        
       //if (response == null){
            response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
       /* }
        else {
            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
            // expand and select currently not supported
            EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

            ODataSerializer serializer = this.odata.createSerializer(ct1);
            SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, responseEntity, options);

            //4. configure the response object
            response.setContent(serializedResponse.getContent());
            response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ct1.toContentTypeString());
        
        }*/
    }
    
    
}
