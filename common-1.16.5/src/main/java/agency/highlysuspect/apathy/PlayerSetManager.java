package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.coreplusminecraft.PlayerSetManagerGuts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class PlayerSetManager extends SavedData {
	public final PlayerSetManagerGuts guts;
	
	public PlayerSetManager() {
		super("apathy-player-sets");
		this.guts = new PlayerSetManagerGuts(this::setDirty);
	}
	
	@Override
	public void load(CompoundTag tag) {
		guts.load(tag);
	}
	
	@Override
	public CompoundTag save(CompoundTag tag) {
		return guts.save(tag);
	}
}
