package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.coreplusminecraft.PlayerSetManagerGuts;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class PlayerSetManager extends SavedData {
	public static final Factory<PlayerSetManager> FACTORY = new Factory<>(
		PlayerSetManager::new, // Default constructor
		PlayerSetManager::new, // From NBT
		null
	);
	public final PlayerSetManagerGuts guts;
	
	public PlayerSetManager() {
		this.guts = new PlayerSetManagerGuts(this::setDirty);
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
		return guts.save(tag);
	}

	public PlayerSetManager(CompoundTag tag, HolderLookup.Provider provider) {
		this();
		guts.load(tag);
	}
}
