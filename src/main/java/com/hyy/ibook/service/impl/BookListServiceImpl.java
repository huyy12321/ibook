package com.hyy.ibook.service.impl;

import com.hyy.ibook.entity.BookList;
import com.hyy.ibook.vo.BookListVO;
import com.hyy.ibook.mapper.BookListMapper;
import com.hyy.ibook.service.BookListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hyy
 * @since 2021-10-08
 */
@Service
public class BookListServiceImpl extends ServiceImpl<BookListMapper, BookList> implements BookListService {

    @Override
    public List<BookListVO> listvo(Integer bookId) {
        return baseMapper.listvo(bookId);
    }
}
