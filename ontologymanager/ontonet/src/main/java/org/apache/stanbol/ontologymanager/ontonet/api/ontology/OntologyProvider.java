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
package org.apache.stanbol.ontologymanager.ontonet.api.ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedFormatException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * A system responsible for maintaining stored ontologies.<br>
 * <br>
 * TODO see if full CRUD operation support is necessary.
 * 
 * @author alexdma
 * 
 * @param <S>
 *            the storage system actually used by this provider.
 */
public interface OntologyProvider<S> {

    /**
     * The key used to configure the prefix to be used for addressing ontologies stored by this provider.
     */
    public String GRAPH_PREFIX = "org.apache.stanbol.ontologymanager.ontonet.graphPrefix";

    /**
     * The key used to configure the import management policy.
     */
    public String IMPORT_POLICY = "org.apache.stanbol.ontologymanager.ontonet.importPolicy";

    /**
     * The key used to configure the default import resolution policy for this provider.
     */
    public String RESOLVE_IMPORTS = "org.apache.stanbol.ontologymanager.ontonet.resolveImports";

    /**
     * Gets the policy adopted by this provider whenever an import statement is found in an ontology <i>that
     * has already been loaded</i> (e.g. when exporting it). It does <b>not</b> influence how the system
     * should <i>resolve</i> imports of newly found ontologies.
     * 
     * @return the import management policy.
     */
    ImportManagementPolicy getImportManagementPolicy();

    /**
     * Gets a string that can be used to directly access the ontology whose logical identifier is
     * <tt>ontologyIRI</tt>.
     * 
     * @param ontologyIRI
     *            the logical identifier of the ontology.
     * @return the key to access the ontology from the store.
     */
    String getKey(IRI ontologyIRI);

    /**
     * Gets the set of all the strings that can be used to access the ontologies stored by this provider.
     * 
     * @return the ontology key set.
     */
    Set<String> getKeys();

    /**
     * Returns the storage system used by this ontology provider (e.g. a {@link TcProvider} or an
     * {@link OWLOntologyManager}).
     * 
     * @return the ontology store.
     */
    S getStore();

    /**
     * Same as {@link OntologyProvider#getStoredOntology(String, Class)}, but instead of the internal key it
     * uses an IRI that <i>publicly</i> identifies or references an ontology. This can be, ordered by
     * preference most relevant first:
     * <ol>
     * <li>The version IRI
     * <li>The ontology IRI
     * <li>The physical IRI, if different from the above
     * </ol>
     * 
     * @param reference
     *            the IRI that references the ontology.
     * @param returnType
     *            the desired type that the method should return, if supported, otherwise an
     *            {@link UnsupportedOperationException} is thrown. Can be null, in which case a default return
     *            type is chosen.
     * @return the stored ontology in the desired format, or null if no such ontology is managed by the
     *         provider.
     */
    <O> O getStoredOntology(IRI reference, Class<O> returnType);

    /**
     * Same as {@link OntologyProvider#getStoredOntology(String, Class, boolean)}, but instead of the internal
     * key it uses an IRI that <i>publicly</i> identifies or references an ontology. This can be, ordered by
     * preference most relevant first:
     * 
     * @param reference
     *            the IRI that references the ontology.
     * @param returnType
     *            The expected type for the returned ontology object. If null, the provider will arbitrarily
     *            select a supported return type. If the supplied type is not supported (i.e. not assignable
     *            to any type contained in the result of {@link #getSupportedReturnTypes()}) an
     *            {@link UnsupportedOperationException} should be thrown.
     * @param forceMerge
     *            if true, the ontology will be merged with all its imports, thus overriding the import
     *            management policy set for this provider.
     * @return the stored ontology in the desired format, or null if no such ontology is managed by the
     *         provider.
     */
    <O> O getStoredOntology(IRI reference, Class<O> returnType, boolean merge);

    /**
     * Returns a stored ontology that is internally identified by the provided key.
     * 
     * @param key
     *            the key used to identify the ontology in this provider. They can or cannot coincide with the
     *            logical and/or physical IRI of the ontology.
     * @param returnType
     *            The expected type for the returned ontology object. If null, the provider will arbitrarily
     *            select a supported return type. If the supplied type is not supported (i.e. not assignable
     *            to any type contained in the result of {@link #getSupportedReturnTypes()}) an
     *            {@link UnsupportedOperationException} should be thrown.
     * @return the stored ontology in the desired format, or null if no such ontology is managed by the
     *         provider.
     */
    <O> O getStoredOntology(String key, Class<O> returnType);

    /**
     * Returns a stored ontology that is internally identified by the provided key.
     * 
     * @param key
     *            the key used to identify the ontology in this provider. They can or cannot coincide with the
     *            logical and/or physical IRI of the ontology.
     * @param returnType
     *            The expected type for the returned ontology object. If null, the provider will arbitrarily
     *            select a supported return type. If the supplied type is not supported (i.e. not assignable
     *            to any type contained in the result of {@link #getSupportedReturnTypes()}) an
     *            {@link UnsupportedOperationException} should be thrown.
     * @param forceMerge
     *            if true, the ontology will be merged with all its imports, thus overriding the import
     *            management policy set for this provider.
     * @return the stored ontology in the desired format, or null if no such ontology is managed by the
     *         provider.
     */
    <O> O getStoredOntology(String key, Class<O> returnType, boolean forceMerge);

    /**
     * Returns an array containing the most specific types for ontology objects that this provider can manage
     * and return on a call to {@link #getStoredOntology(String, Class)}.
     * 
     * @return the supported ontology return types.
     */
    <O> Class<O>[] getSupportedReturnTypes();

    /**
     * Retrieves an ontology by reading its content from a data stream and stores it using the storage system
     * attached to this provider. A key that can be used to identify the ontology in this provider is returned
     * if successful.
     * 
     * @param data
     *            the ontology content.
     * @param formatIdentifier
     *            the MIME type of the expected serialization format of this ontology. If null, all supported
     *            formats will be tried until all parsers fail or one succeeds.
     * @param preferredKey
     *            a string that should preferrably identify the ontology internally within the provider. It
     *            will be ignored if null or empty. It is not guaranteed that the supplied key can be used,
     *            e.g. if there is already a duplicate of the key and the policy does not allow duplicates. In
     *            this case, a different key will be set and returned by this method.
     * @param force
     *            if true, all mappings provided by the offline configuration will be ignored (both for the
     *            root ontology and its recursive imports) and the provider will forcibly try to resolve the
     *            location IRI. If some remote import is found, the import policy is aggressive and Stanbol is
     *            set on offline mode, this method will fail.
     * @return a key that can be used to retrieve the stored ontology afterwards, or null if loading/storage
     *         failed. If it was possible to set it as such, it will be the same as <tt>preferredKey</tt>.
     * @throws IOException
     *             if all attempts to load the ontology failed.
     * @throws UnsupportedFormatException
     *             if no parsers are able to parse the supplied format (or the actual file format).
     */
    String loadInStore(InputStream data, String formatIdentifier, String preferredKey, boolean force) throws IOException,
                                                                                                     UnsupportedFormatException;

    /**
     * Retrieves an ontology physically located at <code>location</code> (unless mapped otherwise by the
     * offline configuration) and stores it using the storage system attached to this provider. A key that can
     * be used to identify the ontology in this provider is returned if successful.
     * 
     * @param location
     *            the physical IRI where the ontology is located.
     * @param formatIdentifier
     *            the MIME type of the expected serialization format of this ontology. If null, all supported
     *            formats will be tried until all parsers fail or one succeeds.
     * @param preferredKey
     *            a string that should preferrably identify the ontology internally within the provider. It
     *            will be ignored if null or empty. It is not guaranteed that the supplied key can be used,
     *            e.g. if there is already a duplicate of the key and the policy does not allow duplicates. In
     *            this case, a different key will be set and returned by this method.
     * @param force
     *            if true, all mappings provided by the offline configuration will be ignored (both for the
     *            root ontology and its recursive imports) and the provider will forcibly try to resolve the
     *            location IRI. If the IRI is not local and Stanbol is set on offline mode, this method will
     *            fail.
     * @return a key that can be used to retrieve the stored ontology afterwards, or null if loading/storage
     *         failed.
     * @throws IOException
     *             if all attempts to load the ontology failed.
     * @throws UnsupportedFormatException
     *             if no parsers are able to parse the supplied format (or the actual file format).
     */
    String loadInStore(IRI location, String formatIdentifier, String preferredKey, boolean force) throws IOException,
                                                                                                 UnsupportedFormatException;

    /**
     * Stores an ontology that has already been loaded into an object. If the object is of a non-native yet
     * supported type, the ontology provider will try to perform a conversion prior to storing it.
     * 
     * @param ontology
     *            the ontology to be stored.
     * @param preferredKey
     *            a string that should preferrably identify the ontology internally within the provider. It
     *            will be ignored if null or empty. It is not guaranteed that the supplied key can be used,
     *            e.g. if there is already a duplicate of the key and the policy does not allow duplicates. In
     *            this case, a different key will be set and returned by this method.
     * @param force
     *            if true, all mappings provided by the offline configuration will be ignored (both for the
     *            root ontology and its recursive imports) and the provider will forcibly try to resolve the
     *            location IRI. If some remote import is found, the import policy is aggressive and Stanbol is
     *            set on offline mode, this method will fail.
     * @return
     */
    String loadInStore(Object ontology, String preferredKey, boolean force);

    /**
     * Sets the policy adopted by this provider whenever an import statement is found in an ontology <i>that
     * has already been loaded</i> (e.g. when exporting it). It does <b>not</b> influence how the system
     * should <i>resolve</i> imports of newly found ontologies.
     * 
     * @param policy
     *            the import management policy.
     */
    void setImportManagementPolicy(ImportManagementPolicy policy);

}
