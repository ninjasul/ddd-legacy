package kitchenpos.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.util.CollectionUtils;

public class InMemoryMenuRepository implements MenuRepository {
	private final HashMap<UUID, Menu> menus = new HashMap<>();

	@Override
	public Menu save(Menu menu) {
		menus.put(menu.getId(), menu);
		return menu;
	}

	@Override
	public Optional<Menu> findById(UUID id) {
		return Optional.ofNullable(menus.get(id));
	}

	@Override
	public List<Menu> findAll() {
		return new ArrayList<>(menus.values());
	}

	@Override
	public List<Menu> findAllByIdIn(List<UUID> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		
		return menus.values()
			.stream()
			.filter(menu -> ids.contains(menu.getId()))
			.toList();
	}

	@Override
	public List<Menu> findAllByProductId(UUID productId) {
		return menus.values()
			.stream()
			.filter(menu -> menuContainsProductId(menu, productId))
			.toList();
	}

	private boolean menuContainsProductId(Menu menu, UUID productId) {
		return menu.getMenuProducts()
			.stream()
			.anyMatch(menuProduct -> menuProduct.getProduct().getId().equals(productId));
	}
}
