package matsyir.runeliteutils;

import java.awt.Color;
import lombok.extern.slf4j.Slf4j;
import static matsyir.runeliteutils.GenUtils.getRgbA;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.util.ColorUtil;

@Slf4j
public class InventoryTagConfigGenerator
{
	private static final int MAX_ID_TO_STOP = 35000; // update this from ItemID
	private static final boolean OVERWRITE_EXISTING_INV_TAGS = true;
	private static int createdConfigs = 0;

	private enum TagType
	{
		MELEE(getRgbA(255, 22, 33, 0.65f)),
		RANGE(getRgbA(22, 255, 33, 0.65f)),
		MAGE(getRgbA( 22, 33, 255, 0.65f)),

		MELEE_TANK(MELEE.color.darker().darker()),
		RANGE_TANK(RANGE.color.darker().darker()),
		MAGE_TANK(MAGE.color.darker().darker());

		static final String tagConfigKeyPrefix = "tag_";

		Color color;
		TagType(Color c)
		{
			this.color = c;
		}

		// Example Config: inventorytags.tag_23853={"color"\:"\#FF0060FF"}
		String getTagConfigValue()
		{
			//return "{\"color\"\\:\"\\#FF0060FF\"}"
			return "{\"color\":\"#" + ColorUtil.colorToAlphaHexCode(color) + "\"}";
		}
	}

	public static void generateInventoryTagsForEquipableItemsWithStats(ConfigManager configManager, ItemManager itemManager)
	{
		log.info("InventoryTagConfigGenerator.generateInventoryTagsForEquipableItemsWithStats - Process started");

		ItemStats itemStats;

		for (int i = 0; i <= MAX_ID_TO_STOP; i++)
		{
			if (createdConfigs % 100 == 0)
			{
				log.info("InventoryTagConfigGenerator.generateInventoryTagsForEquipableItemsWithStats: Created " + createdConfigs + " configs, processed " + i + "/" + MAX_ID_TO_STOP + " IDs");
			}

			itemStats = itemManager.getItemStats(i);

			if (itemStats != null && itemStats.isEquipable())
			{
				ItemEquipmentStats s = itemStats.getEquipment();
				if (s != null)
				{
					boolean hasOffensives = s.getAmagic() > 0 && s.getArange() > 0 && (s.getStr() > 0 || s.getAslash() > 0 || s.getAstab() > 0 || s.getAcrush() > 0);
					if (hasOffensives)
					{
						continue;
					}

					KitType slot = null;
					if (s.getSlot() >= 0 && s.getSlot() < KitType.values().length)
					{
						slot = KitType.values()[s.getSlot()];
					}

					int requiredMageScore = 4;
					int requiredRangeScore = 60;
					int requiredMeleeScore = 100;

					int requiredMageTankScore = 0;
					int requiredMeleeTankScore = 100;
					int requiredRangeTankScore = 1;
					if (slot == KitType.WEAPON)
					{
						requiredMageScore = 10;
					}
					else if (slot == KitType.HANDS || slot == KitType.SHIELD || slot == KitType.AMULET || slot == KitType.ARMS)
					{
						requiredMeleeScore = 20;
						requiredRangeScore = 12;
						requiredMageScore = 10;
					}

					int mageScore = (int)(s.getMdmg() * 3f) + s.getAmagic();
					if (mageScore >= requiredMageScore && s.getDrange() <= 0)
					{
						createInventoryTagConfig(configManager, i, TagType.MAGE);
						continue;
					}

					// rcb = 90 | msb = 69 | rune knife = 121 | crystal bow = 412
					int rangeScore = (s.getRstr() * 4) + s.getArange();
					if (rangeScore >= requiredRangeScore)
					{
						createInventoryTagConfig(configManager, i, TagType.RANGE);
						continue;
					}

					// rune scim = 138
					int meleeScore = (s.getStr() * 2) + s.getAslash() + s.getAstab() + s.getAcrush();
					if (meleeScore >= requiredMeleeScore)
					{
						createInventoryTagConfig(configManager, i, TagType.MELEE);
						continue;
					}

					// still not detected via offensive stats? Check for tank items
					// tag tank items as mage if their magic defence is >= 1
					int mageTankScore = s.getDmagic();
					if (mageTankScore >= requiredMageTankScore && s.getAmagic() >= 0)
					{
						createInventoryTagConfig(configManager, i, TagType.MAGE_TANK);
						continue;
					}

					// tag tank items as melee if their magic defence is <= 0
					int meleeTankScore = s.getDslash() + s.getDstab() + s.getDcrush() + s.getDrange();
					if (meleeTankScore >= requiredMeleeTankScore && s.getDmagic() <= 0)
					{
						createInventoryTagConfig(configManager, i, TagType.MELEE_TANK);
						continue;
					}

					// tag tank items as range if their magic defence is > 0
					int rangeTankScore = s.getDmagic();
					if (rangeTankScore >= requiredRangeTankScore && meleeTankScore >= 5)
					{
						createInventoryTagConfig(configManager, i, TagType.RANGE_TANK);
						continue;
					}
				}
			}
		}
		log.info("InventoryTagConfigGenerator.generateInventoryTagsForEquipableItemsWithStats: Done generating configs. Created " + createdConfigs + " configs.");
		log.info("InventoryTagConfigGenerator.generateInventoryTagsForEquipableItemsWithStats: Process complete");
	}

	private static void createInventoryTagConfig(ConfigManager configManager, int itemId, TagType type)
	{
		String existingTag;
		if (OVERWRITE_EXISTING_INV_TAGS
			|| (existingTag = configManager.getConfiguration("inventorytags", TagType.tagConfigKeyPrefix + itemId)) == null
			|| existingTag.isEmpty())
		{
			configManager.setConfiguration("inventorytags", TagType.tagConfigKeyPrefix + itemId, type.getTagConfigValue());
			createdConfigs++;
		}
	}

}
