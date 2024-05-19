package kitchenpos.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fixture.MenuFixture;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuGroupRepository;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceTest {
	@Mock
	private MenuGroupRepository menuGroupRepository;

	@InjectMocks
	private MenuGroupService menuGroupService;

	@Nested
	class create {

		@ParameterizedTest
		@DisplayName("메뉴 그룹의 이름이 null이거나 비어있으면 메뉴 그룹을 생성할 수 없다")
		@NullAndEmptySource
		void createMenuGroupWithEmptyOrNullName(String name) {
			// given
			MenuGroup menuGroupWithInvalidName = MenuFixture.createMenuGroupWithName(name);

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
			MenuGroup validMenuGroup = MenuFixture.createValidMenuGroup();

			when(menuGroupRepository.save(any(MenuGroup.class))).thenReturn(validMenuGroup);

			// when
			MenuGroup created = menuGroupService.create(validMenuGroup);

			// then
			assertThat(created).isNotNull();
			assertThat(created.getName()).isEqualTo(validMenuGroup.getName());
			assertThat(created.getId()).isNotNull();
		}
	}

	@Nested
	class findAll {
		@Test
		@DisplayName("메뉴 그룹이 비어 있을 때 모든 메뉴 그룹 조회 시 메뉴 그룹을 조회할 수 없다")
		void findAllMenuGroupsWhenEmpty() {
			// given
			when(menuGroupRepository.findAll()).thenReturn(Collections.emptyList());

			// when
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

			when(menuGroupRepository.findAll()).thenReturn(List.of(validMenuGroup, anotherValidMenuGroup));

			// when
			List<MenuGroup> foundMenuGroups = menuGroupService.findAll();

			// then
			assertThat(foundMenuGroups).isNotEmpty();
			assertThat(foundMenuGroups.getFirst().getName()).isEqualTo(validMenuGroup.getName());
			assertThat(foundMenuGroups.getLast().getName()).isEqualTo(anotherValidMenuGroup.getName());
		}
	}
}