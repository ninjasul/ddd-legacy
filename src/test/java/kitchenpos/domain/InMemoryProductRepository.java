package kitchenpos.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.util.CollectionUtils;

public class InMemoryProductRepository implements ProductRepository {
	private final HashMap<UUID, Product> products = new HashMap<>();

	@Override
	public Product save(Product product) {
		products.put(product.getId(), product);
		return product;
	}

	@Override
	public Optional<Product> findById(UUID id) {
		return Optional.ofNullable(products.get(id));
	}

	@Override
	public List<Product> findAllByIdIn(List<UUID> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}

		return products.values()
			.stream()
			.filter(product -> ids.contains(product.getId()))
			.toList();
	}

	@Override
	public List<Product> findAll() {
		return new ArrayList<>(products.values());
	}
}
