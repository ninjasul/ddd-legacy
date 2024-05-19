package kitchenpos.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import fixture.MenuFixture;
import kitchenpos.domain.InMemoryMenuGroupRepository;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuGroupRepository;

class MenuGroupServiceInMemoryTest {
	private MenuGroupRepository menuGroupRepository;

	private MenuGroupService menuGroupService;

	@BeforeEach
	void setUp() {
		menuGroupRepository = new InMemoryMenuGroupRepository();
		menuGroupService = new MenuGroupService(menuGroupRepository);
	}

	@Nested
	class create {

		@ParameterizedTest
		@DisplayName("메뉴 그룹의 이름이 null이거나 비어있으면 메뉴 그룹을 생성할 수 없다")
		@NullAndEmptySource
		void createMenuGroupWithEmptyOrNullName(String name) {
			// given
			MenuGroup menuGroupWithInvalidName = MenuFixture.createRequestMenuGroupWithName(name);

			// then
			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() ->
					// when
					menuGroupService.create(menuGroupWithInvalidName)
				);
		}

		@Test
		@DisplayName("메뉴 그룹의 이름이 비어있지 않으면 메뉴 그룹을 생성할 수 있다")
		void createMenuGroupWithValidName() {
			// given
			MenuGroup requestMenuGroup = MenuFixture.createValidRequestMenuGroupWithName();

			// when
			MenuGroup createdMenuGroup = menuGroupService.create(requestMenuGroup);

			// then
			assertThat(createdMenuGroup).isNotNull();
			assertAll(
				() -> assertThat(createdMenuGroup.getId()).isNotNull(),
				() -> assertThat(createdMenuGroup.getName()).isEqualTo(requestMenuGroup.getName())
			);
		}
	}

	@Nested
	class findAll {
		@Test
		@DisplayName("메뉴 그룹이 비어 있을 때 모든 메뉴 그룹 조회 시 메뉴 그룹을 조회할 수 없다")
		void findAllMenuGroupsWhenEmpty() {
			// given & when
			List<MenuGroup> foundMenuGroups = menuGroupService.findAll();

			// then
			assertThat(foundMenuGroups).isEmpty();
		}

		@Test
		@DisplayName("메뉴 그룹이 비어 있지 있을 때 모든 메뉴 그룹 조회 시 메뉴 그룹을 조회할 수 있다")
		void findAllMenuGroupsWhenNotEmpty() {
			// given
			MenuGroup validMenuGroup = MenuFixture.createValidMenuGroup();
			MenuGroup anotherValidMenuGroup = MenuFixture.createMenuGroupWithName("모닝세트");

			menuGroupRepository.save(validMenuGroup);
			menuGroupRepository.save(anotherValidMenuGroup);

			// when
			List<MenuGroup> foundMenuGroups = menuGroupService.findAll();

			// then
			assertThat(foundMenuGroups).isNotEmpty();
			assertThat(foundMenuGroups).containsExactlyInAnyOrder(validMenuGroup, anotherValidMenuGroup);
		}
	}
}