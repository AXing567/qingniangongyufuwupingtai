
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;

import com.alibaba.fastjson.JSONObject;

import java.util.*;

import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.ContextLoader;

import javax.servlet.ServletContext;

import com.service.TokenService;
import com.utils.*;

import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 预约看房
 * 后端接口
 *
 * @author
 * @email
 */
@RestController
@Controller
@RequestMapping("/fangwuOrder")
public class FangwuOrderController {
    private static final Logger logger = LoggerFactory.getLogger(FangwuOrderController.class);

    @Autowired
    private FangwuOrderService fangwuOrderService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private FangwuService fangwuService;
    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private ShangjiaService shangjiaService;


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        logger.debug("page方法:,,Controller:{},,params:{}", this.getClass().getName(), JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if (false)
            return R.error(511, "永不会进入");
        else if ("用户".equals(role))
            params.put("yonghuId", request.getSession().getAttribute("userId"));
        else if ("商家".equals(role))
            params.put("shangjiaId", request.getSession().getAttribute("userId"));
        if (params.get("orderBy") == null || params.get("orderBy") == "") {
            params.put("orderBy", "id");
        }
        PageUtils page = fangwuOrderService.queryPage(params);

        //字典表数据转换
        List<FangwuOrderView> list = (List<FangwuOrderView>) page.getList();
        for (FangwuOrderView c : list) {
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request) {
        logger.debug("info方法:,,Controller:{},,id:{}", this.getClass().getName(), id);
        FangwuOrderEntity fangwuOrder = fangwuOrderService.selectById(id);
        if (fangwuOrder != null) {
            //entity转view
            FangwuOrderView view = new FangwuOrderView();
            BeanUtils.copyProperties(fangwuOrder, view);//把实体数据重构到view中

            //级联表
            FangwuEntity fangwu = fangwuService.selectById(fangwuOrder.getFangwuId());
            if (fangwu != null) {
                BeanUtils.copyProperties(fangwu, view, new String[]{"id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setFangwuId(fangwu.getId());
            }
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(fangwuOrder.getYonghuId());
            if (yonghu != null) {
                BeanUtils.copyProperties(yonghu, view, new String[]{"id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        } else {
            return R.error(511, "查不到数据");
        }

    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody FangwuOrderEntity fangwuOrder, HttpServletRequest request) {
        logger.debug("save方法:,,Controller:{},,fangwuOrder:{}", this.getClass().getName(), fangwuOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if (false)
            return R.error(511, "永远不会进入");
        else if ("用户".equals(role))
            fangwuOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        fangwuOrder.setInsertTime(new Date());
        fangwuOrder.setCreateTime(new Date());
        fangwuOrderService.insert(fangwuOrder);
        return R.ok();
    }

    /**
     * 后端修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody FangwuOrderEntity fangwuOrder, HttpServletRequest request) {
        logger.debug("update方法:,,Controller:{},,fangwuOrder:{}", this.getClass().getName(), fangwuOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            fangwuOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<FangwuOrderEntity> queryWrapper = new EntityWrapper<FangwuOrderEntity>()
                .eq("id", 0);

        logger.info("sql语句:" + queryWrapper.getSqlSegment());
        FangwuOrderEntity fangwuOrderEntity = fangwuOrderService.selectOne(queryWrapper);
        if (fangwuOrderEntity == null) {
            fangwuOrderService.updateById(fangwuOrder);//根据id更新
            return R.ok();
        } else {
            return R.error(511, "表中有相同数据");
        }
    }


    /**
     * 审核
     */
    @RequestMapping("/shenhe")
    public R shenhe(@RequestBody FangwuOrderEntity fangwuOrder, HttpServletRequest request) {
        logger.debug("shenhe方法:,,Controller:{},,fangwuOrder:{}", this.getClass().getName(), fangwuOrder.toString());

//        if(fangwuOrder.getFangwuOrderYesnoTypes() == 2){//通过
//            fangwuOrder.setFangwuOrderTypes();
//        }else if(fangwuOrder.getFangwuOrderYesnoTypes() == 3){//拒绝
//            fangwuOrder.setFangwuOrderTypes();
//        }
        fangwuOrder.setFangwuOrderShenheTime(new Date());//审核时间
        fangwuOrderService.updateById(fangwuOrder);//审核
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids) {
        logger.debug("delete:,,Controller:{},,ids:{}", this.getClass().getName(), ids.toString());
        fangwuOrderService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save(String fileName, HttpServletRequest request) {
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}", this.getClass().getName(), fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<FangwuOrderEntity> fangwuOrderList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields = new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if (lastIndexOf == -1) {
                return R.error(511, "该文件没有后缀");
            } else {
                String suffix = fileName.substring(lastIndexOf);
                if (!".xls".equals(suffix)) {
                    return R.error(511, "只支持后缀为xls的excel文件");
                } else {
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if (!file.exists()) {
                        return R.error(511, "找不到上传文件，请联系管理员");
                    } else {
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for (List<String> data : dataList) {
                            //循环
                            FangwuOrderEntity fangwuOrderEntity = new FangwuOrderEntity();
//                            fangwuOrderEntity.setFangwuOrderUuidNumber(data.get(0));                    //看房编号 要改的
//                            fangwuOrderEntity.setFangwuId(Integer.valueOf(data.get(0)));   //房屋 要改的
//                            fangwuOrderEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            fangwuOrderEntity.setInsertTime(date);//时间
//                            fangwuOrderEntity.setYuyueTime(sdf.parse(data.get(0)));          //申请看房时间 要改的
//                            fangwuOrderEntity.setFangwuOrderYesnoTypes(Integer.valueOf(data.get(0)));   //预约状态 要改的
//                            fangwuOrderEntity.setFangwuOrderYesnoText(data.get(0));                    //审核意见 要改的
//                            fangwuOrderEntity.setFangwuOrderShenheTime(sdf.parse(data.get(0)));          //审核时间 要改的
//                            fangwuOrderEntity.setCreateTime(date);//时间
                            fangwuOrderList.add(fangwuOrderEntity);


                            //把要查询是否重复的字段放入map中
                            //看房编号
                            if (seachFields.containsKey("fangwuOrderUuidNumber")) {
                                List<String> fangwuOrderUuidNumber = seachFields.get("fangwuOrderUuidNumber");
                                fangwuOrderUuidNumber.add(data.get(0));//要改的
                            } else {
                                List<String> fangwuOrderUuidNumber = new ArrayList<>();
                                fangwuOrderUuidNumber.add(data.get(0));//要改的
                                seachFields.put("fangwuOrderUuidNumber", fangwuOrderUuidNumber);
                            }
                        }

                        //查询是否重复
                        //看房编号
                        List<FangwuOrderEntity> fangwuOrderEntities_fangwuOrderUuidNumber = fangwuOrderService.selectList(new EntityWrapper<FangwuOrderEntity>().in("fangwu_order_uuid_number", seachFields.get("fangwuOrderUuidNumber")));
                        if (fangwuOrderEntities_fangwuOrderUuidNumber.size() > 0) {
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for (FangwuOrderEntity s : fangwuOrderEntities_fangwuOrderUuidNumber) {
                                repeatFields.add(s.getFangwuOrderUuidNumber());
                            }
                            return R.error(511, "数据库的该表中的 [看房编号] 字段已经存在 存在数据为:" + repeatFields.toString());
                        }
                        fangwuOrderService.insertBatch(fangwuOrderList);
                        return R.ok();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(511, "批量插入数据异常，请联系管理员");
        }
    }


    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        logger.debug("list方法:,,Controller:{},,params:{}", this.getClass().getName(), JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if (StringUtil.isEmpty(String.valueOf(params.get("orderBy")))) {
            params.put("orderBy", "id");
        }
        PageUtils page = fangwuOrderService.queryPage(params);

        //字典表数据转换
        List<FangwuOrderView> list = (List<FangwuOrderView>) page.getList();
        for (FangwuOrderView c : list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
     * 前端详情
     */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request) {
        logger.debug("detail方法:,,Controller:{},,id:{}", this.getClass().getName(), id);
        FangwuOrderEntity fangwuOrder = fangwuOrderService.selectById(id);
        if (fangwuOrder != null) {


            //entity转view
            FangwuOrderView view = new FangwuOrderView();
            BeanUtils.copyProperties(fangwuOrder, view);//把实体数据重构到view中

            //级联表
            FangwuEntity fangwu = fangwuService.selectById(fangwuOrder.getFangwuId());
            if (fangwu != null) {
                BeanUtils.copyProperties(fangwu, view, new String[]{"id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setFangwuId(fangwu.getId());
            }
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(fangwuOrder.getYonghuId());
            if (yonghu != null) {
                BeanUtils.copyProperties(yonghu, view, new String[]{"id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        } else {
            return R.error(511, "查不到数据");
        }
    }


    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody FangwuOrderEntity fangwuOrder, HttpServletRequest request) {
        logger.debug("add方法:,,Controller:{},,fangwuOrder:{}", this.getClass().getName(), fangwuOrder.toString());
        FangwuEntity fangwuEntity = fangwuService.selectById(fangwuOrder.getFangwuId());
        if (fangwuEntity == null) {
            return R.error(511, "查不到该房屋");
        }
        if (fangwuOrder.getYuyueTime() == null)
            return R.error(511, "预约时间不能为空");
        // Double fangwuNewMoney = fangwuEntity.getFangwuNewMoney();

        if (false) {
        } else if (fangwuEntity.getFangwuNewMoney() == null) {
            return R.error(511, "房屋价格不能为空");
        }


        FangwuOrderEntity fangwuOrderEntity = fangwuOrderService.selectOne(new EntityWrapper<FangwuOrderEntity>()
                .eq("fangwu_id", fangwuOrder.getFangwuId())
                .eq("yonghu_id", fangwuOrder.getYonghuId())
                .eq("fangwu_order_yesno_types", 1)
        );
        if (fangwuOrderEntity != null)
            return R.error("该用户已经申请过看此房间了,无法重复申请请");

        Integer userId = (Integer) request.getSession().getAttribute("userId");
        fangwuOrder.setYonghuId(userId); //设置订单支付人id
        fangwuOrder.setFangwuOrderUuidNumber(String.valueOf(new Date().getTime()));
        fangwuOrder.setInsertTime(new Date());
        fangwuOrder.setCreateTime(new Date());
        fangwuOrderService.insert(fangwuOrder);//新增订单
        return R.ok();
    }


}
