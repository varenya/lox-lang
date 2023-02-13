package com.craftinginterpreters.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private Environment environment = new Environment();

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

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    environment.assign(expr.name, value);
    return value;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return environment.get(expr.name);
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmtExpr) {
    evaluate(stmtExpr.expr);
    return null;
  }

  @Override
  public Void visitPrintExprStmt(Stmt.PrintExpr printExpr) {
    Object value = evaluate(printExpr.expr);
    System.out.println(stringify(value));
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }
    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block blockStatements) {
    executeBlock(blockStatements.statements, new Environment(environment));
    return null;
  }

  private void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;
      for (Stmt stmt : statements) {
        execute(stmt);
      }

    } finally {
      this.environment = previous;
    }
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

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  void interpret(List<Stmt> stmts) {
    try {
      for (Stmt stmt : stmts) {
        execute(stmt);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }
}
