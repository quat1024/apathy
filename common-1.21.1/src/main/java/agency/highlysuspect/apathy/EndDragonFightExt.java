package agency.highlysuspect.apathy;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class EndDragonFightExt extends SavedData {
	public static final Factory<EndDragonFightExt> FACTORY = new Factory<>(
			EndDragonFightExt::new, // Default constructor
			EndDragonFightExt::new, // From NBT
			null
	);
	public static final int NOT_RUNNING = -100;
	
	private int gatewayTimer;
	
	public EndDragonFightExt() {
		gatewayTimer = NOT_RUNNING;
	}

	public EndDragonFightExt(CompoundTag tag, HolderLookup.Provider provider) {
		gatewayTimer = tag.contains("gateway-timer") ? tag.getInt("gateway-timer") : NOT_RUNNING;
	}
	
	public static EndDragonFightExt get(ServerLevel slevel) {
		return slevel.getDataStorage().computeIfAbsent(
			FACTORY,
			"apathy_dragonfight_ext"
		);
	}
	
	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
		tag.putInt("gateway-timer", gatewayTimer);
		return tag;
	}
	
	public boolean gatewayTimerRunning() {
		return gatewayTimer != NOT_RUNNING;
	}
	
	public boolean tickTimer() {
		if(gatewayTimer == NOT_RUNNING) return false;
		
		gatewayTimer--;
		setDirty();
		
		if(gatewayTimer <= 0) {
			gatewayTimer = NOT_RUNNING;
			return true;
		}
		
		return false;
	}
	
	public void setGatewayTimer(int gatewayTimer) {
		this.gatewayTimer = gatewayTimer;
		setDirty();
	}
}
