package com.good.zdisksystem.service.impl;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.dto.OperationLogQueryDTO;
import com.good.zdisksystem.entity.model.OperationLog;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.vo.OperationLogVO;
import com.good.zdisksystem.mapper.OperationLogMapper;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void addLog(Long userId, String module, String action, String detail, HttpServletRequest request) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setModule(module);
            operationLog.setAction(action);
            operationLog.setDetail(detail);

            // 获取IP地址
            String ip = getIpAddress(request);
            operationLog.setIp(ip);

            operationLog.setCreateTime(LocalDateTime.now());

            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            // 记录日志失败不应影响正常业务
            log.error("记录操作日志失败", e);
        }
    }

    @Override
    public void addLog(String module, String action, String detail) {
        try {
            // 从安全上下文获取当前用户
            User currentUser = RequestUser.getUser();
            if (currentUser == null) {
                log.warn("添加操作日志失败：无法获取当前用户");
                return;
            }

            // 从当前请求上下文获取请求对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                addLog(currentUser.getId(), module, action, detail, request);

            } else {
                log.warn("添加操作日志失败：无法获取请求对象");
            }
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    @Override
    public PageResult<OperationLogVO> getOperationLogs(OperationLogQueryDTO queryDTO) {
        try {
            // 使用PageHelper进行分页
            PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

            // 执行查询
            List<Map<String, Object>> records = operationLogMapper.selectOperationLogs(
                    queryDTO.getUserId(),
                    queryDTO.getModule(),
                    queryDTO.getAction(),
                    queryDTO.getStartTime(),
                    queryDTO.getEndTime()
            );

            // 获取分页信息
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(records);

            // 转换为VO
            List<OperationLogVO> voList = records.stream().map(map -> {
                OperationLogVO vo = new OperationLogVO();

                vo.setId(((Number) map.get("id")).longValue());
                vo.setUserId(((Number) map.get("user_id")).longValue());
                vo.setUsername((String) map.get("username"));
                vo.setModule((String) map.get("module"));
                vo.setAction((String) map.get("action"));
                vo.setDetail((String) map.get("detail"));

                // 日期处理
                vo.setCreateTime(((LocalDateTime) map.get("create_time")));

                return vo;
            }).collect(Collectors.toList());

            // 创建并返回分页结果
            PageResult<OperationLogVO> result = new PageResult<>();
            result.setTotal(pageInfo.getTotal());
            result.setList(voList);
            return result;
        } catch (Exception e) {
            log.error("获取操作日志失败", e);
            throw new CustomException("获取操作日志失败：" + e.getMessage());
        }
    }

    @Override
    public void exportOperationLogs(OperationLogQueryDTO queryDTO, HttpServletResponse response) {
        try {
            // 设置文件名
            String fileName = URLEncoder.encode("操作日志", "UTF-8") + ".xlsx";
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");

            // 不分页，查询所有符合条件的数据
            List<Map<String, Object>> records = operationLogMapper.selectOperationLogs(
                    queryDTO.getUserId(),
                    queryDTO.getModule(),
                    queryDTO.getAction(),
                    queryDTO.getStartTime(),
                    queryDTO.getEndTime()
            );

            // 转换为VO
            List<OperationLogVO> voList = records.stream().map(map -> {
                OperationLogVO vo = new OperationLogVO();

                vo.setId(((Number) map.get("id")).longValue());
                vo.setUserId(((Number) map.get("user_id")).longValue());
                vo.setUsername((String) map.get("username"));
                vo.setModule((String) map.get("module"));
                vo.setAction((String) map.get("action"));
                vo.setDetail((String) map.get("detail"));

                // 日期处理
                vo.setCreateTime(((LocalDateTime) map.get("create_time")));

                return vo;
            }).collect(Collectors.toList());

            // 使用EasyExcel导出
            EasyExcel.write(response.getOutputStream(), OperationLogVO.class)
                    .sheet("操作日志")
                    .doWrite(voList);

        } catch (IOException e) {
            log.error("导出操作日志失败", e);
            throw new CustomException("导出操作日志失败");
        }
    }

    /**
     * 获取请求的真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
