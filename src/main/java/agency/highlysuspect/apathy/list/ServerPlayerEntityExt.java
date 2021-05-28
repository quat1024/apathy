package agency.highlysuspect.apathy.list;

import java.util.Collection;

public interface ServerPlayerEntityExt {
	boolean apathy$isInList(PlayerList list);
	boolean apathy$joinList(PlayerList list);
	boolean apathy$partList(PlayerList list);
	Collection<PlayerList> apathy$allLists();
}
