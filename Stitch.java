/**
 * CSCI E-10b - Final Project
 * Johanna Bodnyk | May 9, 2014
 *
 * This enum class maps stitch abbrevations used in knitting instructions 
 * to the corresponding stitch symbols used in knitting charts,
 * and prose stitch names (used for mouseover text in chart).
 *
 * (This is a limited menu of stitch options for the purposes of simplification.)
 */

public enum Stitch
{
    K (" ", "knit"),
    K2TOG ("/", "knit two together"),
    P ("*", "purl"),
    SKP ("\\", "slip, knit, pass"),
    YO ("O", "yarn over");

    private final String symbol;
    private final String stitchName;

    Stitch(String symbol, String stitchName)
    {
        this.symbol = symbol;
        this.stitchName = stitchName;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public String getStitchName()
    {
        return stitchName;
    }
}