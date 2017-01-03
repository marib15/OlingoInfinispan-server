package myservice.mynamespace.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.transaction.NotSupportedException;
import myservice.mynamespace.service.MapQueryExpressionVisitor;
import org.apache.lucene.search.Query;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.SearchManager;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

/**
 *
 * @author Martin Ribaric
 */
public class InfinispanStorage {

    private final List<Entity> productList; 
    final String cacheName = "JSONs";
   
    private DefaultCacheManager defaultCacheManager = null;
    // for faster cache access
    private final HashMap<String, AdvancedCache> caches = new HashMap<String, AdvancedCache>();
    
    public InfinispanStorage() throws IOException{
        productList = new ArrayList<Entity>();   
        defaultCacheManager = new DefaultCacheManager("infinispan-config.xml", true);
        Set<String> cacheNames = defaultCacheManager.getCacheNames();
    }   
    
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
        if (caches.get(cacheName) != null) {
            return this.caches.get(cacheName);
        } else {
            try {         
                defaultCacheManager.startCaches(cacheName);
                Cache cache = defaultCacheManager.getCache(cacheName);
                this.caches.put(cacheName, cache.getAdvancedCache());
                return cache.getAdvancedCache();
            } catch (Exception e) {
                e.printStackTrace();
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
        if (ignoreReturnValues) {
            AdvancedCache cache = getCache(setNameWhichIsCacheName);
            CachedValue info = (CachedValue)cache.put(entryKey, cachedValue);
            getCache(setNameWhichIsCacheName).withFlags(Flag.IGNORE_RETURN_VALUES).put(entryKey, cachedValue);
            return null;
        } else {
            getCache(setNameWhichIsCacheName).put(entryKey, cachedValue);
            CachedValue resultOfPutForResponse = (CachedValue) getCache(setNameWhichIsCacheName).get(entryKey);
            final Entity response = new Entity()
                .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, entryKey))
		.addProperty(new Property(null, "json", ValueType.PRIMITIVE, resultOfPutForResponse.getJsonValueWrapper().getJson()));
            return response;
        }
    }


    public Entity callFunctionGetEntity(String setNameWhichIsCacheName, String entryKey,UriInfo uriInfo)
                                throws ExpressionVisitException,ODataApplicationException, NotSupportedException, Exception {
        if (entryKey != null) {
            // ignore query and return value directly
            CachedValue value = (CachedValue) getCache(setNameWhichIsCacheName).get(entryKey);
            if (value != null) {                
                final Entity response = new Entity()
                    .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, entryKey))
                    .addProperty(new Property(null, "json", ValueType.PRIMITIVE, value.getJsonValueWrapper().getJson()));
                return response;
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
     * @param uriInfo                - queryInfo object from odata4j layer
     * @return                          -return String
     */
    public Entity callFunctionGet(String setNameWhichIsCacheName,UriInfo uriInfo)
            throws ExpressionVisitException, ODataApplicationException, NotSupportedException, Exception {
        System.out.println("Trieda: InfinispanStorage, metoda: callFunctionGet");
        
        List<Object> queryResult = null;
        FilterOption filterOption = uriInfo.getFilterOption();
            // NO ENTRY KEY -- query on document store expected
            if (filterOption == null) {
                Entity error = null;
                return error ;
            }
            
        AdvancedCache advance = getCache(setNameWhichIsCacheName);
        SearchManager searchManager = org.infinispan.query.Search.getSearchManager(advance);
        MapQueryExpressionVisitor mapQueryExpressionVisitor =
                new MapQueryExpressionVisitor(searchManager.buildQueryBuilderForClass(CachedValue.class).get());
            
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
        
        // Query cache here and get results based on constructed Lucene query
        CacheQuery queryFromVisitor = searchManager.getQuery(query,CachedValue.class);
        // pass query result to the function final response
        queryResult = queryFromVisitor.list();

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

            final Entity response = new Entity()
                    .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, "0"))
                    .addProperty(new Property(null, "json", ValueType.PRIMITIVE, standardizeJSONresponse(sb).toString()));
                return response;
        } else {
            // no results found, clients will get 404 response
            return null;
        }
    }

    public void callFunctionRemove(String setNameWhichIsCacheName, String entryKey) {
 
        CachedValue removed = (CachedValue) getCache(setNameWhichIsCacheName).remove(entryKey);
    }

    public void callFunctionUpdate(String setNameWhichIsCacheName, String entryKey, CachedValue cachedValue)
            throws Exception {
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

}
