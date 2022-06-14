/*
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.controlflow;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.openrewrite.Cursor;
import org.openrewrite.java.tree.Expression;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor(staticName = "forGraph", access = AccessLevel.PACKAGE)
public final class ControlFlowSummary {
    private final ControlFlowNode.Start start;
    private final ControlFlowNode.End end;

    public Set<ControlFlowNode.BasicBlock> getBasicBlocks() {
        return getBasicBlocks(start).collect(Collectors.toSet());
    }

    public Set<ControlFlowNode.ConditionNode> getConditionNodes() {
        return getConditionNodes(start).collect(Collectors.toSet());
    }

    private Stream<ControlFlowNode.BasicBlock> getBasicBlocks(ControlFlowNode controlFlowNode) {
        return controlFlowNode.getSuccessors().stream().flatMap(cfn -> {
            if (cfn instanceof ControlFlowNode.BasicBlock) {
                return Stream.concat(
                        Stream.of((ControlFlowNode.BasicBlock) cfn),
                        getBasicBlocks(cfn)
                );
            } else {
                return getBasicBlocks(cfn);
            }
        });
    }

    private Stream<ControlFlowNode.ConditionNode> getConditionNodes(ControlFlowNode controlFlowNode) {
        return controlFlowNode.getSuccessors().stream().flatMap(cfn -> {
            if (cfn instanceof ControlFlowNode.ConditionNode) {
                return Stream.concat(
                        Stream.of((ControlFlowNode.ConditionNode) cfn),
                        getConditionNodes(cfn)
                );
            } else {
                return getConditionNodes(cfn);
            }
        });
    }

    public Set<Expression> computeReachableExpressions(BarrierGuardPredicate predicate) {
        return computeExecutableCodePoints(predicate)
                .stream()
                .filter(cursor -> cursor.getValue() instanceof Expression)
                .map(cursor -> (Expression) cursor.getValue())
                .collect(Collectors.toSet());
    }

    public Set<Cursor> computeExecutableCodePoints(BarrierGuardPredicate predicate) {
        return computeReachableBasicBlock(predicate)
                .stream()
                .flatMap(b -> b.getNodeCursors().stream())
                .collect(Collectors.toSet());
    }

    public Set<ControlFlowNode.BasicBlock> computeReachableBasicBlock(BarrierGuardPredicate predicate) {
        Set<ControlFlowNode> reachable = new HashSet<>();
        recurseComputeReachableBasicBlock(start, predicate, reachable);
        return reachable
                .stream()
                .filter(cfn -> cfn instanceof ControlFlowNode.BasicBlock)
                .map(cfn -> (ControlFlowNode.BasicBlock) cfn)
                .collect(Collectors.toSet());
    }

    private void recurseComputeReachableBasicBlock(ControlFlowNode visit, BarrierGuardPredicate predicate, Set<ControlFlowNode> reachable) {
        reachable.add(visit);
        final Queue<ControlFlowNode> toVisit = new LinkedList<>();
        if (visit instanceof ControlFlowNode.ConditionNode) {
            toVisit.addAll(((ControlFlowNode.ConditionNode) visit).visit(predicate));
        } else if (!(visit instanceof ControlFlowNode.End)) {
            toVisit.addAll(visit.getSuccessors());
        } else {
            // End node does not need to be visited
            return;
        }
        toVisit.removeAll(reachable);
        toVisit.forEach(n -> recurseComputeReachableBasicBlock(n, predicate, reachable));
    }

    private Set<ControlFlowNode> walk(ControlFlowNode.ConditionNode node, BarrierGuardPredicate isBarrierGuard) {
        Set<ControlFlowNode> nodes = new HashSet<>(2);
        if (!isBarrierGuard.isBarrierGuard(node.asGuard(), true)) {
            nodes.add(node.getTruthySuccessor());
        }
        if (!isBarrierGuard.isBarrierGuard(node.asGuard(), false)) {
            nodes.add(node.getFalsySuccessor());
        }
        return nodes;
    }

    int getBasicBlockCount() {
        return getBasicBlocks().size();
    }

    int getConditionNodeCount() {
        return getConditionNodes().size();
    }

    int getExitCount() {
        return end.getPredecessors().size();
    }
}
