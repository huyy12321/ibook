package com.hyy.ibook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author hyy
 * @since 2021-10-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BookName implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private Integer channelId;

    private LocalDateTime updateTime;

    private String bookId;

    /**
     * 简介
     */
    private String info;

    private String account;

    private String imgUrl;

    private Integer heat;

    private String downUrl;

    private Integer downNewList;

    /**
     * 下载的状态 0未下载 1整合资源中 2可下载
     */
    private Integer downStatus;
}
