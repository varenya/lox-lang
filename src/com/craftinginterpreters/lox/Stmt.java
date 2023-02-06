package com.craftinginterpreters.lox;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);

    R visitPrintExprStmt(PrintExpr stmt);
  }

  static class Expression extends Stmt {
    Expression(Expr expr) {
      this.expr = expr;
    }

    final Expr expr;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  static class PrintExpr extends Stmt {
    PrintExpr(Expr expr) {
      this.expr = expr;
    }

    final Expr expr;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintExprStmt(this);
    }
  }

  abstract <R> R accept(Visitor<R> visitor);
}
