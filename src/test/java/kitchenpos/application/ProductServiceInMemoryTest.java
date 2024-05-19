package kitchenpos.application;

import static fixture.MenuFixture.*;
import static fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import fixture.MenuFixture;
import fixture.ProductFixture;
import kitchenpos.domain.InMemoryMenuRepository;
import kitchenpos.domain.InMemoryProductRepository;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuRepository;
import kitchenpos.domain.Product;
import kitchenpos.domain.ProductRepository;
import kitchenpos.infra.FakePurgomalumClient;
import kitchenpos.infra.PurgomalumClient;

class ProductServiceInMemoryTest {
	private ProductRepository productRepository = new InMemoryProductRepository();

	private MenuRepository menuRepository = new InMemoryMenuRepository();

	private PurgomalumClient purgomalumClient = new FakePurgomalumClient();

	private ProductService productService;

	@BeforeEach
	void setUp() {
		productService = new ProductService(productRepository, menuRepository, purgomalumClient);
	}

	@Nested
	class create {
		@ParameterizedTest
		@NullSource
		@ValueSource(strings = {"-1"})
		@DisplayName("상품 생성 시 가격이 null이거나 0미만이면 상품 생성을 할 수 없다")
		void createProductWithNullPrice(BigDecimal price) {
			// given
			Product invalidPricedProduct = ProductFixture.createRequestWithNameAndPrice(VALID_PRODUCT_NAME, price);

			// then
			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() ->
					// when
					productService.create(invalidPricedProduct)
				);
		}

		@Test
		@DisplayName("상품 생성 시 상품명이 null이면 상품 생성을 할 수 없다")
		void createProductWithNullName() {
			// given
			Product nullNamedProduct = ProductFixture.createRequestWithNameAndPrice(null, BigDecimal.TEN);

			// then
			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() ->
					// when
					productService.create(nullNamedProduct)
				);
		}

		@ParameterizedTest
		@ValueSource(strings = {"욕설", "비속어"})
		@DisplayName("상품 생성 시 상품명에 비속어가 포함되어 있으면 상품 생성을 할 수 없다")
		void createProductWithProfaneName(String name) {
			// given
			Product productWithProfanity = ProductFixture.createRequestWithNameAndPrice(name, BigDecimal.TEN);

			// then
			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() ->
					// when
					productService.create(productWithProfanity)
				);
		}

		@Test
		@DisplayName("상품 생성을 할 수 있다")
		void createProductSuccessfully() {
			// given
			Product validProduct = ProductFixture.createRequestWithNameAndPrice(VALID_PRODUCT_NAME,
				VALID_PRODUCT_PRICE);

			// when
			Product createdProduct = productService.create(validProduct);

			// then
			assertThat(createdProduct).isNotNull();
			assertThat(createdProduct.getName()).isEqualTo(validProduct.getName());
			assertThat(createdProduct.getPrice()).isEqualTo(validProduct.getPrice());
		}
	}

	@Nested
	class changePrice {
		@ParameterizedTest
		@NullSource
		@ValueSource(strings = {"-1"})
		@DisplayName("상품 가격 변경 시 가격이 null이거나 0 미만이면 상품 가격 변경을 할 수 없다")
		void changePriceWithNullPrice(BigDecimal invalidPrice) {
			// given
			Product invalidPricedProduct = ProductFixture.createRequestWithNameAndPrice(VALID_PRODUCT_NAME,
				invalidPrice);

			// then
			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() ->
					// when
					productService.changePrice(invalidPricedProduct.getId(), invalidPricedProduct)
				);
		}

		@Test
		@DisplayName("상품 가격 변경 시 상품 조회가 실패하면 상품 가격 변경을 할 수 없다")
		void changePriceWithNonExistentProduct() {
			// given
			Product nonExistentProduct = ProductFixture.createRequestWithNameAndPrice(VALID_PRODUCT_NAME,
				VALID_PRODUCT_PRICE);

			// then
			assertThatExceptionOfType(NoSuchElementException.class)
				.isThrownBy(() ->
					// when
					productService.changePrice(nonExistentProduct.getId(), nonExistentProduct)
				);
		}

		@Test
		@DisplayName("상품 가격 변경 시 해당 상품이 메뉴에 등록되어 있지 않으면 상품의 가격을 변경할 수 있다")
		void changePriceWithoutMenus() {
			// given
			Product product = ProductFixture.createRequestWithNameAndPrice(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE);

			productRepository.save(product);

			// when
			Product updatedProduct = productService.changePrice(product.getId(),
				product);

			// then
			assertThat(updatedProduct).isNotNull();
			assertThat(updatedProduct.getPrice()).isEqualTo(product.getPrice());
		}

		@Test
		@DisplayName("상품 가격 변경 시 상품들의 가격 합계가 메뉴 가격보다 낮을 때 해당 상품의 메뉴가 비노출 처리된다")
		void changePriceMenuPriceHigher() {
			// given
			Product product = ProductFixture.createValid();
			Menu menu = MenuFixture.create(
				MenuFixture.createValidMenuGroup(),
				VALID_MENU_NAME,
				VALID_PRODUCT_PRICE.add(BigDecimal.TEN),
				List.of(MenuFixture.createMenuProductWithProductAndQuantity(product, VALID_MENU_PRODUCT_QUANTITY)),
				true
			);

			productRepository.save(product);
			menuRepository.save(menu);

			// when
			productService.changePrice(product.getId(), product);

			// then
			assertThat(menu.isDisplayed()).isFalse();
		}

		@Test
		@DisplayName("상품 가격 변경 시 상품들의 가격 합계가 메뉴 가격보다 낮지 않을 때 해당 상품의 메뉴가 노출 처리된다")
		void changePriceMenuPriceEqual() {
			// given
			Product product = ProductFixture.createValid();
			Menu menu = MenuFixture.create(
				MenuFixture.createValidMenuGroup(),
				VALID_MENU_NAME,
				BigDecimal.TEN,
				List.of(MenuFixture.createMenuProductWithProductAndQuantity(product, VALID_MENU_PRODUCT_QUANTITY)),
				true
			);

			productRepository.save(product);
			menuRepository.save(menu);

			// when
			productService.changePrice(product.getId(), product);

			// then
			assertThat(menu.isDisplayed()).isTrue();
		}
	}

	@Nested
	class findAll {
		@Test
		@DisplayName("상품 데이터가 비어 있을 때 모든 상품을 조회하면 모든 상품을 조회할 수 없다")
		void findAllProductsWhenEmpty() {
			// given & when
			var products = productService.findAll();

			// then
			assertThat(products).isEmpty();
		}

		@Test
		@DisplayName("상품 데이터가 비어 있지 않을 때 모든 상품을 조회하면 모든 상품을 조회할 수 있다")
		void findAllProductsWhenNotEmpty() {
			// given
			Product validProduct = ProductFixture.createValid();
			Product anotherProduct = ProductFixture.create("불고기버거", BigDecimal.TEN);

			productRepository.save(validProduct);
			productRepository.save(anotherProduct);

			// when
			var products = productService.findAll();

			// then
			assertThat(products).isNotEmpty();
			assertThat(products).containsExactlyInAnyOrder(validProduct, anotherProduct);
		}
	}
}