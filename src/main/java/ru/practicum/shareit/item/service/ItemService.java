package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    /**
     * Реализует добавление Вещи в хранилище
     * @param userId идентификатор Пользователя владельца
     * @param itemDto Вещь
     * @return объект ItemDto
     */
    ItemDto addItem(long userId, ItemDto itemDto);

    /**
     * Реализует обновление полей хранимой Вещи
     * @param userId идентификатор Пользователя владельца
     * @param itemId идентификатор Вещи
     * @param itemDto Вещь
     * @return объект ItemDto
     */
    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    /**
     * Возвращает ItemDto Вещи Пользователя
     * @param itemId идентификатор Вещи
     * @param userId идентификатор Пользователя владельца Вещи
     * @return ItemDto
     */
    ItemDto getItemById(long itemId, long userId);

    /**
     * Возвращает коллекцию DTO Вещей Пользователя
     * @param userId идентификатор Пользователя владельца Вещи
     * @return коллекцию ItemDto
     */
    List<ItemDto> getAllItems(long userId);

    /**
     * Реализует поиск Вещей в хранилище по ключевому слову
     * @param text ключевое слово для поиска
     * @return коллекцию ItemDto
     */
    List<ItemDto> searchItems(String text);

    /**
     * Реализует удаление Вещи из хранилища
     * @param userId идентификатор Пользователя владельца Вещи
     * @param itemId идентификатор удаляемой вещи
     */
    void removeItem(long userId, long itemId);

    /**
     * Реализует добавления Комментария
     * @param userId идентификатор Пользователя владельца Вещи
     * @param itemId идентификатор Вещи
     * @param commentDto Комментарий
     * @return CommentDto
     */
    CommentDto addComment(long userId, long itemId, CommentDto commentDto);
}