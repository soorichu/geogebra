package org.geogebra.common.io.latex;

import org.geogebra.common.util.debug.Log;
import org.geogebra.common.util.lang.Unicode;

import com.himamis.retex.editor.share.model.MathArray;
import com.himamis.retex.editor.share.model.MathCharacter;
import com.himamis.retex.editor.share.model.MathComponent;
import com.himamis.retex.editor.share.model.MathFormula;
import com.himamis.retex.editor.share.model.MathFunction;
import com.himamis.retex.editor.share.model.MathSequence;
import com.himamis.retex.editor.share.serializer.Serializer;

/**
 * Serializes internal formulas representation into GeoGebra string
 *
 */
public class GeoGebraSerializer implements Serializer {

	@Override
	public String serialize(MathFormula formula) {
		MathSequence sequence = formula.getRootComponent();
		StringBuilder stringBuilder = new StringBuilder();
		serialize(sequence, stringBuilder);
		return stringBuilder.toString();
	}

	private static void serialize(MathComponent mathComponent,
			MathSequence parent, int index,
			StringBuilder stringBuilder) {
		if (mathComponent instanceof MathCharacter) {
			serialize((MathCharacter) mathComponent, parent, index,
					stringBuilder);
		} else if (mathComponent instanceof MathFunction) {
			serialize((MathFunction) mathComponent, stringBuilder);
		} else if (mathComponent instanceof MathArray) {
			serialize((MathArray) mathComponent, stringBuilder);
		} else if (mathComponent instanceof MathSequence) {
			serialize((MathSequence) mathComponent, stringBuilder);
		}
	}

	/**
	 * @param c
	 *            math formula fragment
	 * @return string
	 */
	public static String serialize(MathComponent c) {
		StringBuilder sb = new StringBuilder();
		GeoGebraSerializer.serialize(c, null, 0, sb);
		return sb.toString();
	}

	private static void serialize(MathCharacter mathCharacter,
			MathSequence parent, int index,
			StringBuilder stringBuilder) {
		if (mathCharacter.getUnicode() == MathCharacter.ZERO_SPACE) {

			if (parent != null && index + 1 < parent.size()) {
				if (parent.getArgument(index + 1) instanceof MathArray) {
					stringBuilder.append(" ");
				}
			}
			return;
		}
		stringBuilder.append(mathCharacter.getUnicode());
	}

	private static void serialize(MathFunction mathFunction,
			StringBuilder stringBuilder) {
		String mathFunctionName = mathFunction.getName();
		if ("^".equals(mathFunctionName)) {
			stringBuilder.append(mathFunctionName + '(');
			serialize(mathFunction.getArgument(0), stringBuilder);
			stringBuilder.append(')');
		} else if ("_".equals(mathFunction.getName())) {
			stringBuilder.append(mathFunctionName + '{');
			serialize(mathFunction.getArgument(0), stringBuilder);
			// a_{1}sin(x) should be a_{1} sin(x)
			stringBuilder.append("}");
		} else if ("frac".equals(mathFunctionName)) {
			stringBuilder.append('(');
			serialize(mathFunction.getArgument(0), stringBuilder);
			stringBuilder.append(")/(");
			serialize(mathFunction.getArgument(1), stringBuilder);
			stringBuilder.append(")");
		} else if ("sqrt".equals(mathFunctionName)) {
			maybeInsertTimes(mathFunction, stringBuilder);
			stringBuilder.append("sqrt(");
			serialize(mathFunction.getArgument(0), stringBuilder);
			stringBuilder.append(')');
		} else if ("nroot".equals(mathFunctionName)) {
			maybeInsertTimes(mathFunction, stringBuilder);
			stringBuilder.append("nroot(");
			serialize(mathFunction.getArgument(1), stringBuilder);
			stringBuilder.append(",");
			serialize(mathFunction.getArgument(0), stringBuilder);
			stringBuilder.append(')');
		} // Strict control of available functions is needed, so that SUM/ and
			// Prod doesn't work
		else if ("sum".equals(mathFunctionName)) {
			stringBuilder.append("Sum");
			serializeArgs(mathFunction, stringBuilder,
					new int[] { 3, 0, 1, 2 });
		} else if ("prod".equals(mathFunctionName)) {
			stringBuilder.append("Product");
			serializeArgs(mathFunction, stringBuilder,
					new int[] { 3, 0, 1, 2 });
		} else if ("int".equals(mathFunctionName)) {
			stringBuilder.append("Integral");
			serializeArgs(mathFunction, stringBuilder,
					new int[] { 2, 0, 1 });
		} else if ("lim".equals(mathFunctionName)) {
			stringBuilder.append("Limit");
			serializeArgs(mathFunction, stringBuilder,
					new int[] { 2, 3, 0, 1 });
		} else {
			// some general function
			maybeInsertTimes(mathFunction, stringBuilder);
			stringBuilder.append(mathFunctionName);
			stringBuilder.append('(');
			for (int i = 0; i < mathFunction.size(); i++) {
				serialize(mathFunction.getArgument(i), stringBuilder);
				stringBuilder.append(',');
			}
			if (mathFunction.size() > 0) {
				stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			}
			stringBuilder.append(')');
		}
	}

	private static void serializeArgs(MathFunction mathFunction,
			StringBuilder stringBuilder, int[] order) {
		for (int i = 0; i < order.length; i++) {
			stringBuilder.append(i == 0 ? "((" : ",(");
			serialize(mathFunction.getArgument(order[i]), stringBuilder);
			stringBuilder.append(")");
		}
		stringBuilder.append(")");
	}

	private static void maybeInsertTimes(MathFunction mathFunction,
			StringBuilder stringBuilder) {
		MathSequence mathSequence = mathFunction.getParent();
		if (mathSequence != null && mathFunction.getParentIndex() > 0) {
			MathComponent mathComponent = mathSequence
					.getArgument(mathFunction.getParentIndex() - 1);
			if (mathComponent instanceof MathCharacter) {
				MathCharacter mathCharacter = (MathCharacter) mathComponent;
				if (mathCharacter.isCharacter() && mathCharacter
						.getUnicode() != MathCharacter.ZERO_SPACE) {
					stringBuilder.append("*");
				}
			}
			if (mathComponent instanceof MathFunction) {
				MathFunction mathCharacter = (MathFunction) mathComponent;
				if ("_".equals(mathCharacter.getName())) {
					stringBuilder.append("*");
				}
			}
		}
	}

	private static void serialize(MathArray mathArray,
			StringBuilder stringBuilder) {
		String open = mathArray.getOpen().getKey() + "";
		String close = mathArray.getClose().getKey() + "";
		String field = mathArray.getField().getKey() + "";
		String row = mathArray.getRow().getKey() + "";
		if ((Unicode.LFLOOR + "").equals(open)) {
			open = "floor(";
			close = ")";
		} else if ((Unicode.LCEIL + "").equals(open)) {
			open = "ceil(";
			close = ")";
		}
		if (mathArray.isMatrix()) {
			stringBuilder.append(open);
		}
		for (int i = 0; i < mathArray.rows(); i++) {
			stringBuilder.append(open);
			for (int j = 0; j < mathArray.columns(); j++) {
				serialize(mathArray.getArgument(i, j), stringBuilder);
				stringBuilder.append(field);
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - field.length());
			stringBuilder.append(close);
			stringBuilder.append(row);
		}
		stringBuilder.deleteCharAt(stringBuilder.length() - row.length());
		if (mathArray.isMatrix()) {
			stringBuilder.append(close);
		}
	}

	private static void serialize(MathSequence mathSequence,
			StringBuilder stringBuilder) {
		if (mathSequence == null) {
			return;
		}
		for (int i = 0; i < mathSequence.size(); i++) {
			serialize(mathSequence.getArgument(i), mathSequence, i,
					stringBuilder);
		}
	}

	/**
	 * @param formula
	 *            original formula
	 * @return formula after stringify + parse
	 */
	public static MathFormula reparse(MathFormula formula) {
		Parser parser = new Parser(formula.getMetaModel());
		MathFormula formula1 = null;
		try {
			formula1 = parser.parse(serialize(formula.getRootComponent()));

		} catch (ParseException e) {
			Log.warn("Problem parsing: " + formula.getRootComponent());
			e.printStackTrace();
		}
		return formula1 == null ? formula : formula1;
	}


}
