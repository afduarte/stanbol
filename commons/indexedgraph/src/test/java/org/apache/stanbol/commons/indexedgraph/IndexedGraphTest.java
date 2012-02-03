package org.apache.stanbol.commons.indexedgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DebugGraphics;
import javax.xml.soap.Text;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.test.MGraphTest;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexedGraphTest  extends MGraphTest {

    protected static final Logger log = LoggerFactory.getLogger(IndexedGraphTest.class);
    
    private UriRef uriRef1 = new UriRef("http://example.org/foo");
    private UriRef uriRef2 = new UriRef("http://example.org/bar");
    private UriRef uriRef3 = new UriRef("http://example.org/test");
    private Triple triple1 = new TripleImpl(uriRef1, uriRef2, uriRef3);
    private Triple triple2 = new TripleImpl(uriRef2, uriRef2, uriRef1);
    private Triple triple3 = new TripleImpl(uriRef3, uriRef1, uriRef3);
    private Triple triple4 = new TripleImpl(uriRef1, uriRef3, uriRef2);
    private Triple triple5 = new TripleImpl(uriRef2, uriRef3, uriRef2);
    
    @Override
    protected MGraph getEmptyMGraph() {
        return new IndexedMGraph();
    }
    @Test
    public void iteratorRemove() {
        TripleCollection itc = new IndexedTripleCollection();
        itc.add(triple1);
        itc.add(triple2);
        itc.add(triple3);
        itc.add(triple4);
        itc.add(triple5);
        Iterator<Triple> iter = itc.iterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        Assert.assertEquals(0, itc.size());
    }

    @Test
    public void removeAll() {
        TripleCollection itc = new IndexedTripleCollection();
        itc.add(triple1);
        itc.add(triple2);
        itc.add(triple3);
        itc.add(triple4);
        itc.add(triple5);
        TripleCollection itc2 = new IndexedTripleCollection();
        itc2.add(triple1);
        itc2.add(triple3);
        itc2.add(triple5);
        itc.removeAll(itc2);
        Assert.assertEquals(2, itc.size());
    }
    
    @Test
    public void filterIteratorRemove() {
        TripleCollection itc = new IndexedTripleCollection();
        itc.add(triple1);
        itc.add(triple2);
        itc.add(triple3);
        itc.add(triple4);
        itc.add(triple5);       
        Iterator<Triple> iter = itc.filter(uriRef1, null, null);
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        Assert.assertEquals(3, itc.size());
    }

    @Test(expected=ConcurrentModificationException.class)
    public void remove() {
        TripleCollection itc = new IndexedTripleCollection();
        itc.add(triple1);
        itc.add(triple2);
        itc.add(triple3);
        itc.add(triple4);
        itc.add(triple5);
        Iterator<Triple> iter = itc.filter(uriRef1, null, null);
        while (iter.hasNext()) {
            Triple triple = iter.next();
            itc.remove(triple);
        }
        Assert.assertEquals(3, itc.size());
    }
    /**
     * Holds the test data to perform 
     * {@link TripleCollection#filter(NonLiteral, UriRef, Resource)}
     * tests on {@link TripleCollection} implementations
     * @author rwesten
     */
    public static final class TestCase {
        public final List<NonLiteral> subjects;
        public final List<Resource> objects;
        public final List<UriRef> predicates;

        /**
         * Create a new Test with a maximum number of subjects, predicates and
         * objects based on data in the parsed triple collection
         * @param tc the data
         * @param sNum the maximum number of subjects
         * @param pNum the maximum number of predicates
         * @param oNum the maximum number of objects
         */
        public TestCase(TripleCollection tc,int sNum, int pNum, int oNum){
            Set<NonLiteral> subjects = new LinkedHashSet<NonLiteral>();
            Set<Resource> objects = new LinkedHashSet<Resource>();
            Set<UriRef> predicates = new LinkedHashSet<UriRef>();
            for(Iterator<Triple> it = tc.iterator();it.hasNext();){
                Triple t = it.next();
                if(subjects.size() < 100){
                    subjects.add(t.getSubject());
                }
                if(predicates.size() < 5){
                    predicates.add(t.getPredicate());
                }
                if(objects.size() < 100){
                    objects.add(t.getObject());
                }
            }
            this.subjects = Collections.unmodifiableList(
                new ArrayList<NonLiteral>(subjects));
            this.predicates = Collections.unmodifiableList(
                new ArrayList<UriRef>(predicates));
            this.objects = Collections.unmodifiableList(
                new ArrayList<Resource>(objects));
        }
    }
    @Test
    public void testPerformance(){
        MGraph sg = new SimpleMGraph();
        int iterations = 1000;
        int graphsize = 100000;
        createGraph(sg, graphsize);
        MGraph ig = new IndexedMGraph(sg);
        long start;
        //Simple Graph reference test
        TestCase testCase = new TestCase(sg, 100, 5, 100);
        log.info("Filter Performance Test (graph size {} triples, iterations {})",graphsize,iterations);
        log.info(" --- TEST {} with {} triples ---",sg.getClass().getSimpleName(),sg.size());
        start = System.currentTimeMillis();
        List<Long> sgr = executeTest(sg, testCase, iterations);
        log.info(" --- TEST completed in {}ms",System.currentTimeMillis()-start);

        log.info(" --- TEST {} {} triples ---",ig.getClass().getSimpleName(),sg.size());
        start = System.currentTimeMillis();
        List<Long> igr = executeTest(ig, testCase, iterations);
        log.info(" --- TEST completed in {}ms",System.currentTimeMillis()-start);
        Assert.assertEquals(sgr, igr); //validate filter implementation
    }
    
    public List<Long> executeTest(TripleCollection graph, TestCase test, int testCount){
        List<Long> testResults = new ArrayList<Long>();
        long start;
        long resultCount;
        //[S,P,O]
        start = System.currentTimeMillis();
        resultCount = testSPO(graph, test, testCount);
        testResults.add(new Long(resultCount));
        log.info("... run [S,P,O] in {}ms with {} results",System.currentTimeMillis()-start,resultCount);
        //[S,P,n]
        start = System.currentTimeMillis();
        resultCount = testSPn(graph, test, testCount);
        testResults.add(new Long(resultCount));
        log.info("... run [S,P,n] in {}ms with {} results",System.currentTimeMillis()-start,resultCount);
        //[S,n,O]
        start = System.currentTimeMillis();
        resultCount = testSnO(graph, test, testCount);
        testResults.add(new Long(resultCount));
        log.info("... run [S,n,O] in {}ms with {} results",System.currentTimeMillis()-start,resultCount);
        //[n,P,O]
        start = System.currentTimeMillis();
        resultCount = testnPO(graph, test, testCount);
        testResults.add(new Long(resultCount));
        log.info("... run [n,P,O] in {}ms with {} results",System.currentTimeMillis()-start,resultCount);
        //[S,n,n]
        start = System.currentTimeMillis();
        resultCount = testSnn(graph, test, testCount);
        testResults.add(new Long(resultCount));
        log.info("... run [S,n,n] in {}ms with {} results",System.currentTimeMillis()-start,resultCount);
        //[n,P,n]
        start = System.currentTimeMillis();
        resultCount = testnPn(graph, test, testCount);
        testResults.add(new Long(resultCount));
        log.info("... run [n,P,n] in {}ms with {} results",System.currentTimeMillis()-start,resultCount);
        //[n,n,O]
        start = System.currentTimeMillis();
        resultCount = testnnO(graph, test, testCount);
        testResults.add(new Long(resultCount));
        log.info("... run [n,n,O] in {}ms with {} results",System.currentTimeMillis()-start,resultCount);
        return testResults;
    }

    private long testSPO(TripleCollection graph, TestCase test, int testCount) {
        Iterator<Triple> it;
        long count = 0;
        int si = -1;
        int pi = -1;
        int oi;
        for(int num = 0;num < testCount;num++){
            oi = num%test.objects.size();
            if(oi == 0) {
                pi++;
                pi = pi%test.predicates.size();
            }
            if(pi == 0) {
                si++;
                si = si%test.subjects.size();
            }
            it = graph.filter(test.subjects.get(si), test.predicates.get(pi), test.objects.get(oi));
            while(it.hasNext()){
                it.next();
                count++;
            }
        }
        return count;
    }
    
    private long testSPn(TripleCollection graph, TestCase test, int testCount) {
        Iterator<Triple> it;
        long count = 0;
        int si = -1;
        int pi = 0;
        for(int num = 0;num < testCount;num++){
            pi = num%test.predicates.size();
            if(pi == 0) {
                si++;
                si = si%test.subjects.size();
            }
            it = graph.filter(test.subjects.get(si), test.predicates.get(pi), null);
            while(it.hasNext()){
                it.next();
                count++;
            }
        }
        return count;
    }
    
    private long testSnO(TripleCollection graph, TestCase test, int testCount) {
        Iterator<Triple> it;
        long count = 0;
        int si = -1;
        int oi;
        for(int num = 0;num < testCount;num++){
            oi = num%test.objects.size();
            if(oi == 0) {
                si++;
                si = si%test.subjects.size();
            }
            it = graph.filter(test.subjects.get(si), null, test.objects.get(oi));
            while(it.hasNext()){
                it.next();
                count++;
            }
        }
        return count;
    }
    
    private long testnPO(TripleCollection graph, TestCase test, int testCount) {
        Iterator<Triple> it;
        long count = 0;
        int pi = -1;
        int oi;
        for(int num = 0;num < testCount;num++){
            oi = num%test.objects.size();
            if(oi == 0) {
                pi++;
                pi = pi%test.predicates.size();
            }
            it = graph.filter(null, test.predicates.get(pi), test.objects.get(oi));
            while(it.hasNext()){
                it.next();
                count++;
            }
        }
        return count;
    }
    private long testSnn(TripleCollection graph, TestCase test, int testCount) {
        Iterator<Triple> it;
        long count = 0;
        int si = 0;
        for(int num = 0;num < testCount;num++){
            si = num%test.subjects.size();
            it = graph.filter(test.subjects.get(si), null, null);
            while(it.hasNext()){
                it.next();
                count++;
            }
        }
        return count;
    }
    private long testnPn(TripleCollection graph, TestCase test, int testCount) {
        Iterator<Triple> it;
        long count = 0;
        int pi;
        for(int num = 0;num < testCount;num++){
            pi = num%test.predicates.size();
            it = graph.filter(null, test.predicates.get(pi), null);
            while(it.hasNext()){
                it.next();
                count++;
            }
        }
        return count;
    }
    private long testnnO(TripleCollection graph, TestCase test, int testCount) {
        Iterator<Triple> it;
        long count = 0;
        int oi;
        for(int num = 0;num < testCount;num++){
            oi = num%test.objects.size();
            it = graph.filter(null, null, test.objects.get(oi));
            while(it.hasNext()){
                it.next();
                count++;
            }
        }
        return count;
    }
    
    private static void createGraph(TripleCollection tc, int triples){
        LiteralFactory lf = LiteralFactory.getInstance();
        //randoms are in the range [0..3]
        double l = 1.0; //literal
        double i = l / 3; //int
        double d = l * 2 / 3;//double
        double b = 2.0;//bNode
        double nb = b - (l * 2 / 3); //create new bNode
        double random;
        NonLiteral subject = null;
        UriRef predicate = null;
        List<UriRef> predicateList = new ArrayList<UriRef>();
        predicateList.add(RDF.first);
        predicateList.add(RDF.rest);
        predicateList.add(RDF.type);
        predicateList.add(RDFS.label);
        predicateList.add(RDFS.comment);
        predicateList.add(RDFS.range);
        predicateList.add(RDFS.domain);
        predicateList.add(FOAF.name);
        predicateList.add(FOAF.nick);
        predicateList.add(FOAF.homepage);
        predicateList.add(FOAF.age);
        predicateList.add(FOAF.depiction);
        String URI_PREFIX = "http://www.test.org/bigGraph/ref";
        Language DE = new Language("de");
        Language EN = new Language("en");
        Iterator<UriRef> predicates = predicateList.iterator();
        List<BNode> bNodes = new ArrayList<BNode>();
        bNodes.add(new BNode());
        for (int count = 0; tc.size() < triples; count++) {
            random = Math.random() * 3;
            if (random >= 2.5 || count == 0) {
                if (random <= 2.75) {
                    subject = new UriRef(URI_PREFIX + count);
                } else {
                    int rndIndex = (int) ((random - 2.75) * bNodes.size() / (3.0 - 2.75));
                    subject = bNodes.get(rndIndex);
                }
            }
            if (random > 2.0 || count == 0) {
                if (!predicates.hasNext()) {
                    Collections.shuffle(predicateList);
                    predicates = predicateList.iterator();
                }
                predicate = predicates.next();
            }
            if (random <= l) { //literal
                if (random <= i) {
                    tc.add(new TripleImpl(subject, predicate, lf.createTypedLiteral(count)));
                } else if (random <= d) {
                    tc.add(new TripleImpl(subject, predicate, lf.createTypedLiteral(random)));
                } else {
                    PlainLiteral text;
                    if (random <= i) {
                        text = new PlainLiteralImpl("Literal for " + count);
                    } else if (random <= d) {
                        text = new PlainLiteralImpl("An English literal for " + count, EN);
                    } else {
                        text = new PlainLiteralImpl("Ein Deutsches Literal für " + count, DE);
                    }
                    tc.add(new TripleImpl(subject, predicate, text));
                }
            } else if (random <= b) { //bnode
                BNode bnode;
                if (random <= nb) {
                    bnode = new BNode();
                    bNodes.add(bnode);
                } else { //>nb <b
                    int rndIndex = (int) ((random - nb) * bNodes.size() / (b - nb));
                    bnode = bNodes.get(rndIndex);
                }
                tc.add(new TripleImpl(subject, predicate, bnode));
            } else { //UriRef
                tc.add(new TripleImpl(subject, predicate,
                        new UriRef(URI_PREFIX + (int) count * random)));
            }
        }        
    }
    
}
