package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.FileShare;
import com.good.zdisksystem.entity.vo.ShareFileVO;
import com.good.zdisksystem.entity.vo.UserFileVO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileShareMapper extends BaseMapper<FileShare> {

    @Select("SELECT fs.*, f.name as fileName, f.size as fileSize, f.type as fileType, " +
            "u.username as ownerName " +
            "FROM file_share fs " +
            "LEFT JOIN file f ON fs.file_id = f.id " +
            "LEFT JOIN user u ON fs.user_id = u.id " +
            "WHERE fs.user_id = #{userId} AND fs.is_deleted = 0 " +
            "ORDER BY fs.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<ShareFileVO> getMyShares(@Param("userId") Long userId,
                                 @Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM file_share " +
            "WHERE user_id = #{userId} AND is_deleted = 0")
    Long getMySharesCount(@Param("userId") Long userId);

    @Select("SELECT fs.*, f.name as fileName, f.size as fileSize, f.type as fileType, " +
            "u.username as ownerName " +
            "FROM share_receive sr " +
            "LEFT JOIN file_share fs ON sr.share_id = fs.id " +
            "LEFT JOIN file f ON fs.file_id = f.id " +
            "LEFT JOIN user u ON fs.user_id = u.id " +
            "WHERE sr.receiver_id = #{userId} AND sr.is_deleted = 0 " +
            "ORDER BY sr.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<ShareFileVO> getReceivedShares(@Param("userId") Long userId,
                                        @Param("offset") Integer offset,
                                        @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM share_receive " +
            "WHERE receiver_id = #{userId} AND is_deleted = 0")
    Long getReceivedSharesCount(@Param("userId") Long userId);

    /**
     * 增加浏览次数
     */
    @Update("UPDATE file_share SET view_count = view_count + 1 WHERE id = #{shareId}")
    void incrementViewCount(Long shareId);

    /**
     * 增加下载次数
     */
    @Update("UPDATE file_share SET download_count = download_count + 1 WHERE id = #{shareId}")
    void incrementDownloadCount(Long shareId);

    @Select("SELECT fs.*, f.name as fileName, f.size as fileSize, f.type as fileType, " +
            "u.username as ownerName " +
            "FROM file_share fs " +
            "LEFT JOIN file f ON fs.file_id = f.id " +
            "LEFT JOIN user u ON fs.user_id = u.id " +
            "WHERE fs.user_id = #{userId} AND fs.is_deleted = 0 " +
            "AND (f.name LIKE #{keyword} OR u.username LIKE #{keyword}) " +
            "ORDER BY fs.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<ShareFileVO> searchMyShares(@Param("userId") Long userId,
                                    @Param("keyword") String keyword,
                                    @Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM file_share fs " +
            "LEFT JOIN file f ON fs.file_id = f.id " +
            "LEFT JOIN user u ON fs.user_id = u.id " +
            "WHERE fs.user_id = #{userId} AND fs.is_deleted = 0 " +
            "AND (f.name LIKE #{keyword} OR u.username LIKE #{keyword})")
    Long getMySharesSearchCount(@Param("userId") Long userId,
                              @Param("keyword") String keyword);

    @Select("SELECT fs.*, f.name as fileName, f.size as fileSize, f.type as fileType, " +
            "u.username as ownerName " +
            "FROM share_receive sr " +
            "LEFT JOIN file_share fs ON sr.share_id = fs.id " +
            "LEFT JOIN file f ON fs.file_id = f.id " +
            "LEFT JOIN user u ON fs.user_id = u.id " +
            "WHERE sr.receiver_id = #{userId} AND sr.is_deleted = 0 " +
            "AND (f.name LIKE #{keyword} OR u.username LIKE #{keyword}) " +
            "ORDER BY sr.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<ShareFileVO> searchReceivedShares(@Param("userId") Long userId,
                                          @Param("keyword") String keyword,
                                          @Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM share_receive sr " +
            "LEFT JOIN file_share fs ON sr.share_id = fs.id " +
            "LEFT JOIN file f ON fs.file_id = f.id " +
            "LEFT JOIN user u ON fs.user_id = u.id " +
            "WHERE sr.receiver_id = #{userId} AND sr.is_deleted = 0 " +
            "AND (f.name LIKE #{keyword} OR u.username LIKE #{keyword})")
    Long getReceivedSharesSearchCount(@Param("userId") Long userId,
                                     @Param("keyword") String keyword);

    @Select("SELECT f.*, u.username as ownerName " +
            "FROM file f " +
            "LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.user_id = #{userId} AND f.is_deleted = 1 " +
            "AND (f.name LIKE #{keyword} OR u.username LIKE #{keyword}) " +
            "ORDER BY f.delete_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<UserFileVO> getRecycleBinFiles(@Param("userId") Long userId,
                                        @Param("keyword") String keyword,
                                        @Param("offset") Integer offset,
                                        @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM file f " +
            "LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.user_id = #{userId} AND f.is_deleted = 1 " +
            "AND (f.name LIKE #{keyword} OR u.username LIKE #{keyword})")
    Long getRecycleBinCount(@Param("userId") Long userId,
                           @Param("keyword") String keyword);
}
