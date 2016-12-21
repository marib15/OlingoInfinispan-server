package myservice.mynamespace.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import myservice.mynamespace.data.InfinispanStorage;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

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
                 String entityCollection = infinispanStorage.callFunctionGet(edmEntitySet.getName(),uriInfo);
                 System.out.println(entityCollection);
            } catch (Exception ex) {
                Logger.getLogger(InfinispanCollectionProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
   
    
}
