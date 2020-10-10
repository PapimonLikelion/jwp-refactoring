package kitchenpos.application;

import static kitchenpos.utils.TestObjects.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kitchenpos.domain.MenuGroup;

class MenuGroupServiceTest extends ServiceTest {

    @Autowired
    private MenuGroupService menuGroupService;

    @DisplayName("create: 메뉴 그룹 생성")
    @Test
    void create() {
        final MenuGroup menuGroup = createMenuGroup("이십마리메뉴");
        final MenuGroup actual = menuGroupService.create(menuGroup);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getName()).isEqualTo("이십마리메뉴");
    }

    @DisplayName("list: 메뉴 그룹 전체 조회")
    @Test
    void list() {
        final List<MenuGroup> menuGroups = menuGroupService.list();

        assertThat(menuGroups).isNotEmpty();
    }
}