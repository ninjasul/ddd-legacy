package kitchenpos.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InMemoryMenuGroupRepository implements MenuGroupRepository {

	private final HashMap<UUID, MenuGroup> menuGroups = new HashMap<>();

	@Override
	public MenuGroup save(MenuGroup menuGroup) {
		menuGroups.put(menuGroup.getId(), menuGroup);
		return menuGroup;
	}

	@Override
	public Optional<MenuGroup> findById(UUID id) {
		return Optional.ofNullable(menuGroups.get(id));
	}

	@Override
	public List<MenuGroup> findAll() {
		return new ArrayList<>(menuGroups.values());
	}
}