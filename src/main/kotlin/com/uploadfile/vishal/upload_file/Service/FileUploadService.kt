//package com.uploadfile.vishal.upload_file.Service
//
//import com.uploadfile.vishal.upload_file.Exception.UnsupportedFormat
//import org.springframework.core.io.Resource
//import org.springframework.core.io.UrlResource
//import org.springframework.http.HttpHeaders
//import org.springframework.http.MediaType
//import reactor.core.publisher.Mono
//import org.springframework.http.*
//import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
//import org.springframework.http.codec.multipart.FilePart
//import org.springframework.stereotype.Service
//import org.springframework.util.unit.DataSize
//import reactor.core.publisher.Flux
//
//import java.io.FileNotFoundException
//import java.lang.IndexOutOfBoundsException
//import java.nio.file.Files
//import java.nio.file.Path
//import java.nio.file.Paths
//import java.util.concurrent.atomic.AtomicLong
//import javax.naming.SizeLimitExceededException
//enum class FileFormat {
//    jpeg, pdf, csv, xls, doc, png
//}
//@Service
//class FileUploadDownloadService {
//    private val fileStorage = Paths.get("./upload").toAbsolutePath().normalize()
//
//    fun uploadFile(filePartMono: Flux<FilePart>, contentLength: Long): Mono<ResponseEntity<String>> {
//        var count = AtomicLong()
//        return filePartMono
//            .flatMap { fp:FilePart->
//                when{
//                    (count.incrementAndGet()>1) -> Flux.error(IndexOutOfBoundsException())
//                else -> Flux.just(fp)
//                }
//            }
//            .single()
//        //file-part-mono represent the file to be uploaded, content-Length represent the length of the file in bytes
//        //transfer the Mono<FilePart> into Mono<ResponseEntity<String>>
//            .flatMap { fp: FilePart ->
//            //declared an enum class to keep the format type and then fetching them as a FileFormats
//            val supportedFormats = enumValues<FileFormat>().map { it.name}
//            val sizeLimit = DataSize.ofKilobytes(5000).toBytes();
//            val c=0;
//            //checks for all the validations if all is Ok then transfer file using transfer to and display the url in response
//            when {
//                contentLength <= 174L -> Mono.error<ResponseEntity<String>?>(FileNotFoundException()) //todo
//                contentLength > sizeLimit -> Mono.error(SizeLimitExceededException())
//                fp.filename().substring(fp.filename().lastIndexOf('.') + 1) !in supportedFormats -> Mono.error(
//                    UnsupportedFormat()
//                )
//                else -> {
//                    fp.transferTo(fileStorage.resolve(fp.filename()))
//                        .then(Mono.just(ResponseEntity.ok().body("/download/${fp.filename()}")))
//                }
//            }
//        }
//    }
//    fun downloadFile( filename: String): Mono<ResponseEntity<Resource>> {
//        //get path using filestorage
//        val filePath: Path = fileStorage.toAbsolutePath().normalize().resolve(filename)
//        return Mono.fromCallable {
//            when {
//                //check if file exist if not return file not found exception
//                !Files.exists(filePath) -> throw FileNotFoundException()
//                else -> {
//                    //create a resource object from file using url resource class, represent resource loaded from a url
//                    val resource: Resource = UrlResource(filePath.toUri())
//                    //creates http headers used to specify http header for the response
//                    val httpHeaders = HttpHeaders()
//                    //the fxn sets filename header to a value of filename
//                    httpHeaders.add("File-Name", filename)
//                    //content disposition as attachment
//                    //filename =<filename> tells the browser to treat the response as an attachment
//                    httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name=" + resource.filename)
//                    //it then prompts the user to download the file with the specified filename
//                    ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
//                        .headers(httpHeaders).body(resource)
//                }
//            }
//        }
//    }
//}
package com.uploadfile.vishal.upload_file.Service

import com.uploadfile.vishal.upload_file.Exception.UnsupportedFormat
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import org.springframework.http.*
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.unit.DataSize
import reactor.core.publisher.Flux

import java.io.FileNotFoundException
import java.lang.IndexOutOfBoundsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicLong
import javax.naming.SizeLimitExceededException
enum class FileFormat {
    jpeg, pdf, csv, xls, doc, png
}
@Service
class FileUploadDownloadService {
    private val fileStorage = Paths.get("./upload").toAbsolutePath().normalize()

    fun uploadFile(filePartMono: Flux<FilePart>, contentLength: Long): Mono<ResponseEntity<String>> {
        var count = AtomicLong()
        var maxContentLength = DataSize.ofKilobytes(5000).toBytes()

        return filePartMono
            .flatMap { fp:FilePart ->
                when {
                    count.incrementAndGet() > 1 -> Flux.error(IndexOutOfBoundsException())
                    contentLength > maxContentLength -> {
                        maxContentLength = DataSize.ofKilobytes(10000).toBytes()
                        println(maxContentLength)
                        Flux.error(SizeLimitExceededException())
                    }
                    else -> Flux.just(fp)
                }
            }
            .single()
            .flatMap { fp: FilePart ->
                val supportedFormats = enumValues<FileFormat>().map { it.name}
                when {
                    fp.filename().substring(fp.filename().lastIndexOf('.') + 1) !in supportedFormats -> Mono.error(
                        UnsupportedFormat()
                    )
                    else -> {
                        fp.transferTo(fileStorage.resolve(fp.filename()))
                            .then(Mono.just(ResponseEntity.ok().body("/download/${fp.filename()}")))
                    }
                }
            }
    }


    fun downloadFile( filename: String): Mono<ResponseEntity<Resource>> {
        //get path using filestorage
        val filePath: Path = fileStorage.toAbsolutePath().normalize().resolve(filename)
        return Mono.fromCallable {
            when {
                //check if file exist if not return file not found exception
                !Files.exists(filePath) -> throw FileNotFoundException()
                else -> {
                    //create a resource object from file using url resource class, represent resource loaded from a url
                    val resource: Resource = UrlResource(filePath.toUri())
                    //creates http headers used to specify http header for the response
                    val httpHeaders = HttpHeaders()
                    //the fxn sets filename header to a value of filename
                    httpHeaders.add("File-Name", filename)
                    //content disposition as attachment
                    //filename =<filename> tells the browser to treat the response as an attachment
                    httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name=" + resource.filename)
                    //it then prompts the user to download the file with the specified filename
                    ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
                        .headers(httpHeaders).body(resource)
                }
            }
        }
    }
}