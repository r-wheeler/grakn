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
 *
 */

package ai.grakn.property;

import ai.grakn.GraknGraph;
import ai.grakn.concept.EntityType;
import ai.grakn.concept.Label;
import ai.grakn.concept.OntologyConcept;
import ai.grakn.concept.RelationType;
import ai.grakn.concept.ResourceType;
import ai.grakn.concept.Role;
import ai.grakn.concept.RuleType;
import ai.grakn.concept.Type;
import ai.grakn.exception.GraphOperationException;
import ai.grakn.generator.FromGraphGenerator.FromGraph;
import ai.grakn.generator.GraknGraphs.Open;
import ai.grakn.generator.PutOntologyConceptFunctions;
import ai.grakn.generator.Labels.Unused;
import ai.grakn.util.ErrorMessage;
import ai.grakn.util.Schema;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.function.BiFunction;

import static ai.grakn.util.Schema.MetaSchema.isMetaLabel;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;

@RunWith(JUnitQuickcheck.class)
public class GraknGraphPutPropertyTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Property
    public void whenCallingAnyPutMethod_CreateAnOntologyElementWithTheGivenName(
            @Open GraknGraph graph,
            @Unused Label label, @From(PutOntologyConceptFunctions.class) BiFunction<GraknGraph, Label, OntologyConcept> putOntologyElement) {
        OntologyConcept type = putOntologyElement.apply(graph, label);
        assertEquals(label, type.getLabel());
    }

    @Property
    public void whenCallingAnyPutTypeMethod_CreateATypeWithDefaultProperties(
            @Open GraknGraph graph,
            @Unused Label label, @From(PutOntologyConceptFunctions.class) BiFunction<GraknGraph, Label, OntologyConcept> putOntologyElement) {
        OntologyConcept ontologyConcept = putOntologyElement.apply(graph, label);

        assertThat("Type should only have one sub-type: itself", ontologyConcept.subs(), contains(ontologyConcept));
        if(ontologyConcept.isType()) {
            Type type = ontologyConcept.asType();
            assertThat("Type should not play any roles", type.plays(), empty());
            assertThat("Type should not have any scopes", type.scopes(), empty());
            assertFalse("Type should not be abstract", type.isAbstract());
            assertFalse("Type should not be implicit", type.isImplicit());
            assertThat("Rules of hypotheses should be empty", type.getRulesOfHypothesis(), empty());
            assertThat("Rules of conclusion should be empty", type.getRulesOfConclusion(), empty());
        }
    }

    @Property
    public void whenCallingPutEntityType_CreateATypeWithSuperTypeEntity(
            @Open GraknGraph graph, @Unused Label label) {
        EntityType entityType = graph.putEntityType(label);
        assertEquals(graph.admin().getMetaEntityType(), entityType.sup());
    }

    @Property
    public void whenCallingPutEntityTypeWithAnExistingEntityTypeLabel_ItReturnsThatType(
            @Open GraknGraph graph, @FromGraph EntityType entityType) {
        EntityType newType = graph.putEntityType(entityType.getLabel());
        assertEquals(entityType, newType);
    }

    @Property
    public void whenCallingPutEntityTypeWithAnExistingNonEntityTypeLabel_Throw(
            @Open GraknGraph graph, @FromGraph Type type) {
        assumeFalse(type.isEntityType());

        exception.expect(GraphOperationException.class);
        if(Schema.MetaSchema.isMetaLabel(type.getLabel())){
            exception.expectMessage(ErrorMessage.RESERVED_WORD.getMessage(type.getLabel().getValue()));
        } else {
            exception.expectMessage(ErrorMessage.UNIQUE_PROPERTY_TAKEN.getMessage(Schema.VertexProperty.TYPE_LABEL.name(), type.getLabel(), type));
        }
        graph.putEntityType(type.getLabel());
    }

    @Property
    public void whenCallingPutResourceType_CreateATypeWithSuperTypeResource(
            @Open GraknGraph graph, @Unused Label label, ResourceType.DataType<?> dataType) {
        ResourceType<?> resourceType = graph.putResourceType(label, dataType);
        assertEquals(graph.admin().getMetaResourceType(), resourceType.sup());
    }

    @Property
    public void whenCallingPutResourceType_CreateATypeWithDefaultProperties(
            @Open GraknGraph graph, @Unused Label label, ResourceType.DataType<?> dataType) {
        ResourceType<?> resourceType = graph.putResourceType(label, dataType);

        assertEquals("The data-type should be as specified", dataType, resourceType.getDataType());
        assertNull("The resource type should have no regex constraint", resourceType.getRegex());
    }

    @Property
    public void whenCallingPutResourceTypeWithThePropertiesOfAnExistingResourceType_ItReturnsThatType(
            @Open GraknGraph graph, @FromGraph  ResourceType<?> resourceType) {
        assumeFalse(resourceType.equals(graph.admin().getMetaResourceType()));

        Label label = resourceType.getLabel();
        ResourceType.DataType<?> dataType = resourceType.getDataType();

        ResourceType<?> newType = graph.putResourceType(label, dataType);

        assertEquals(resourceType, newType);
    }

    @Property
    public void whenCallingPutResourceTypeWithAnExistingNonResourceTypeLabel_Throw(
            @Open GraknGraph graph, @FromGraph Type type, ResourceType.DataType<?> dataType) {
        assumeFalse(type.isResourceType());

        exception.expect(GraphOperationException.class);
        if(Schema.MetaSchema.isMetaLabel(type.getLabel())){
            exception.expectMessage(ErrorMessage.RESERVED_WORD.getMessage(type.getLabel().getValue()));
        } else {
            exception.expectMessage(ErrorMessage.UNIQUE_PROPERTY_TAKEN.getMessage(Schema.VertexProperty.TYPE_LABEL.name(), type.getLabel(), type));
        }
        graph.putResourceType(type.getLabel(), dataType);
    }

    @Property
    public void whenCallingPutResourceTypeWithAnExistingNonUniqueResourceTypeLabelButADifferentDataType_Throw(
            @Open GraknGraph graph, @FromGraph ResourceType<?> resourceType,
            ResourceType.DataType<?> dataType) {
        assumeThat(dataType, not(is(resourceType.getDataType())));
        Label label = resourceType.getLabel();

        exception.expect(GraphOperationException.class);
        if(isMetaLabel(label)) {
            exception.expectMessage(ErrorMessage.META_TYPE_IMMUTABLE.getMessage(label));
        } else {
            exception.expectMessage(ErrorMessage.IMMUTABLE_VALUE.getMessage(resourceType.getDataType(), dataType, Schema.VertexProperty.DATA_TYPE.name()));
        }

        graph.putResourceType(label, dataType);
    }

    @Property
    public void whenCallingPutRuleType_CreateATypeWithSuperTypeRule(@Open GraknGraph graph, @Unused Label label) {
        RuleType ruleType = graph.putRuleType(label);
        assertEquals(graph.admin().getMetaRuleType(), ruleType.sup());
    }

    @Property
    public void whenCallingPutRuleTypeWithAnExistingRuleTypeLabel_ItReturnsThatType(
            @Open GraknGraph graph, @FromGraph RuleType ruleType) {
        RuleType newType = graph.putRuleType(ruleType.getLabel());
        assertEquals(ruleType, newType);
    }

    @Property
    public void whenCallingPutRuleTypeWithAnExistingNonRuleTypeLabel_Throw(
            @Open GraknGraph graph, @FromGraph Type type) {
        assumeFalse(type.isRuleType());

        exception.expect(GraphOperationException.class);
        if(Schema.MetaSchema.isMetaLabel(type.getLabel())){
            exception.expectMessage(ErrorMessage.RESERVED_WORD.getMessage(type.getLabel().getValue()));
        } else {
            exception.expectMessage(ErrorMessage.UNIQUE_PROPERTY_TAKEN.getMessage(Schema.VertexProperty.TYPE_LABEL.name(), type.getLabel(), type));
        }

        graph.putRuleType(type.getLabel());
    }

    @Property
    public void whenCallingPutRelationType_CreateATypeWithSuperTypeRelation(
            @Open GraknGraph graph, @Unused Label label) {
        RelationType relationType = graph.putRelationType(label);
        assertEquals(graph.admin().getMetaRelationType(), relationType.sup());
    }

    @Property
    public void whenCallingPutRelationType_CreateATypeThatOwnsNoRoles(
            @Open GraknGraph graph, @Unused Label label) {
        RelationType relationType = graph.putRelationType(label);
        graph.showImplicitConcepts(true);
        assertThat(relationType.relates(), empty());
    }

    @Property
    public void whenCallingPutRelationTypeWithAnExistingRelationTypeLabel_ItReturnsThatType(
            @Open GraknGraph graph, @FromGraph RelationType relationType) {
        RelationType newType = graph.putRelationType(relationType.getLabel());
        assertEquals(relationType, newType);
    }

    @Property
    public void whenCallingPutRelationTypeWithAnExistingNonRelationTypeLabel_Throw(
            @Open GraknGraph graph, @FromGraph Type type) {
        assumeFalse(type.isRelationType());

        exception.expect(GraphOperationException.class);
        if(Schema.MetaSchema.isMetaLabel(type.getLabel())){
            exception.expectMessage(ErrorMessage.RESERVED_WORD.getMessage(type.getLabel().getValue()));
        } else {
            exception.expectMessage(ErrorMessage.UNIQUE_PROPERTY_TAKEN.getMessage(Schema.VertexProperty.TYPE_LABEL.name(), type.getLabel(), type));
        }
        graph.putRelationType(type.getLabel());
    }

    @Property
    public void whenCallingPutRoleType_CreateATypeWithSuperTypeRole(@Open GraknGraph graph, @Unused Label label) {
        Role role = graph.putRole(label);
        assertEquals(graph.admin().getMetaRoleType(), role.sup());
    }

    @Property
    public void whenCallingPutRoleType_CreateATypeWithDefaultProperties(
            @Open GraknGraph graph, @Unused Label label) {
        Role role = graph.putRole(label);

        assertThat("The role type should be played by no types", role.playedByTypes(), empty());
        assertThat("The role type should be owned by no relation types", role.relationTypes(), empty());
    }

    @Property
    public void whenCallingPutRoleTypeWithAnExistingRoleTypeLabel_ItReturnsThatType(
            @Open GraknGraph graph, @FromGraph Role role) {
        Role newType = graph.putRole(role.getLabel());
        assertEquals(role, newType);
    }

    @Property
    public void whenCallingPutRoleTypeWithAnExistingNonRoleTypeLabel_Throw(
            @Open GraknGraph graph, @FromGraph Type type) {
        assumeFalse(type.isRole());

        exception.expect(GraphOperationException.class);
        if(Schema.MetaSchema.isMetaLabel(type.getLabel())){
            exception.expectMessage(ErrorMessage.RESERVED_WORD.getMessage(type.getLabel().getValue()));
        } else {
            exception.expectMessage(ErrorMessage.UNIQUE_PROPERTY_TAKEN.getMessage(Schema.VertexProperty.TYPE_LABEL.name(), type.getLabel(), type));
        }
        graph.putRole(type.getLabel());
    }
}
