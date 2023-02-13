package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output directory>");
      System.exit(64);
    }
    String outputDir = args[0];
    defineAst(
        outputDir,
        "Expr",
        Arrays.asList(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right",
            "Assign    : Token name, Expr value",
            "Variable    : Token name"));

    defineAst(
        outputDir,
        "Stmt",
        Arrays.asList(
            "Block   : List<Stmt> statements",
            "Expression   : Expr expr",
            "PrintExpr  : Expr expr",
            "Var  : Token name, Expr initializer"));
  }

  private static void defineAst(String outputDir, String baseName, List<String> types)
      throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

    writer.println("package com.craftinginterpreters.lox;");
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + baseName + " {");

    defineVisitor(writer, baseName, types);

    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      String fieldNames = type.split(":")[1].trim();
      defineType(typeName, baseName, fieldNames, writer);
    }

    writer.println();
    writer.println("    abstract <R> R accept(Visitor<R> visitor);");

    writer.println("}");
    writer.close();
  }

  private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
    writer.println("        interface Visitor<R> {");

    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      writer.println(
          "   R visit"
              + typeName
              + baseName
              + "("
              + typeName
              + " "
              + baseName.toLowerCase()
              + " );");
    }
    writer.println("    }");
  }

  private static void defineType(
      String typeName, String baseName, String fieldNames, PrintWriter writer) {
    writer.println();
    writer.println("static class " + typeName + " extends " + baseName + " {");
    writer.println("       " + typeName + "( " + fieldNames + " ) {");

    String[] fields = fieldNames.split(", ");

    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("            this." + name + " = " + name + ";");
    }
    writer.println("}");

    writer.println();

    for (String field : fields) {
      writer.println("      final " + field + ";");
    }

    writer.println();

    writer.println("    @Override");
    writer.println("    <R> R accept(Visitor<R> visitor) {");
    writer.println("       return visitor.visit" + typeName + baseName + "(this);");
    writer.println("        }");

    writer.println(" }");
  }
}
