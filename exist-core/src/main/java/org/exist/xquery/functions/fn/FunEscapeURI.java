/*
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
package org.exist.xquery.functions.fn;

import org.exist.dom.QName;
import org.exist.util.SaxonURIUtil;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Dependency;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Profiler;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;

/**
 * @author wolf
 *
 */
@Deprecated
public class FunEscapeURI extends BasicFunction {

    public final static FunctionSignature signature =
        new FunctionSignature(
            new QName("escape-uri", Function.BUILTIN_FUNCTION_NS),
            "This function applies the URI escaping rules defined in section 2 " +
            "of [RFC 2396] as amended by [RFC 2732], with one exception, to " +
            "the string supplied as $uri, which typically represents all or part " +
            "of a URI. The effect of the function is to escape a set of identified " +
            "characters in the string. Each such character is replaced in the string " +
            "by an escape sequence, which is formed by encoding the character " +
            "as a sequence of octets in UTF-8, and then representing each of these " +
            "octets in the form %HH, where HH is the hexadecimal representation " +
            "of the octet. $escape-reserved indicates whether to escape reserved characters.",
            new SequenceType[] {
                new FunctionParameterSequenceType("uri", Type.STRING, Cardinality.ZERO_OR_ONE, "The URI"),
                 new FunctionParameterSequenceType("escape-reserved", Type.BOOLEAN, Cardinality.EXACTLY_ONE, "The escaped-reserved")
            },
            new FunctionReturnSequenceType(Type.STRING, Cardinality.EXACTLY_ONE, "the identified characters in $uri encoded with escape sequences"));
    
    public FunEscapeURI(XQueryContext context) {
        super(context, signature);
    }
    
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);       
            context.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                {context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);}
        }
        
        Sequence result;
        if (args[0].isEmpty())
            {result = StringValue.EMPTY_STRING;}
        else {
            final String uri = args[0].getStringValue();
            final boolean escapeReserved = args[1].effectiveBooleanValue();
            return new StringValue(this, SaxonURIUtil.escape(uri, escapeReserved));
        }
        
        if (context.getProfiler().isEnabled()) 
            {context.getProfiler().end(this, "", result);} 
        
        return result;            
    }
}
