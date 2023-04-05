package com.uploadfile.vishal.upload_file.Exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.FileNotFoundException
import com.uploadfile.vishal.upload_file.*;
import com.uploadfile.vishal.upload_file.Exception.UnsupportedFormat
import javax.naming.SizeLimitExceededException

//
@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(FileNotFoundException::class)
    fun handleNotFound(): ResponseEntity<Response> {
        val response: Response = Response("File Not Found", 404)
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SizeLimitExceededException::class)
    fun handleMaxLimit(ex: SizeLimitExceededException): ResponseEntity<Response> {
        val response: Response = Response("File Size is too large! please upload less than 5MB", 413)
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(IndexOutOfBoundsException::class)
    fun handleMaxLimit(ex: IndexOutOfBoundsException): ResponseEntity<Response> {
        val response: Response = Response("Only one file allowed", 416) //range not satisficable
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(UnsupportedFormat::class)
    fun formatException(ex: UnsupportedFormat): ResponseEntity<Response>{
        val response: Response = Response("only jpeg, pdf, csv, xls, doc & png are allowed", 415)
        return ResponseEntity.badRequest().body(response);
    }
    data class Response (var Message:String?, var error: Long)
}
