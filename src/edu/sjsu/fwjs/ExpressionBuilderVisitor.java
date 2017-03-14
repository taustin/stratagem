package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.fwjs.parser.StratagemBaseVisitor;
import edu.sjsu.fwjs.parser.StratagemParser;

public class ExpressionBuilderVisitor extends StratagemBaseVisitor<Expression>{
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
	public Expression visitIf(StratagemParser.IfContext ctx) {
		Expression cond = visit(ctx.expr());
		Expression thn = visit(ctx.seq(0));
		Expression els = visit(ctx.seq(1));
		return new IfExpr(cond, thn, els);
	}

	@Override
	public Expression visitMulDivMod(StratagemParser.MulDivModContext ctx) {
		Expression lhs = visit(ctx.expr(0));
		Expression rhs = visit(ctx.expr(1));
		return binOpExpHelper(ctx.op.getType(), lhs, rhs);
	}

	@Override
	public Expression visitAddSub(StratagemParser.AddSubContext ctx) {
		Expression lhs = visit(ctx.expr(0));
		Expression rhs = visit(ctx.expr(1));
		return binOpExpHelper(ctx.op.getType(), lhs, rhs);
	}

	@Override
	public Expression visitComparison(StratagemParser.ComparisonContext ctx) {
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
	public Expression visitFunctionApp(StratagemParser.FunctionAppContext ctx) {
		Expression f = visit(ctx.expr());
		List<Expression> args = new ArrayList<>();
		Expression arg = visit(ctx.args().getChild(1));
		args.add(arg);
		return new FunctionAppExpr(f, args);
	}

	@Override
	public Expression visitFunctionDecl(StratagemParser.FunctionDeclContext ctx) {
		List<String> params = new ArrayList<>();
		for (int i=1; i<ctx.params().getChildCount()-1; i+=2) {
			params.add(ctx.params().getChild(i).getText());
		}
		Expression body = visit(ctx.seq());
		return new FunctionDeclExpr(params, body);
	}

	@Override
	public Expression visitLet(StratagemParser.LetContext ctx) {
		String id = ctx.ID().getText();
		Expression value = visit(ctx.expr(0));
		Expression next = visit(ctx.expr(1));

		List<String> params = new ArrayList<>();
		params.add(id);
		FunctionDeclExpr implicitDecl = new FunctionDeclExpr(params, next);

		List<Expression> args = new ArrayList<>();
		args.add(value);
		return new FunctionAppExpr(implicitDecl, args);
	}

	@Override
	public Expression visitInt(StratagemParser.IntContext ctx) {
		int val = Integer.valueOf(ctx.LIT_INT().getText());
		return new ValueExpr(new IntVal(val));
	}

	@Override
	public Expression visitBool(StratagemParser.BoolContext ctx) {
		boolean val = Boolean.valueOf(ctx.LIT_BOOL().getText());
		return new ValueExpr(new BoolVal(val));
	}

	@Override
	public Expression visitString(StratagemParser.StringContext ctx) {
		String val = ctx.LIT_STRING().getText();
		return new ValueExpr(new StringVal(val));
	}

	@Override
	public Expression visitUnit(StratagemParser.UnitContext ctx) {
		return new ValueExpr(new UnitVal());
	}

	@Override
	public Expression visitId(StratagemParser.IdContext ctx) {
		String id = ctx.ID().getText();
		return new VarExpr(id);
	}
}
