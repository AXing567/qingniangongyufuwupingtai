package com.service.impl;

import com.utils.StringUtil;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.util.*;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.dao.FangwuCollectionDao;
import com.entity.FangwuCollectionEntity;
import com.service.FangwuCollectionService;
import com.entity.view.FangwuCollectionView;

/**
 * 房屋收藏 服务实现类
 */
@Service("fangwuCollectionService")
@Transactional
public class FangwuCollectionServiceImpl extends ServiceImpl<FangwuCollectionDao, FangwuCollectionEntity> implements FangwuCollectionService {

    /**
     * 重写queryPage方法以查询分页信息
     * 此方法首先检查传入的参数是否包含分页所需的'limit'和'page'字段如果没有，则设置默认值为第1页，每页10条记录
     * 然后，使用这些参数创建一个Page对象，并查询当前页的数据记录
     * 最后，将查询结果封装到PageUtils对象中并返回
     *
     * @param params 包含分页信息（如页码和每页记录数）的参数映射
     * @return 返回封装了分页数据的PageUtils对象
     */
    @Override
    public PageUtils queryPage(Map<String,Object> params) {
        // 检查参数中是否包含分页信息，如果没有，则设置默认的分页参数
        if(params != null && (params.get("limit") == null || params.get("page") == null)){
            params.put("page","1");
            params.put("limit","10");
        }
        // 创建一个Page对象，用于执行分页查询
        Page<FangwuCollectionView> page =new Query<FangwuCollectionView>(params).getPage();
        // 设置当前页的数据记录
        page.setRecords(baseMapper.selectListView(page,params));
        // 将分页信息封装到PageUtils对象中并返回
        return new PageUtils(page);
    }


}
