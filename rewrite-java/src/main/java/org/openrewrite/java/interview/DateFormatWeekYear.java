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
package org.openrewrite.java.interview;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

public class DateFormatWeekYear extends Recipe {

    @Override
    public String getDisplayName() {
        return "Week Year (\"YYYY\") should not be used for date formatting";
    }

    @Override
    public String getDescription() {
        return "The `YYYY` pattern in `SimpleDateFormat` and `DateTimeFormatter` does not behave as expected. " +
                "Use `yyyy` instead.";
    }

    @Override
    public @Nullable Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(1);
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("RSPEC-3986");
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new DateFormatWeekYearVisitor<>();
    }

    private static class DateFormatWeekYearVisitor<P> extends JavaIsoVisitor<P> {

    }
}
