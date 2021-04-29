package com.lukashornych.mathmare;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Random;

/**
 * Single randomly generated math expression to be resolved by player.
 *
 * @author Lukáš Hornych 2021
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Expression {

    private final static Random RANDOM = new Random();

    private final int operandA;
    private final int operandB;
    private final Operator operator;
    @Getter(AccessLevel.PRIVATE) private final int expectedResult;

    /**
     * Generates new random expression
     */
    public static Expression generate() {
        int operandA = generateOperand();
        int operandB = generateOperand();
        if (operandA < operandB) {
            final int operandTemp = operandA;
            operandA = operandB;
            operandB = operandTemp;
        }

        final Operator operator = generateOperator();
        final int expectedResult = calculateExpectedResult(operandA, operandB, operator);

        return new Expression(operandA, operandB, operator, expectedResult);
    }

    /**
     * Compares player entered result to expected result
     *
     * @param result player entered result
     */
    public boolean isResultCorrect(int result) {
        return result == getExpectedResult();
    }

    /**
     * Creates string from this expression to be displayed to solve
     */
    public String toSolvableString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%1$2d", operandA));
        switch (operator) {
            case PLUS: sb.append(" + "); break;
            case MINUS: sb.append(" - "); break;
        }
        sb.append(String.format("%1$2d", operandB));
        sb.append(" = ");

        return sb.toString();
    }


    private static int generateOperand() {
        return RANDOM.nextInt(30);
    }

    private static Operator generateOperator() {
        final Operator[] allOperators = Operator.values();
        return allOperators[RANDOM.nextInt(allOperators.length)];
    }

    private static int calculateExpectedResult(int operandA, int operandB, Operator operator) {
        if (operator.equals(Operator.PLUS)) {
            return operandA + operandB;
        }
        if (operator.equals(Operator.MINUS)) {
            return operandA - operandB;
        }

        throw new IllegalArgumentException("Unsupported operator");
    }

    public enum Operator {
        PLUS, MINUS
    }
}
