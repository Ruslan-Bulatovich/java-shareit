package java.ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository items;
    @Autowired
    protected TestEntityManager entityManager;
    private User user1;
    private User user2;
    private Item item1;
    private Item item2;

    @BeforeEach
    public void setUp() {

        user1 = new User();
        user1.setName("test name");
        user1.setEmail("test@test.ru");

        user2 = new User();
        user2.setName("test name2");
        user2.setEmail("test2@test.ru");

        item1 = new Item();
        item1.setId(null);
        item1.setName("test name");
        item1.setDescription("item test description");
        item1.setAvailable(false);
        item1.setOwner(user1);

        item2 = new Item();
        item2.setId(null);
        item2.setName("test name 2");
        item2.setDescription("item test description 2");
        item2.setAvailable(false);
        item2.setOwner(user1);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

    }

    @Test
    public void testFindAllItemsIdByOwnerId() {
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();
        List<Long> expectItemId = List.of(item1.getId(), item2.getId());
        List<Long> actualItemId = items.findAllItemIdByOwnerId(user1.getId());
        Assertions.assertEquals(expectItemId, actualItemId);
    }

    @Test
    public void testFindAllItemsIdByNotOwnerId() {
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();
        List<Long> expectItemId = List.of();
        List<Long> actualItemId = items.findAllItemIdByOwnerId(user2.getId());
        Assertions.assertEquals(expectItemId, actualItemId);
    }

    @Test
    public void testExistsItemByOwnerId() {
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();
        Boolean expect = true;
        Boolean actual = items.existsItemByOwnerId(user1.getId());
        Assertions.assertEquals(expect, actual);
    }

    @Test
    public void testExistsItemByNotOwnerId() {
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();
        Boolean expect = false;
        Boolean actual = items.existsItemByOwnerId(user2.getId());
        Assertions.assertEquals(expect, actual);
    }

    @Test
    public void testFindAllByNameContainingIgnoreCaseAndAvailableTrue() {
        entityManager.persist(item1);
        entityManager.flush();
        List<Item> expect = List.of(item1);
        List<Item> actual = items.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(PageRequest.of(0, 10), "test name", "");
        Assertions.assertEquals(expect, actual);
    }

    @Test
    public void testFindAllByBlankNameContainingIgnoreCaseAndAvailableTrueShouldReturnEmptyList() {
        entityManager.persist(item1);
        entityManager.flush();
        List<Item> expect = Collections.emptyList();
        List<Item> actual = items.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(PageRequest.of(0, 10), "test", "item test description");
        Assertions.assertEquals(expect, actual);
    }

    @Test
    public void testFindAllByOwnerId() {
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();
        List<Item> expect = List.of(item1, item2);
        List<Item> actual = items.findAllByOwnerId(PageRequest.of(0, 10), user1.getId());
        Assertions.assertEquals(expect, actual);
    }

    @Test
    public void testFindAllByNotOwnerId() {
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();
        List<Item> expect = List.of();
        List<Item> actual = items.findAllByOwnerId(PageRequest.of(0, 10), user2.getId());
        Assertions.assertEquals(expect, actual);
    }
}
