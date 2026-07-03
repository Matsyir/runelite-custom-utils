package matsyir.runeliteutils;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "RuneliteUtils"
)
public class RuneliteUtilsPlugin extends Plugin
{
	@Inject
	private Client client;
	
	@Inject
	private ItemManager itemManager;

	@Inject
	private RuneliteUtilsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("RuneliteUtils - startUp() called");

		clientThread.invokeLater(() ->
		{
			InventoryTagConfigGenerator.generateInventoryTagsForEquipableItemsWithStats(configManager, itemManager);
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("RuneliteUtils - shutDown() called");
	}

	@Provides
	RuneliteUtilsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneliteUtilsConfig.class);
	}
}
