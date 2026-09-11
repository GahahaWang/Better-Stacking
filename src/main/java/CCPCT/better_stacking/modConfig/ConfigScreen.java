package CCPCT.better_stacking.modConfig;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ConfigScreen {

    private static final Component SUFFIX_TOOLTIP = text("""
            0: no suffix
            1: engineer suffix, k M G T...
            2: mc suffix: s/stack, sb/shulker box, sbc/shulker box double chest""");

    private ConfigScreen() {
    }

    public static Screen getConfigScreen(Screen parent) {
        ModConfig config = ModConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(text("Better Stacking Config"))
                .setSavingRunnable(ModConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory generalTab = builder.getOrCreateCategory(text("general"));
        ConfigCategory itemTab = builder.getOrCreateCategory(text("item"));
        ConfigCategory entityTab = builder.getOrCreateCategory(text("entity"));
        ConfigCategory xpTab = builder.getOrCreateCategory(text("xp"));


        // === GENERAL TAB ===
        generalTab.addEntry(entryBuilder.startBooleanToggle(text("Enable Mod"), config.modEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.get().modEnabled = newValue)
                .build());

        generalTab.addEntry(entryBuilder.startIntField(text("Entity update time interval"), config.entityUpdateTimeInterval)
                .setDefaultValue(20)
                .setTooltip(text("Ticks between entity updates.\nHigher = less frequent update, cheaper"))
                .setMin(1).setMax(1200)
                .setSaveConsumer(newValue -> ModConfig.get().entityUpdateTimeInterval = newValue)
                .build());

        generalTab.addEntry(entryBuilder.startBooleanToggle(text("Label render through blocks"), config.renderThroughBlocks)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> ModConfig.get().renderThroughBlocks = newValue)
                .build());

        generalTab.addEntry(entryBuilder.startAlphaColorField(text("Label Color"), config.labelColour)
                .setDefaultValue(0xA0FFFF00)
                .setAlphaMode(true)
                .setSaveConsumer(newValue -> ModConfig.get().labelColour = newValue)
                .build());

        generalTab.addEntry(entryBuilder.startAlphaColorField(text("Label Background Color"), config.labelBgColour)
                .setDefaultValue(0x67676767)
                .setAlphaMode(true)
                .setSaveConsumer(newValue -> ModConfig.get().labelBgColour = newValue)
                .build());

        generalTab.addEntry(entryBuilder.startFloatField(text("Label Size"), config.labelSize)
                .setDefaultValue(1.0f)
                .setMin(0.1f).setMax(10f)
                .setSaveConsumer(newValue -> ModConfig.get().labelSize = newValue)
                .build());

        generalTab.addEntry(entryBuilder.startFloatField(text("Label Offset"), config.labelOffset)
                .setTooltip(text("How high above the head the label sits, in blocks.\n0 = head"))
                .setDefaultValue(0.5f)
                .setMin(-64f).setMax(64f)
                .setSaveConsumer(newValue -> ModConfig.get().labelOffset = newValue)
                .build());

        generalTab.addEntry(entryBuilder.startIntField(text("Label Render Distance"), config.labelRenderDistance)
                .setTooltip(text("Blocks. Labels further away are not drawn at all.\n0 = no limit"))
                .setDefaultValue(64)
                .setMin(0).setMax(512)
                .setSaveConsumer(newValue -> ModConfig.get().labelRenderDistance = newValue)
                .build());


        // === ITEM TAB ===
        itemTab.addEntry(entryBuilder.startBooleanToggle(text("Item General"), config.itemGeneral)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> ModConfig.get().itemGeneral = newValue)
                .build());

        itemTab.addEntry(entryBuilder.startIntField(text("Minimum Item Entities"), config.itemCount)
                .setTooltip(text("Item entities in one block needed before they stack"))
                .setDefaultValue(1)
                .setMin(1).setMax(1000)
                .setSaveConsumer(newValue -> ModConfig.get().itemCount = newValue)
                .build());

        itemTab.addEntry(entryBuilder.startBooleanToggle(text("Show Item Label"), config.itemShowLabel)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.get().itemShowLabel = newValue)
                .build());

        itemTab.addEntry(entryBuilder.startBooleanToggle(text("Show Item Name in Label"), config.itemLabelShowName)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.get().itemLabelShowName = newValue)
                .build());

        itemTab.addEntry(entryBuilder.startIntField(text("Show Suffix for large amount of item"), config.itemSuffixMode)
                .setDefaultValue(0)
                .setTooltip(SUFFIX_TOOLTIP)
                .setMin(0).setMax(2)
                .setSaveConsumer(newValue -> ModConfig.get().itemSuffixMode = newValue)
                .build());


        // === ENTITY TAB ===
        entityTab.addEntry(entryBuilder.startBooleanToggle(text("Entity General"), config.entityGeneral)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> ModConfig.get().entityGeneral = newValue)
                .build());

        entityTab.addEntry(entryBuilder.startIntField(text("Minimum Entity"), config.entityCount)
                .setTooltip(text("Entities in one block needed before they stack"))
                .setDefaultValue(5)
                .setMin(1).setMax(1000)
                .setSaveConsumer(newValue -> ModConfig.get().entityCount = newValue)
                .build());

        entityTab.addEntry(entryBuilder.startBooleanToggle(text("Show Entity Label"), config.entityShowLabel)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.get().entityShowLabel = newValue)
                .build());

        entityTab.addEntry(entryBuilder.startBooleanToggle(text("Show Entity Type in Label"), config.entityLabelShowName)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.get().entityLabelShowName = newValue)
                .build());

        entityTab.addEntry(entryBuilder.startIntField(text("Show Suffix for large amount of entity"), config.entitySuffixMode)
                .setDefaultValue(0)
                .setTooltip(SUFFIX_TOOLTIP)
                .setMin(0).setMax(2)
                .setSaveConsumer(newValue -> ModConfig.get().entitySuffixMode = newValue)
                .build());


        // === XP TAB ===
        xpTab.addEntry(entryBuilder.startBooleanToggle(text("XP General"), config.xpGeneral)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> ModConfig.get().xpGeneral = newValue)
                .build());

        xpTab.addEntry(entryBuilder.startIntField(text("Minimum XP Orbs"), config.xpCount)
                .setTooltip(text("Orbs in one block needed before they stack"))
                .setDefaultValue(1)
                .setMin(1).setMax(1000)
                .setSaveConsumer(newValue -> ModConfig.get().xpCount = newValue)
                .build());

        xpTab.addEntry(entryBuilder.startBooleanToggle(text("Show XP Label"), config.xpShowLabel)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.get().xpShowLabel = newValue)
                .build());

        xpTab.addEntry(entryBuilder.startIntField(text("Show Suffix for large amount of xp"), config.xpSuffixMode)
                .setDefaultValue(0)
                .setTooltip(SUFFIX_TOOLTIP)
                .setMin(0).setMax(2)
                .setSaveConsumer(newValue -> ModConfig.get().xpSuffixMode = newValue)
                .build());

        return builder.build();
    }

    static Component text(String str) {
        return Component.literal(str);
    }
}
