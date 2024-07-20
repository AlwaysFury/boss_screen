package com.boss.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.Tag;
import com.boss.task.dao.TagDao;
import com.boss.task.service.TagService;
import org.springframework.stereotype.Service;

/**
 * 标签服务
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagDao, Tag> implements TagService {

}
