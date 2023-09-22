package kitchenpos.application;

import static kitchenpos.Fixtures.menu;
import static kitchenpos.Fixtures.menuProduct;
import static kitchenpos.Fixtures.product;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kitchenpos.menu.tobe.domain.Menu;
import kitchenpos.menu.tobe.domain.MenuRepository;
import kitchenpos.product.tobe.application.ProductService;
import kitchenpos.product.tobe.application.dto.ChangeProductPriceRequest;
import kitchenpos.product.tobe.application.dto.CreateProductRequest;
import kitchenpos.product.tobe.domain.Product;
import kitchenpos.product.tobe.domain.ProductRepository;
import kitchenpos.product.tobe.domain.service.ProductNameNormalPolicy;
import kitchenpos.product.tobe.domain.service.ProductNamePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductServiceTest {

    private ProductRepository productRepository;
    private MenuRepository menuRepository;
    private ProductNamePolicy productNamePolicy;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository();
        menuRepository = new InMemoryMenuRepository();
        productNamePolicy = new ProductNameNormalPolicy(new FakeProfanityClient());
        productService = new ProductService(productRepository, menuRepository, productNamePolicy);
    }

    @DisplayName("상품을 등록할 수 있다.")
    @Test
    void create() {
        final var expected = createProductRequest("후라이드", 16_000L);
        final var actual = productService.create(expected);
        assertThat(actual).isNotNull();
        assertAll(
            () -> assertThat(actual.getId()).isNotNull(),
            () -> assertThat(actual.getName().getValue()).isEqualTo(expected.getName()),
            () -> assertThat(actual.getPrice().getValue()).isEqualTo(expected.getPrice())
        );
    }

    @DisplayName("상품의 가격이 올바르지 않으면 등록할 수 없다.")
    @ValueSource(strings = "-1000")
    @NullSource
    @ParameterizedTest
    void create(final BigDecimal price) {
        final var expected = createProductRequest("후라이드", price);
        assertThatThrownBy(() -> productService.create(expected))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품의 이름이 올바르지 않으면 등록할 수 없다.")
    @ValueSource(strings = {"비속어", "욕설이 포함된 이름"})
    @NullSource
    @ParameterizedTest
    void create(final String name) {
        final var expected = createProductRequest(name, 16_000L);
        assertThatThrownBy(() -> productService.create(expected))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품의 가격을 변경할 수 있다.")
    @Test
    void changePrice() {
        final UUID productId = productRepository.save(product("후라이드", 16_000L)).getId();
        final ChangeProductPriceRequest expected = changePriceRequest(15_000L);
        final var actual = productService.changePrice(productId, expected);
        assertThat(actual.getPrice().getValue()).isEqualTo(expected.getPrice());
    }

    @DisplayName("상품의 가격이 올바르지 않으면 변경할 수 없다.")
    @ValueSource(strings = "-1000")
    @NullSource
    @ParameterizedTest
    void changePrice(final BigDecimal price) {
        final UUID productId = productRepository.save(product("후라이드", 16_000L)).getId();
        final ChangeProductPriceRequest expected = changePriceRequest(price);
        assertThatThrownBy(() -> productService.changePrice(productId, expected))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품의 가격이 변경될 때 메뉴의 가격이 메뉴에 속한 상품 금액의 합보다 크면 메뉴가 숨겨진다.")
    @Test
    void changePriceInMenu() {
        final Product product = productRepository.save(product("후라이드", 16_000L));
        final Menu menu = menuRepository.save(menu(19_000L, true, menuProduct(product, 2L)));
        productService.changePrice(product.getId(), changePriceRequest(8_000L));
        assertThat(menuRepository.findById(menu.getId()).get().isDisplayed()).isFalse();
    }

    @DisplayName("상품의 목록을 조회할 수 있다.")
    @Test
    void findAll() {
        productRepository.save(product("후라이드", 16_000L));
        productRepository.save(product("양념치킨", 16_000L));
        final List<Product> actual = productService.findAll();
        assertThat(actual).hasSize(2);
    }

    private CreateProductRequest createProductRequest(final String name, final long price) {
        return createProductRequest(name, BigDecimal.valueOf(price));
    }

    private CreateProductRequest createProductRequest(final String name, final BigDecimal price) {
        return new CreateProductRequest(name, price);
    }

    private ChangeProductPriceRequest changePriceRequest(final long price) {
        return new ChangeProductPriceRequest(BigDecimal.valueOf(price));
    }

    private ChangeProductPriceRequest changePriceRequest(final BigDecimal price) {
        return new ChangeProductPriceRequest(price);
    }
}
