/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myservice.mynamespace.data;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.transaction.NotSupportedException;
import javax.xml.ws.Response;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.SearchManager;
import org.apache.olingo.server.api.ODataResponse;

/**
 *
 * @author Martin
 */
public class InfinispanStorage {
    
    //private static final Logger log = Logger.getLogger(InfinispanFunctions.class.getName());
    
    private DefaultCacheManager defaultCacheManager = null;
    // for faster cache access
    private HashMap<String, AdvancedCache> caches = new HashMap<String, AdvancedCache>();
    
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
                defaultCacheManager.startCache(cacheName);
                Cache cache = defaultCacheManager.getCache(cacheName);
                this.caches.put(cacheName, cache.getAdvancedCache());
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
    private String callFunctionPut(String setNameWhichIsCacheName, String entryKey, CachedValue cachedValue,
                                         boolean ignoreReturnValues) {

        //log.trace("Putting into " + setNameWhichIsCacheName + " cache, entryKey: " +
                //entryKey + " value: " + cachedValue.toString() + " ignoreReturnValues=" + ignoreReturnValues);

        if (ignoreReturnValues) {
            getCache(setNameWhichIsCacheName).withFlags(Flag.IGNORE_RETURN_VALUES).put(entryKey, cachedValue);
            return null;
        } else {
            getCache(setNameWhichIsCacheName).put(entryKey, cachedValue);
            CachedValue resultOfPutForResponse = (CachedValue) getCache(setNameWhichIsCacheName).get(entryKey);
            return  standardizeJSONresponse(
                    new StringBuilder(resultOfPutForResponse.getJsonValueWrapper().getJson())).toString();
        }
    }


    /**
     * Get the entry.
     * Method supports both key-value approach or query approach.
     * <p/>
     * Decision logic is driven by passed parameters (entryKey is specified, or queryInfo.filter is specified)
     * <p/>
     * [ODATA SPEC] Note that standardizeJSONresponse() functions is called for return values. Results of this function
     * will be directly returned to clients
     *
     * @param setNameWhichIsCacheName - cache name
     * @param entryKey                 - key of desired entry
     * @param queryInfo                - queryInfo object from odata4j layer
     * @return
     */
    public String callFunctionGet(String setNameWhichIsCacheName, String entryKey,
                                        QueryInfo queryInfo) throws Exception {
        List<Object> queryResult = null;
        if (entryKey != null) {
            // ignore query and return value directly
            CachedValue value = (CachedValue) getCache(setNameWhichIsCacheName).get(entryKey);
            if (value != null) {
                //log.trace("CallFunctionGet entry with key " + entryKey + " was found. Returning response with status 200.");

                return standardizeJSONresponse(new StringBuilder(value.getJsonValueWrapper().getJson())).toString();
            } else {
                // no results found, clients will get 404 response
                //log.trace("CallFunctionGet entry with key " + entryKey + " was not found. Returning response with status 404.");

                return null;
            }

        } else {
            // NO ENTRY KEY -- query on document store expected
            if (queryInfo.filter == null) {
                String error = "Chyba";
                return error ;
            }

            //log.trace("Query report for $filter " + queryInfo.filter.toString());

            SearchManager searchManager = org.infinispan.query.Search.getSearchManager(getCache(setNameWhichIsCacheName));
            MapQueryExpressionVisitor mapQueryExpressionVisitor =
                    new MapQueryExpressionVisitor(searchManager.buildQueryBuilderForClass(CachedValue.class).get());
            mapQueryExpressionVisitor.visit(queryInfo.filter);

            // Query cache here and get results based on constructed Lucene query
            CacheQuery queryFromVisitor = searchManager.getQuery(mapQueryExpressionVisitor.getBuiltLuceneQuery(),
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
                if (queryInfo.top != null) {
                    int n = queryInfo.top.intValue();
                    if (n < queryResult.size()) {
                        queryResult = queryResult.subList(0, n);
                    }
                    //log.trace("TOP query filter option applied, value: " + n);
                }

                // skip first n results
                if (queryInfo.skip != null) {
                    int n = queryInfo.skip.intValue();
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

            if (queryInfo.orderBy != null) {
                throw new NotSupportedException("orderBy is not supported yet. Planned for version 1.1.");
            }
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
            return standardizeJSONresponse(sb).toString();
        } else {
            // no results found, clients will get 404 response
            return null;
        }
    }

    public void callFunctionRemove(String setNameWhichIsCacheName, String entryKey) {
        //log.trace("Removing entry from cache. EntryKey = " + entryKey);
        CachedValue removed = (CachedValue) getCache(setNameWhichIsCacheName).remove(entryKey);
        
    }

    public void callFunctionUpdate(String setNameWhichIsCacheName, String entryKey, CachedValue cachedValue)
            throws Exception {

        //log.trace("Replacing in " + setNameWhichIsCacheName + " cache, entryKey: " + entryKey + " value: " + cachedValue.toString());
        getCache(setNameWhichIsCacheName).replace(entryKey, cachedValue);
    }
    
    private StringBuilder standardizeJSONresponse(StringBuilder value) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"d\" : ");
        sb.append(value.toString());
        sb.append("}");
        return sb;
    }
}
