package matsyir.runeliteutils;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RuneliteUtilsTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RuneliteUtilsPlugin.class);
		RuneLite.main(args);
	}
}