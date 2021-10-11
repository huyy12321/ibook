package com.hyy.ibook;

import com.hyy.ibook.Entity.BookList;
import com.hyy.ibook.Entity.BookName;
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

    private List<BookList> bookLists;
}
