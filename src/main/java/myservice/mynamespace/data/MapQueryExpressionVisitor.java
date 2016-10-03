/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myservice.mynamespace.data;

import java.util.List;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.*;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 *
 * @author Martin
 */
class MapQueryExpressionVisitor implements ExpressionVisitor{

    private Query tmpQuery;
    private QueryBuilder queryBuilder;
    
    MapQueryExpressionVisitor(QueryBuilder get) {
        this.queryBuilder = queryBuilder;
    }

    public Query getBuiltLuceneQuery() {

        return (Query) tmpQuery;
    }
    
    public Object visitBinaryOperator(BinaryOperatorKind bok, Object t, Object t1) throws ExpressionVisitException, ODataApplicationException {
         BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
         if (bok == BinaryOperatorKind.AND){
             booleanQuery.add(this.tmpQuery, BooleanClause.Occur.MUST);
         }
         if (bok == BinaryOperatorKind.OR){
             booleanQuery.add(this.tmpQuery, BooleanClause.Occur.SHOULD);
         }
         
         if (bok == BinaryOperatorKind.EQ){
             
         }
         
         //this.tmpQuery = booleanQuery;
         return booleanQuery;
         
    }

    public Object visitUnaryOperator(UnaryOperatorKind uok, Object t) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitMethodCall(MethodKind mk, List list) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitLambdaExpression(String string, String string1, Expression exprsn) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitLiteral(Literal ltrl) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitMember(UriInfoResource uir) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitAlias(String string) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitTypeLiteral(EdmType et) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitLambdaReference(String string) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visitEnum(EdmEnumType eet, List list) throws ExpressionVisitException, ODataApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
