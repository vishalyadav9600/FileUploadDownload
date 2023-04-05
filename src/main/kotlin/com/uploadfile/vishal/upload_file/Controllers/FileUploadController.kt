package com.uploadfile.vishal.upload_file.Controllers

import com.uploadfile.vishal.upload_file.Service.FileUploadDownloadService
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.IOException

@RestController
class FileUploadController(private val fileUploadService: FileUploadDownloadService) {

    @PostMapping("/upload")
    fun uploadFile(
            //multipart request with the method argument
        @RequestPart("myFile") filePartMono: Flux<FilePart>,
            //maps all or a particular header to particular method
        @RequestHeader("Content-Length") contentLength: Long
        ): Mono<ResponseEntity<String>> {
        return fileUploadService.uploadFile(filePartMono, contentLength)
    }
    @GetMapping("/download/{fileName}")
    @Throws(IOException::class)
    fun downloadFile(@PathVariable fileName: String): Mono<ResponseEntity<Resource>> {
        return fileUploadService.downloadFile(fileName)
    }

}
