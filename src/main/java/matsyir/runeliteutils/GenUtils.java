package matsyir.runeliteutils;

import java.awt.Color;

public class GenUtils
{
	// return rgba color, use ints for rgb but float 0-1 for alpha
	public static Color getRgbA(int r, int g, int b, float a)
	{
		return new Color(r, g, b, (int)(a * 255f));
	}
}
