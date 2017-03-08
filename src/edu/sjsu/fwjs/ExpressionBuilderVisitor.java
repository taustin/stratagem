package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptBaseVisitor;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;

public class ExpressionBuilderVisitor extends FeatherweightJavaScriptBaseVisitor<Expression>{
	@Override
	public Expression visitProg(FeatherweightJavaScriptParser.ProgContext ctx) {
		return visit(ctx.seq());
	}

	@Override
	public Expression visitSeq(FeatherweightJavaScriptParser.SeqContext ctx) {
		List<Expression> exprs = new ArrayList<>();
		for (int i=0; i<ctx.expr().size(); i++) {
			Expression exp = visit(ctx.expr(i));
			if (exp != null) exprs.add(exp);
		}
		return new SeqExpr(exprs);
	}

	@Override
	public Expression visitIf(FeatherweightJavaScriptParser.IfContext ctx) {
		Expression cond = visit(ctx.expr());
		Expression thn = visit(ctx.seq(0));
		Expression els = visit(ctx.seq(1));
		return new IfExpr(cond, thn, els);
	}

	@Override
	public Expression visitMulDivMod(FeatherweightJavaScriptParser.MulDivModContext ctx) {
		Expression lhs = visit(ctx.expr(0));
		Expression rhs = visit(ctx.expr(1));
		return binOpExpHelper(ctx.op.getType(), lhs, rhs);
	}

	@Override
	public Expression visitAddSub(FeatherweightJavaScriptParser.AddSubContext ctx) {
		Expression lhs = visit(ctx.expr(0));
		Expression rhs = visit(ctx.expr(1));
		return binOpExpHelper(ctx.op.getType(), lhs, rhs);
	}

	@Override
	public Expression visitComparison(FeatherweightJavaScriptParser.ComparisonContext ctx) {
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
		case FeatherweightJavaScriptParser.ADD:
			op = Op.ADD;
			break;
		case FeatherweightJavaScriptParser.SUB:
			op = Op.SUBTRACT;
			break;
		case FeatherweightJavaScriptParser.MUL:
			op = Op.MULTIPLY;
			break;
		case FeatherweightJavaScriptParser.DIV:
			op = Op.DIVIDE;
			break;
		case FeatherweightJavaScriptParser.MOD:
			op = Op.MOD;
			break;
		case FeatherweightJavaScriptParser.LT:
			op = Op.LT;
			break;
		case FeatherweightJavaScriptParser.LE:
			op = Op.LE;
			break;
		case FeatherweightJavaScriptParser.GT:
			op = Op.GT;
			break;
		case FeatherweightJavaScriptParser.GE:
			op = Op.GE;
			break;
		case FeatherweightJavaScriptParser.EQ:
			op = Op.EQ;
			break;
		case FeatherweightJavaScriptParser.NE:
			op = Op.NE;
			break;
		}
		return new BinOpExpr(op, lhs, rhs);
	}

	@Override
	public Expression visitFunctionApp(FeatherweightJavaScriptParser.FunctionAppContext ctx) {
		Expression f = visit(ctx.expr());
		List<Expression> args = new ArrayList<Expression>();
		Expression arg = visit(ctx.args().getChild(1));
		args.add(arg);
		return new FunctionAppExpr(f, args);
	}

	@Override
	public Expression visitFunctionDecl(FeatherweightJavaScriptParser.FunctionDeclContext ctx) {
		List<String> params = new ArrayList<String>();
		for (int i=1; i<ctx.params().getChildCount()-1; i+=2) {
			params.add(ctx.params().getChild(i).getText());
		}
		Expression body = visit(ctx.seq());
		return new FunctionDeclExpr(params, body);
	}

	@Override
	public Expression visitLet(FeatherweightJavaScriptParser.LetContext ctx) {
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
	public Expression visitInt(FeatherweightJavaScriptParser.IntContext ctx) {
		int val = Integer.valueOf(ctx.LIT_INT().getText());
		return new ValueExpr(new IntVal(val));
	}

	@Override
	public Expression visitBool(FeatherweightJavaScriptParser.BoolContext ctx) {
		boolean val = Boolean.valueOf(ctx.LIT_BOOL().getText());
		return new ValueExpr(new BoolVal(val));
	}

	@Override
	public Expression visitString(FeatherweightJavaScriptParser.StringContext ctx) {
		String val = ctx.LIT_STRING().getText();
		return new ValueExpr(new StringVal(val));
	}

	@Override
	public Expression visitUnit(FeatherweightJavaScriptParser.UnitContext ctx) {
		return new ValueExpr(new UnitVal());
	}

	@Override
	public Expression visitId(FeatherweightJavaScriptParser.IdContext ctx) {
		String id = ctx.ID().getText();
		return new VarExpr(id);
	}
}
