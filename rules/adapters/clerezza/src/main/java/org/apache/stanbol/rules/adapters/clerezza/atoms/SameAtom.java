/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.rules.adapters.clerezza.atoms;

import org.apache.clerezza.rdf.core.sparql.query.BinaryOperation;
import org.apache.clerezza.rdf.core.sparql.query.ConstructQuery;
import org.apache.clerezza.rdf.core.sparql.query.Expression;
import org.apache.stanbol.rules.adapters.AbstractAdaptableAtom;
import org.apache.stanbol.rules.adapters.clerezza.ClerezzaSparqlObject;
import org.apache.stanbol.rules.base.api.RuleAtom;
import org.apache.stanbol.rules.base.api.RuleAtomCallExeption;
import org.apache.stanbol.rules.base.api.UnavailableRuleObjectException;
import org.apache.stanbol.rules.base.api.UnsupportedTypeForExportException;
import org.apache.stanbol.rules.manager.atoms.ExpressionAtom;

/**
 * It adapts any SameAtom to the equality (=) binary operation in Clerezza.
 * 
 * @author anuzzolese
 * 
 */
public class SameAtom extends AbstractAdaptableAtom {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T adapt(RuleAtom ruleAtom) throws RuleAtomCallExeption,
                                         UnsupportedTypeForExportException,
                                         UnavailableRuleObjectException {

        org.apache.stanbol.rules.manager.atoms.SameAtom tmp = (org.apache.stanbol.rules.manager.atoms.SameAtom) ruleAtom;

        ExpressionAtom argument1 = tmp.getStringFunctionAtom1();
        ExpressionAtom argument2 = tmp.getStringFunctionAtom2();

        ClerezzaSparqlObject clerezzaArgument1 = (ClerezzaSparqlObject) adapter.adaptTo(argument1,
            ConstructQuery.class);
        ClerezzaSparqlObject clerezzaArgument2 = (ClerezzaSparqlObject) adapter.adaptTo(argument2,
            ConstructQuery.class);

        Expression lhe = (Expression) clerezzaArgument1.getClerezzaObject();
        Expression rhe = (Expression) clerezzaArgument2.getClerezzaObject();

        return (T) new ClerezzaSparqlObject(new BinaryOperation("=", lhe, rhe));

    }

}
