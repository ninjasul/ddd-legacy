package kitchenpos.infra;

import java.util.HashSet;
import java.util.List;

public class FakePurgomalumClient implements PurgomalumClient {
	private final HashSet<String> profanities = new HashSet<>(List.of("비속어", "욕설"));

	@Override
	public boolean containsProfanity(final String text) {
		return profanities.contains(text);
	}
}
