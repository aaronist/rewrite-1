package org.openrewrite.java.dataflow;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.Expression;

interface AdditionalFlowStepPredicate {
    boolean isAdditionalFlowStep(
            Expression startExpression,
            Cursor startCursor,
            Expression endExpression,
            Cursor endCursor
    );
}