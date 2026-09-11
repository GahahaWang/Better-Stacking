package CCPCT.better_stacking.util;

import CCPCT.better_stacking.modConfig.ModConfig;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

public final class EntityClusterManager {

    static final int CATEGORY_ITEM = 0;
    static final int CATEGORY_XP = 1;
    static final int CATEGORY_MOB = 2;

    /** Stands in as the "type" of experience orbs, which have no per-variant type object. */
    private static final Object XP_TYPE = ExperienceOrb.class;

    private static final int MAX_POOLED_CLUSTERS = 4096;
    private static final int MAX_RETAINED_MEMBERS = 1024;

    private static final IntOpenHashSet culledIds = new IntOpenHashSet();
    private static final ObjectArrayList<ClusterEntry> activeClusters = new ObjectArrayList<>();

    private static final Object2ObjectOpenHashMap<ClusterKey, Cluster> buckets = new Object2ObjectOpenHashMap<>();
    private static final ObjectArrayList<Cluster> clusterPool = new ObjectArrayList<>();
    /** Re-used for map look-ups so that only genuinely new buckets allocate a key. */
    private static final ClusterKey probe = new ClusterKey();

    /** Mirrors {@code !culledIds.isEmpty()}; read once per entity per frame, so it stays a plain flag. */
    private static boolean culling;

    private static int ticksUntilUpdate;
    private static WeakReference<ClientLevel> lastLevel = new WeakReference<>(null);

    private EntityClusterManager() {
    }

    public static List<ClusterEntry> getActiveClusters() {
        return activeClusters;
    }

    /** Hot path: called for every entity, every frame, from the render dispatcher mixin. */
    public static boolean shouldSkipRender(Entity entity) {
        return culling && culledIds.contains(entity.getId());
    }

    /** Forces a rebuild on the next client tick, e.g. right after the config changed. */
    public static void invalidate() {
        ticksUntilUpdate = 0;
    }

    public static void tick(Minecraft client) {
        ClientLevel level = client.level;

        if (level == null) {
            if (lastLevel.get() != null) {
                lastLevel = new WeakReference<>(null);
                clear();
            }
            return;
        }

        if (lastLevel.get() != level) {
            // Dimension change or reconnect: entity ids now belong to a different world.
            lastLevel = new WeakReference<>(level);
            clear();
            ticksUntilUpdate = 0;
        }

        if (--ticksUntilUpdate > 0) return;
        ticksUntilUpdate = Math.max(1, ModConfig.get().entityUpdateTimeInterval);
        updateClusterData(client, level);
    }

    private static void clear() {
        culledIds.clear();
        activeClusters.clear();
        culling = false;
    }

    private static void updateClusterData(Minecraft client, ClientLevel level) {
        clear();

        ModConfig config = ModConfig.get();
        if (!config.modEnabled) return;

        final boolean items = config.itemGeneral;
        final boolean xp = config.xpGeneral;
        final boolean mobs = config.entityGeneral;
        if (!items && !xp && !mobs) return;

        final Entity self = client.player;

        for (Entity entity : level.entitiesForRendering()) {
            // Players never reach a bucket anyway (they are not Mobs), but the camera owner is worth
            // an explicit guard. Anything ridden or riding is skipped too: its partner stays visible
            // regardless, so hiding only one half of the pair looks broken.
            if (entity == self || entity.isRemoved() || entity.isVehicle() || entity.isPassenger()) continue;

            final int category;
            final int value;
            final Object type;
            String customName = null;
            boolean baby = false;

            if (entity instanceof ItemEntity item) {
                if (!items) continue;
                ItemStack stack = item.getItem();
                category = CATEGORY_ITEM;
                value = stack.getCount();
                type = stack.getItem();
                // Renamed stacks do not merge in an inventory, so they must not merge in a label either.
                Component given = stack.get(DataComponents.CUSTOM_NAME);
                if (given != null) customName = given.getString();
            } else if (entity instanceof ExperienceOrb orb) {
                if (!xp) continue;
                category = CATEGORY_XP;
                value = orb.getValue();
                type = XP_TYPE;
            } else if (entity instanceof Mob mob) {
                if (!mobs) continue;
                category = CATEGORY_MOB;
                value = 1;
                type = mob.getType();
                baby = mob.isBaby();
                if (mob.hasCustomName()) {
                    Component given = mob.getCustomName();
                    if (given != null) customName = given.getString();
                }
            } else {
                continue;
            }

            long pos = BlockPos.asLong(Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ()));
            probe.set(pos, category, type, customName, baby);

            Cluster cluster = buckets.get(probe);
            if (cluster == null) {
                cluster = obtainCluster();
                cluster.start(entity, category, value);
                buckets.put(probe.copy(), cluster);
            } else {
                cluster.entityCount++;
                cluster.value += value;
                cluster.members.add(entity.getId());
            }
        }

        for (Cluster cluster : buckets.values()) {
            if (cluster.entityCount >= minimumCount(config, cluster.category)) {
                culledIds.addAll(cluster.members);
                Component label = buildLabel(config, cluster);
                if (label != null) activeClusters.add(new ClusterEntry(cluster.leader, label));
            }
            releaseCluster(cluster);
        }
        buckets.clear();

        culling = !culledIds.isEmpty();
    }

    private static int minimumCount(ModConfig config, int category) {
        return switch (category) {
            case CATEGORY_ITEM -> Math.max(1, config.itemCount);
            case CATEGORY_XP -> Math.max(1, config.xpCount);
            default -> Math.max(1, config.entityCount);
        };
    }

    /** @return the finished label, or {@code null} when this category should not be labelled. */
    private static Component buildLabel(ModConfig config, Cluster cluster) {
        final boolean show;
        final boolean withName;
        final int suffixMode;

        switch (cluster.category) {
            case CATEGORY_ITEM -> {
                show = config.itemShowLabel;
                withName = config.itemLabelShowName;
                suffixMode = config.itemSuffixMode;
            }
            case CATEGORY_XP -> {
                show = config.xpShowLabel;
                withName = false;
                suffixMode = config.xpSuffixMode;
            }
            default -> {
                show = config.entityShowLabel;
                withName = config.entityLabelShowName;
                suffixMode = config.entitySuffixMode;
            }
        }
        if (!show) return null;

        String count = CountFormat.format(cluster.value, suffixMode);
        if (!withName) return Component.literal("x" + count);

        StringBuilder text = new StringBuilder(32);
        if (cluster.leader instanceof LivingEntity living && living.isBaby()) text.append("Baby ");
        text.append(displayName(cluster.leader)).append(" x").append(count);
        return Component.literal(text.toString());
    }

    private static String displayName(Entity leader) {
        if (leader instanceof ItemEntity item) return item.getItem().getHoverName().getString();
        return leader.getName().getString();
    }

    private static Cluster obtainCluster() {
        return clusterPool.isEmpty() ? new Cluster() : clusterPool.pop();
    }

    private static void releaseCluster(Cluster cluster) {
        cluster.leader = null;
        cluster.members.clear();
        // One giant pile should not pin its backing array for the rest of the session.
        if (cluster.members.elements().length > MAX_RETAINED_MEMBERS) cluster.members.trim(MAX_RETAINED_MEMBERS);
        if (clusterPool.size() < MAX_POOLED_CLUSTERS) clusterPool.push(cluster);
    }

    /** A leader plus the label that stands in for everything hidden behind it. */
    public record ClusterEntry(Entity leader, Component label) {
    }

    /** Mutable bucket, recycled through {@link #clusterPool}. */
    private static final class Cluster {
        Entity leader;
        int category;
        int entityCount;
        int value;
        /** Ids of every member except the leader, i.e. exactly what gets culled. */
        final IntArrayList members = new IntArrayList();

        void start(Entity leader, int category, int value) {
            this.leader = leader;
            this.category = category;
            this.entityCount = 1;
            this.value = value;
        }
    }

    /**
     * Identity of a bucket. Mutable on purpose: {@link #probe} is re-used for look-ups and only
     * copied when a bucket is really created, which keeps the scan allocation free.
     */
    private static final class ClusterKey {
        private long pos;
        private int category;
        private Object type;
        private String customName;
        private boolean baby;
        private int hash;

        void set(long pos, int category, Object type, String customName, boolean baby) {
            this.pos = pos;
            this.category = category;
            this.type = type;
            this.customName = customName;
            this.baby = baby;

            int h = Long.hashCode(pos);
            h = h * 31 + category;
            h = h * 31 + System.identityHashCode(type);
            h = h * 31 + (customName == null ? 0 : customName.hashCode());
            this.hash = h * 31 + (baby ? 1 : 0);
        }

        ClusterKey copy() {
            ClusterKey copy = new ClusterKey();
            copy.set(pos, category, type, customName, baby);
            return copy;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClusterKey other)) return false;
            return hash == other.hash
                    && pos == other.pos
                    && category == other.category
                    && type == other.type
                    && baby == other.baby
                    && Objects.equals(customName, other.customName);
        }
    }
}
