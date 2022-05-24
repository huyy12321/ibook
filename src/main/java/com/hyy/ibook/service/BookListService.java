package com.hyy.ibook.service;

import com.hyy.ibook.entity.BookList;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hyy.ibook.vo.BookListVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hyy
 * @since 2021-10-08
 */
public interface BookListService extends IService<BookList> {

    List<BookListVO> listvo(Integer bookId);
}
