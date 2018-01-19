package twilightforest.structures.courtyard;

import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.structures.StructureTFComponent;
import twilightforest.structures.StructureTFComponentTemplate;

import java.util.Random;

public class ComponentNagaCourtyardHedgeCap extends StructureTFComponentTemplate {
    private static final ResourceLocation HEDGE = new ResourceLocation(TwilightForestMod.ID, "courtyard/hedge_end");
    private static final ResourceLocation HEDGE_BIG = new ResourceLocation(TwilightForestMod.ID, "courtyard/hedge_end_big");

    @SuppressWarnings({"WeakerAccess", "unused"})
    public ComponentNagaCourtyardHedgeCap() {
        super();
    }

    @SuppressWarnings("WeakerAccess")
    public ComponentNagaCourtyardHedgeCap(TFFeature feature, int i, int x, int y, int z, Rotation rotation) {
        super(feature, i, x, y, z, rotation);
    }

    @Override
    public boolean addComponentParts(World worldIn, Random random, StructureBoundingBox structureBoundingBoxIn) {
        MinecraftServer server = worldIn.getMinecraftServer();
        TemplateManager templateManager = worldIn.getSaveHandler().getStructureTemplateManager();

        TEMPLATE = templateManager.getTemplate(server, HEDGE);

        this.setTemplatePositionFromRotation();
        this.setBoundingBoxFromTemplate();

        PlacementSettings placementSettings = new PlacementSettings()
                .setRotation(this.rotation)
                .setReplacedBlock(Blocks.STRUCTURE_VOID)
                .setBoundingBox(this.boundingBox);

        TEMPLATE.addBlocksToWorld(worldIn, templatePosition, new CourtyardStairsTemplateProcessor(templatePosition, placementSettings), placementSettings, 2);

        Template templateBig = templateManager.getTemplate(server, HEDGE_BIG);
        templateBig.addBlocksToWorld(worldIn, templatePosition, placementSettings.setIntegrity(ComponentNagaCourtyardMain.HEDGE_FLOOF));

        this.setDebugCorners(worldIn);

        this.setDebugEntity(worldIn, templatePosition, this.getClass().getName() + " : " + this.componentType);

        System.out.println("\n" + this.componentType + " at " + this.templatePosition + ":\n");

        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            System.out.println(stackTraceElement);
        }

        return true;
    }
}
