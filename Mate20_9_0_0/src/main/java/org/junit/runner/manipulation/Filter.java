package org.junit.runner.manipulation;

import java.util.Iterator;
import org.junit.runner.Description;

public abstract class Filter {
    public static final Filter ALL = new Filter() {
        public boolean shouldRun(Description description) {
            return true;
        }

        public String describe() {
            return "all tests";
        }

        public void apply(Object child) throws NoTestsRemainException {
        }

        public Filter intersect(Filter second) {
            return second;
        }
    };

    public abstract String describe();

    public abstract boolean shouldRun(Description description);

    public static Filter matchMethodDescription(final Description desiredDescription) {
        return new Filter() {
            public boolean shouldRun(Description description) {
                if (description.isTest()) {
                    return desiredDescription.equals(description);
                }
                Iterator it = description.getChildren().iterator();
                while (it.hasNext()) {
                    if (shouldRun((Description) it.next())) {
                        return true;
                    }
                }
                return false;
            }

            public String describe() {
                return String.format("Method %s", new Object[]{desiredDescription.getDisplayName()});
            }
        };
    }

    public void apply(Object child) throws NoTestsRemainException {
        if (child instanceof Filterable) {
            ((Filterable) child).filter(this);
        }
    }

    public Filter intersect(final Filter second) {
        if (second == this || second == ALL) {
            return this;
        }
        return new Filter() {
            public boolean shouldRun(Description description) {
                return this.shouldRun(description) && second.shouldRun(description);
            }

            public String describe() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(this.describe());
                stringBuilder.append(" and ");
                stringBuilder.append(second.describe());
                return stringBuilder.toString();
            }
        };
    }
}
