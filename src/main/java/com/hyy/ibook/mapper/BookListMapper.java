package com.hyy.ibook.mapper;

import com.hyy.ibook.entity.BookList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hyy.ibook.vo.BookListVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hyy
 * @since 2021-10-08
 */
public interface BookListMapper extends BaseMapper<BookList> {

    @Select("SELECT id,list_name,book_id FROM book_list WHERE (book_id = #{bookId})")
    List<BookListVO> listvo(Integer bookId);
}
