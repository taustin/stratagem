package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptBaseVisitor;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;

public class ExpressionBuilderVisitor extends FeatherweightJavaScriptBaseVisitor<Expression>{
	@Override
	public Expression visitProg(FeatherweightJavaScriptParser.ProgContext ctx) {
		List<Expression> exprs = new ArrayList<Expression>();
		for (int i=0; i<ctx.expr().size(); i++) {
			Expression exp = visit(ctx.expr(i));
			if (exp != null) exprs.add(exp);
		}
		return listToSeqExp(exprs);
	}

	@Override
	public Expression visitBareExpr(FeatherweightJavaScriptParser.BareExprContext ctx) {
		return visit(ctx.expr());
	}

	@Override
	public Expression visitIfThenElse(FeatherweightJavaScriptParser.IfThenElseContext ctx) {
		Expression cond = visit(ctx.expr());
		Expression thn = visit(ctx.block(0));
		Expression els = visit(ctx.block(1));
		return new IfExpr(cond, thn, els);
	}

	@Override
	public Expression visitIfThen(FeatherweightJavaScriptParser.IfThenContext ctx) {
		Expression cond = visit(ctx.expr());
		Expression thn = visit(ctx.block());
		return new IfExpr(cond, thn, new ValueExpr(new UnitVal()));
	}

	@Override
	public Expression visitWhile(FeatherweightJavaScriptParser.WhileContext ctx) {
		Expression cond = visit(ctx.expr());
		Expression body = visit(ctx.block());
		return new WhileExpr(cond, body);
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
		}
		return new BinOpExpr(op, lhs, rhs);
	}

	@Override
	public Expression visitFunctionApp(FeatherweightJavaScriptParser.FunctionAppContext ctx) {
		Expression f = visit(ctx.expr());
		List<Expression> args = new ArrayList<Expression>();
		for (int i=1; i<ctx.args().getChildCount()-1; i+=2) {
			Expression arg = visit(ctx.args().getChild(i));
			args.add(arg);
		}
		return new FunctionAppExpr(f, args);
	}

	@Override
	public Expression visitFunctionDecl(FeatherweightJavaScriptParser.FunctionDeclContext ctx) {
		List<String> params = new ArrayList<String>();
		for (int i=1; i<ctx.params().getChildCount()-1; i+=2) {
			params.add(ctx.params().getChild(i).getText());
		}
		List<Expression> exprList = new ArrayList<Expression>();
		for (int i=0; i<ctx.stat().size(); i++) {
			Expression exp = visit(ctx.stat(i));
			exprList.add(exp);
		}
		Expression body = listToSeqExp(exprList);
		return new FunctionDeclExpr(params, body);
	}

	@Override
	public Expression visitVarDecl(FeatherweightJavaScriptParser.VarDeclContext ctx) {
		String id = ctx.ID().getText();
		Expression exp = visit(ctx.expr());
		return new VarDeclExpr(id, exp);
	}

	@Override
	public Expression visitAssign(FeatherweightJavaScriptParser.AssignContext ctx) {
		String id = ctx.ID().getText();
		Expression exp = visit(ctx.expr());
		return new AssignExpr(id, exp);
	}

	@Override
	public Expression visitInt(FeatherweightJavaScriptParser.IntContext ctx) {
		int val = Integer.valueOf(ctx.INT().getText());
		return new ValueExpr(new IntVal(val));
	}

	@Override
	public Expression visitBool(FeatherweightJavaScriptParser.BoolContext ctx) {
		boolean val = Boolean.valueOf(ctx.BOOL().getText());
		return new ValueExpr(new BoolVal(val));
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

	@Override
	public Expression visitParens(FeatherweightJavaScriptParser.ParensContext ctx) {
		return visit(ctx.expr());
	}

	@Override
	public Expression visitFullBlock(FeatherweightJavaScriptParser.FullBlockContext ctx) {
		List<Expression> stmts = new ArrayList<Expression>();
		for (int i=1; i<ctx.getChildCount()-1; i++) {
			Expression exp = visit(ctx.getChild(i));
			stmts.add(exp);
		}
		return listToSeqExp(stmts);
	}

	/**
	 * Converts a list of expressions to one sequence expression,
	 * if the list contained more than one expression.
	 */
	private Expression listToSeqExp(List<Expression> exprs) {
		if (exprs.isEmpty()) return new ValueExpr(new UnitVal());
		Expression exp = exprs.get(0);
		for (int i=1; i<exprs.size(); i++) {
			exp = new SeqExpr(exp, exprs.get(i));
		}
		return exp;
	}

	@Override
	public Expression visitSimpBlock(FeatherweightJavaScriptParser.SimpBlockContext ctx) {
		return visit(ctx.stat());
	}
}
