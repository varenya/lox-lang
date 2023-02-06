package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
  private final List<Token> tokens;
  private int current = 0;

  private static class ParseError extends RuntimeException {}

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  private Stmt statement() {
    if (match(PRINT)) {
      return printStatement();
    }
    return expressionStatement();
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expect ; after value");
    return new Stmt.PrintExpr(value);
  }

  private Stmt expressionStatement() {
    Expr expression = expression();
    consume(SEMICOLON, "Expect ; after value");
    return new Stmt.Expression(expression);
  }

  private Expr expression() {
    return equality();
  }

  private Expr equality() {
    Expr expr = comparison();
    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr comparison() {
    Expr expr = term();
    while (match(GREATER_EQUAL, GREATER, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr term() {
    Expr expr = factor();
    while (match(PLUS, MINUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr factor() {
    Expr expr = unary();
    while (match(STAR, SLASH)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return primary();
  }

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);
    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }
    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      System.out.println(new AstPrinter().print(expr));
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }
    throw error(peek(), "Expect expression.");
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private Token peek() {
    return this.tokens.get(current);
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private Token consume(TokenType tokenType, String errorMessage) {
    boolean checkType = check(tokenType);
    if (checkType) return advance();
    throw error(peek(), errorMessage);
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  List<Stmt> parse() {
    try {
      List<Stmt> statements = new ArrayList<>();
      while (!isAtEnd()) {
        statements.add(statement());
      }
      return statements;
    } catch (ParseError error) {
      return null;
    }
  }
}
