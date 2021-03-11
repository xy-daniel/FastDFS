package com.crudsavior.fastdfs.controller;

import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * FileController
 *
 * @author arctic
 * @date 2021/3/5
 **/
@RestController
@RequestMapping("/file")
@Api(value = "fastDFS分布式文件存储--文件上传下载",description = "fastDFS分布式文件存储--文件上传下载")
@CrossOrigin//跨域的意思
public class FileController {

    //FastFileStorageClient 直接注入就能用 fastdfs自带的
    @Resource
    private FastFileStorageClient fastFileStorageClient;

    /**
     * 文件上传
     */
    @ApiOperation("上传")
    @Async("asyncServiceExecutor")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public CompletableFuture<HashMap<String, String>> upload(@RequestPart("file") MultipartFile file) {
        HashMap<String, String> result = new HashMap<>();
        try {
            //fastDFS storage存储节点路径
            String FASTDFS_SERVER_IMAGE = "http://139.9.119.104:8888/";
            result.put("msg", FASTDFS_SERVER_IMAGE + fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(),
                    FilenameUtils.getExtension(file.getOriginalFilename()), null).getFullPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 文件删除
     * @param path 文件路径
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ApiOperation("删除")
    public HashMap<String, Object> delete(@RequestParam String path) {
        HashMap<String, Object> result = new HashMap<>();
        fastFileStorageClient.deleteFile(path);
        result.put("msg", "大哥啦~~~！！，删除成功！");
        return result;
    }


    /**
     * 文件下载
     * @param url 文件路径
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ApiOperation("下载")
    public void downLoad(@RequestParam String url, HttpServletResponse response) throws IOException {
        String substring = url.substring(url.lastIndexOf(".") + 1);
        byte[] bytes = fastFileStorageClient.downloadFile(url.substring(0, url.indexOf("/")), url.substring(url.indexOf("/") + 1), new DownloadByteArray());
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(UUID.randomUUID().toString().replaceAll("-", "") + "." + substring, "UTF-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        IOUtils.write(bytes, outputStream);
    }

}
