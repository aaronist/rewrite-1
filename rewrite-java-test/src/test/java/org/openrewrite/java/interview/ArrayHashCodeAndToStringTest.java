package org.openrewrite.java.interview;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class ArrayHashCodeAndToStringTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ArrayHashCodeAndToString());
    }

    @Test
    void replace_Array_hashCode_with_Arrays_hashCode() {
        rewriteRun(
          java(
            """
              class A {
                  public static void main(String[] args) {
                      int argHash = args.hashCode();
                  }
              }
              """,
            """
              import java.util.Arrays;
              
              class A {
                  public static void main(String[] args) {
                      int argHash = Arrays.hashCode(args);
                  }
              }
              """
          )
        );
    }

    @Test
    void replace_Array_hashCode_with_Arrays_hashCode2() {
        rewriteRun(
          java(
            """
              class A {
                  public static void main(String[] args) {
                      int argHash = foo(args.hashCode());
                  }
                  
                  public static int foo(int i) {
                      return i;
                  }
              }
              """,
            """
              import java.util.Arrays;
              
              class A {
                  public static void main(String[] args) {
                      int argHash = foo(Arrays.hashCode(args));
                  }
                  
                  public static int foo(int i) {
                      return i;
                  }
              }
              """
          )
        );
    }

    @Test
    void replace_Array_toString_with_Arrays_toString() {
        rewriteRun(
          java(
            """
              class A {
                  public static void main(String[] args) {
                      String argStr = args.toString();
                  }
              }
              """,
            """
              import java.util.Arrays;
              
              class A {
                  public static void main(String[] args) {
                      int argHash = Arrays.toString(args);
                  }
              }
              """
          )
        );
    }
}
