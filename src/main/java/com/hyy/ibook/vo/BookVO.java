package com.hyy.ibook.vo;

import com.hyy.ibook.entity.BookName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huyangyang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookVO {
    private BookName bookName;

    private List<BookListVO> bookLists;
}
