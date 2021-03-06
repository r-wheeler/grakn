/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */
package ai.grakn.migration.export;

import ai.grakn.GraknGraph;
import ai.grakn.concept.Concept;
import ai.grakn.concept.OntologyConcept;
import ai.grakn.graql.VarPattern;
import ai.grakn.util.Schema;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Export contents of a Grakn graph as a Graql insert query.
 * @author alexandraorth
 */
public class GraphWriter {

    private static final String EOL = ";\n";

    private final GraknGraph graph;

    public GraphWriter(GraknGraph graph){
        this.graph = graph;
        this.graph.showImplicitConcepts(true);
    }

    /**
     * Export the ontology of a Grakn graph as Graql string
     * @return Graql insert query with ontology of given graph
     */
    public String dumpOntology(){
        return join(types().map(OntologyConceptMapper::map));
    }

    /**
     *  Export the data of a Grakn graph as a Graql string
     * @return Graql insert query with data in given graph
     */
    public String dumpData(){
        return join(types()
                .filter(Concept::isType)
                .map(Concept::asType)
                .flatMap(c -> c.instances().stream())
                .map(Concept::asThing)
                .map(InstanceMapper::map));
    }

    /**
     * Turn a stream of Graql patterns into a Graql insert query.
     * @param stream stream of Graql patterns
     * @return Graql patterns as a string
     */
    private String join(Stream<VarPattern> stream){
        return stream
                .filter(varPattern -> varPattern.admin().getProperties().findAny().isPresent())
                .map(Object::toString)
                .collect(joining(EOL, "", EOL));
    }

    /**
     * Get all the types in a graph.
     * @return a stream of all types with non-reserved IDs
     */
    private Stream<? extends OntologyConcept> types(){
        return graph.admin().getMetaConcept().subs().stream()
                .filter(t -> !Schema.MetaSchema.isMetaLabel(t.getLabel()));
    }
}
