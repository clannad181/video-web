package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.entity.FileInfo;
import com.cjym.videoweb.model.vo.FileCheckResultVo;
import com.cjym.videoweb.model.vo.FileCheckVo;
import com.cjym.videoweb.model.vo.FileResultVo;
import com.cjym.videoweb.result.R;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<FileInfo> {

    R<FileCheckResultVo> checkFile(FileCheckVo fileCheckVo);

    R<String> uploadChunk(String fileMd5, Integer chunkIndex, MultipartFile file);

    R<FileResultVo> mergeChunks(String fileMd5);

    R<FileResultVo> simpleUpload(MultipartFile file, String category);

    void downloadFile(Long fileId, HttpServletResponse response);

    void previewFile(String filePath, HttpServletResponse response);
}
