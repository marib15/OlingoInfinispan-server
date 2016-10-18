/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myservice.mynamespace.service;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import myservice.mynamespace.data.InfinispanStorage;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
/**
 *
 * @author Martin
 */
public class InfinispanProcessor implements PrimitiveProcessor{
    
    private OData odata;
    private InfinispanStorage infinispanStorage;
    private ServiceMetadata serviceMetadata;
    
    public InfinispanProcessor(InfinispanStorage infinispanStorage){
        this.infinispanStorage = infinispanStorage;
        System.out.println("konstruktor procesor");
    }
    
    public void init(OData odata, ServiceMetadata serviceMetadata) {
	this.odata = odata;
	this.serviceMetadata = serviceMetadata;
        System.out.println("init metoda"+ serviceMetadata);
    }
    
    public void readPrimitive(ODataRequest request, ODataResponse response, 
                                    UriInfo uriInfo, ContentType responseFormat){
        System.out.println("readPrimitiv dosiahnute");
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        System.out.println("Resource Paths:" + resourcePaths.get(0));
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        System.out.println("Entity Set" + edmEntitySet);
        try {
            String contentJSON = infinispanStorage.callFunctionGet(null, null, null);
        } catch (Exception ex) {
            Logger.getLogger(InfinispanProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
	response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    
    }
    
    public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                           ContentType requestFormat, ContentType responseFormat){
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        try {
            infinispanStorage.callFunctionUpdate(null, null, null);
        } catch (Exception ex) {
            Logger.getLogger(InfinispanProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    
    }
    
    public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo){
        
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
	EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        
        infinispanStorage.callFunctionRemove(null, null);
        
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    
    }
    
}
