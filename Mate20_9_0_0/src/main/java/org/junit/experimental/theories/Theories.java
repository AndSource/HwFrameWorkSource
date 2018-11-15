package org.junit.experimental.theories;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class Theories extends BlockJUnit4ClassRunner {

    public static class TheoryAnchor extends Statement {
        private List<AssumptionViolatedException> fInvalidParameters = new ArrayList();
        private int successes = 0;
        private final TestClass testClass;
        private final FrameworkMethod testMethod;

        public TheoryAnchor(FrameworkMethod testMethod, TestClass testClass) {
            this.testMethod = testMethod;
            this.testClass = testClass;
        }

        private TestClass getTestClass() {
            return this.testClass;
        }

        public void evaluate() throws Throwable {
            runWithAssignment(Assignments.allUnassigned(this.testMethod.getMethod(), getTestClass()));
            boolean hasTheoryAnnotation = this.testMethod.getAnnotation(Theory.class) != null;
            if (this.successes == 0 && hasTheoryAnnotation) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Never found parameters that satisfied method assumptions.  Violated assumptions: ");
                stringBuilder.append(this.fInvalidParameters);
                Assert.fail(stringBuilder.toString());
            }
        }

        protected void runWithAssignment(Assignments parameterAssignment) throws Throwable {
            if (parameterAssignment.isComplete()) {
                runWithCompleteAssignment(parameterAssignment);
            } else {
                runWithIncompleteAssignment(parameterAssignment);
            }
        }

        protected void runWithIncompleteAssignment(Assignments incomplete) throws Throwable {
            for (PotentialAssignment source : incomplete.potentialsForNextUnassigned()) {
                runWithAssignment(incomplete.assignNext(source));
            }
        }

        protected void runWithCompleteAssignment(final Assignments complete) throws Throwable {
            new BlockJUnit4ClassRunner(getTestClass().getJavaClass()) {
                protected void collectInitializationErrors(List<Throwable> list) {
                }

                public Statement methodBlock(FrameworkMethod method) {
                    final Statement statement = super.methodBlock(method);
                    return new Statement() {
                        public void evaluate() throws Throwable {
                            try {
                                statement.evaluate();
                                TheoryAnchor.this.handleDataPointSuccess();
                            } catch (AssumptionViolatedException e) {
                                TheoryAnchor.this.handleAssumptionViolation(e);
                            } catch (Throwable e2) {
                                TheoryAnchor.this.reportParameterizedError(e2, complete.getArgumentStrings(TheoryAnchor.this.nullsOk()));
                            }
                        }
                    };
                }

                protected Statement methodInvoker(FrameworkMethod method, Object test) {
                    return TheoryAnchor.this.methodCompletesWithParameters(method, complete, test);
                }

                public Object createTest() throws Exception {
                    Object[] params = complete.getConstructorArguments();
                    if (!TheoryAnchor.this.nullsOk()) {
                        Assume.assumeNotNull(params);
                    }
                    return getTestClass().getOnlyConstructor().newInstance(params);
                }
            }.methodBlock(this.testMethod).evaluate();
        }

        private Statement methodCompletesWithParameters(final FrameworkMethod method, final Assignments complete, final Object freshInstance) {
            return new Statement() {
                public void evaluate() throws Throwable {
                    Object[] values = complete.getMethodArguments();
                    if (!TheoryAnchor.this.nullsOk()) {
                        Assume.assumeNotNull(values);
                    }
                    method.invokeExplosively(freshInstance, values);
                }
            };
        }

        protected void handleAssumptionViolation(AssumptionViolatedException e) {
            this.fInvalidParameters.add(e);
        }

        protected void reportParameterizedError(Throwable e, Object... params) throws Throwable {
            if (params.length == 0) {
                throw e;
            }
            throw new ParameterizedAssertionError(e, this.testMethod.getName(), params);
        }

        private boolean nullsOk() {
            Theory annotation = (Theory) this.testMethod.getMethod().getAnnotation(Theory.class);
            if (annotation == null) {
                return false;
            }
            return annotation.nullsAccepted();
        }

        protected void handleDataPointSuccess() {
            this.successes++;
        }
    }

    public Theories(Class<?> klass) throws InitializationError {
        super(klass);
    }

    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        validateDataPointFields(errors);
        validateDataPointMethods(errors);
    }

    private void validateDataPointFields(List<Throwable> errors) {
        for (Field field : getTestClass().getJavaClass().getDeclaredFields()) {
            if (field.getAnnotation(DataPoint.class) != null || field.getAnnotation(DataPoints.class) != null) {
                StringBuilder stringBuilder;
                if (!Modifier.isStatic(field.getModifiers())) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("DataPoint field ");
                    stringBuilder.append(field.getName());
                    stringBuilder.append(" must be static");
                    errors.add(new Error(stringBuilder.toString()));
                }
                if (!Modifier.isPublic(field.getModifiers())) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("DataPoint field ");
                    stringBuilder.append(field.getName());
                    stringBuilder.append(" must be public");
                    errors.add(new Error(stringBuilder.toString()));
                }
            }
        }
    }

    private void validateDataPointMethods(List<Throwable> errors) {
        for (Method method : getTestClass().getJavaClass().getDeclaredMethods()) {
            if (method.getAnnotation(DataPoint.class) != null || method.getAnnotation(DataPoints.class) != null) {
                StringBuilder stringBuilder;
                if (!Modifier.isStatic(method.getModifiers())) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("DataPoint method ");
                    stringBuilder.append(method.getName());
                    stringBuilder.append(" must be static");
                    errors.add(new Error(stringBuilder.toString()));
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("DataPoint method ");
                    stringBuilder.append(method.getName());
                    stringBuilder.append(" must be public");
                    errors.add(new Error(stringBuilder.toString()));
                }
            }
        }
    }

    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
    }

    protected void validateTestMethods(List<Throwable> errors) {
        for (FrameworkMethod each : computeTestMethods()) {
            if (each.getAnnotation(Theory.class) != null) {
                each.validatePublicVoid(false, errors);
                each.validateNoTypeParametersOnArgs(errors);
            } else {
                each.validatePublicVoidNoArg(false, errors);
            }
            Iterator it = ParameterSignature.signatures(each.getMethod()).iterator();
            while (it.hasNext()) {
                ParametersSuppliedBy annotation = (ParametersSuppliedBy) ((ParameterSignature) it.next()).findDeepAnnotation(ParametersSuppliedBy.class);
                if (annotation != null) {
                    validateParameterSupplier(annotation.value(), errors);
                }
            }
        }
    }

    private void validateParameterSupplier(Class<? extends ParameterSupplier> supplierClass, List<Throwable> errors) {
        Constructor<?>[] constructors = supplierClass.getConstructors();
        if (constructors.length != 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ParameterSupplier ");
            stringBuilder.append(supplierClass.getName());
            stringBuilder.append(" must have only one constructor (either empty or taking only a TestClass)");
            errors.add(new Error(stringBuilder.toString()));
            return;
        }
        Class<?>[] paramTypes = constructors[0].getParameterTypes();
        if (paramTypes.length != 0 && !paramTypes[0].equals(TestClass.class)) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("ParameterSupplier ");
            stringBuilder2.append(supplierClass.getName());
            stringBuilder2.append(" constructor must take either nothing or a single TestClass instance");
            errors.add(new Error(stringBuilder2.toString()));
        }
    }

    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> testMethods = new ArrayList(super.computeTestMethods());
        List<FrameworkMethod> theoryMethods = getTestClass().getAnnotatedMethods(Theory.class);
        testMethods.removeAll(theoryMethods);
        testMethods.addAll(theoryMethods);
        return testMethods;
    }

    public Statement methodBlock(FrameworkMethod method) {
        return new TheoryAnchor(method, getTestClass());
    }
}
