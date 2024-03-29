package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);

    R visitExpressionStmt(Expression stmt);

    R visitPrintExprStmt(PrintExpr stmt);

    R visitVarStmt(Var stmt);
  }

  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    final List<Stmt> statements;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }
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

  static class Var extends Stmt {
    Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    final Token name;
    final Expr initializer;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
  }

  abstract <R> R accept(Visitor<R> visitor);
}
