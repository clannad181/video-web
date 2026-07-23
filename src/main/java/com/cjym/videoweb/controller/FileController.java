package com.cjym.videoweb.controller;

import com.cjym.videoweb.model.vo.FileCheckResultVo;
import com.cjym.videoweb.model.vo.FileCheckVo;
import com.cjym.videoweb.model.vo.FileMergeVo;
import com.cjym.videoweb.model.vo.FileResultVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/file")
@Tag(name = "文件管理", description = "文件上传下载")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/check")
    @Operation(summary = "文件预检查（秒传）", description = "根据文件MD5检查是否已存在，已存在则秒传；返回已上传的分片列表")
    public R<FileCheckResultVo> checkFile(@RequestBody FileCheckVo fileCheckVo) {
        return fileService.checkFile(fileCheckVo);
    }

    @PostMapping("/chunk")
    @Operation(summary = "上传分片", description = "上传单个文件分片")
    public R<String> uploadChunk(
            @Parameter(description = "文件MD5", required = true) @RequestParam String fileMd5,
            @Parameter(description = "分片索引（从0开始）", required = true) @RequestParam Integer chunkIndex,
            @Parameter(description = "分片文件", required = true) @RequestParam MultipartFile file) {
        return fileService.uploadChunk(fileMd5, chunkIndex, file);
    }

    @PostMapping("/merge")
    @Operation(summary = "合并分片", description = "将所有分片合并为完整文件")
    public R<FileResultVo> mergeChunks(@RequestBody FileMergeVo fileMergeVo) {
        return fileService.mergeChunks(fileMergeVo.getFileMd5());
    }

    @PostMapping("/upload")
    @Operation(summary = "小文件直传", description = "小文件一步上传（自动秒传判断 + 分类存储），适用于头像、缩略图等")
    public R<FileResultVo> simpleUpload(
            @Parameter(description = "文件", required = true) @RequestParam MultipartFile file,
            @Parameter(description = "分类目录（avatar/video/image/document）") @RequestParam(required = false) String category) {
        return fileService.simpleUpload(file, category);
    }

    @GetMapping("/download/{fileId}")
    @Operation(summary = "文件下载", description = "根据文件ID下载文件")
    public void downloadFile(
            @Parameter(description = "文件ID", required = true) @PathVariable Long fileId,
            HttpServletResponse response) {
        fileService.downloadFile(fileId, response);
    }

    @GetMapping("/preview/**")
    @Operation(summary = "文件预览", description = "根据文件路径预览文件（支持图片、视频等）")
    public void previewFile(HttpServletResponse response) {
        // 从请求路径中提取 /file/preview/ 后的部分作为文件路径
        // Spring 无法直接获取 ** 的值，需要在方法中手动解析
        previewFileInternal(response);
    }

    private void previewFileInternal(HttpServletResponse response) {
        // 这里使用 RequestContextHolder 获取请求路径（简化处理）
        jakarta.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                        org.springframework.web.context.request.RequestContextHolder
                                .currentRequestAttributes()).getRequest();
        String requestUri = request.getRequestURI();
        String prefix = "/api/admin/file/preview/";
        if (requestUri.startsWith(prefix)) {
            String filePath = requestUri.substring(prefix.length());
            fileService.previewFile(filePath, response);
        }
    }
}
