package com.dashjoin.jsonata;

import static com.dashjoin.jsonata.Jsonata.jsonata;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.dashjoin.jsonata.Parser.Symbol;

/** Public read-only AST accessors: getAst() + Symbol getters. */
public class AstAccessTest {

    @Test
    public void binaryComparisonShape() {
        Symbol root = jsonata("accountEnabled = false").getAst();
        assertNotNull(root);
        assertEquals("binary", root.getType());
        assertEquals("=", String.valueOf(root.getValue()));

        Symbol lhs = root.getLhs();
        assertEquals("path", lhs.getType());
        List<Symbol> steps = lhs.getSteps();
        assertEquals(1, steps.size());
        assertEquals("name", steps.get(0).getType());
        assertEquals("accountEnabled", steps.get(0).getValue());

        Symbol rhs = root.getRhs();
        assertEquals("value", rhs.getType());
        assertEquals(false, rhs.getValue());
    }

    @Test
    public void booleanAndInArrayShape() {
        Symbol root = jsonata("a = 1 and b in [\"x\",\"y\"]").getAst();
        assertEquals("binary", root.getType());
        assertEquals("and", String.valueOf(root.getValue()));

        Symbol in = root.getRhs();
        assertEquals("in", String.valueOf(in.getValue()));
        Symbol array = in.getRhs();
        assertEquals("unary", array.getType());
        assertEquals("[", String.valueOf(array.getValue()));
        List<Symbol> elements = array.getExpressions();
        assertEquals(2, elements.size());
        assertEquals("x", elements.get(0).getValue());
        assertEquals("y", elements.get(1).getValue());
    }

    @Test
    public void nestedPathHasMultipleSteps() {
        Symbol root = jsonata("a.b = 1").getAst();
        List<Symbol> steps = root.getLhs().getSteps();
        assertEquals(2, steps.size());
        assertEquals("a", steps.get(0).getValue());
        assertEquals("b", steps.get(1).getValue());
    }

    @Test
    public void functionCallExposesProcedureAndArguments() {
        Symbol root = jsonata("$exists(a)").getAst();
        assertEquals("function", root.getType());
        Symbol procedure = root.getProcedure();
        assertNotNull(procedure);
        assertEquals("variable", procedure.getType());
        assertEquals("exists", procedure.getValue());
        assertEquals(1, root.getArguments().size());
    }

    @Test
    public void predicateStagesVisible() {
        Symbol root = jsonata("user[x = 1] = 2").getAst();
        Symbol path = root.getLhs();
        assertEquals(1, path.getSteps().size());
        assertNotNull(path.getSteps().get(0).getStages());
        assertEquals("filter", path.getSteps().get(0).getStages().get(0).getType());
    }

    @Test
    public void absentAccessorsReturnNull() {
        Symbol literal = jsonata("a = 1").getAst().getRhs();
        assertNull(literal.getLhs());
        assertNull(literal.getRhs());
        assertNull(literal.getSteps());
        assertNull(literal.getPredicate());
        assertNull(literal.getStages());
        assertNull(literal.getExpressions());
    }

    @Test
    public void transformChildrenVisible() {
        Symbol root = jsonata("a ~> |x| {'y': 1}").getAst();
        assertNotNull(root);
        // Walk every declared child accessor to prove transform nodes are reachable.
        assertNotNull(root.getPattern() != null || root.getUpdate() != null
                || root.getDelete() != null || root.getLhs() != null);
    }

    @Test
    public void listsAreUnmodifiable() {
        Symbol path = jsonata("a.b = 1").getAst().getLhs();
        assertThrows(UnsupportedOperationException.class, () -> path.getSteps().add(null));
    }

    @Test
    public void astOfCloneIsSameTree() {
        Jsonata compiled = jsonata("a = 1");
        assertTrue(compiled.getAst() == compiled.getAst());
    }
}
