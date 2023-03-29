package agency.highlysuspect.apathy;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

public class CoolNbtUtil {
	public static ListTag writeVec3(Vec3 yes) {
		ListTag asdf = new ListTag();
		asdf.add(DoubleTag.valueOf(yes.x));
		asdf.add(DoubleTag.valueOf(yes.y));
		asdf.add(DoubleTag.valueOf(yes.z));
		return asdf;
	}
	
	public static Vec3 readVec3(ListTag asdf) {
		return new Vec3(asdf.getDouble(0), asdf.getDouble(1), asdf.getDouble(2));
	}
	
	public static final int VEC3_LIST_ID = DoubleTag.valueOf(69420).getId();
}
