package com.craftinginterpreters.lox;

public class Interpreter implements Expr.Visitor<Object> {
  @Override
  public Object visitLiteralExpr(Expr.Literal literal) {
    return literal.value;
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping group) {
    return evaluate(group.expression);
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);
    switch (expr.operator.type) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return -1 * (double) right;
    }
    return null;
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary binary) {
    Object left = evaluate(binary.left);
    Object right = evaluate(binary.right);
    switch (binary.operator.type) {
      case MINUS:
        checkNumberOperands(binary.operator, left, right);
        return (double) left - (double) right;
      case PLUS:
        if (left instanceof String && right instanceof String) {
          return (String) left + (String) right;
        }
        if (left instanceof Double && right instanceof Double) {
          return (double) left + (double) right;
        }
        throw new RuntimeError(binary.operator, "Operands must be two numbers or two strings");
      case SLASH:
        checkNumberOperands(binary.operator, left, right);
        return (double) left / (double) right;
      case STAR:
        checkNumberOperands(binary.operator, left, right);
        return (double) left * (double) right;
      case GREATER:
        checkNumberOperands(binary.operator, left, right);
        return (double) left > (double) right;
      case GREATER_EQUAL:
        checkNumberOperands(binary.operator, left, right);
        return (double) left >= (double) right;
      case LESS:
        checkNumberOperands(binary.operator, left, right);
        return (double) left < (double) right;
      case LESS_EQUAL:
        checkNumberOperands(binary.operator, left, right);
        return (double) left <= (double) right;
      case BANG_EQUAL:
        return !isEqual(left, right);
    }
    return null;
  }

  private boolean isTruthy(Object value) {
    if (value == null) {
      return false;
    }
    if (value instanceof Boolean) return (boolean) value;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) {
      return true;
    }
    if (a == null) return false;
    return a.equals(b);
  }

  private String stringify(Object object) {
    if (object == null) return "nil";
    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }
    return object.toString();
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    throw new RuntimeError(operator, "Operands must be a number");
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  void interpret(Expr expr) {
    try {

      Object value = evaluate(expr);
      System.out.println(stringify(value));
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }
}
