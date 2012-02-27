package geogebra.common.euclidian;

import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.main.AbstractApplication;

/**
 * Interface for class responsible for drawing LaTeX
 */
public interface DrawEquationInterface {

	/**
	 * @param app application
	 * @param b true to use Java fonts
	 */
	void setUseJavaFontsForLaTeX(AbstractApplication app, boolean b);
	
	/**
	 * @param app application
	 * @param geo geo
	 * @param g2 graphics
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param text text
	 * @param font font
	 * @param serif true for serif
	 * @param fgColor foreground color
	 * @param bgColor background color
	 * @param useCache true to cache
	 * @return dimensions of result
	 */
	public geogebra.common.awt.Dimension drawEquation(AbstractApplication app,
			GeoElement geo, geogebra.common.awt.Graphics2D g2, int x, int y, String text,
			geogebra.common.awt.Font font, boolean serif, geogebra.common.awt.Color fgColor, geogebra.common.awt.Color bgColor,
			boolean useCache); 
}
