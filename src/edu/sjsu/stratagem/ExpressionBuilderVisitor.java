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
        Expression arg = visit(ctx.args().getChild(1));
        return new FunctionAppExpr(f, arg);
    }

    @Override
    public Expression visitFunctionDecl(StratagemParser.FunctionDeclContext ctx) {
        StratagemParser.TypeContext paramTypeContext = ctx.params().type();

        String paramName = ctx.params().ID().getText();
        Type paramType = paramTypeContext == null ? AnyType.singleton
                                                  : parseType(paramTypeContext);
        Expression body = visit(ctx.seq());

        return new FunctionDeclExpr(paramName, paramType, body);
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
        StratagemParser.TypeContext typeContext = ctx.type();

        String id = ctx.ID().getText();
        Expression value = visit(ctx.expr(0));
        Expression body = visit(ctx.expr(1));
        Type paramType = typeContext == null ? AnyType.singleton
                                             : parseType(typeContext);

        FunctionDeclExpr implicitDecl = new FunctionDeclExpr(id, paramType, body);
        return new FunctionAppExpr(implicitDecl, value);
    }

    @Override
    public Expression visitParens(StratagemParser.ParensContext ctx) {
        return visit(ctx.expr());
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
        } else if (ctx.TYPE_UNIT() != null) {
            return UnitType.singleton;
        } else if (ctx.TYPE_ANY() != null) {
            return AnyType.singleton;
        } else {
            throw new StratagemException("Unknown primitive type");
        }
    }

    private Type parseClosureType(StratagemParser.Type_funContext ctx) {
        StratagemParser.Type_primContext prim = ctx.type_prim();
        Type arg;

        if (prim != null) {
            arg = parsePrimitiveType(prim);
        } else {
            StratagemParser.Type_funContext fun = ctx.type_fun();
            arg = parseClosureType(fun);
        }

        Type ret = parseType(ctx.type());
        return new ClosureType(arg, ret);
    }
}
