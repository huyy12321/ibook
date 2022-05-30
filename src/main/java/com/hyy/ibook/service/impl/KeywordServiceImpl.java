package com.hyy.ibook.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hyy.ibook.entity.Keyword;
import com.hyy.ibook.mapper.KeywordMapper;
import com.hyy.ibook.service.KeywordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hyy
 * @since 2021-10-11
 */
@Service
public class KeywordServiceImpl extends ServiceImpl<KeywordMapper, Keyword> implements KeywordService {

    @Override
    public void addKeyword(String keyword) {
        Keyword keyword1 = baseMapper.selectOne(Wrappers.<Keyword>lambdaQuery()
                .eq(Keyword::getKeyword, keyword));
        if(keyword1 == null) {
            baseMapper.insert(Keyword.builder().keyword(keyword).count(0).updateTime(LocalDateTime.now()).build());
        } else{
            keyword1.setUpdateTime(LocalDateTime.now());
            keyword1.setCount(keyword1.getCount() + 1);
            baseMapper.updateById(keyword1);
        }
    }
}
