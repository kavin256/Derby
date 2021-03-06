/*

   Derby - Class org.apache.derby.impl.sql.compile.RowCountNode

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package org.apache.derby.impl.sql.compile;

import java.util.Vector;
import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.services.compiler.MethodBuilder;
import org.apache.derby.iapi.services.sanity.SanityManager;
import org.apache.derby.iapi.reference.ClassName;
import org.apache.derby.iapi.services.classfile.VMOpcode;
import org.apache.derby.iapi.types.SQLLongint;
import org.apache.derby.iapi.reference.ClassName;

/**
 * The result set generated by this node (RowCountResultSet) implements the
 * filtering of rows needed for the <result offset clause> and the <fetch first
 * clause>.  It sits on top of the normal SELECT's top result set, but under any
 * ScrollInsensitiveResultSet. The latter's positioning is needed for the correct
 * functioning of <result offset clause> and <fetch first clause> in the
 * presence of scrollable and/or updatable result sets and CURRENT OF cursors.
 */
public final class RowCountNode extends SingleChildResultSetNode
{
    /**
     * If not null, this represents the value of a <result offset clause>.
     */
    private ValueNode offset;
    /**
     * If not null, this represents the value of a <fetch first clause>.
     */
    private ValueNode fetchFirst;


    /**
     * Initializer for a RowCountNode
     *
     * @exception StandardException
     */
    public void init(Object childResult,
                     Object rcl,
                     Object offset,
                     Object fetchFirst)
        throws StandardException {

        init(childResult, null);
        resultColumns = (ResultColumnList) rcl;

        this.offset = (ValueNode)offset;
        this.fetchFirst = (ValueNode)fetchFirst;
    }


    /**
     * Generate code.
     *
     * @param acb activation class builder
     * @param mb  method builder
     *
     * @exception StandardException         Thrown on error
     */
    public void generate(ActivationClassBuilder acb,
                         MethodBuilder mb)
            throws StandardException {

        /* Get the next ResultSet #, so that we can number this ResultSetNode,
         * its ResultColumnList and ResultSet.
         */
        assignResultSetNumber();

        costEstimate = childResult.getFinalCostEstimate();

        acb.pushGetResultSetFactoryExpression(mb);

        childResult.generate(acb, mb); // arg1

        acb.pushThisAsActivation(mb);  // arg2
        mb.push(resultSetNumber);      // arg3

        boolean dynamicOffset = false;
        boolean dynamicFetchFirst = false;

        // arg4
        if (offset != null) {
            generateExprFun(acb, mb, offset);
        } else {
            mb.pushNull(ClassName.GeneratedMethod);
        }

        // arg5
        if (fetchFirst != null) {
            generateExprFun(acb, mb, fetchFirst);
        } else {
            mb.pushNull(ClassName.GeneratedMethod);
        }

        mb.push(costEstimate.rowCount()); // arg6
        mb.push(costEstimate.getEstimatedCost()); // arg7

        mb.callMethod(VMOpcode.INVOKEINTERFACE,
                      (String) null,
                      "getRowCountResultSet",
                      ClassName.NoPutResultSet,
                      7);
    }


    private void generateExprFun(
        ExpressionClassBuilder ecb,
        MethodBuilder mb,
        ValueNode vn) throws StandardException {

        // Generates:
        //     Object exprFun { }
        MethodBuilder exprFun = ecb.newExprFun();

        /* generates:
         *    return  <dynamic parameter.generate(ecb)>;
         * and adds it to exprFun
         */
        vn.generateExpression(ecb, exprFun);
        exprFun.methodReturn();

        // we are done modifying exprFun, complete it.
        exprFun.complete();

        // Pass in the method that will be used to evaluates the dynamic
        // parameter in RowCountResultSet.
        ecb.pushMethodReference(mb, exprFun);
    }


    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return  This object as a String
     */

    public String toString() {
        if (SanityManager.DEBUG) {
            return "offset: " + offset + "\n" +
                "fetchFirst:" + fetchFirst + "\n" +
                super.toString();
        } else {
            return "";
        }
    }
}
