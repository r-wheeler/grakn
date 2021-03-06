package ai.grakn.graql.internal.gremlin.fragment;

import ai.grakn.graql.Graql;
import ai.grakn.graql.Var;
import ai.grakn.util.Schema;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static ai.grakn.util.Schema.VertexProperty.INSTANCE_TYPE_ID;
import static ai.grakn.util.Schema.EdgeLabel.PLAYS;
import static ai.grakn.util.Schema.EdgeLabel.SUB;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OutPlaysFragmentTest {

    private final Var start = Graql.var();
    private final Var end = Graql.var();
    private final OutPlaysFragment fragment = new OutPlaysFragment(null, start, end, false);

    @Test
    @SuppressWarnings("unchecked")
    public void testApplyTraversalFollowsSubsUpwards() {
        GraphTraversal<Vertex, Vertex> traversal = __.V();
        fragment.applyTraversal(traversal, null);

        // Make sure we traverse upwards subs once and plays
        assertThat(traversal, is(__.V()
                .union(__.<Vertex>not(__.has(INSTANCE_TYPE_ID.name())).not(__.hasLabel(Schema.BaseType.SHARD.name())), __.repeat(__.out(SUB.getLabel())).emit()).unfold()
                .out(PLAYS.getLabel())
        ));
    }
}
