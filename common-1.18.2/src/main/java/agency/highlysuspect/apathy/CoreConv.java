package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.core.wrapper.VecThree;
import net.minecraft.world.Difficulty;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.stream.Collectors;

public class CoreConv {
	public static ApathyDifficulty toApathyDifficulty(Difficulty diff) {
		return switch(diff) {
			case PEACEFUL -> ApathyDifficulty.PEACEFUL;
			case EASY -> ApathyDifficulty.EASY;
			case NORMAL -> ApathyDifficulty.NORMAL;
			case HARD -> ApathyDifficulty.HARD;
		};
	}
	
	public static Set<ApathyDifficulty> toApathyDifficulty(Set<Difficulty> diffSet) {
		return diffSet.stream().map(CoreConv::toApathyDifficulty).collect(Collectors.toSet());
	}
	
	public static Difficulty fromApathyDifficulty(ApathyDifficulty diff) {
		return switch(diff) {
			case PEACEFUL -> Difficulty.PEACEFUL;
			case EASY -> Difficulty.EASY;
			case NORMAL -> Difficulty.NORMAL;
			case HARD -> Difficulty.HARD;
		};
	}
	
	public static VecThree toVecThree(Vec3 vec3) {
		return new VecThree() {
			@Override
			public double x() {
				return vec3.x;
			}
			
			@Override
			public double y() {
				return vec3.y;
			}
			
			@Override
			public double z() {
				return vec3.z;
			}
		};
	}
	
	public static Vec3 fromVecThree(VecThree three) {
		return new Vec3(three.x(), three.y(), three.z());
	}
}
