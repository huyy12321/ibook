package com.hyy.ibook.service;

import com.hyy.ibook.Entity.Keyword;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hyy
 * @since 2021-10-11
 */
public interface KeywordService extends IService<Keyword> {

    /**
     * 增加关键字搜索记录
     * @param keyword 关键字
     */
    void addKeyword(String keyword);
}
