package org.geogebra.common.kernel.barycentric;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.arithmetic.Equation;
import org.geogebra.common.kernel.commands.CommandProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.implicit.AlgoDependentImplicitPoly;
import org.geogebra.common.kernel.implicit.GeoImplicit;
import org.geogebra.common.main.MyError;

/**
 * TriangleCurve[Point,Point,Point,Equation in A,B,C]
 * 
 * @author Zbynek
 *
 */
public class CmdTriangleCurve extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdTriangleCurve(Kernel kernel) {
		super(kernel);
	}

	@Override
	final public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		switch (n) {
		case 4:
			GeoNumeric ta = null, tb = null, tc = null;
			arg = new GeoElement[4];
			for (int i = 0; i < 3; i++) {
				arg[i] = resArg(c, i, new EvalInfo(false));
			}
			ta = new GeoNumeric(cons);
			tb = new GeoNumeric(cons);
			tc = new GeoNumeric(cons);
			cons.addLocalVariable("A", ta);
			cons.addLocalVariable("B", tb);
			cons.addLocalVariable("C", tc);
			if (!(c.getArgument(3).unwrap() instanceof Equation)) {
				clearLocal();
				throw argErr(app, c, c.getArgument(3));
			}
			// need to allow constants as A+B=C does not include x
			boolean oldMacroMode = cons.isSuppressLabelsActive();
			cons.setSuppressLabelCreation(true);
			arg[3] = kernelA.getAlgebraProcessor().processEquation(
					(Equation) c.getArgument(3).unwrap(), c.getArgument(3),
					true)[0];
			cons.setSuppressLabelCreation(oldMacroMode);
			if ((ok[0] = arg[0] instanceof GeoPoint)
					&& (ok[1] = arg[1] instanceof GeoPoint)
					&& (ok[2] = arg[2] instanceof GeoPoint)
					&& (ok[3] = arg[3].isGeoImplicitCurve()
							&& arg[3].getParentAlgorithm() instanceof AlgoDependentImplicitPoly)) {

				AlgoTriangleCurve algo = new AlgoTriangleCurve(cons,
						c.getLabel(), (GeoPoint) arg[0], (GeoPoint) arg[1],
						(GeoPoint) arg[2], (GeoImplicit) arg[3], ta, tb, tc);

				GeoElement[] ret = { algo.getResult() };
				clearLocal();
				return ret;

			}
			clearLocal();
			throw argErr(app, c, getBadArg(ok, arg));

		default:
			clearLocal();
			throw argNumErr(app, c, n);
		}
	}

	private void clearLocal() {
		cons.removeLocalVariable("A");
		cons.removeLocalVariable("B");
		cons.removeLocalVariable("C");
	}

	private final GeoElement resArg(Command c, int pos, EvalInfo info)
			throws MyError {
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		// resolve arguments to get GeoElements

		// resolve variables in argument expression
		c.getArgument(pos).resolveVariables(info.withLabels(false));

		// resolve i-th argument and get GeoElements
		// use only first resolved argument object for result
		GeoElement result = resArg(c.getArgument(pos), info)[0];

		cons.setSuppressLabelCreation(oldMacroMode);
		return result;
	}

}
