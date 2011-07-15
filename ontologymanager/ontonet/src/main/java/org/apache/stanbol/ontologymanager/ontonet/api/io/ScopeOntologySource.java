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
package org.apache.stanbol.ontologymanager.ontonet.api.io;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.LoggerFactory;

/**
 * An ontology source that rewrites the physical IRI by appending the logical one to the scope ID. If the
 * ontology is anonymous, the original physical IRI is retained.
 * 
 * @author alessandro
 * 
 */
public class ScopeOntologySource extends AbstractOntologyInputSource {

    public ScopeOntologySource(IRI scopeIri, OWLOntology ontology, IRI origin) {
        bindRootOntology(ontology);
        LoggerFactory.getLogger(ScopeOntologySource.class).debug("Rewriting {} to {}/{}",
            new IRI[] {origin, scopeIri, ontology.getOntologyID().getOntologyIRI()});
        IRI iri = !ontology.isAnonymous() ? IRI.create(scopeIri + "/"
                                                       + ontology.getOntologyID().getOntologyIRI()) : origin;
        bindPhysicalIri(iri);
    }

    @Override
    public String toString() {
        return "SCOPE_ONT_IRI<" + getPhysicalIRI() + ">";
    }

}
