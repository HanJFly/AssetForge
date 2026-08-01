package com.hjf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import com.hjf.common.result.CommonException;
import com.hjf.common.result.Result;
import com.hjf.entity.AssetCategory;
import com.hjf.mapper.AssetCategoryMapper;
import com.hjf.param.AssetCategoryPageParam;
import com.hjf.param.AssetCategoryParam;
import com.hjf.service.IAssetCategoryService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.AssetCategoryPageVO;
import com.hjf.vo.AssetCategoryVO;
import jakarta.persistence.Access;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 资产分类表 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class AssetCategoryServiceImpl extends ServiceImpl<AssetCategoryMapper, AssetCategory> implements IAssetCategoryService {

    @Autowired
    private AssetCategoryMapper assetCategoryMapper;

    @Override
    public List<AssetCategoryVO> getTree() {
        List<AssetCategoryVO> assetCategoryVOList = assetCategoryMapper.getTree();

        Map<Long, List<AssetCategoryVO>> assetCategoryVOMap = assetCategoryVOList.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

        for (AssetCategoryVO assetCategoryVO : assetCategoryVOList) {
            assetCategoryVO.setChildren(
                    assetCategoryVOMap.getOrDefault(assetCategoryVO.getId(), new ArrayList<>())
            );
        }

        return assetCategoryVOList.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0L)
                .collect(Collectors.toList());
    }


    /*
    * 分页查询
    * */
    @Override
    public AssetCategoryPageVO queryPage(AssetCategoryPageParam param) {
        // 分页查询
        PageHelper.startPage(param.getPage(), param.getSize());

        List<AssetCategoryVO> assetCategories = assetCategoryMapper.queryPage(param);
        Page<AssetCategoryVO> pageInfo = (Page<AssetCategoryVO>) assetCategories;

        AssetCategoryPageVO assetCategoryPageVO = new AssetCategoryPageVO();
        assetCategoryPageVO.setTotal(pageInfo.getTotal());
        assetCategoryPageVO.setRecords(pageInfo);
        assetCategoryPageVO.setPage(param.getPage());
        assetCategoryPageVO.setSize(param.getSize());


        return assetCategoryPageVO;
    }

    /*
    * 详情
    * */
    @Override
    public AssetCategoryVO detail(AssetCategoryParam param) {
        QueryWrapper<AssetCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", param.getId());
        AssetCategory assetCategory = assetCategoryMapper.selectOne(queryWrapper);
        if (assetCategory == null){
            throw new CommonException(404, "资产分类不存在");
        }
        AssetCategoryVO assetCategoryVO = BeanUtil.copyProperties(assetCategory, AssetCategoryVO.class);

        if( assetCategoryVO.getParentId() == null ||  assetCategoryVO.getParentId() == 0){
            assetCategoryVO.setParentName("无");
        }else {
            AssetCategory assetCategory1 = assetCategoryMapper.selectById(assetCategoryVO.getParentId());
            assetCategoryVO.setParentName(assetCategory1 != null  ? assetCategory1.getName(): "未知");
        }

        return assetCategoryVO;
    }

    /*
    * 创建
    * */
    @Override
    public void create(AssetCategoryParam param) {
        //校验name是否为空
        if (StringUtil.isEmpty(param.getName())){
            throw new CommonException(400, "名称不能为空");
        }
        //检验name是否在同级中已经存在
        if(param.getParentId() == null || param.getParentId() == 0){
            QueryWrapper<AssetCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", 0);
            // 查询同级下分类是否存在
            List<AssetCategory> assetCategories = assetCategoryMapper.selectList(queryWrapper);
            if (assetCategories.stream().anyMatch(a -> a.getName().equals(param.getName()))){
                throw new CommonException(400, "名称已存在");
            }
            // 获取同级分类最大排序
            queryWrapper.orderByDesc("sort_order");
            queryWrapper.last("limit 1");
            AssetCategory assetCategory = assetCategoryMapper.selectOne(queryWrapper);
            param.setSortOrder(assetCategory != null ? assetCategory.getSortOrder() + 1 : 1);
        }else {
            QueryWrapper<AssetCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", param.getParentId());
            List<AssetCategory> assetCategories = assetCategoryMapper.selectList(queryWrapper);
            if (assetCategories.stream().anyMatch(a -> a.getName().equals(param.getName()))){
                throw new CommonException(400, "名称已存在");
            }

            // 获取同级分类最大排序
            queryWrapper.orderByDesc("sort_order");
            queryWrapper.last("limit 1");
            AssetCategory assetCategory = assetCategoryMapper.selectOne(queryWrapper);
            param.setSortOrder(assetCategory != null ? assetCategory.getSortOrder() + 1 : 1);
        }

        AssetCategory assetCategory = BeanUtil.copyProperties(param, AssetCategory.class);
        assetCategoryMapper.insert(assetCategory);

    }

    /*
    * 修改
    * */
    @Override
    public void update(AssetCategoryParam param) {
        AssetCategory assetCategory = assetCategoryMapper.selectById(param.getId());
        if (assetCategory == null || assetCategory.getIsDeleted() == 1){
            throw new CommonException(404, "分类不存在");
        }
       List<AssetCategory> assetCategories = assetCategoryMapper.selectList(new QueryWrapper<AssetCategory>()
               .eq("parent_id", param.getParentId()));
        if (assetCategories.stream().anyMatch(a -> a.getName().equals(param.getName()) && !a.getId().equals(param.getId()))){
            throw new CommonException(400, "名称已存在");
        }
        assetCategory = BeanUtil.copyProperties(param, AssetCategory.class);
        assetCategoryMapper.updateById(assetCategory);
    }


    /*
    * 删除
    * */
    @Override
    public void delete(AssetCategoryParam param) {
        AssetCategory assetCategory = assetCategoryMapper.selectById(param.getId());
        if (assetCategory == null || assetCategory.getIsDeleted() == 1){
            throw new CommonException(404, "分类不存在");
        }
        QueryWrapper<AssetCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", param.getId());
        if (assetCategoryMapper.selectCount(queryWrapper) > 0){
            throw new CommonException(400, "请先删除子分类");
        }
        assetCategory.setIsDeleted((byte) 1);
        assetCategoryMapper.updateById(assetCategory);

    }
}
