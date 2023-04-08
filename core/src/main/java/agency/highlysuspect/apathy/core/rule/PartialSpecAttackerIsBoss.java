package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import com.google.gson.JsonObject;

/**
 * TODO: consider deprecating for the same reason allow_if/deny_if were deprecated?
 */
public class PartialSpecAttackerIsBoss implements Spec<Partial, PartialSpecAttackerIsBoss> {
	private PartialSpecAttackerIsBoss() {}
	public static final PartialSpecAttackerIsBoss INSTANCE = new PartialSpecAttackerIsBoss();
	
	public static final AttackerTag BOSS_TAG = Apathy.instance.parseAttackerTag(Apathy.MODID + ":bosses");
	
	@Override
	public Partial build() {
		return (attacker, defender) -> BOSS_TAG.apathy$is(attacker);
	}
	
	@Override
	public JsonSerializer<PartialSpecAttackerIsBoss> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecAttackerIsBoss> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAttackerIsBoss thing, JsonObject json) {
			//Nothing to write
		}
		
		@Override
		public PartialSpecAttackerIsBoss read(JsonObject json) {
			return PartialSpecAttackerIsBoss.INSTANCE;
		}
	}
}
