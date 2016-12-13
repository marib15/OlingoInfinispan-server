/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myservice.mynamespace.data;

import java.beans.Expression;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.NotSupportedException;
import javax.xml.ws.Response;
import myservice.mynamespace.service.ApacheProvider;
import myservice.mynamespace.service.MapQueryExpressionVisitor;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.SearchManager;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;
import org.infinispan.CacheCollection;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;

/**
 *
 * @author Martin
 */
public class InfinispanStorage {

    
   private List<Entity> productList; 
   String cacheName = "JSONs";
   
   private DefaultCacheManager defaultCacheManager = null;
    // for faster cache access
    private HashMap<String, AdvancedCache> caches = new HashMap<String, AdvancedCache>();
    
    private final Map<String, String> eis = new LinkedHashMap<String, String>();
    
   public InfinispanStorage() throws IOException{
       System.out.println("konstruktor InfinispanStorage");
       productList = new ArrayList<Entity>();   
           defaultCacheManager = new DefaultCacheManager("infinispan-config.xml", true);
            //defaultCacheManager = new DefaultCacheManager();
          
       Set<String> cacheNames = defaultCacheManager.getCacheNames();
       for (String cacheNam : cacheNames) {
           //log.info("Registering cache with name " + cacheName + " in OData InfinispanProducer...");
           // cacheName = entitySetName
           eis.put(cacheNam, null);
       }
       initSampleData();

   }
    
    public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet) {
        System.out.println("Trieda: InfinispanStorage, metoda: readEntitySetData");
        if(edmEntitySet.getName().equals(ApacheProvider.ES_JSONS_NAME)){
			return getJSONs();
		}

		return null;    }
    
    private EntityCollection getJSONs() {
        System.out.println("Trieda: InfinispanStorage, metoda: getJSONs");
        EntityCollection retEntitySet = new EntityCollection();

		for(Entity productEntity : this.productList){
			   retEntitySet.getEntities().add(productEntity);
		}

		return retEntitySet;    }

    
    //private static final Logger log = Logger.getLogger(InfinispanFunctions.class.getName());
    
    
    
    /**
     *
     * Get embedded Infinispan cache which acts as an underlying store for JSON documents.
     *
     * Look into global map for registered cache. Avoiding multiple asking CacheManager.
     * <p/>
     * If there is no cache with the given name, get it from CacheManager and put.
     *
     * @param cacheName -- name of cache, AdvancedCache is returned.
     * @return AdvancedCache instance in dependence on a given name.
     */
    private AdvancedCache getCache(String cacheName) {
        System.out.println("Trieda: InfinispanStorage, metoda: getCache");
        if (caches.get(cacheName) != null) {
            System.out.println("if v getCache");
            return this.caches.get(cacheName);
        } else {
            try {
                System.out.println("else v getCachce");         
                defaultCacheManager.startCaches(cacheName);
                Cache cache = defaultCacheManager.getCache(cacheName);
                this.caches.put(cacheName, cache.getAdvancedCache());
                System.out.println("pred navratom");
                return cache.getAdvancedCache();
            } catch (Exception e) {
                e.printStackTrace();
                //log.error("ERROR DURING STARTING CACHE " + cacheName, e);
            }
        }
        return this.caches.get(cacheName);
    }
    
    /**
     * HTTP POST request accepted, issued on service/cacheName_put?params...&$filter=... URI
     *
     * @return
     */
    public Entity callFunctionPut(String setNameWhichIsCacheName, String entryKey, CachedValue cachedValue,
                                         boolean ignoreReturnValues) {
        System.out.println("Trieda: InfinispanStorage, metoda: callFunctionPut");
        System.out.println("zaciatok funkcie callFunctionPut cacheName je: " + setNameWhichIsCacheName);
        System.out.println("entryKey: " + entryKey + " cachedValue: " + cachedValue.toString());
        //log.trace("Putting into " + setNameWhichIsCacheName + " cache, entryKey: " +
               // entryKey + " value: " + cachedValue.toString() + " ignoreReturnValues=" + ignoreReturnValues);

        if (ignoreReturnValues) {
            AdvancedCache cache = getCache(setNameWhichIsCacheName);
            CachedValue info = (CachedValue)cache.put(entryKey, cachedValue);
            getCache(setNameWhichIsCacheName).withFlags(Flag.IGNORE_RETURN_VALUES).put(entryKey, cachedValue);
            return null;
        } else {
           getCache(setNameWhichIsCacheName).put(entryKey, cachedValue);
           CachedValue resultOfPutForResponse = (CachedValue) getCache(setNameWhichIsCacheName).get(entryKey);
           final Entity response = new Entity()
                .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, Integer.valueOf(entryKey)))
		.addProperty(new Property(null, "json", ValueType.PRIMITIVE, resultOfPutForResponse.getJsonValueWrapper().getJson().toString()));
                return response;
        }
    }


    public Entity callFunctionGetEntity(String setNameWhichIsCacheName, String entryKey,
                                        UriInfo uriInfo) throws ExpressionVisitException, ODataApplicationException, NotSupportedException, Exception {
        System.out.println("Trieda: InfinispanStorage, metoda: callFunctionGetList");
        
        if (entryKey != null) {
            // ignore query and return value directly
            CachedValue value = (CachedValue) getCache(setNameWhichIsCacheName).get(entryKey);
            if (value != null) {
                //log.trace("CallFunctionGet entry with key " + entryKey + " was found. Returning response with status 200.");
            //String response = standardizeJSONresponse(new StringBuilder(value.getJsonValueWrapper().getJson())).toString();
                //return response;
                
            final Entity response = new Entity()
                .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, Integer.valueOf(entryKey)))
		.addProperty(new Property(null, "json", ValueType.PRIMITIVE, value.getJsonValueWrapper().getJson().toString()));
                //response.setId(createId("JSONs", Integer.valueOf(entryKey)));
                return response;
            } else {
                // no results found, clients will get 404 response
                //log.trace("CallFunctionGet entry with key " + entryKey + " was not found. Returning response with status 404.");

                return null;
            }
        }
        return null;
    }

    /**
     * Get the entry.
     * Method supports both key-value approach or query approach.
     *
     * Decision logic is driven by passed parameters (entryKey is specified, or queryInfo.filter is specified)
     * 
     * [O+
     * 69DATA SPEC] Note that standardizeJSONresponse() functions is called for return values. Results of this function
     * will be directly returned to clients
     *
     * @param setNameWhichIsCacheName - cache name
     * @param entryKey                 - key of desired entry
     * @param uriInfo                - queryInfo object from odata4j layer
     * @return                          -return String
     */
    public Entity callFunctionGet(String setNameWhichIsCacheName, String entryKey,
                                        UriInfo uriInfo) throws ExpressionVisitException, ODataApplicationException, NotSupportedException, Exception {
        System.out.println("Trieda: InfinispanStorage, metoda: callFunctionGet");
        
        List<Object> queryResult = null;
        FilterOption filterOption = uriInfo.getFilterOption();
            // NO ENTRY KEY -- query on document store expected
            if (filterOption == null) {
                Entity error = null;
                return error ;
            }
            
            //log.trace("Query report for $filter " + queryInfo.filter.toString());
            AdvancedCache advance = getCache(setNameWhichIsCacheName);
            boolean indexing = advance.getCacheConfiguration().indexing().index().isEnabled();
            SearchManager searchManager = org.infinispan.query.Search.getSearchManager(advance);
            MapQueryExpressionVisitor mapQueryExpressionVisitor =
                    new MapQueryExpressionVisitor(searchManager.buildQueryBuilderForClass(CachedValue.class).get());
            
            
            /*if (filterOption == null){
            CacheQuery queryFromVisitor = searchManager.getQuery(mapQueryExpressionVisitor.getBuiltLuceneQuery(),
                    CachedValue.class);
            queryResult = queryFromVisitor.list();
            }
            CacheCollection<CachedValue> allCachedValue = advance.values();*/
            VisitableExpression expression = filterOption.getExpression();
            Query query = null;
            if (expression instanceof Binary){
                if (((Binary) expression).getOperator()== BinaryOperatorKind.AND){
                    query = (Query) mapQueryExpressionVisitor.visitBinaryOperator(BinaryOperatorKind.AND,
                         ((Binary) expression).getLeftOperand(), ((Binary) expression).getRightOperand());
                }
                if (((Binary) expression).getOperator()== BinaryOperatorKind.OR){
                    query = (Query) mapQueryExpressionVisitor.visitBinaryOperator(BinaryOperatorKind.OR,
                         ((Binary) expression).getLeftOperand(), ((Binary) expression).getRightOperand());

                }
                if (((Binary) expression).getOperator() == BinaryOperatorKind.EQ){
                    query = (Query) mapQueryExpressionVisitor.visitBinaryOperator(BinaryOperatorKind.EQ, 
                         ((Binary) expression).getLeftOperand(), ((Binary) expression).getRightOperand());

                }
            }
           // Query query = (Query) expression.accept(mapQueryExpressionVisitor);
           /*if (expression instanceof Binary){
                Binary binaryExpression = (Binary) expression;
                mapQueryExpressionVisitor.visitBinaryOperator(binaryExpression.getOperator(), 
                        binaryExpression.getLeftOperand(),binaryExpression.getRightOperand());
            };*/
            //mapQueryExpressionVisitor.visit(uriInfo.getFilterOption());

            
            
            // Query cache here and get results based on constructed Lucene query
            CacheQuery queryFromVisitor = searchManager.getQuery(query,
                    CachedValue.class);
            // pass query result to the function final response
            queryResult = queryFromVisitor.list();

            //log.trace(" \n Search results (obtained from search manager," +
                   // " used visitor for query translation) size:" + queryResult.size() + ":");
            for (Object one_result : queryResult) {
                //log.trace(one_result);
            }

            // *********************************************************************************
            // We have set queryResult object containing list of results from querying the cache
            // Now apply other filters/order by/top/skip etc. requests

            try {
                // return first n results
                if (uriInfo.getTopOption() != null) {
                    int n = uriInfo.getTopOption().getValue();
                    if (n < queryResult.size()) {
                        queryResult = queryResult.subList(0, n);
                    }
                    //log.trace("TOP query filter option applied, value: " + n);
                }

                // skip first n results
                if (uriInfo.getSkipOption() != null) {
                    int n = uriInfo.getSkipOption().getValue();
                    if (n < queryResult.size()) {
                        queryResult = queryResult.subList(n, queryResult.size());
                        //log.trace("SKIP query filter option applied, value: " + n);
                    } else {
                        // skip all
                        queryResult = queryResult.subList(queryResult.size(), queryResult.size());
                        //log.trace("SKIP query filter option applied, skipped all values as n = " +
                             //   n + " and results size = " + queryResult.size());
                    }
                }

            } catch (Exception e) {
                throw new Exception("TOP or SKIP query option failed: " + e.getMessage());
            }

            if (uriInfo.getOrderByOption() != null) {
                throw new NotSupportedException("orderBy is not supported yet. Planned for version 1.1.");
            }

        int resultsCount = queryResult.size();
        if (resultsCount > 0) {
            StringBuilder sb = new StringBuilder();
            // build response

            if (resultsCount > 1) {
                sb.append("["); // start array of results
            }

            int counter = 0;
            for (Object one_result : queryResult) {
                counter++;
                // stack more JSON strings responses if needed
                CachedValue cv = (CachedValue) one_result;
                sb.append(cv.getJsonValueWrapper().getJson());

//                sb.append("\n"); // for better readability?

                if ((resultsCount > 1) && (resultsCount > counter)) {
                    // delimit results inside of an array, don't add "," after the last one JSON
                    sb.append(", \n");
                }
            }

            if (resultsCount > 1) {
                sb.append("]"); // end array of results
            }

            //log.trace("CallFunctionGet method... returning query results in JSON format: " + standardizeJSONresponse(sb).toString());
            //return standardizeJSONresponse(sb).toString();
            return null;
        } else {
            // no results found, clients will get 404 response
            return null;
        }
        
       // return null;
    }

    public void callFunctionRemove(String setNameWhichIsCacheName, String entryKey) {
        System.out.println("Trieda: InfinispanStorage, metoda: callFunctionRemove");
        //log.trace("Removing entry from cache. EntryKey = " + entryKey);
        CachedValue removed = (CachedValue) getCache(setNameWhichIsCacheName).remove(entryKey);
        
    }

    public void callFunctionUpdate(String setNameWhichIsCacheName, String entryKey, CachedValue cachedValue)
            throws Exception {
        System.out.println("Trieda: InfinispanStorage, metoda: callFunctionUpdate");
        //log.trace("Replacing in " + setNameWhichIsCacheName + " cache, entryKey: " + entryKey + " value: " + cachedValue.toString());
        getCache(setNameWhichIsCacheName).replace(entryKey, cachedValue);
    }
    
    private StringBuilder standardizeJSONresponse(StringBuilder value) {
        System.out.println("Trieda: InfinispanStorage, metoda: standardizeJSONresponse");
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"d\" : ");
        sb.append(value.toString());
        sb.append("}");
        return sb;
    }

    private void initSampleData() {
        System.out.println("Trieda: InfinispanStorage, metoda: initSampleData");
        final Entity e1 = new Entity()
			.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 1))
			.addProperty(new Property(null, "json", ValueType.PRIMITIVE, "[Martin, 23, cierna]"));
	//e1.setId(createId("JSONs", 1));
	productList.add(e1);
        //String id = "ID";
        Property propertyID1 = e1.getProperty("ID");
        Property propertyJSON1 = e1.getProperty("json");
        CachedValue json1 = new CachedValue((String)propertyJSON1.getValue());
        System.out.println("Property1- " + propertyID1.getValue() + " json- " + json1.toString());
        String entryKey = (String) propertyID1.getValue().toString();
        callFunctionPut(cacheName, (String) propertyID1.getValue().toString(), json1, true);
        

	final Entity e2 = new Entity()
			.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 2))
			.addProperty(new Property(null, "json", ValueType.PRIMITIVE, "[Michal, 25, biela]"));
	//e2.setId(createId("JSONs", 2));
	productList.add(e2);
        Property propertyID2 = e2.getProperty("ID");
        Property propertyJSON2 = e2.getProperty("json");
        CachedValue json2 = new CachedValue((String)propertyJSON2.getValue());
        callFunctionPut(cacheName, (String) propertyID2.getValue().toString(), json2, true);

	final Entity e3 = new Entity()
			.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 3))
			.addProperty(new Property(null, "json", ValueType.PRIMITIVE, "[Ondra,21, fialova]"));
	//e3.setId(createId("JSONs", 3));
	productList.add(e3);    
        Property propertyID3 = e3.getProperty("ID");
        Property propertyJSON3 = e3.getProperty("json");
        CachedValue json3 = new CachedValue((String)propertyJSON3.getValue());
        callFunctionPut(cacheName, (String) propertyID3.getValue().toString(), json3, true);
    }
    
   /* private URI createId(String entitySetName, Object id) {
                System.out.println("Trieda: InfinispanStorage, metoda: crateId");
		try {
                     URI uri = new URI(entitySetName + "(" + String.valueOf(id) + ")");
                     System.out.println("výsledn uri- " + uri);   
			return uri;
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}*/
}
