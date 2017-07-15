package edu.sjsu.stratagem;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.stratagem.exception.StratagemException;
import edu.sjsu.stratagem.parser.StratagemBaseVisitor;
import edu.sjsu.stratagem.parser.StratagemParser;

public class ExpressionBuilderVisitor extends StratagemBaseVisitor<Expression>{
    @Override
    public Expression visitBinOp(StratagemParser.BinOpContext ctx) {
        Expression lhs = visit(ctx.expr(0));
        Expression rhs = visit(ctx.expr(1));
        return binOpExpHelper(ctx.op.getType(), lhs, rhs);
    }

    /**
     * Converts binops from parser to binops from  interpreter,
     * and then build a BinOpExpr.
     */
    private BinOpExpr binOpExpHelper(int type, Expression lhs, Expression rhs) {
        Op op = null;
        switch (type) {
        case StratagemParser.ADD:
            op = Op.ADD;
            break;
        case StratagemParser.SUB:
            op = Op.SUBTRACT;
            break;
        case StratagemParser.MUL:
            op = Op.MULTIPLY;
            break;
        case StratagemParser.DIV:
            op = Op.DIVIDE;
            break;
        case StratagemParser.MOD:
            op = Op.MOD;
            break;
        case StratagemParser.LT:
            op = Op.LT;
            break;
        case StratagemParser.LE:
            op = Op.LE;
            break;
        case StratagemParser.GT:
            op = Op.GT;
            break;
        case StratagemParser.GE:
            op = Op.GE;
            break;
        case StratagemParser.EQ:
            op = Op.EQ;
            break;
        case StratagemParser.NE:
            op = Op.NE;
            break;
        }
        return new BinOpExpr(op, lhs, rhs);
    }

    @Override
    public Expression visitBool(StratagemParser.BoolContext ctx) {
        boolean val = Boolean.valueOf(ctx.LIT_BOOL().getText());
        return new ValueExpr(new BoolVal(val));
    }

    @Override
    public Expression visitFunctionApp(StratagemParser.FunctionAppContext ctx) {
        Expression f = visit(ctx.expr());
        List<Expression> args = new ArrayList<>();

        for (StratagemParser.ExprContext expr : ctx.args().expr()) {
            Expression arg = visit(expr);
            args.add(arg);
        }

        return new FunctionAppExpr(f, args);
    }

    @Override
    public Expression visitFunctionDecl(StratagemParser.FunctionDeclContext ctx) {
        List<String> paramNames = new ArrayList<>();
        List<Type> paramTypes = new ArrayList<>();

        for (StratagemParser.ParamContext param : ctx.params().param()) {
            paramNames.add(param.ID().getText());
            paramTypes.add(parseType(param.type()));
        }

        Type returnType = parseType(ctx.type());
        Expression body = visit(ctx.seq());

        return new FunctionDeclExpr(paramNames, paramTypes, returnType, body);
    }

    @Override
    public Expression visitId(StratagemParser.IdContext ctx) {
        String id = ctx.ID().getText();
        return new VarExpr(id);
    }

    @Override
    public Expression visitIf(StratagemParser.IfContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.seq(0));
        Expression els = visit(ctx.seq(1));
        return new IfExpr(cond, thn, els);
    }

    @Override
    public Expression visitInt(StratagemParser.IntContext ctx) {
        int val = Integer.valueOf(ctx.LIT_INT().getText());
        return new ValueExpr(new IntVal(val));
    }

    @Override
    public Expression visitLet(StratagemParser.LetContext ctx) {
        String id = ctx.ID().getText();
        Expression value = visit(ctx.expr(0));
        Expression body = visit(ctx.expr(1));

        List<String> paramNames = new ArrayList<>();
        List<Type> paramTypes = new ArrayList<>();

        paramNames.add(id);
        paramTypes.add(parseType(ctx.type()));

        FunctionDeclExpr implicitDecl = new FunctionDeclExpr(paramNames, paramTypes, null, body);

        List<Expression> args = new ArrayList<>();
        args.add(value);
        return new FunctionAppExpr(implicitDecl, args);
    }

    @Override
    public Expression visitPrint(StratagemParser.PrintContext ctx) {
        Expression arg = visit(ctx.args().getChild(1));
        return new PrintExpr(arg);
    }

    @Override
    public Expression visitProg(StratagemParser.ProgContext ctx) {
        return visit(ctx.seq());
    }

    @Override
    public Expression visitSeq(StratagemParser.SeqContext ctx) {
        List<Expression> exprs = new ArrayList<>();
        for (int i=0; i<ctx.expr().size(); i++) {
            Expression exp = visit(ctx.expr(i));
            if (exp != null) exprs.add(exp);
        }
        return new SeqExpr(exprs);
    }

    @Override
    public Expression visitString(StratagemParser.StringContext ctx) {
        String val = ctx.LIT_STRING().getText();
        return new ValueExpr(new StringVal(val));
    }

    @Override
    public Expression visitUnit(StratagemParser.UnitContext ctx) {
        return new ValueExpr(UnitVal.singleton);
    }

    private Type parseType(StratagemParser.TypeContext ctx) {
        if (ctx.type_prim() != null) {
            return parsePrimitiveType(ctx.type_prim());
        } else {
            return parseClosureType(ctx.type_fun());
        }
    }

    private Type parsePrimitiveType(StratagemParser.Type_primContext ctx) {
        if (ctx.TYPE_BOOL() != null) {
            return BoolType.singleton;
        } else if (ctx.TYPE_INT() != null) {
            return IntType.singleton;
        } else if (ctx.TYPE_STRING() != null) {
            return StringType.singleton;
        } else if (ctx.UNIT() != null) {
            return UnitType.singleton;
        } else {
            throw new StratagemException("Unknown primitive type");
        }
    }

    private static Type[] typeArrayHint = new Type[] {};

    private Type parseClosureType(StratagemParser.Type_funContext ctx) {
        ArrayList<Type> args = new ArrayList<>();
        for (StratagemParser.Type_fun_pieceContext piece : ctx.type_fun_piece()) {
            Type arg;
            if (piece.type_prim() != null) {
                arg = parsePrimitiveType(piece.type_prim());
            } else {
                arg = parseClosureType(piece.type_fun());
            }
            args.add(arg);
        }
        Type ret = args.get(args.size() - 1);
        args.remove(args.size() - 1);
        return new ClosureType(args.toArray(typeArrayHint), ret);
    }
}
