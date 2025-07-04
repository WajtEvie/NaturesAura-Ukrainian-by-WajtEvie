package de.ellpeck.naturesaura.blocks;

import de.ellpeck.naturesaura.Helper;
import de.ellpeck.naturesaura.api.misc.ILevelData;
import de.ellpeck.naturesaura.api.render.IVisualizable;
import de.ellpeck.naturesaura.blocks.tiles.BlockEntityPickupStopper;
import de.ellpeck.naturesaura.data.BlockStateGenerator;
import de.ellpeck.naturesaura.misc.LevelData;
import de.ellpeck.naturesaura.packet.PacketHandler;
import de.ellpeck.naturesaura.packet.PacketParticles;
import de.ellpeck.naturesaura.reg.ICustomBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public class BlockPickupStopper extends BlockContainerImpl implements IVisualizable, ICustomBlockState {

    public BlockPickupStopper() {
        super("pickup_stopper", BlockEntityPickupStopper.class, Properties.of().strength(2F).sound(SoundType.STONE));

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPickup(ItemEntityPickupEvent.Pre event) {
        var player = event.getPlayer();
        if (player != null && !player.isShiftKeyDown()) {
            var item = event.getItemEntity();
            var data = (LevelData) ILevelData.getLevelData(item.level());
            for (var tile : data.pickupStoppers) {
                var radius = tile.getRadius();
                if (radius <= 0F)
                    continue;
                var stopperPos = tile.getBlockPos();
                if (!new AABB(stopperPos).inflate(radius).intersects(item.getBoundingBox()))
                    continue;

                event.setCanPickup(TriState.FALSE);

                if (item.level().getGameTime() % 3 == 0)
                    PacketHandler.sendToAllAround(item.level(), item.blockPosition(), 32,
                        new PacketParticles((float) item.getX(), (float) item.getY(), (float) item.getZ(), PacketParticles.Type.PICKUP_STOPPER));
                break;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getVisualizationBounds(Level level, BlockPos pos) {
        var tile = level.getBlockEntity(pos);
        if (tile instanceof BlockEntityPickupStopper) {
            double radius = ((BlockEntityPickupStopper) tile).getRadius();
            if (radius > 0)
                return new AABB(pos).inflate(radius);
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getVisualizationColor(Level level, BlockPos pos) {
        return 0xf4aa42;
    }

    @Override
    public void generateCustomBlockState(BlockStateGenerator generator) {
        generator.simpleBlock(this, generator.models().cubeBottomTop(this.getBaseName(),
            generator.modLoc("block/" + this.getBaseName()),
            generator.modLoc("block/" + this.getBaseName() + "_top"),
            generator.modLoc("block/" + this.getBaseName() + "_top")));
    }

}
