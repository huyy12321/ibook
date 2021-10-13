package com.hyy.ibook.service;

import com.hyy.ibook.Entity.BookName;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hyy
 * @since 2021-10-08
 */
public interface BookNameService extends IService<BookName> {

    /**
     * 同步书名
     * @param keyword
     */
    void updateBookName(String keyword);

    /**
     * 同步书籍目录
     * @param id
     */
    void updateBookList(String id);

    /**
     * 同步书籍内容
     * @param id
     */
    void updateBookInfo(String id,Integer listId);

    void down(Integer id);
}
