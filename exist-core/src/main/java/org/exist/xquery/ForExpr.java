/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * Changes made by Evolved Binary are proprietary and are not Open Source.
 *
 * NOTE: Parts of this file contain code from The eXist-db Authors.
 *       The original license header is included below.
 * ---------------------------------------------------------------------
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.xquery;

import net.bytebuddy.asm.Advice;
import org.exist.dom.QName;
import org.exist.dom.persistent.NodeSet;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.*;
import xyz.elemental.xquery.FTComparison;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents an XQuery "for" expression.
 * 
 * @author <a href="mailto:wolfgang@exist-db.org">Wolfgang Meier</a>
 */
public class ForExpr extends BindingExpression {

    private QName positionalVariable = null;
    private QName scoreVariable = null;
    private boolean allowEmpty = false;
    private boolean isOuterFor = true;

    private LocalVariable score = null;

    public ForExpr(XQueryContext context, boolean allowingEmpty) {
        super(context);
        this.allowEmpty = allowingEmpty;
    }

    @Override
    public ClauseType getType() {
        return ClauseType.FOR;
    }

    /**
     * A "for" expression may have an optional positional variable whose
     * QName can be set via this method.
     * 
     * @param var the name of the variable to set
     */
    public void setPositionalVariable(final QName var) {
        positionalVariable = var;
    }

    /**
     * Name of the score variable for full text search
     * @param scoreVariable
     */
    public void setScoreVariable(QName scoreVariable) {
        this.scoreVariable = scoreVariable;
    }

	/* (non-Javadoc)
     * @see org.exist.xquery.Expression#analyze(org.exist.xquery.Expression)
     */
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
        super.analyze(contextInfo);
        // Save the local variable stack
        final LocalVariable mark = context.markLocalVariables(false);
        try {
            contextInfo.setParent(this);
            final AnalyzeContextInfo varContextInfo = new AnalyzeContextInfo(contextInfo);
            inputSequence.analyze(varContextInfo);
            // Declare the iteration variable
            final LocalVariable inVar = new LocalVariable(varName);
            inVar.setSequenceType(sequenceType);
            inVar.setStaticType(varContextInfo.getStaticReturnType());
            context.declareVariableBinding(inVar);
            // Declare positional variable
            if (positionalVariable != null) {
                //could probably be detected by the parser
                if (varName.equals(positionalVariable)) {
                    throw new XPathException(this, ErrorCodes.XQST0089,
                            "bound variable and positional variable have the same name");
                    //TODO - We need to check more variables for conflict.
                }
                final LocalVariable posVar = new LocalVariable(positionalVariable);
                posVar.setSequenceType(POSITIONAL_VAR_TYPE);
                posVar.setStaticType(Type.INTEGER);
                context.declareVariableBinding(posVar);
            }

            if(scoreVariable != null) {
                final LocalVariable scoreVar = new LocalVariable(scoreVariable);
                scoreVar.setStaticType(Type.FLOAT);
                context.declareVariableBinding(scoreVar);
            }

            final AnalyzeContextInfo newContextInfo = new AnalyzeContextInfo(contextInfo);
            newContextInfo.addFlag(SINGLE_STEP_EXECUTION);
            returnExpr.analyze(newContextInfo);
        } finally {
            // restore the local variable stack
            context.popLocalVariables(mark);
        }
    }

    /**
     * This implementation tries to process the "where" clause in advance, i.e. in one single
     * step. This is possible if the input sequence is a node set and the where expression
     * has no dependencies on other variables than those declared in this "for" statement.
     * 
     * @see org.exist.xquery.Expression#eval(Sequence, Item)
     */
    public Sequence eval(Sequence contextSequence, Item contextItem)
            throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);
            context.getProfiler().message(this, Profiler.DEPENDENCIES,
                "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                {context.getProfiler().message(this, Profiler.START_SEQUENCES,
                "CONTEXT SEQUENCE", contextSequence);}
            if (contextItem != null)
                {context.getProfiler().message(this, Profiler.START_SEQUENCES,
                "CONTEXT ITEM", contextItem.toSequence());}
        }
        context.expressionStart(this);
        LocalVariable var;
        Sequence in;
        // Save the local variable stack
        LocalVariable mark = context.markLocalVariables(false);
        Sequence resultSequence = new ValueSequence(unordered);
        try {
            // Evaluate the "in" expression
            in = inputSequence.eval(contextSequence, null);
            clearContext(getExpressionId(), in);
            // Declare the iteration variable
            var = createVariable(varName);
            var.setSequenceType(sequenceType);
            context.declareVariableBinding(var);
            registerUpdateListener(in);
            // Declare positional variable
            LocalVariable at = null;
            if (positionalVariable != null) {
                at = new LocalVariable(positionalVariable);
                at.setSequenceType(POSITIONAL_VAR_TYPE);
                context.declareVariableBinding(at);
            }

            if(scoreVariable != null) {
                score = new LocalVariable(scoreVariable);
                score.setStaticType(Type.FLOAT);
                context.declareVariableBinding(score);
            }
            //TODO - bind variable to context, but not set value

            // Assign the whole input sequence to the bound variable.
            // This is required if we process the "where" or "order by" clause
            // in one step.
            var.setValue(in);
            // Save the current context document set to the variable as a hint
            // for path expressions occurring in the "return" clause.
            if (in instanceof NodeSet) {
                var.setContextDocs(in.getDocumentSet());
            } else {
                var.setContextDocs(null);
            }
            // See if we can process the "where" clause in a single step (instead of
            // calling the where expression for each item in the input sequence)
            // This is possible if the input sequence is a node set and has no
            // dependencies on the current context item.
            if (isOuterFor) {
                if (returnExpr instanceof WhereClause) {
                    if (at == null) {
                        in = ((WhereClause) returnExpr).preEval(in);
                    }
                } else if (returnExpr instanceof FLWORClause) {
                    in = ((FLWORClause) returnExpr).preEval(in);
                }
            }

            final IntegerValue atVal = new IntegerValue(this, 1);
            if (positionalVariable != null) {
                at.setValue(atVal);
            }
            //Type.EMPTY is *not* a subtype of other types ;
            //the tests below would fail without this prior cardinality check
            if (in.isEmpty() && sequenceType != null &&
                    !sequenceType.getCardinality().isSuperCardinalityOrEqualOf(Cardinality.EMPTY_SEQUENCE)) {
                throw new XPathException(this, ErrorCodes.XPTY0004,
                        "Invalid cardinality for variable $" + varName +
                                ". Expected " + sequenceType.getCardinality().getHumanDescription() +
                                ", got " + in.getCardinality().getHumanDescription());
            }

            // Loop through each variable binding
            int p = 0;
            if (in.isEmpty() && allowEmpty) {
                processItem(var, AtomicValue.EMPTY_VALUE, Sequence.EMPTY_SEQUENCE, resultSequence, at, p, score);
            } else {
                for (final SequenceIterator i = in.iterate(); i.hasNext(); p++) {
                    //TODO - Here I already need the score
                    // Inspect where, see if there is FTComparions, calculate it, setup score and call it again
                    processItem(var, i.nextItem(), in, resultSequence, at, p, score);
                }
            }
        } finally {
            // restore the local variable stack 
            context.popLocalVariables(mark, resultSequence);
        }

        clearContext(getExpressionId(), in);
        if (sequenceType != null) {
            //Type.EMPTY is *not* a subtype of other types ; checking cardinality first
            //only a check on empty sequence is accurate here
            if (resultSequence.isEmpty() &&
                    !sequenceType.getCardinality().isSuperCardinalityOrEqualOf(Cardinality.EMPTY_SEQUENCE))
                {throw new XPathException(this, ErrorCodes.XPTY0004,
                    "Invalid cardinality for variable $" + varName + ". Expected " +
                    sequenceType.getCardinality().getHumanDescription() +
                    ", got " + Cardinality.EMPTY_SEQUENCE.getHumanDescription());}
            //TODO : ignore nodes right now ; they are returned as xs:untypedAtomicType
            if (!Type.subTypeOf(sequenceType.getPrimaryType(), Type.NODE)) {
                if (!resultSequence.isEmpty() &&
                        !Type.subTypeOf(resultSequence.getItemType(),
                        sequenceType.getPrimaryType()))
                    {throw new XPathException(this, ErrorCodes.XPTY0004,
                        "Invalid type for variable $" + varName +
                        ". Expected " + Type.getTypeName(sequenceType.getPrimaryType()) +
                        ", got " +Type.getTypeName(resultSequence.getItemType()));}
            //trigger the old behaviour
            } else {
                var.checkType();
            }
        }
        setActualReturnType(resultSequence.getItemType());

        if (callPostEval()) {
            resultSequence = postEval(resultSequence);
        }

        context.expressionEnd(this);
        if (context.getProfiler().isEnabled())
            {context.getProfiler().end(this, "", resultSequence);}
        return resultSequence;
    }

    private void processItem(LocalVariable var, Item contextItem, Sequence in, Sequence resultSequence, LocalVariable
            at, int p, LocalVariable score) throws XPathException {
        context.proceed(this);
        context.setContextSequencePosition(p, in);
        if (positionalVariable != null) {
            at.setValue(new IntegerValue(this, p + 1));
        }
        if(scoreVariable != null) {
            if (contextItem instanceof FTComparison.ScoredElementImpl scoredElement) {
                score.setValue(new FloatValue(scoredElement.getScore()));
            } else {
//                score.setValue(new FloatValue(this, r.nextFloat()));
//                score.setValue(calculaterScore(contextItem));
                score.setValue(FloatValue.ZERO);
            }
        }

        final Sequence contextSequence = contextItem.toSequence();
        // set variable value to current item
        var.setValue(contextSequence);
        if (sequenceType == null)
            {var.checkType();} //because it makes some conversions !
        //Reset the context position
        context.setContextSequencePosition(0, null);

        // EXPERIMENT
        final List<FTComparison> ftComparisons = new ArrayList<>();
        findContainsText(returnExpr, ftComparisons);

        for (final FTComparison ftComparison : ftComparisons) {
            ftComparison.clearResult();
            final Sequence ftComparisonResult = ftComparison.eval(contextSequence, contextItem);
            if (ftComparisonResult instanceof FTComparison.ScoredBoolean scoredBoolean) {
                // TODO(AR) how to combine scores if there is more than one
                setScore(scoredBoolean.getScore());
            }
        }

        //Expression have list of steps,
        // clause wraps probably expression

        resultSequence.addAll(returnExpr.eval(null, null));

        // free resources
        var.destroy(context, resultSequence);
    }

    private void findContainsText(Expression ex, final List<FTComparison> ftComparisons) {
        if (ex instanceof DebuggableExpression debuggableExpression) {
            ex = debuggableExpression.getFirst();
        } else if (ex instanceof WhereClause whereClause) {
            if (whereClause.getReturnExpression() instanceof WhereClause returnWhereClause) {
                findContainsText(returnWhereClause, ftComparisons);
//                if (deeperFtComparisons != null) {
//                    ftComparisons.addAll(deeperFtComparisons);
//                }
            }
            ex = whereClause.getWhereExpr();
        } else if (ex instanceof FTComparison ftComparison) {
            ftComparisons.add(ftComparison);
            ex = null;
        } else {
            ex = null;
        }

        if (ex != null) {
            findContainsText(ex, ftComparisons);
        }

        return;
    }

    private boolean callPostEval() {
        FLWORClause prev = getPreviousClause();
        while (prev != null) {
            switch (prev.getType()) {
                case LET:
                case FOR:
                    return false;
                case ORDERBY:
                case GROUPBY:
                    return true;
            }
            prev = prev.getPreviousClause();
        }
        return true;
    }

    @Override
    public Sequence preEval(Sequence seq) throws XPathException {
        // if preEval gets called, we know we're inside another FOR
        isOuterFor = false;
        return super.preEval(seq);
    }

    /* (non-Javadoc)
         * @see org.exist.xquery.Expression#dump(org.exist.xquery.util.ExpressionDumper)
         */
    public void dump(ExpressionDumper dumper) {
        dumper.display("for ", line);
        dumper.startIndent();
        dumper.display("$").display(varName);
        if (sequenceType != null) {
            dumper.display(" as ").display(sequenceType);
        }
        if (allowEmpty) {
            dumper.display(" allowing empty ");
        }
        if (positionalVariable != null)
            {dumper.display(" at ").display(positionalVariable);}
        dumper.display(" in ");
        inputSequence.dump(dumper);
        dumper.endIndent().nl();
        //TODO : QuantifiedExpr
        if (returnExpr instanceof LetExpr)
            {dumper.display(" ", returnExpr.getLine());}
        else
            {dumper.display("return", returnExpr.getLine());} 
        dumper.startIndent();
        returnExpr.dump(dumper);
        dumper.endIndent().nl();
    }

    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("for ");
        result.append('$').append(varName);
        if (sequenceType != null)
            {result.append(" as ").append(sequenceType);}
        if (allowEmpty) {
            result.append(" allowing empty ");
        }
        if (positionalVariable != null) {
            result.append(" at ").append('$').append(positionalVariable);
        }
        if (scoreVariable != null) {
            result.append(" score ").append('$').append(scoreVariable);
        }
        result.append(" in ");
        result.append(inputSequence.toString());
        result.append(" ");
        //TODO : QuantifiedExpr
        if (returnExpr instanceof LetExpr)
            {result.append(" ");}
        else
            {result.append("return ");}
        result.append(returnExpr.toString());
        return result.toString();
    }

    /* (non-Javadoc)
    * @see org.exist.xquery.AbstractExpression#resetState()
    */
    public void resetState(boolean postOptimization) {
        super.resetState(postOptimization);
    }

    public void accept(ExpressionVisitor visitor) {
        visitor.visitForExpression(this);
    }

//    private Random r = new Random();

    public void setScore(float score) {
        this.score.setValue(new FloatValue(score));
    }
}