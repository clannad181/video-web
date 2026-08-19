package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.mapper.FileInfoMapper;
import com.cjym.videoweb.model.entity.FileInfo;
import com.cjym.videoweb.model.vo.FileCheckResultVo;
import com.cjym.videoweb.model.vo.FileCheckVo;
import com.cjym.videoweb.model.vo.FileResultVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.FileService;
import cn.hutool.crypto.digest.DigestUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Value("${file.upload.chunk-size}")
    private DataSize chunkMaxSize;

    @Override
    @Transactional
    public R<FileCheckResultVo> checkFile(FileCheckVo fileCheckVo) {
        String fileMd5 = fileCheckVo.getFileMd5();
        long maxChunkBytes = chunkMaxSize.toBytes();

        // 校验客户端分片大小不超过服务端配置
        if (fileCheckVo.getChunkSize() != null && fileCheckVo.getChunkSize() > maxChunkBytes) {
            return R.error("分片大小不能超过 " + chunkMaxSize.toMegabytes() + "MB");
        }

        // 1. 查询是否已有完整文件（秒传）
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileMd5, fileMd5);
        wrapper.eq(FileInfo::getStatus, 1);
        FileInfo existFile = baseMapper.selectOne(wrapper);
        if (existFile != null) {
            FileResultVo fileResult = new FileResultVo();
            fileResult.setFileId(existFile.getId());
            fileResult.setFileName(existFile.getFileName());
            fileResult.setFileMd5(existFile.getFileMd5());
            fileResult.setFileSize(existFile.getFileSize());
            fileResult.setUrl(existFile.getUrl());
            fileResult.setInstantUpload(true);
            FileCheckResultVo result = new FileCheckResultVo();
            result.setExist(true);
            result.setFile(fileResult);
            result.setChunkSize(maxChunkBytes);
            return R.success(result);
        }

        // 2. 检查已上传的分片
        List<Integer> uploadedChunks = new ArrayList<>();
        String chunkDirPath = uploadPath + File.separator + "chunks" + File.separator + fileMd5;
        File chunkDir = new File(chunkDirPath);
        if (chunkDir.exists() && chunkDir.isDirectory()) {
            File[] chunkFiles = chunkDir.listFiles();
            if (chunkFiles != null) {
                for (File chunkFile : chunkFiles) {
                    try {
                        uploadedChunks.add(Integer.parseInt(chunkFile.getName()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // 3. 不存在则创建上传记录
        if (uploadedChunks.isEmpty()) {
            LambdaQueryWrapper<FileInfo> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(FileInfo::getFileMd5, fileMd5);
            if (baseMapper.selectCount(existWrapper) == 0) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileMd5(fileMd5);
                fileInfo.setFileName(fileCheckVo.getFileName());
                fileInfo.setFileSize(fileCheckVo.getFileSize());
                fileInfo.setTotalChunks(fileCheckVo.getTotalChunks());
                fileInfo.setChunkSize(fileCheckVo.getChunkSize());
                fileInfo.setCategory(fileCheckVo.getCategory() != null ? fileCheckVo.getCategory() : "document");
                fileInfo.setFilePath("");
                fileInfo.setStatus(0);
                baseMapper.insert(fileInfo);
            }
        }

        FileCheckResultVo result = new FileCheckResultVo();
        result.setExist(false);
        result.setUploadedChunks(uploadedChunks);
        result.setChunkSize(maxChunkBytes);
        return R.success(result);
    }

    @Override
    public R<String> uploadChunk(String fileMd5, Integer chunkIndex, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.error("分片文件不能为空");
        }
        try {
            // 分片目录：uploadPath/chunks/{md5}/
            String chunkDirPath = uploadPath + File.separator + "chunks" + File.separator + fileMd5;
            File chunkDir = new File(chunkDirPath);
            if (!chunkDir.exists()) {
                chunkDir.mkdirs();
            }
            // 分片文件以索引命名
            File chunkFile = new File(chunkDirPath + File.separator + chunkIndex);
            file.transferTo(chunkFile);
            return R.success("分片上传成功");
        } catch (IOException e) {
            return R.error("分片上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<FileResultVo> mergeChunks(String fileMd5) {
        // 1. 查询上传记录
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getFileMd5, fileMd5);
        FileInfo fileInfo = baseMapper.selectOne(wrapper);
        if (fileInfo == null) {
            return R.error("未找到上传记录");
        }
        if (fileInfo.getStatus() != null && fileInfo.getStatus() == 1) {
            // 已经合并完成
            FileResultVo result = new FileResultVo();
            result.setFileId(fileInfo.getId());
            result.setFileName(fileInfo.getFileName());
            result.setFileMd5(fileInfo.getFileMd5());
            result.setFileSize(fileInfo.getFileSize());
            result.setUrl(fileInfo.getUrl());
            result.setInstantUpload(false);
            return R.success(result);
        }

        // 2. 验证所有分片是否齐全
        String chunkDirPath = uploadPath + File.separator + "chunks" + File.separator + fileMd5;
        File chunkDir = new File(chunkDirPath);
        if (!chunkDir.exists() || !chunkDir.isDirectory()) {
            return R.error("未找到分片文件");
        }
        File[] chunkFiles = chunkDir.listFiles();
        if (chunkFiles == null || chunkFiles.length < (fileInfo.getTotalChunks() != null ? fileInfo.getTotalChunks() : 0)) {
            return R.error("分片未上传完整");
        }

        // 3. 构建目标路径：uploadPath/{category}/{yyyy-MM}/{md5}.{ext}
        String category = fileInfo.getCategory() != null ? fileInfo.getCategory() : "document";
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String ext = getFileExtension(fileInfo.getFileName());
        String targetFileName = fileMd5 + "_" + System.currentTimeMillis() + ext;
        String targetDirPath = uploadPath + File.separator + category + File.separator + dateStr;
        File targetDir = new File(targetDirPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        String targetFilePath = targetDirPath + File.separator + targetFileName;

        // 4. 按索引顺序合并分片
        try {
            List<Integer> indices = new ArrayList<>();
            for (File f : chunkFiles) {
                try {
                    indices.add(Integer.parseInt(f.getName()));
                } catch (NumberFormatException ignored) {
                }
            }
            indices.sort(Integer::compareTo);

            try (FileOutputStream fos = new FileOutputStream(targetFilePath);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                for (int idx : indices) {
                    File chunkFile = new File(chunkDirPath + File.separator + idx);
                    Files.copy(chunkFile.toPath(), bos);
                }
                bos.flush();
            }

            // 5. 删除分片目录
            deleteDirectory(chunkDir);

            // 6. 更新数据库记录
            String relativePath = category + "/" + dateStr + "/" + targetFileName;
            String url = urlPrefix + "/" + relativePath;
            long actualFileSize = new File(targetFilePath).length();
            fileInfo.setFilePath(relativePath);
            fileInfo.setFileType(ext.replace(".", ""));
            fileInfo.setFileSize(actualFileSize);
            fileInfo.setUrl(url);
            fileInfo.setStatus(1);
            baseMapper.updateById(fileInfo);

            FileResultVo result = new FileResultVo();
            result.setFileId(fileInfo.getId());
            result.setFileName(fileInfo.getFileName());
            result.setFileMd5(fileInfo.getFileMd5());
            result.setFileSize(actualFileSize);
            result.setUrl(url);
            result.setInstantUpload(false);
            return R.success(result);

        } catch (IOException e) {
            return R.error("分片合并失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<FileResultVo> simpleUpload(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            return R.error("文件不能为空");
        }
        try {
            // 1. 计算文件 MD5
            String fileMd5 = DigestUtil.md5Hex(file.getInputStream());

            // 2. 秒传判断：MD5 是否已存在
            LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileInfo::getFileMd5, fileMd5);
            wrapper.eq(FileInfo::getStatus, 1);
            FileInfo existFile = baseMapper.selectOne(wrapper);
            if (existFile != null) {
                FileResultVo result = new FileResultVo();
                result.setFileId(existFile.getId());
                result.setFileName(existFile.getFileName());
                result.setFileMd5(existFile.getFileMd5());
                result.setFileSize(existFile.getFileSize());
                result.setUrl(existFile.getUrl());
                result.setInstantUpload(true);
                return R.success(result);
            }

            // 3. 构建存储路径
            String cat = (category != null && !category.isEmpty()) ? category : "document";
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String ext = getFileExtension(file.getOriginalFilename());
            String targetFileName = fileMd5 + "_" + System.currentTimeMillis() + ext;
            String relativePath = cat + "/" + dateStr + "/" + targetFileName;
            String targetDirPath = uploadPath + File.separator + cat + File.separator + dateStr;
            File targetDir = new File(targetDirPath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            // 4. 保存文件
            File destFile = new File(targetDirPath + File.separator + targetFileName);
            file.transferTo(destFile);

            // 5. 入库
            String url = urlPrefix + "/" + relativePath;
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setFileName(file.getOriginalFilename());
            fileInfo.setFileSize(destFile.length());
            fileInfo.setFileType(ext.replace(".", ""));
            fileInfo.setFilePath(relativePath);
            fileInfo.setCategory(cat);
            fileInfo.setTotalChunks(1);
            fileInfo.setChunkSize(destFile.length());
            fileInfo.setStatus(1);
            fileInfo.setUrl(url);
            baseMapper.insert(fileInfo);

            FileResultVo result = new FileResultVo();
            result.setFileId(fileInfo.getId());
            result.setFileName(fileInfo.getFileName());
            result.setFileMd5(fileMd5);
            result.setFileSize(destFile.length());
            result.setUrl(url);
            result.setInstantUpload(false);
            return R.success(result);
        } catch (IOException e) {
            return R.error("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadFile(Long fileId, HttpServletResponse response) {
        FileInfo fileInfo = baseMapper.selectById(fileId);
        if (fileInfo == null || fileInfo.getStatus() != 1) {
            throw new RuntimeException("文件不存在");
        }
        File file = new File(uploadPath + File.separator + fileInfo.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件不存在于磁盘");
        }
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8));
            response.setHeader("Content-Length", String.valueOf(file.length()));
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void previewFile(String filePath, HttpServletRequest request, HttpServletResponse response) {
        File file = new File(uploadPath + File.separator + filePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }

        String contentType = getContentType(filePath);
        long fileLength = file.length();
        String rangeHeader = request.getHeader("Range");

        response.setContentType(contentType);
        response.setHeader("Accept-Ranges", "bytes");

        try {
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // 解析 Range: "bytes=0-1023" or "bytes=1024-"
                String rangeValue = rangeHeader.substring("bytes=".length());
                String[] parts = rangeValue.split("-");
                long start = parts[0].isEmpty() ? 0 : Long.parseLong(parts[0]);
                long end = (parts.length > 1 && !parts[1].isEmpty())
                        ? Long.parseLong(parts[1])
                        : fileLength - 1;

                // 校验 range 合法性
                if (start >= fileLength) {
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    response.setHeader("Content-Range", "bytes */" + fileLength);
                    return;
                }
                end = Math.min(end, fileLength - 1);
                long contentLength = end - start + 1;

                // 206 Partial Content
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Length", String.valueOf(contentLength));
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);

                try (RandomAccessFile raf = new RandomAccessFile(file, "r");
                     OutputStream os = response.getOutputStream()) {
                    raf.seek(start);
                    byte[] buffer = new byte[8192];
                    long remaining = contentLength;
                    int len;
                    while (remaining > 0 && (len = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                        os.write(buffer, 0, len);
                        remaining -= len;
                    }
                    os.flush();
                }
            } else {
                // 完整文件返回 200
                response.setHeader("Content-Length", String.valueOf(fileLength));
                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = response.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    os.flush();
                }
            }
        } catch (IOException e) {
//            throw new RuntimeException("文件预览失败: " + e.getMessage());
        }
    }

    // ========== 辅助方法 ==========

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String getContentType(String filePath) {
        String ext = getFileExtension(filePath).toLowerCase();
        return switch (ext) {
            // 图片
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".svg" -> "image/svg+xml";
            case ".bmp" -> "image/bmp";
            // 视频
            case ".mp4" -> "video/mp4";
            case ".webm" -> "video/webm";
            case ".ogg" -> "video/ogg";  // 也常包含音频流
            case ".avi" -> "video/x-msvideo";
            case ".mov" -> "video/quicktime";
            case ".flv" -> "video/x-flv";
            case ".mkv" -> "video/x-matroska";
            case ".m4v" -> "video/x-m4v";
            case ".wmv" -> "video/x-ms-wmv";
            case ".3gp" -> "video/3gpp";
            case ".ts" -> "video/mp2t";
            // 音频
            case ".mp3" -> "audio/mpeg";
            case ".wav" -> "audio/wav";
            case ".flac" -> "audio/flac";
            case ".aac" -> "audio/aac";
            case ".wma" -> "audio/x-ms-wma";
            // 文档
            case ".pdf" -> "application/pdf";
            case ".json" -> "application/json";
            case ".xml" -> "application/xml";
            case ".html", ".htm" -> "text/html";
            case ".css" -> "text/css";
            case ".js" -> "application/javascript";
            default -> "application/octet-stream";
        };
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        dir.delete();
    }
}
