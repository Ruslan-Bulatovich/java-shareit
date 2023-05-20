package ru.practicum.shareit.item.mapper;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;

@Component
@AllArgsConstructor
public class CommentMapper {
    private final ModelMapper modelMapper;

    public CommentMapper() {
        modelMapper = new ModelMapper();
    }

    public CommentDto convertToDto(Comment comment) {
        return modelMapper.map(comment, CommentDto.class);
    }

    public ItemDto.CommentData convertToData(Comment comment) {
        return modelMapper.map(comment, ItemDto.CommentData.class);
    }

    public Comment convertFromDto(CommentDto commentDto) {
        return modelMapper.map(commentDto, Comment.class);
    }
}
