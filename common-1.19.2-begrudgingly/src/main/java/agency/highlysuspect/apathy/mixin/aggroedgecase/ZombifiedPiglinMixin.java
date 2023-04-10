package agency.highlysuspect.apathy.mixin.aggroedgecase;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreGenOptions;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ZombifiedPiglin.class)
public class ZombifiedPiglinMixin {
	/**
	 * Bro i am so fucking tired bro.
	 * I need to target the inside of the final forEach lambda but i dont wanna figure out how to do it.
	 * Time to break out the old favorite COPY PASTE OVERWRITE!!!!!!!!!! Wow its almost like im a five year old!!!!!
	 * 
	 * @author quat SORRY
	 */
	@SuppressWarnings({"CodeBlock2Expr", "ConstantConditions"}) //copying vanilla decompiler output
	@Overwrite
	private void alertOthers() {
		ZombifiedPiglin thiss = (ZombifiedPiglin) (Object) this;
		Attacker thisExt = (Attacker) this;
		
		Level level = thiss.level;
		if(!Apathy.instance.generalCfg.get(CoreGenOptions.angryPiggies).contains(VerConv.toApathyDifficulty(level.getDifficulty()))) {
			//Oh wow, an overwrite-head-cancel! Even better than an inject-head-cancel
			return;
		}
		
		double $$0 = thiss.getAttributeValue(Attributes.FOLLOW_RANGE);
		AABB $$1 = AABB.unitCubeFromLowerCorner(thiss.position()).inflate($$0, 10.0D, $$0);
		thiss.level.getEntitiesOfClass(ZombifiedPiglin.class, $$1, EntitySelector.NO_SPECTATORS).stream().filter(($$0x) -> {
			return $$0x != thiss;
		}).filter(($$0x) -> {
			return $$0x.getTarget() == null;
		}).filter(($$0x) -> {
			return !$$0x.isAlliedTo(thiss.getTarget());
		}).forEach(($$0x) -> {
			//RIGHT HERE
			//i need to spread my provocation time to the entity thats about to give chase 
			Attacker otherExt = (Attacker) $$0x;
			otherExt.apathy$setProvocationTime(thisExt.apathy$getProvocationTime());
			//back 2 normal
			
			$$0x.setTarget(thiss.getTarget());
		});
	}
}
