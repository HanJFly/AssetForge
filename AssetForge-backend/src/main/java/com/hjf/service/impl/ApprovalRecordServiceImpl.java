package com.hjf.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.common.result.CommonException;
import com.hjf.common.result.Result;
import com.hjf.context.LoginUserIRoleUtile;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.entity.*;
import com.hjf.mapper.*;
import com.hjf.param.*;
import com.hjf.service.IApprovalRecordService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.util.JwtUtils;
import com.hjf.vo.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批记录表（统一审批流） 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Slf4j
@Service
public class ApprovalRecordServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> implements IApprovalRecordService {

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    @Autowired
    private RequisitionOrderMapper requisitionOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransferOrderMapper transferOrderMapper;

    @Autowired
    private ReturnOrderMapper returnOrderMapper;

    @Autowired
    private ScrapOrderMapper scrapOrderMapper;

    @Autowired
    private RequisitionOrderItemMapper requisitionOrderItemMapper;

    @Autowired
    private TransferOrderItemMapper transferOrderItemMapper;

    @Autowired
    private ReturnOrderItemMapper returnOrderItemMapper;

    @Autowired
    private ScrapOrderItemMapper scrapOrderItemMapper;

    @Autowired
    private AssetCategoryMapper assetCategoryMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private LoginUserIRoleUtile loginUserIRoleUtile;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;


    @Override
    public ApprovalRecordTodoPageVO todoPage(ApprovalRecordTodoPageParam param) {
        //分页
        LoginUserContext context = requireLoginUser();
        boolean assetAdmin = isAssetAdmin(context.getId());
        if (!assetAdmin) {
            param.setApproverId(context.getId());
        } else {
            param.setApproverId(null);
        }
        log.info("审批中心待审批查询: userId={}, assetAdmin={}, approverId={}, page={}, size={}",
                context.getId(), assetAdmin, param.getApproverId(), param.getPage(), param.getSize());
        PageHelper.startPage(param.getPage(), param.getSize());

        //查询
        List<ApprovalRecord> approvalRecord = approvalRecordMapper.todoPage(param);
        log.info("审批中心待审批结果数量={}", approvalRecord == null ? 0 : approvalRecord.size());
        Page<ApprovalRecord> pageInfo = (Page<ApprovalRecord>) approvalRecord;
        List<TodoPageVo> list = new ArrayList<>();
        ApprovalRecordTodoPageVO resultPage = new ApprovalRecordTodoPageVO();
        if (approvalRecord == null || approvalRecord.isEmpty()) {
            resultPage.setRecords(list);
            resultPage.setTotal(0);
            resultPage.setPage(param.getPage());
            resultPage.setSize(param.getSize());
            return resultPage;
        }
        for (ApprovalRecord record : approvalRecord) {

            TodoPageVo vo = new TodoPageVo();
            vo.setId(record.getId());
            vo.setStatus(record.getApprovalStatus());
            //申请单
            if ("APPLY".equals(record.getApprovalType())) {
                vo.setApprovalType("APPLY");
                vo.setBusinessId(record.getTargetId());
                //查询申领单单号，赋值给processNo
                QueryWrapper<RequisitionOrder> qw = new QueryWrapper<>();
                qw.eq("id", record.getTargetId());
                RequisitionOrder requisitionOrder = requisitionOrderMapper.selectOne(qw);
                vo.setProcessNo(requisitionOrder.getOrderNo());
                //查询申领人姓名
                vo.setApplicantId(record.getApplicantId());
                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", record.getApplicantId());
                User user = userMapper.selectOne(qwU);
                vo.setApplicantName(user.getRealName());
                //设置title
                vo.setTitle(vo.getApplicantName() + "提交资产申领");
                vo.setCreatedAt(record.getCreatedAt());
            }

            //转移单
            if ("TRANSFER".equals(record.getApprovalType())) {
                vo.setApprovalType("TRANSFER");
                vo.setBusinessId(record.getTargetId());
                //查询转移单号，赋值给processNo
                QueryWrapper<TransferOrder> qw = new QueryWrapper<>();
                qw.eq("id", record.getTargetId());
                TransferOrder transferOrder = transferOrderMapper.selectOne(qw);
                vo.setProcessNo(transferOrder.getOrderNo());
                //查询转移人姓名
                vo.setApplicantId(record.getApplicantId());
                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", record.getApplicantId());
                User user = userMapper.selectOne(qwU);
                vo.setApplicantName(user.getRealName());
                //设置title
                vo.setTitle(vo.getApplicantName() + "提交资产转移");
                vo.setCreatedAt(record.getCreatedAt());
            }
            //归还单
            if ("RETURN".equals(record.getApprovalType())) {
                vo.setApprovalType("RETURN");
                vo.setBusinessId(record.getTargetId());
                //查询归还单号，赋值给processNo
                QueryWrapper<ReturnOrder> qw = new QueryWrapper<>();
                qw.eq("id", record.getTargetId());
                ReturnOrder returnOrder = returnOrderMapper.selectOne(qw);
                vo.setProcessNo(returnOrder.getOrderNo());
                //查询归还人姓名
                vo.setApplicantId(record.getApplicantId());
                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", record.getApplicantId());
                User user = userMapper.selectOne(qwU);
                vo.setApplicantName(user.getRealName());
                //设置title
                vo.setTitle(vo.getApplicantName() + "提交资产归还");
                vo.setCreatedAt(record.getCreatedAt());
            }
            //报废单
            if ("SCRAP".equals(record.getApprovalType())) {
                vo.setApprovalType("SCRAP");
                vo.setBusinessId(record.getTargetId());
                //查询报废单号，赋值给processNo
                QueryWrapper<ScrapOrder> qw = new QueryWrapper<>();
                qw.eq("id", record.getTargetId());
                ScrapOrder scrapOrder = scrapOrderMapper.selectOne(qw);
                vo.setProcessNo(scrapOrder.getOrderNo());
                //查询报废人姓名
                vo.setApplicantId(record.getApplicantId());
                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", record.getApplicantId());
                User user = userMapper.selectOne(qwU);
                vo.setApplicantName(user.getRealName());
                //设置title
                vo.setTitle(vo.getApplicantName() + "提交资产报废");
                vo.setCreatedAt(record.getCreatedAt());


            }
            if ("ASSET".equals(record.getApprovalType())) {
                vo.setApprovalType("ASSET");
                vo.setBusinessId(record.getTargetId());
                Asset asset = assetMapper.selectById(record.getTargetId());
                if (asset != null) {
                    vo.setProcessNo(asset.getAssetCode());
                    vo.setTitle(asset.getName() + "资产登记审批");
                }
                vo.setApplicantId(record.getApplicantId());
                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", record.getApplicantId());
                User user = userMapper.selectOne(qwU);
                if (user != null) {
                    vo.setApplicantName(user.getRealName());
                    if (vo.getTitle() == null) {
                        vo.setTitle(user.getRealName() + "提交资产登记");
                    }
                }
                vo.setCreatedAt(record.getCreatedAt());
            }
            list.add(vo);
        }


        resultPage.setRecords(list);
        resultPage.setTotal((int) pageInfo.getTotal());
        resultPage.setPage(pageInfo.getPageNum());
        resultPage.setSize(pageInfo.getPageSize());


        return resultPage;

    }

    @Override
    public ApprovalRecordDonePageVO donePage(ApprovalRecordDonePageParam param) {
        LoginUserContext context = requireLoginUser();
        boolean assetAdmin = isAssetAdmin(context.getId());
        if (!assetAdmin) {
            param.setApproverId(context.getId());
        } else {
            param.setApproverId(null);
        }
        log.info("审批中心已审批查询: userId={}, assetAdmin={}, approverId={}, page={}, size={}",
                context.getId(), assetAdmin, param.getApproverId(), param.getPage(), param.getSize());
        PageHelper.startPage(param.getPage(), param.getSize());
        List<ApprovalRecord> approvalRecord = approvalRecordMapper.donePage(param);
        log.info("审批中心已审批结果数量={}", approvalRecord == null ? 0 : approvalRecord.size());
        Page<ApprovalRecord> pageInfo = (Page<ApprovalRecord>) approvalRecord;
        List<DonePageRecordVO> list = new ArrayList<>();
        ApprovalRecordDonePageVO resultPage = new ApprovalRecordDonePageVO();
        if (approvalRecord == null || approvalRecord.isEmpty()) {
            resultPage.setRecords(list);
            resultPage.setTotal(0);
            resultPage.setPage(param.getPage());
            resultPage.setSize(param.getSize());
            return resultPage;
        }
        for (ApprovalRecord re : approvalRecord) {
            DonePageRecordVO record = new DonePageRecordVO();
            record.setId(re.getId());
            record.setBusinessType(re.getApprovalType());
            record.setDecision(re.getApprovalStatus());
            //申领单
            if ("APPLY".equals(re.getApprovalType())) {
                RequisitionOrder requisitionOrder = requisitionOrderMapper.selectOne(
                        new LambdaQueryWrapper<RequisitionOrder>()
                                .eq(RequisitionOrder::getId, re.getTargetId())
                );
                record.setProcessNo(requisitionOrder.getOrderNo());


                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", re.getApplicantId());
                User user = userMapper.selectOne(qwU);
                record.setTitle(user.getRealName() + "提交资产申领");
                record.setApprovedAt(re.getApprovedAt());

            }
            //转移单
            if ("TRANSFER".equals(re.getApprovalType())) {
                QueryWrapper<TransferOrder> qw = new QueryWrapper<>();
                qw.eq("id", re.getTargetId());
                TransferOrder transferOrder = transferOrderMapper.selectOne(qw);
                record.setProcessNo(transferOrder.getOrderNo());

                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", re.getApplicantId());
                User user = userMapper.selectOne(qwU);
                record.setTitle(user.getRealName() + "提交资产转移");

                record.setApprovedAt(re.getApprovedAt());
            }
            //归还单
            if ("RETURN".equals(re.getApprovalType())) {
                QueryWrapper<ReturnOrder> qw = new QueryWrapper<>();
                qw.eq("id", re.getTargetId());
                ReturnOrder returnOrder = returnOrderMapper.selectOne(qw);
                record.setProcessNo(returnOrder.getOrderNo());

                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", re.getApplicantId());
                User user = userMapper.selectOne(qwU);
                record.setTitle(user.getRealName() + "提交资产归还");

                record.setApprovedAt(re.getApprovedAt());
            }
            //报废单
            if ("SCRAP".equals(re.getApprovalType())) {
                QueryWrapper<ScrapOrder> qw = new QueryWrapper<>();
                qw.eq("id", re.getTargetId());
                ScrapOrder scrapOrder = scrapOrderMapper.selectOne(qw);
                record.setProcessNo(scrapOrder.getOrderNo());

                QueryWrapper<User> qwU = new QueryWrapper<>();
                qwU.eq("id", re.getApplicantId());
                User user = userMapper.selectOne(qwU);
                record.setTitle(user.getRealName() + "提交资产报废");

                record.setApprovedAt(re.getApprovedAt());
            }
            if ("ASSET".equals(re.getApprovalType())) {
                Asset asset = assetMapper.selectById(re.getTargetId());
                if (asset != null) {
                    record.setProcessNo(asset.getAssetCode());
                    record.setTitle(asset.getName() + "资产登记审批");
                }
                record.setApprovedAt(re.getApprovedAt());
            }

            list.add(record);
        }
        resultPage.setRecords(list);
        resultPage.setTotal((int) pageInfo.getTotal());
        resultPage.setPage(pageInfo.getPageNum());
        resultPage.setSize(pageInfo.getPageSize());
        return resultPage;

    }

    /*
    * 获取审批记录详情
    * */
    @Override
    public ApprovalRecordDetailVO detail(ApprovalRecordDetailParam param) {
        ApprovalRecord approvalRecord = approvalRecordMapper.selectById(param.getId());
        if (approvalRecord == null) {
            throw new CommonException(404, "没有该审批记录");
        }
        LoginUserContext context = requireLoginUser();
        validateApprovalAccess(approvalRecord, context.getId());
        ApprovalRecordDetailVO vo = new ApprovalRecordDetailVO();
        vo.setId(approvalRecord.getId());

        vo.setBusinessId(approvalRecord.getTargetId());
        vo.setBusinessType(approvalRecord.getApprovalType());

        //生成 processNo
        LocalDateTime time = approvalRecord.getCreatedAt() != null ? approvalRecord.getCreatedAt() : approvalRecord.getApprovedAt();
        if (time == null) {
            time = LocalDateTime.now();
        }
        String yearMonth = DateUtil.format(time, "yyyyMM");
        String processNo = "AP" + yearMonth + approvalRecord.getTargetId();
        vo.setProcessNo(processNo);

        //设置status
        vo.setStatus(approvalRecord.getApprovalStatus());
        //设置applicantName和applicantId
        vo.setApplicantId(approvalRecord.getApplicantId());
        QueryWrapper<User> qwU = new QueryWrapper<>();
        qwU.eq("id", approvalRecord.getApplicantId());
        User user = userMapper.selectOne(qwU);
        vo.setApplicantName(user.getRealName());
        //设置currentApproverName和currentApproverId
        vo.setCurrentApproverId(approvalRecord.getApproverId());
        QueryWrapper<User> qwU2 = new QueryWrapper<>();
        qwU2.eq("id", approvalRecord.getApproverId());
        User user2 = userMapper.selectOne(qwU2);
        vo.setCurrentApproverName(user2.getRealName());

        ApprovalRecordDetailFormData formData = new ApprovalRecordDetailFormData();
        //设置title
        switch (approvalRecord.getApprovalType()) {
            case "APPLY":

                //设置formData
                //设置orderNo
                RequisitionOrder requisitionOrder = requisitionOrderMapper.selectById(approvalRecord.getTargetId());
                //设置title
                vo.setTitle(requisitionOrder.getApplicantName() + "提交资产申领");
                formData.setOrderNo(requisitionOrder.getOrderNo());
                //设置reason
                formData.setReason(requisitionOrder.getReason());
                //设置itemList
                RequisitionOrderItem item = requisitionOrderItemMapper.selectById(approvalRecord.getTargetId());
                ApprovalRecordDetailFormData.itemList itemList = new ApprovalRecordDetailFormData.itemList();
                itemList.setCategoryId(item.getCategoryId());
                itemList.setCategoryName(item.getCategoryName());
                itemList.setQuantity(item.getQuantity());
                formData.getItemList().add(itemList);
                vo.getFormData().add(formData);

                //设置historyList

                QueryWrapper<ApprovalRecord> qw = new QueryWrapper<>();
                qw.eq("target_id", approvalRecord.getTargetId());
                qw.eq("approval_type", "APPLY");
                List<ApprovalRecord> targetlist = approvalRecordMapper.selectList(qw);

                //修改的地方
                QueryWrapper<User> qwU3 = new QueryWrapper<>();
                qwU3.in("id", targetlist.stream().map(ApprovalRecord::getApproverId).collect(Collectors.toList()));
                List<User> user3 = userMapper.selectList(qwU3);
                Map<Long, String> userMap = user3.stream().collect(Collectors.toMap(User::getId, User::getRealName));


                for (ApprovalRecord re : targetlist) {
                    ApprovalRecordDetailHistoryList historyList = new ApprovalRecordDetailHistoryList();
                    historyList.setApproverName(userMap.get(re.getApproverId()));
                    historyList.setDecision(re.getApprovalStatus());
                    historyList.setComment(re.getApprovalRemark());
                    historyList.setActionTime(re.getApprovedAt());
                    vo.getHistoryList().add(historyList);
                }


                break;
            case "TRANSFER":

                //设置formData
                TransferOrder transferOrder = transferOrderMapper.selectById(approvalRecord.getTargetId());
                //设置orderNo
                formData.setOrderNo(transferOrder.getOrderNo());
                vo.setTitle(transferOrder.getFromUserName() + "提交资产转移");
                //设置reason
                formData.setReason(transferOrder.getReason());
                //设置itemList
                LambdaQueryWrapper<TransferOrderItem> transferItemQw = new LambdaQueryWrapper<>();
                transferItemQw.eq(TransferOrderItem::getOrderId, approvalRecord.getTargetId());
                List<TransferOrderItem> transferItems = transferOrderItemMapper.selectList(transferItemQw);
                for (TransferOrderItem item2 : transferItems) {
                    ApprovalRecordDetailFormData.itemList itemList2 = new ApprovalRecordDetailFormData.itemList();
                    itemList2.setCategoryName(item2.getCategoryName());
                    QueryWrapper<AssetCategory> qwC = new QueryWrapper<>();
                    qwC.eq("name", item2.getCategoryName());
                    AssetCategory assetCategory = assetCategoryMapper.selectOne(qwC);
                    itemList2.setCategoryId(assetCategory != null ? assetCategory.getId() : null);
                    formData.getItemList().add(itemList2);
                }
                vo.getFormData().add(formData);

                //设置historyList
                QueryWrapper<ApprovalRecord> qw2 = new QueryWrapper<>();
                qw2.eq("target_id", approvalRecord.getTargetId());
                qw2.eq("approval_type", "TRANSFER");
                List<ApprovalRecord> targetlist2 = approvalRecordMapper.selectList(qw2);
                for (ApprovalRecord re : targetlist2) {
                    ApprovalRecordDetailHistoryList historyList2 = new ApprovalRecordDetailHistoryList();
                    QueryWrapper<User> qwU4 = new QueryWrapper<>();
                    qwU4.eq("id", re.getApproverId());
                    User user4 = userMapper.selectOne(qwU4);
                    historyList2.setApproverName(user4 != null ? user4.getRealName() : "-");

                    historyList2.setDecision(re.getApprovalStatus());
                    historyList2.setComment(re.getApprovalRemark());
                    historyList2.setActionTime(re.getApprovedAt());
                    vo.getHistoryList().add(historyList2);
                }

                break;
            case "RETURN":

                //设置formData
                ReturnOrder returnOrder = returnOrderMapper.selectById(approvalRecord.getTargetId());
                //设置orderNo
                formData.setOrderNo(returnOrder.getOrderNo());
                //设置title
                vo.setTitle(returnOrder.getReturnUserName() + "提交资产归还");
                //设置reason
                formData.setReason(returnOrder.getReason());
                //设置itemList
                ReturnOrderItem item3 = returnOrderItemMapper.selectById(approvalRecord.getTargetId());
                ApprovalRecordDetailFormData.itemList itemList3 = new ApprovalRecordDetailFormData.itemList();
                itemList3.setCategoryName(item3.getCategoryName());
                QueryWrapper<AssetCategory> qwC2 = new QueryWrapper<>();
                qwC2.eq("name", item3.getCategoryName());
                AssetCategory assetCategory2 = assetCategoryMapper.selectOne(qwC2);
                itemList3.setCategoryId(assetCategory2.getId());
                formData.getItemList().add(itemList3);
                vo.getFormData().add(formData);
                //设置historyList
                QueryWrapper<ApprovalRecord> qw3 = new QueryWrapper<>();
                qw3.eq("target_id", approvalRecord.getTargetId());
                qw3.eq("approval_type", "RETURN");
                List<ApprovalRecord> targetlist3 = approvalRecordMapper.selectList(qw3);
                for (ApprovalRecord re : targetlist3) {
                    ApprovalRecordDetailHistoryList historyList3 = new ApprovalRecordDetailHistoryList();
                    QueryWrapper<User> qwU5 = new QueryWrapper<>();
                    qwU5.eq("id", re.getApproverId());
                    User user5 = userMapper.selectOne(qwU5);
                    historyList3.setApproverName(user5 != null ? user5.getRealName() : "-");
                    historyList3.setDecision(re.getApprovalStatus());
                    historyList3.setComment(re.getApprovalRemark());
                    historyList3.setActionTime(re.getApprovedAt());
                    vo.getHistoryList().add(historyList3);
                }

                break;
            case "SCRAP":

                //设置formData
                ScrapOrder scrapOrder = scrapOrderMapper.selectById(approvalRecord.getTargetId());
                //设置orderNo
                formData.setOrderNo(scrapOrder.getOrderNo());
                //设置title
                vo.setTitle(scrapOrder.getApplicantName() + "提交资产报废");
                //设置reason
                formData.setReason(scrapOrder.getReason());
                //设置itemList
                ScrapOrderItem item4 = scrapOrderItemMapper.selectById(approvalRecord.getTargetId());
                ApprovalRecordDetailFormData.itemList itemList4 = new ApprovalRecordDetailFormData.itemList();
                itemList4.setCategoryName(item4.getCategoryName());
                QueryWrapper<AssetCategory> qwC3 = new QueryWrapper<>();
                qwC3.eq("name", item4.getCategoryName());
                AssetCategory assetCategory3 = assetCategoryMapper.selectOne(qwC3);
                itemList4.setCategoryId(assetCategory3.getId());
                formData.getItemList().add(itemList4);
                vo.getFormData().add(formData);
                //设置historyList
                QueryWrapper<ApprovalRecord> qw4 = new QueryWrapper<>();
                qw4.eq("target_id", approvalRecord.getTargetId());
                qw4.eq("approval_type", "SCRAP");
                List<ApprovalRecord> targetlist4 = approvalRecordMapper.selectList(qw4);
                for (ApprovalRecord re : targetlist4) {
                    ApprovalRecordDetailHistoryList historyList4 = new ApprovalRecordDetailHistoryList();
                    QueryWrapper<User> qwU6 = new QueryWrapper<>();
                    qwU6.eq("id", re.getApproverId());
                    User user6 = userMapper.selectOne(qwU6);
                    historyList4.setApproverName(user6 != null ? user6.getRealName() : "-");
                    historyList4.setDecision(re.getApprovalStatus());
                    historyList4.setComment(re.getApprovalRemark());
                    historyList4.setActionTime(re.getApprovedAt());
                    vo.getHistoryList().add(historyList4);
                }
                break;
            case "ASSET":
                Asset asset = assetMapper.selectById(approvalRecord.getTargetId());
                if (asset == null) {
                    throw new CommonException(404, "资产不存在");
                }
                vo.setTitle(asset.getName() + "资产登记审批");
                formData.setOrderNo(asset.getAssetCode());
                formData.setReason("资产登记入库审批");
                ApprovalRecordDetailFormData.itemList assetItem = new ApprovalRecordDetailFormData.itemList();
                assetItem.setCategoryId(asset.getCategoryId());
                AssetCategory assetCategoryInfo = assetCategoryMapper.selectById(asset.getCategoryId());
                assetItem.setCategoryName(assetCategoryInfo != null ? assetCategoryInfo.getName() : null);
                assetItem.setQuantity(1);
                formData.getItemList().add(assetItem);
                vo.getFormData().add(formData);

                QueryWrapper<ApprovalRecord> assetQw = new QueryWrapper<>();
                assetQw.eq("target_id", approvalRecord.getTargetId());
                assetQw.eq("approval_type", "ASSET");
                List<ApprovalRecord> assetHistory = approvalRecordMapper.selectList(assetQw);
                for (ApprovalRecord re : assetHistory) {
                    ApprovalRecordDetailHistoryList history = new ApprovalRecordDetailHistoryList();
                    User approver = userMapper.selectById(re.getApproverId());
                    history.setApproverName(approver != null ? approver.getRealName() : "-");
                    history.setDecision(re.getApprovalStatus());
                    history.setComment(re.getApprovalRemark());
                    history.setActionTime(re.getApprovedAt());
                    vo.getHistoryList().add(history);
                }
                break;
            default:
                break;
        }

        return vo;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalRecordActionVO action(ApprovalRecordActionParam param) {
        ApprovalRecord approvalRecord = approvalRecordMapper.selectById(param.getId());
        ApprovalRecordActionVO vo = new ApprovalRecordActionVO();
        if (approvalRecord == null) {
            throw new CommonException(404, "审批记录不存在");
        }
        LoginUserContext context = requireLoginUser();
        validateApprovalAccess(approvalRecord, context.getId());
        if(approvalRecord.getApplicantId() == context.getId()){
            throw new CommonException(400, "不能审批自己");
        }
        if ("PENDING".equals(approvalRecord.getApprovalStatus())) {
            if ("APPROVED".equals(param.getDecision())) {

                approvalRecord.setApprovalStatus("APPROVED");
                approvalRecord.setApprovedAt(LocalDateTime.now());
                approvalRecord.setApprovalRemark(param.getComment());
                approvalRecordMapper.updateById(approvalRecord);

                //审批通过后，更新对应的业务单据的审批状态
                updateTargetTypeTable(approvalRecord.getTargetType(), approvalRecord.getTargetId(), "APPROVED", context, param);


                vo.setApprovalStatus("APPROVED");
                vo.setSuccess(true);
            } else if ("REJECTED".equals(param.getDecision())) {

                approvalRecord.setApprovalStatus("REJECTED");
                approvalRecord.setApprovedAt(LocalDateTime.now());
                approvalRecord.setApprovalRemark(param.getComment());
                approvalRecordMapper.updateById(approvalRecord);

                //审批拒绝后，更新对应的业务单据的审批状态
                updateTargetTypeTable(approvalRecord.getTargetType(), approvalRecord.getTargetId(), "REJECTED", context, param);
                vo.setApprovalStatus("REJECTED");
                vo.setSuccess(true);
            } else {
                vo.setSuccess(false);
            }
        }
        return vo;

    }

    @Override
    public ApprovalRecordTransferVO transfer(ApprovalRecordTransferParam param) {
        ApprovalRecord approvalRecord = approvalRecordMapper.selectById(param.getId());
        if (approvalRecord == null) {
            throw new CommonException(404, "审批记录不存在");
        }
        LoginUserContext context = requireLoginUser();
        validateApprovalAccess(approvalRecord, context.getId());
        User user = userMapper.selectById(param.getTargetApproverId());
        if (user == null) {
            throw new CommonException(404, "转交的用户不存在");
        }
        //需要添加一个当转交ID与当前操作ID一致时，不能转交

         //先获取当前登录用户ID,判断是否与转交用户一致,一致则不能转交
        if (context.getId().equals(param.getTargetApproverId())) {
            throw new CommonException(400, "不能转交给自己");
        }


        if ("PENDING".equals(approvalRecord.getApprovalStatus())) {
            approvalRecord.setApproverId(param.getTargetApproverId());
            approvalRecord.setTransferredTo(param.getTargetApproverId());
            approvalRecord.setApprovalRemark(param.getComment());
            approvalRecord.setApprovedAt(LocalDateTime.now());
            approvalRecordMapper.updateById(approvalRecord);

            //添加转交记录
            ApprovalRecord transferRecord = new ApprovalRecord();
            transferRecord.setApprovalType(approvalRecord.getApprovalType());
            transferRecord.setTargetId(approvalRecord.getTargetId());
            transferRecord.setTargetType(approvalRecord.getTargetType());
            transferRecord.setApplicantId(approvalRecord.getApplicantId());
            transferRecord.setApproverId(param.getTargetApproverId());
            transferRecord.setApprovalStatus("PENDING");
            transferRecord.setCreatedAt(LocalDateTime.now());
            transferRecord.setUpdatedAt(LocalDateTime.now());
            approvalRecordMapper.insert(transferRecord);

        }

        ApprovalRecordTransferVO vo = new ApprovalRecordTransferVO();
        vo.setSuccess(true);
        return vo;


    }



    private LoginUserContext requireLoginUser() {
        LoginUserContext context = LoginUserInfoUtile.get();
        if (context == null || context.getId() == null) {
            throw new CommonException(401, "未授权，请重新登录");
        }
        return context;
    }

    private void validateApprovalAccess(ApprovalRecord approvalRecord, Long currentUserId) {
        if (approvalRecord == null || currentUserId == null) {
            throw new CommonException(403, "无权访问该审批记录");
        }
        if (isAssetAdmin(currentUserId)) {
            return;
        }
        if (!currentUserId.equals(approvalRecord.getApproverId())) {
            throw new CommonException(403, "无权访问该审批记录");
        }
    }

    private boolean isAssetAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        String selectedRoleId = loginUserIRoleUtile.getRole(String.valueOf(userId));
        if (selectedRoleId != null && !selectedRoleId.isBlank()) {
            try {
                Role selectedRole = roleMapper.selectById(Long.valueOf(selectedRoleId));
                if (selectedRole != null && "ASSET_ADMIN".equals(selectedRole.getCode())) {
                    return true;
                }
            } catch (NumberFormatException ex) {
                // ignore invalid cached role id and fall back to database roles
            }
        }

        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(Role::getId, userRoles.stream().map(UserRole::getRoleId).toList());
        List<Role> roles = roleMapper.selectList(roleWrapper);
        return roles != null && roles.stream().anyMatch(role -> "ASSET_ADMIN".equals(role.getCode()));
    }

    //更新对应的业务单据的审批状态
    public void updateTargetTypeTable(String targetType, Long targetId, String status, LoginUserContext context, ApprovalRecordActionParam param) {
        switch (targetType){
            case "requisition_order":
                RequisitionOrder requisitionOrder = requisitionOrderMapper.selectById(targetId);
                requisitionOrder.setApprovalStatus(status);
                requisitionOrder.setApproverName(context.getRealName());
                requisitionOrder.setApprovalRemark(param.getComment());
                requisitionOrder.setApprovedAt(LocalDateTime.now());
                requisitionOrderMapper.updateById(requisitionOrder);
                break;
            case "transfer_order":
                TransferOrder transferOrder = transferOrderMapper.selectById(targetId);
                transferOrder.setApprovalStatus(status);
                transferOrder.setApproverName(context.getRealName());
                transferOrder.setApprovalRemark(param.getComment());
                transferOrder.setApprovedAt(LocalDateTime.now());
                transferOrderMapper.updateById(transferOrder);
                break;
            case "return_order":
                ReturnOrder returnOrder = returnOrderMapper.selectById(targetId);
                returnOrder.setApprovalStatus(status);
                returnOrder.setApproverName(context.getRealName());
                returnOrder.setApprovalRemark(param.getComment());
                returnOrder.setApprovedAt(LocalDateTime.now());
                returnOrderMapper.updateById(returnOrder);
                break;
            case "scrap_order":
                ScrapOrder scrapOrder = scrapOrderMapper.selectById(targetId);
                scrapOrder.setApprovalStatus(status);
                scrapOrder.setApproverName(context.getRealName());
                scrapOrder.setApprovalRemark(param.getComment());
                scrapOrder.setApprovedAt(LocalDateTime.now());
                scrapOrderMapper.updateById(scrapOrder);
                break;
            case "asset":
                Asset asset = assetMapper.selectById(targetId);
               if("REJECTED".equals(status)){
                   asset.setStatus("REJECTED");
                   asset.setUpdatedAt(LocalDateTime.now());
                   assetMapper.updateById(asset);
               }
               if("APPROVED".equals(status)){
                   asset.setStatus("STOCK");
                   asset.setUpdatedAt(LocalDateTime.now());
                   assetMapper.updateById(asset);
                   //创建资产台账
                   AssetLedger assetLedger = new AssetLedger();
                   assetLedger.setAssetId(asset.getId());
                   String assetCode = asset.getAssetCode();
                   String result = assetCode.substring(3);
                   String LedgerNo = "LEDGER" + result;
                   assetLedger.setLedgerNo(LedgerNo);
                   assetLedger.setDepartmentId(asset.getDepartmentId());
                   assetLedger.setDepartmentName(asset.getDepartmentName());
                   assetLedger.setEntryDate(LocalDate.now());
                   assetLedger.setOriginalValue(asset.getPurchaseAmount());
                   assetLedger.setResidualRate(BigDecimal.valueOf(0.05));
                    AssetCategory assetCategory = assetCategoryMapper.selectById(asset.getCategoryId());
                    if (assetCategory == null) {
                        throw new CommonException(400, "资产分类不存在，无法完成入库审批");
                    }
                    Integer standardLifeMonths = assetCategory.getStandardLifeMonths();
                    if (standardLifeMonths == null || standardLifeMonths <= 0) {
                        throw new CommonException(400, "资产分类未配置标准使用年限，无法完成入库审批");
                    }
                    if (asset.getPurchaseAmount() == null) {
                        throw new CommonException(400, "资产原值为空，无法完成入库审批");
                    }
                    assetLedger.setStandardLifeMonths(standardLifeMonths);
                   BigDecimal monthlyDepreciation = asset.getPurchaseAmount()           // 原值
                           .multiply(BigDecimal.ONE.subtract(new BigDecimal("0.05")))      // × (1 - 残值率)
                           .divide(BigDecimal.valueOf(standardLifeMonths), 2, RoundingMode.HALF_UP); // ÷ 标准年限
                   assetLedger.setMonthlyDepreciation(monthlyDepreciation);
                   assetLedger.setAccumulatedDepreciation(BigDecimal.ZERO);
                   assetLedger.setNetValue(asset.getPurchaseAmount());
                   assetLedger.setMonthsUsed(0);
                   assetLedger.setCreatedAt(LocalDateTime.now());
                   assetLedger.setUpdatedAt(LocalDateTime.now());
                   assetLedgerMapper.insert(assetLedger);

                   break;

               }


            default:
                throw new CommonException(500, "未知的审批类型");
        }
    }




}
