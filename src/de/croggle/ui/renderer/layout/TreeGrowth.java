package de.croggle.ui.renderer.layout;

/**
 * Simple enumeration to represent directions in which a certain two-dimensional
 * process progresses. This can be for example the direction a tree grows or the
 * direction a bitmap is drawn, relative to a fixed axis coordinate system.
 * 
 */
public enum TreeGrowth {
	/**
	 * Enum value representing tree growth from negative values to positive
	 * values, i.e. growth following the axis' direction.
	 */
	NEG_POS,
	/**
	 * Enum value representing tree growth from positive values to negative
	 * values, i.e. growth inverse to the axis
	 */
	POS_NEG
}
