package myservice.mynamespace.service;

import java.util.List;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.*;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 *
 * @author Martin Ribaric
 */
public class MapQueryExpressionVisitor implements ExpressionVisitor{

    private Query tmpQuery;
    private final QueryBuilder queryBuilder;
    
    public MapQueryExpressionVisitor(QueryBuilder get) {
        System.out.println("Trieda MapQueryExpressionVisitor konstruktor");
        this.queryBuilder = get;
    }

    public Query getBuiltLuceneQuery() {
        return (Query) tmpQuery;
    }
    
    @Override
    public Object visitBinaryOperator(BinaryOperatorKind bok, Object t, Object t1) throws ExpressionVisitException, ODataApplicationException {
         BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
         if (bok == BinaryOperatorKind.AND){
            Binary leftSide = (Binary) t;
            Binary rightSide = (Binary) t1;
            visitBinaryOperator(leftSide.getOperator(), leftSide.getLeftOperand(), leftSide.getRightOperand());
            booleanQuery.add(this.tmpQuery, BooleanClause.Occur.MUST);
            visitBinaryOperator(rightSide.getOperator(), rightSide.getLeftOperand(), rightSide.getRightOperand());
            booleanQuery.add(this.tmpQuery, BooleanClause.Occur.MUST);
            BooleanQuery query = booleanQuery.build();
            this.tmpQuery = query;
         }
         if (bok == BinaryOperatorKind.OR){
            Binary leftSide = (Binary) t;
            Binary rightSide = (Binary) t1;
            visitBinaryOperator(leftSide.getOperator(), leftSide.getLeftOperand(), leftSide.getRightOperand());
            booleanQuery.add(this.tmpQuery, BooleanClause.Occur.SHOULD);
            visitBinaryOperator(rightSide.getOperator(), rightSide.getLeftOperand(), rightSide.getRightOperand());
            booleanQuery.add(this.tmpQuery, BooleanClause.Occur.SHOULD);  
            BooleanQuery query = booleanQuery.build();
            this.tmpQuery = query;
         }
        if (bok == BinaryOperatorKind.EQ){
              Member leftSide = (MemberImpl) t;
              if (t1 instanceof Literal){
                Literal rightSideL = (Literal) t1;
                this.tmpQuery = this.queryBuilder.phrase()               
                .onField(leftSide.getResourcePath().getUriResourceParts().get(0).getSegmentValue())
                .sentence(rightSideL.getText())
                .createQuery();
              }
              if (t1 instanceof Member){
                  Member rightSideM = (MemberImpl) t1;
                  this.tmpQuery = this.queryBuilder.phrase()               
                .onField(leftSide.getResourcePath().getUriResourceParts().get(0).getSegmentValue())
                .sentence(rightSideM.getResourcePath().getUriResourceParts().get(0).getSegmentValue())
                .createQuery();
              }
         }
         
         return tmpQuery;
         
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
